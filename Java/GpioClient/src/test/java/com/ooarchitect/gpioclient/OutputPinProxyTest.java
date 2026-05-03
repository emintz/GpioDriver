/*
 * OutputPinTest.java
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
import org.junit.Rule;
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
 * Validates OutputPinV2
 */
@ExtendWith(MockitoExtension.class)
class OutputPinProxyTest {

  private static final byte LOW = 0;
  private static final byte HIGH = 1;

  private static final byte EXPECTED_HIGH = (byte)(0x80 + ESP32s2Pin.GPIO_18.number());
  private static final byte EXPECTED_LOW = ESP32s2Pin.GPIO_18.number();

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private Consumer<IOStatusMessage<ESP32s2Pin>> callback;
  @Mock
  private Consumer<IOStatusMessage<ESP32s2Pin>> openRequestedCallback;
  @Mock
  private Transmitter<ESP32s2Pin> outputChannel;

  private OutputPinProxy<ESP32s2Pin> outputPin;

  @BeforeEach
  public void beforeEachTest() {
    outputPin = new OutputPinProxy<>(
        ESP32s2Pin.GPIO_18,
        outputChannel);
    outputPin.setStatusCallback(callback);
  }

  @Test
  public void outputFailureOnSend() {
    var inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    Mockito.when(outputChannel.sendMutation(Mockito.anyByte())).thenReturn(false);

    // Note that we are faking the openXXX, so we verify callbacks
    // on the callback installed at construction.
    outputPin.setState(PinState.OPEN_PENDING);

    outputPin.receivePinConfigurationStatus(IOStatusCode.OPEN_SUCCEEDED, LOW);
    Truth.assertThat(outputPin.available()).isFalse();
    Truth.assertThat(outputPin.active()).isTrue();
    Truth.assertThat(outputPin.offline()).isFalse();
    Truth.assertThat(outputPin.send(Level.LOW)).isFalse();
    Truth.assertThat(outputPin.send(Level.HIGH)).isFalse();

    outputPin.receivePinConfigurationStatus(IOStatusCode.CLOSE_SUCCEEDED, HIGH);
    Truth.assertThat(outputPin.active()).isFalse();
    Truth.assertThat(outputPin.offline()).isTrue();
    inorder.verify(callback).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_SUCCEEDED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verify(outputChannel).sendMutation(EXPECTED_LOW);
    inorder.verify(outputChannel).sendMutation(EXPECTED_HIGH);
    inorder.verify(callback).accept(
        new IOStatusMessage<>(
            IOStatusCode.CLOSE_FAILED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            HIGH));
    inorder.verifyNoMoreInteractions();
  }


  @Test
  public void successfulClose() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.CLOSE,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT
    )).thenReturn(true);
    outputPin.setState(PinState.ACTIVE);
    Truth.assertThat(outputPin.close()).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.CLOSE_SUCCEEDED, LOW);
    Truth.assertThat(outputPin.available()).isTrue();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.CLOSE,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inorder.verify(callback).accept(
        new IOStatusMessage<>(
            IOStatusCode.CLOSE_SUCCEEDED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void failingClose() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.CLOSE,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT)).thenReturn(true);
    outputPin.setState(PinState.ACTIVE);
    Truth.assertThat(outputPin.close()).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.CLOSE_FAILED, LOW);
    Truth.assertThat(outputPin.available()).isFalse();
    Truth.assertThat(outputPin.offline()).isTrue();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.CLOSE,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inorder.verify(callback).accept(
        new IOStatusMessage<>(
            IOStatusCode.CLOSE_FAILED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void successfulPinReset() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.RESET,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT
        )).thenReturn(true);
    outputPin.setState(PinState.OFFLINE);
    Truth.assertThat(outputPin.offline()).isTrue();

    Truth.assertThat(outputPin.reset()).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.RESET_SUCCEEDED, (byte) 0);
    Truth.assertThat(outputPin.offline()).isFalse();
    Truth.assertThat(outputPin.available()).isTrue();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.RESET,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inorder.verify(callback).accept(
        new IOStatusMessage<>(
            IOStatusCode.RESET_SUCCEEDED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void successfulDoOpen() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT)).thenReturn(true);
    Truth.assertThat(outputPin.available()).isTrue();
    Truth.assertThat(outputPin.doOpen(openRequestedCallback)).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.OPEN_SUCCEEDED, (byte) 0);
    Truth.assertThat(outputPin.active()).isTrue();
    Truth.assertThat(outputPin.available()).isFalse();
    Truth.assertThat(outputPin.offline()).isFalse();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inorder.verify(openRequestedCallback).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_SUCCEEDED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void unsupportedDoOpen() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT)).thenReturn(true);
    Truth.assertThat(outputPin.available()).isTrue();
    Truth.assertThat(outputPin.doOpen(openRequestedCallback)).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.UNSUPPORTED, (byte) 0);
    Truth.assertThat(outputPin.active()).isFalse();
    Truth.assertThat(outputPin.available()).isTrue();
    Truth.assertThat(outputPin.offline()).isFalse();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inorder.verify(openRequestedCallback).accept(
        new IOStatusMessage<>(
        IOStatusCode.UNSUPPORTED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void successfulOpen() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT)).thenReturn(true);
    Truth.assertThat(outputPin.available()).isTrue();
    var pinHandle = outputPin.open(openRequestedCallback);
    Truth.assertThat(pinHandle).isNotNull();
    Truth.assertThat(pinHandle.active()).isFalse();
    Truth.assertThat(pinHandle.offline()).isFalse();
    outputPin.receivePinConfigurationStatus(IOStatusCode.OPEN_SUCCEEDED, (byte) 0);
    Truth.assertThat(pinHandle.active()).isTrue();
    Truth.assertThat(outputPin.offline()).isFalse();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inorder.verify(openRequestedCallback).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_SUCCEEDED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void failedOpen() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT)).thenReturn(false);
    Truth.assertThat(outputPin.available()).isTrue();
    var pinHandle = outputPin.open(openRequestedCallback);
    Truth.assertThat(pinHandle).isNull();
  }

  @Test
  public void unsupportedOpen() {
    Mockito.when(outputChannel.sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT)).thenReturn(true);
    Truth.assertThat(outputPin.available()).isTrue();
    var pinHandle = outputPin.open(openRequestedCallback);
    Truth.assertThat(pinHandle).isNotNull();
    Truth.assertThat(pinHandle.active()).isFalse();
    Truth.assertThat(pinHandle.offline()).isFalse();
    outputPin.receivePinConfigurationStatus(IOStatusCode.UNSUPPORTED, (byte) 0);
    Truth.assertThat(pinHandle.active()).isFalse();
    Truth.assertThat(pinHandle.offline()).isFalse();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inorder.verify(openRequestedCallback).accept(
        new IOStatusMessage<>(
            IOStatusCode.UNSUPPORTED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            LOW));
    inorder.verifyNoMoreInteractions();
  }
}
