/*
 * InputPinTest.java
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
 * Validate {@link }InputPinProxy}
 */
@ExtendWith(MockitoExtension.class)
class InputPinProxyTest {

  private static final byte HIGH = 1;
  private static final byte LOW = 0;

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private Consumer<IOStatusCode> callback;
  @Mock
  private Consumer<IOStatusCode> openRequestedCallback;
  @Mock
  private Transmitter<ESP32s2Pin> outputChannel;
  @Mock
  private Consumer<Level> levelConsumer;

  private InputPinProxy<ESP32s2Pin> inputPin;
  private InOrder inOrder;

  @BeforeEach
  public void beforeEachTest() {
    inputPin = new InputPinProxy<>(
        ESP32s2Pin.GPIO_18,
        outputChannel);
    inputPin.setStatusCallback(callback);
    inOrder = Mockito.inOrder(
        callback, openRequestedCallback, outputChannel, levelConsumer);
  }

  @Test
  public void construction() {
    Truth.assertThat(inputPin).isNotNull();
    Truth.assertThat(inputPin.available()).isTrue();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void successfulDoOpenReceiveCloseCycle() {

    Mockito.doReturn(true)
            .when(outputChannel).sendCommand(
                ConfigurationCommandCode.CLOSE,
                StatusScope.INPUT,
                ESP32s2Pin.GPIO_18,
                PullMode.FLOAT);
    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.OPEN,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.UP);

    Truth.assertThat(inputPin.doOpen(
        PullMode.UP,
        levelConsumer,
        openRequestedCallback)).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.OPEN_PENDING);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.OPEN_SUCCEEDED,
        HIGH)).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isTrue();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.HIGH);
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);

    inputPin.receiveMutation(Level.LOW);
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isTrue();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);

    Truth.assertThat(inputPin.close()).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.CLOSE_PENDING);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.CLOSE_SUCCEEDED,
        LOW)).isTrue();
    Truth.assertThat(inputPin.available()).isTrue();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();

    inOrder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.UP);
    inOrder.verify(openRequestedCallback).accept(IOStatusCode.OPEN_SUCCEEDED);
    inOrder.verify(levelConsumer).accept(Level.LOW);
    inOrder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.CLOSE,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inOrder.verify(openRequestedCallback).accept(IOStatusCode.CLOSE_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();
  }


  @Test
  public void redundantDoOpen() {

    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.OPEN,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.DOWN);
    Truth.assertThat(inputPin.doOpen(
        PullMode.DOWN,
        levelConsumer,
        openRequestedCallback)).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.OPEN_PENDING);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.OPEN_SUCCEEDED,
        LOW)).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isTrue();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);

    inputPin.receiveMutation(Level.LOW);
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isTrue();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);

    Truth.assertThat(inputPin.doOpen(
        PullMode.BOTH,
        levelConsumer,
        callback)).isFalse();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isTrue();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    inputPin.receiveMutation(Level.HIGH);
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isTrue();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.value()).isEqualTo(Level.HIGH);
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.ACTIVE);

    inOrder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.DOWN);
    inOrder.verify(openRequestedCallback).accept(IOStatusCode.OPEN_SUCCEEDED);
    inOrder.verify(levelConsumer).accept(Level.LOW);
    inOrder.verify(levelConsumer).accept(Level.HIGH);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void successfulOpen() {

    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.OPEN,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.UP);

    var pinForTheUser = inputPin.open(
        PullMode.UP,
        levelConsumer,
        openRequestedCallback);
    Truth.assertThat(pinForTheUser).isNotNull();
    Truth.assertThat(pinForTheUser.active()).isFalse();
    Truth.assertThat(pinForTheUser.offline()).isFalse();
    Truth.assertThat(pinForTheUser.value()).isEqualTo(Level.LOW);


    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.OPEN_SUCCEEDED,
        HIGH)).isTrue();
    Truth.assertThat(pinForTheUser.active()).isTrue();
    Truth.assertThat(pinForTheUser.offline()).isFalse();
    Truth.assertThat(pinForTheUser.value()).isEqualTo(Level.HIGH);
  }

  @Test
  public void resetSuccessfullyFromAvailable() {
    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.RESET,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.FLOAT);

    inputPin.setValue(Level.HIGH);

    Truth.assertThat(inputPin.reset()).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.RESET_PENDING);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.RESET_SUCCEEDED,
        LOW)).isTrue();

    inOrder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.RESET,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inOrder.verify(callback).accept(IOStatusCode.RESET_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void resetSuccessfullyFromOpenPending() {
    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.RESET,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.FLOAT);

    inputPin.setValue(Level.HIGH);
    inputPin.setState(PinState.OPEN_PENDING);

    Truth.assertThat(inputPin.reset()).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.RESET_PENDING);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.RESET_SUCCEEDED,
        LOW)).isTrue();

    inOrder.verify(outputChannel).sendCommand(
            ConfigurationCommandCode.RESET,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.FLOAT);

    inOrder.verify(callback).accept(IOStatusCode.RESET_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void resetSuccessfullyFromActive() {
    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.RESET,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.FLOAT);

    inputPin.setValue(Level.HIGH);
    inputPin.setState(PinState.ACTIVE);

    Truth.assertThat(inputPin.reset()).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.RESET_PENDING);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.RESET_SUCCEEDED,
        LOW)).isTrue();

    inOrder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.RESET,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inOrder.verify(callback).accept(IOStatusCode.RESET_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void resetSuccessfullyFromClosePending() {
    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.RESET,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.FLOAT);

    inputPin.setValue(Level.HIGH);
    inputPin.setState(PinState.CLOSE_PENDING);

    Truth.assertThat(inputPin.reset()).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.RESET_PENDING);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.RESET_SUCCEEDED,
        LOW)).isTrue();

    inOrder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.RESET,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inOrder.verify(callback).accept(IOStatusCode.RESET_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void resetSuccessfullyFromOffline() {
    Mockito.doReturn(true)
        .when(outputChannel).sendCommand(
            ConfigurationCommandCode.RESET,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            PullMode.FLOAT);

    inputPin.setValue(Level.HIGH);
    inputPin.setState(PinState.OFFLINE);

    Truth.assertThat(inputPin.reset()).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.RESET_PENDING);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.RESET_SUCCEEDED,
        LOW)).isTrue();

    inOrder.verify(outputChannel).sendCommand(
        ConfigurationCommandCode.RESET,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_18,
        PullMode.FLOAT);
    inOrder.verify(callback).accept(IOStatusCode.RESET_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();

  }

  @Test
  public void resetSuccessfullyFromAResetPending() {
    // We presume that a prior reset call set the pin value
    // LOW.
    inputPin.setValue(Level.LOW);
    inputPin.setState(PinState.RESET_PENDING);

    Truth.assertThat(inputPin.reset()).isTrue();
    Truth.assertThat(inputPin.available()).isFalse();
    Truth.assertThat(inputPin.active()).isFalse();
    Truth.assertThat(inputPin.offline()).isFalse();
    Truth.assertThat(inputPin.getState()).isEqualTo(PinState.RESET_PENDING);
    Truth.assertThat(inputPin.value()).isEqualTo(Level.LOW);

    Truth.assertThat(inputPin.receivePinConfigurationStatus(
        IOStatusCode.RESET_SUCCEEDED,
        LOW)).isTrue();

    // Note that reset() is idempotent, so it has no effect
    // when the pin is waiting for reset status.
    inOrder.verify(callback).accept(IOStatusCode.RESET_SUCCEEDED);
    inOrder.verifyNoMoreInteractions();
  }
}
