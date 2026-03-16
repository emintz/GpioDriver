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

@ExtendWith(MockitoExtension.class)
class ResponseHandlerTest {

  ResponseHandler<ESP32s2Pin> handler;

  @Mock ByteSupplier byteSupplier;
  @Mock PinValueConsumer pinValueConsumer;
  @Mock Consumer<PinConfigStatus<ESP32s2Pin>> statusConsumer;

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @BeforeEach
  public void setUpForTest() {
    handler = new ResponseHandler<>(
        ESP32s2Pin.class,
        byteSupplier,
        pinValueConsumer,
        statusConsumer);
  }

  private void processOneByte(int input) {
    Mockito.when(byteSupplier.get()).thenReturn((byte) input);
    handler.processOneByte();
  }

  @Test
  public void receiveOneMutation() {
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseHandler.State.CREATED);
    processOneByte(0x10);
    Mockito.verify(pinValueConsumer).accept((byte) 0x10);
    Mockito.verifyNoMoreInteractions(pinValueConsumer);
    Mockito.verifyNoInteractions(statusConsumer);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseHandler.State.MUTATING);
  }

  @Test
  void receiveTwoMutations() {
    processOneByte(0x10);
    processOneByte(0x11);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(pinValueConsumer).accept((byte) 0x10);
    inOrder.verify(pinValueConsumer).accept((byte) 0x11);
    Mockito.verifyNoMoreInteractions(pinValueConsumer);
    Mockito.verifyNoInteractions(statusConsumer);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseHandler.State.MUTATING);
  }

  @Test
  void receiveOneStatus() {
    processOneByte(0xFF);
    Truth.assertThat(handler.currentState())
        .isEqualTo(ResponseHandler.State.RECEIVING_STATUS_MESSAGE);
    processOneByte(3);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseHandler.State.HAVE_PIN_STATUS);
    processOneByte(18);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseHandler.State.HAVE_PIN_NUMBER);
    processOneByte(137);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseHandler.State.HAVE_SIDE_DATA);
    processOneByte(0x7F);
    Truth.assertThat(handler.currentState()).isEqualTo(ResponseHandler.State.STATUS_RECEIVED);
    InOrder inOrder = Mockito.inOrder(pinValueConsumer, statusConsumer);
    inOrder.verify(statusConsumer).accept(
        new PinConfigStatus<>(
            PinMutationStatus.OPEN_FAILED,
            ESP32s2Pin.GPIO_18,
            (byte) 137));
    inOrder.verifyNoMoreInteractions();
  }
}
