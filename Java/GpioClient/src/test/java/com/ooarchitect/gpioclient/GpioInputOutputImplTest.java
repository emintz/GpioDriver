/*
 * TestByteSupplier.java
 *
 * Copyright (C) 2026  Eric Mintz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ooarchitect.gpioclient;

import com.fazecast.jSerialComm.SerialPort;
import com.google.common.truth.Truth;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

/**
 * Validates {@link GpioInputOutputImpl}
 */
@ExtendWith(MockitoExtension.class)
class GpioInputOutputImplTest {
  private static final byte LEAD_IN = (byte) 0xFF;
  private static final byte LEAD_OUT = (byte) 0x7F;
  private static final byte PIN_LOW = 0;
  private static final byte PIN_HIGH = 1;

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private SerialPort serialPort;

  @Mock
  Consumer<Level> levelConsumer;

  @Mock
  Consumer<IOStatusCode> statusConsumer;

  InOrder inOrder;

  private GpioInputOutputImpl<ESP32s2Pin> gpioInputOutput;

  private void receiveByte(byte input) {
    byte[] expectedBuffer = new byte[1];
    Mockito.doAnswer(
        invocation -> {
          byte[] outputBuffer = invocation.getArgument(0);
          outputBuffer[0] = input;
          return 1;
        }
    ).when(serialPort).readBytes(expectedBuffer,1, 0);
    try {
      gpioInputOutput.dispatcher().processOneByte();
    } catch (InterruptedException e) {
     Assertions.fail("Unexpected exception", e);
    }
  }

  private void receiveMutation(ESP32s2Pin pin, Level level) {
    receiveByte((byte) (level.value() | pin.number()));
  }

  private void receiveStatus(
      ESP32s2Pin pin,
      IOStatusCode statusCode,
      StatusScope scope,
      byte sideData) {
    receiveByte((byte) 0xFF);
    receiveByte(pin.number());
    receiveByte((byte) statusCode.ordinal());
    receiveByte((byte) scope.ordinal());
    receiveByte(sideData);
    receiveByte((byte) 0x7F);
  }

  private void verifyCommandSent(
      ConfigurationCommandCode commandCode,
      StatusScope scope,
      ESP32s2Pin pin,
      PullMode resistorConfiguration) {
    var buffer = new byte[] {
        LEAD_IN,
        (byte) commandCode.ordinal(),
        (byte) scope.ordinal(),
        pin.number(),
        (byte) resistorConfiguration.ordinal(),
        LEAD_OUT
    };
    inOrder.verify(serialPort).writeBytes(buffer, buffer.length, 0);
  }

  private void verifyStatusConsumed(
      IOStatusCode code,
      StatusScope scope,
      ESP32s2Pin pin,
      byte sideData) {
    inOrder.verify(statusConsumer).accept(code);
  }

  private void verifyMutationReceived() {
    inOrder.verify(serialPort)
        .readBytes(
            ArgumentMatchers.any(byte[].class),
            ArgumentMatchers.eq(1),
            ArgumentMatchers.eq(0));
  }

  private void verifyStatusReceived() {
    inOrder.verify(serialPort, Mockito.times(6))
        .readBytes(
            ArgumentMatchers.any(byte[].class),
            ArgumentMatchers.eq(1),
            ArgumentMatchers.eq(0));
  }

  @BeforeEach
  void beforeEachTest() {
    var receiver = new Receiver(serialPort);
    var transmitter = new Transmitter<ESP32s2Pin>(serialPort);
    gpioInputOutput = new GpioInputOutputImpl<>(
        ESP32s2Pin.class,
        receiver,
        transmitter);
    gpioInputOutput.populateFields();
    inOrder =  Mockito.inOrder(serialPort, levelConsumer, statusConsumer);
  }

  @Test
  public void availableAndOnLineAtConstruction() {
    for (var pin : ESP32s2Pin.values()) {
      Truth.assertThat(gpioInputOutput.available(pin)).isTrue();
      Truth.assertThat(gpioInputOutput.active(pin)).isFalse();
      Truth.assertThat(gpioInputOutput.offline(pin)).isFalse();;
    }
  }

  @Test
  public void openPin18WhileForInputAvailable() {
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    // Open the pin, causing the library to send a pin open request to
    // the server. The pin should become unavailable and inactive on
    // return.
    var inputPin = gpioInputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusConsumer);
    Truth.assertThat(inputPin).isNotNull();
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    // Receive an "Open Successful" message to complete the operation.
    // Mark the pin as HIGH.
    receiveStatus(
        ESP32s2Pin.GPIO_18,
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.INPUT,
        PIN_HIGH);
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(inputPin.active()).isTrue();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.HIGH);

    receiveMutation(ESP32s2Pin.GPIO_18, Level.LOW);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    receiveMutation(ESP32s2Pin.GPIO_18, Level.HIGH);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.HIGH);

    // GpioInputOutputImpl.openForInput() must send an open request to the
    // server.
    verifyCommandSent(
        ConfigurationCommandCode.OPEN,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.UP);
    verifyStatusReceived();
    verifyStatusConsumed(
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PIN_HIGH);
    verifyMutationReceived();
    inOrder.verify(levelConsumer).accept(Level.LOW);
    inOrder.verify(levelConsumer).accept(Level.HIGH);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void openPin19WhileForInputWhileOpenForInput() {
    gpioInputOutput.inputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.ACTIVE);
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }


  @Test
  public void openPin19WhileForInputWhileOpenForOutput() {
    gpioInputOutput.outputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.ACTIVE);
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }


  @Test
  public void openPin19WhileForInputWhileInputOffline() {
    gpioInputOutput.inputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.OFFLINE);
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }


  @Test
  public void openPin19WhileForInputWhileOutputOffline() {
    gpioInputOutput.outputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.OFFLINE);
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void openPin18WhileForOutputAvailable() {
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    // Open the pin, causing the library to send a pin open request to
    // the server. The pin should become unavailable and inactive on
    // return.

    var outputPin = gpioInputOutput.openForOutput(
        ESP32s2Pin.GPIO_18,
        statusConsumer);
    Truth.assertThat(outputPin).isNotNull();
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(outputPin.active()).isFalse();
    Truth.assertThat(outputPin.offline()).isFalse();

    // Receive an "Open Successful" message to complete the operation.
    // Mark the pin as HIGH.
    receiveStatus(
        ESP32s2Pin.GPIO_18,
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.OUTPUT,
        PIN_LOW);
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(outputPin.active()).isTrue();
    Truth.assertThat(outputPin.offline()).isFalse();

    verifyCommandSent(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);

    verifyStatusReceived();
    verifyStatusConsumed(
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PIN_LOW);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void openPin19WhileForOutputWhileOpenForInput() {
    gpioInputOutput.inputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.ACTIVE);
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForOutput(
        ESP32s2Pin.GPIO_18,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }


  @Test
  public void openPin19WhileForOutputWhileOpenForOutput() {
    gpioInputOutput.outputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.ACTIVE);
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForOutput(
        ESP32s2Pin.GPIO_18,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }


  @Test
  public void openPin19WhileForOutputWhileInputOffline() {
    gpioInputOutput.inputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.OFFLINE);
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForOutput(
        ESP32s2Pin.GPIO_18,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }


  @Test
  public void openPin19WhileForOutputWhileOutputOffline() {
    gpioInputOutput.outputPinProxy(ESP32s2Pin.GPIO_18).setState(PinState.OFFLINE);
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.openForOutput(
        ESP32s2Pin.GPIO_18,
        statusConsumer)).isNull();
    inOrder.verifyNoMoreInteractions();
  }
}
