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

import com.google.common.truth.Truth;
import com.ooarchitect.gpioclient.testing.TestByteSupplier;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.function.Consumer;

/**
 * Validate the entire GPIO system with an end-to-end test.
 * This is just shy of an integration test because the
 * low level input and output are faked or mocked.
 */
@ExtendWith(MockitoExtension.class)
class GpioInputOutputEndToEndTest {

  private static final byte MESSAGE_START = (byte) 0xFF;
  private static final byte MESSAGE_END = (byte)  0x7F;

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();


  @Mock
  private Consumer<IOStatusCode> statusCallback;

  @Mock
  private OutputChannel outputChannel;

  @Mock
  private PinLevelConsumer pinLevelConsumer;

  InOrder inOrder;

  private TestByteSupplier byteSupplier;

  private GpioInputOutput<ESP32s2Pin> gpioInputOutput;

  @BeforeEach
  public void beforeEachTest() {
    inOrder = Mockito.inOrder(
        statusCallback,
        outputChannel,
        pinLevelConsumer,
        statusCallback);
    byteSupplier = new TestByteSupplier();
    gpioInputOutput = GpioInputOutput.create(
        ESP32s2Pin.class,
        outputChannel,
        byteSupplier);
    gpioInputOutput.start();
  }

  @AfterEach
  public void afterEachTest() {
    gpioInputOutput.stop();
  }

  private void send(IOStatusMessage<ESP32s2Pin> ioStatusMessage) {
    byte[] rawMessage = new byte[] {
      MESSAGE_START,
      ioStatusMessage.gpioPin().number(),
      (byte) ioStatusMessage.status().ordinal(),
      (byte) ioStatusMessage.scope().ordinal(),
      ioStatusMessage.side_data(),
      MESSAGE_END,
    };
    byteSupplier.put(rawMessage);
  }

  void send(byte rawMessage) {
    byteSupplier.put(rawMessage);
  }

  @Test
  public void testStartStop() {
    // Note: test passes if the shutdown does not hang.
    Truth.assertThat(gpioInputOutput).isNotNull();
  }

  @Test
  public void openForInputMutateClose() {
    Mockito.doReturn(true).when(outputChannel).send(Mockito.any());
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    InputPin inputPin = gpioInputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        pinLevelConsumer,
        statusCallback);
    Truth.assertThat(inputPin).isNotNull();
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    send(new IOStatusMessage<>(
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        (byte) 1));
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.HIGH);

    send((byte) 18);
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    send((byte) (0x80 + 18));
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.HIGH);

    inputPin.close();
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    send(new IOStatusMessage<>(
        IOStatusCode.CLOSE_SUCCEEDED,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        (byte) 0));
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    // Open request, client --> server
    inOrder.verify(outputChannel).send(
        new byte[] {
            (byte) ConfigurationCommandCode.OPEN.ordinal(),
            (byte) StatusScope.INPUT.ordinal(),
            ESP32s2Pin.GPIO_18.number(),
            (byte) PullMode.UP.ordinal()
        });

    // Open succeeded, server -> client
    inOrder.verify(statusCallback).accept(
            IOStatusCode.OPEN_SUCCEEDED);

    // First mutation (HIGH -> LOW), server -> client
    inOrder.verify(pinLevelConsumer).consume(Level.LOW);

    // Second mutation, (LOW -> HIGH), server -> client
    inOrder.verify(pinLevelConsumer).consume(Level.HIGH);

    // Close command, client -> server
    inOrder.verify(outputChannel).send(
        new byte[] {
            (byte) ConfigurationCommandCode.CLOSE.ordinal(),
            (byte) StatusScope.INPUT.ordinal(),
            ESP32s2Pin.GPIO_18.number(),
            (byte) PullMode.FLOAT.ordinal()
        });

    // Close succeeded, server -> client.
    inOrder.verify(statusCallback).accept(
        IOStatusCode.CLOSE_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void openForOutputMutateClose() {
    Mockito.doReturn(true).when(outputChannel).send(Mockito.any());
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    OutputPin outputPin = gpioInputOutput.openForOutput(
        ESP32s2Pin.GPIO_18,
        statusCallback);
    Truth.assertThat(outputPin).isNotNull();
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    send(new IOStatusMessage<>(
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        (byte) 0));
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    outputPin.send(Level.HIGH);
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    outputPin.send(Level.LOW);
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();


    // Open request, client --> server
    inOrder.verify(outputChannel).send(
        new byte[] {
            (byte) ConfigurationCommandCode.OPEN.ordinal(),
            (byte) StatusScope.OUTPUT.ordinal(),
            ESP32s2Pin.GPIO_18.number(),
            (byte) PullMode.FLOAT.ordinal()
        });

    outputPin.close();
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();

    send(new IOStatusMessage<>(
        IOStatusCode.CLOSE_SUCCEEDED,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        (byte) 0));
    Truth.assertThat(gpioInputOutput.available(ESP32s2Pin.GPIO_18)).isTrue();
    Truth.assertThat(gpioInputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    Truth.assertThat(gpioInputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();


    // Open succeeded, server -> client
    inOrder.verify(statusCallback).accept(
        IOStatusCode.OPEN_SUCCEEDED);

    // Raise pin voltage, client -> server
    inOrder.verify(outputChannel)
        .send((byte) (0x80 + ESP32s2Pin.GPIO_18.number()));

    // Lower pin voltage, client -> server
    inOrder.verify(outputChannel)
        .send(ESP32s2Pin.GPIO_18.number());

    // Close command, client -> server
    inOrder.verify(outputChannel).send(
        new byte[] {
            (byte) ConfigurationCommandCode.CLOSE.ordinal(),
            (byte) StatusScope.OUTPUT.ordinal(),
            ESP32s2Pin.GPIO_18.number(),
            (byte) PullMode.FLOAT.ordinal()
        });

    // Close succeeded, server -> client.
    inOrder.verify(statusCallback).accept(
        IOStatusCode.CLOSE_SUCCEEDED);

    inOrder.verifyNoMoreInteractions();
  }
}
