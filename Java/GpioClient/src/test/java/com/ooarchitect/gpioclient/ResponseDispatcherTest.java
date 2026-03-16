/*
 * ResponseDispatcherTest.java
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
import org.junit.jupiter.api.BeforeAll;
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

@ExtendWith(MockitoExtension.class)
class ResponseDispatcherTest {

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  static PhysicalToGpioPin<ESP32s2Pin> physicalToGpioPin;

  ResponseDispatcher<ESP32s2Pin> handler;

  @Mock ByteSupplier byteSupplier;
  @Mock PinValueConsumer pinValueConsumer;
  @Mock Consumer<IOStatusMessage<ESP32s2Pin>> statusConsumer;

  @BeforeAll
  public static void beforeRunningTests() {
    physicalToGpioPin = new PhysicalToGpioPin<>(ESP32s2Pin.class);
  }

  @BeforeEach
  public void setUpForTest() {
    handler = new ResponseDispatcher<>(
        byteSupplier,
        pinValueConsumer,
        statusConsumer,
        physicalToGpioPin);
  }

  private void processOneByte(int input) {
    try {
      Mockito.when(byteSupplier.get()).thenReturn((byte) input);
      handler.processOneByte();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void oneMutation() {
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.CREATED);
    processOneByte(0x10);
    Mockito.verify(pinValueConsumer).accept((byte) 0x10);
    Mockito.verifyNoMoreInteractions(pinValueConsumer);
    Mockito.verifyNoInteractions(statusConsumer);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
  }

  @Test
  void twoMutations() {
    processOneByte(0x10);
    processOneByte(0x11);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(pinValueConsumer).accept((byte) 0x10);
    inOrder.verify(pinValueConsumer).accept((byte) 0x11);
    Mockito.verifyNoMoreInteractions(pinValueConsumer);
    Mockito.verifyNoInteractions(statusConsumer);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
  }

  @Test
  void oneStatus() {
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    processOneByte(IOStatusCode.OPEN_FAILED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_STATUS);
    processOneByte(StatusScope.OUTPUT.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_INPUT_OUTPUT_SCOPE);
    processOneByte(137);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_SIDE_DATA);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.STATUS_RECEIVED);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(statusConsumer).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_FAILED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            (byte) 137));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void oneStatusOneMutation() {
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    processOneByte((byte) IOStatusCode.OPEN_FAILED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_STATUS);
    processOneByte(StatusScope.OUTPUT.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_INPUT_OUTPUT_SCOPE);
    processOneByte(137);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_SIDE_DATA);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.STATUS_RECEIVED);
    processOneByte(42);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(statusConsumer).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_FAILED, StatusScope.OUTPUT, ESP32s2Pin.GPIO_18,
            (byte) 137));
    inOrder.verify(pinValueConsumer).accept((byte) 42);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void oneMutationOneStatus() {
    processOneByte(42);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    processOneByte(IOStatusCode.OPEN_FAILED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_STATUS);
    processOneByte(StatusScope.OUTPUT.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_INPUT_OUTPUT_SCOPE);
    processOneByte(137);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_SIDE_DATA);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.STATUS_RECEIVED);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(pinValueConsumer).accept((byte) 42);
    inOrder.verify(statusConsumer).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_FAILED, StatusScope.OUTPUT, ESP32s2Pin.GPIO_18,
            (byte) 137));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void spuriousDoubleEndBetweenMutations() {
    processOneByte(42);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.IDLE);
    processOneByte(137);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(pinValueConsumer).accept((byte) 42);
    inOrder.verify(pinValueConsumer).accept((byte) 137);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void emptyStatusBetweenMutations() {
    processOneByte(42);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.IDLE);
    processOneByte(137);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(pinValueConsumer).accept((byte) 42);
    inOrder.verify(pinValueConsumer).accept((byte) 137);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void oneResetAfterMutation() {
    // An ordinary mutation
    processOneByte(42);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
    // Bogus end of status
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // Pin Number -- ignored in this case
    processOneByte(0);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // Recovery status, nominally due to a reset request.
    processOneByte(IOStatusCode.RESET_SUCCEEDED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // Side data --ignored
    processOneByte(0x10);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // Side data -- ignored
    processOneByte(0x0F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // End of message
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.IDLE);
    processOneByte(2);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(pinValueConsumer).accept((byte) 42);
    inOrder.verify(pinValueConsumer).accept((byte) 2);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void twoResetsAfterMutation() {
    // An ordinary mutation
    processOneByte(42);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.MUTATING);
    // Bogus end of status
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);

    // Pin Number -- ignored in this case
    processOneByte(0);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // First recovery status, nominally due to a reset request. Should be ignored
    processOneByte(IOStatusCode.RESET_SUCCEEDED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // Side data --ignored
    processOneByte(0x10);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // Side data -- ignored
    processOneByte(0x0F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // End of message
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.IDLE);

    // Second reset status, should be processed
    // Stat of message
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    // GPIO Pin, ignored. Must be valid.
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    // Status
    processOneByte(IOStatusCode.RESET_SUCCEEDED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_STATUS);
    // Inpur-Ourpur axopw
    processOneByte(StatusScope.OUTPUT.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_INPUT_OUTPUT_SCOPE);
    // Side data, ignored, but useful for verification
    processOneByte(7);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_SIDE_DATA);
    // End message
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.STATUS_RECEIVED);

    // Close with a mutation
    processOneByte(2);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);

    inOrder.verify(pinValueConsumer).accept((byte) 42);
    inOrder.verify(statusConsumer).accept(new IOStatusMessage<>(
        IOStatusCode.RESET_SUCCEEDED, StatusScope.OUTPUT, ESP32s2Pin.GPIO_18,
        (byte) 7));
    inOrder.verify(pinValueConsumer).accept((byte) 2);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void messageOverrun() {
    // Message starts
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    // GPIO number
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    // Pin status
    processOneByte(3);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_STATUS);
    // Input-output scope
    processOneByte(StatusScope.OUTPUT.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_INPUT_OUTPUT_SCOPE);
    // Side data
    processOneByte(137);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_SIDE_DATA);
    // Overrun
    processOneByte(99);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    // Message ends
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.IDLE);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void statusShortByTwo() {
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.RECOVERING);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void statusSideDataEqualsMessageEnd() {
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    processOneByte(IOStatusCode.OPEN_FAILED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_STATUS);
    processOneByte(StatusScope.OUTPUT.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_INPUT_OUTPUT_SCOPE);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_SIDE_DATA);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.STATUS_RECEIVED);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(statusConsumer).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_FAILED, StatusScope.OUTPUT, ESP32s2Pin.GPIO_18,
            (byte) 0x7F));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void statusSideDataEqualsMessageStart() {
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseDispatcher.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_NUMBER);
    processOneByte(IOStatusCode.OPEN_FAILED.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_PIN_STATUS);
    processOneByte(StatusScope.OUTPUT.ordinal());
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_INPUT_OUTPUT_SCOPE);
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.HAVE_SIDE_DATA);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseDispatcher.State.STATUS_RECEIVED);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(statusConsumer).accept(
        new IOStatusMessage<>(
            IOStatusCode.OPEN_FAILED,
            StatusScope.OUTPUT, ESP32s2Pin.GPIO_18,
            (byte) 0xFF));
    inOrder.verifyNoMoreInteractions();
  }
}
