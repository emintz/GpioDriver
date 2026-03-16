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
class OutputPinTest {

  private static final byte LOW = 0;
  private static final byte HIGH = 1;

  private static final byte EXPECTED_HIGH = (byte)(0x80 + ESP32s2Pin.GPIO_18.number());
  private static final byte EXPECTED_LOW = (byte)ESP32s2Pin.GPIO_18.number();

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private Consumer<IOStatusCode> callback;
  @Mock
  private Consumer<IOStatusCode> openRequestedCallback;
  @Mock
  private OutputChannel outputChannel;

  private OutputPin outputPin;

  @BeforeEach
  public void beforeEachTest() {
    outputPin = new OutputPin(
        ESP32s2Pin.GPIO_18.number(),
        outputChannel);
    outputPin.setStatusCallback(callback);
  }

  @Test
  public void outputFailureOnSend() {
    Mockito.when(outputChannel.send(Mockito.anyByte())).thenReturn(false);

    // Note that we are faking the open, so we verify callbacks
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
    var inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(callback).accept(IOStatusCode.OPEN_SUCCEEDED);
    inorder.verify(outputChannel).send(EXPECTED_LOW);
    inorder.verify(outputChannel).send(EXPECTED_HIGH);
    inorder.verify(callback).accept(IOStatusCode.CLOSE_FAILED);
    inorder.verifyNoMoreInteractions();
  }


  @Test
  public void successfulClose() {
    byte[] expectedCloseCommand = {
        (byte) ConfigurationCommandCode.CLOSE.ordinal(),
        (byte) StatusScope.OUTPUT.ordinal(),
        18,
        (byte) PullMode.FLOAT.ordinal(),
    };
    Mockito.when(outputChannel.send(expectedCloseCommand)).thenReturn(true);
    outputPin.setState(PinState.ACTIVE);
    Truth.assertThat(outputPin.close()).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.CLOSE_SUCCEEDED, LOW);
    Truth.assertThat(outputPin.available()).isTrue();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).send(expectedCloseCommand);
    inorder.verify(callback).accept(IOStatusCode.CLOSE_SUCCEEDED);
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void failingClose() {
    byte[] expectedCloseCommand = {
        (byte) ConfigurationCommandCode.CLOSE.ordinal(),
        (byte) StatusScope.OUTPUT.ordinal(),
        18,
        (byte) PullMode.FLOAT.ordinal(),
    };
    Mockito.when(outputChannel.send(expectedCloseCommand)).thenReturn(true);
    outputPin.setState(PinState.ACTIVE);
    Truth.assertThat(outputPin.close()).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.CLOSE_FAILED, LOW);
    Truth.assertThat(outputPin.available()).isFalse();
    Truth.assertThat(outputPin.offline()).isTrue();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).send(expectedCloseCommand);
    inorder.verify(callback).accept(IOStatusCode.CLOSE_FAILED);
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void successfulPinReset() {
    byte[] expectedResetCommand = new byte[] {
        (byte) ConfigurationCommandCode.RESET.ordinal(),
        (byte) StatusScope.OUTPUT.ordinal(),
        18,
        (byte) PullMode.FLOAT.ordinal(),
    };
    Mockito.when(outputChannel.send(expectedResetCommand)).thenReturn(true);
    outputPin.setState(PinState.OFFLINE);
    Truth.assertThat(outputPin.offline()).isTrue();

    Truth.assertThat(outputPin.reset()).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.RESET_SUCCEEDED, (byte) 0);
    Truth.assertThat(outputPin.offline()).isFalse();
    Truth.assertThat(outputPin.available()).isTrue();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).send(expectedResetCommand);
    inorder.verify(callback).accept(IOStatusCode.RESET_SUCCEEDED);
    inorder.verifyNoMoreInteractions();
  }

  @Test
  public void unsupportedOpen() {
    byte[] expectedOpenCommand = new byte[] {
        (byte) ConfigurationCommandCode.OPEN.ordinal(),
        (byte) StatusScope.OUTPUT.ordinal(),
        18,
        (byte) PullMode.FLOAT.ordinal(),
    };
    Mockito.when(outputChannel.send(expectedOpenCommand)).thenReturn(true);
    Truth.assertThat(outputPin.available()).isTrue();
    Truth.assertThat(outputPin.open(openRequestedCallback)).isTrue();
    outputPin.receivePinConfigurationStatus(IOStatusCode.UNSUPPORTED, (byte) 0);
    Truth.assertThat(outputPin.active()).isFalse();
    Truth.assertThat(outputPin.available()).isTrue();
    Truth.assertThat(outputPin.offline()).isFalse();
    InOrder inorder = Mockito.inOrder(callback, outputChannel, openRequestedCallback);
    inorder.verify(outputChannel).send(expectedOpenCommand);
    inorder.verify(openRequestedCallback).accept(IOStatusCode.UNSUPPORTED);
    inorder.verifyNoMoreInteractions();
  }
}
