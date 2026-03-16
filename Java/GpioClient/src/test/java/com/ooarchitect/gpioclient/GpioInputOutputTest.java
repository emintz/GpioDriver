package com.ooarchitect.gpioclient;

import com.google.common.truth.Truth;
import com.ooarchitect.gpioclient.testing.TestByteSupplier;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

/**
 * Validate {@link GpioInputOutput}
 */
@ExtendWith(MockitoExtension.class)
class GpioInputOutputTest {

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private GpioReader<ESP32s2Pin> reader;

  @Mock
  private GpioWriter<ESP32s2Pin> writer;

  @Mock
  Consumer<IOStatusCode> statusCallback;

  @Mock
  Consumer<IOStatusMessage<ESP32s2Pin>> statusMessageConsumer;

  @Mock
  InputPinImpl inputPin;

  @Mock
  OutputPinImpl outputPin;

  @Mock
  PinLevelConsumer levelConsumer;

  InOrder inOrder;

  TestByteSupplier byteSupplier;
  GpioInputOutput.StatusRouter<ESP32s2Pin> statusRouter;
  GpioInputOutput<ESP32s2Pin> inputOutput;
  ResponseDispatcher<ESP32s2Pin> responseDispatcher;

  @BeforeEach
  public void beforeEachTest() {
    inOrder = Mockito.inOrder(
        reader, writer, statusCallback, statusMessageConsumer, levelConsumer);
    var physicalToGpioPin = new PhysicalToGpioPin<>(ESP32s2Pin.class);
    byteSupplier = new TestByteSupplier();
    statusRouter = new GpioInputOutput.StatusRouter<ESP32s2Pin>(
        reader, writer);
    responseDispatcher = new ResponseDispatcher<ESP32s2Pin>(
        byteSupplier,
        reader,
        statusMessageConsumer,
        physicalToGpioPin);
    inputOutput = new GpioInputOutput<ESP32s2Pin>(
        reader,
        writer,
        responseDispatcher);
  }

  @Test
  public void readerAndWriterInactive() {
    Mockito.doReturn(false).when(reader).active(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(false).when(writer).active(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.active(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(reader).active(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).active(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readActiveWriteInactive() {
    Mockito.doReturn(true).when(reader).active(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(false).when(writer).active(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(reader).active(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).active(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerInactiveWriterActive() {
    Mockito.doReturn(false).when(reader).active(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(true).when(writer).active(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.active(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(reader).active(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).active(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerActiveWriterActive() {
    Mockito.doReturn(true).when(reader).active(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(true).when(writer).active(ESP32s2Pin.GPIO_18);
    var e = Assertions.assertThrows(
        IllegalStateException.class,
        () -> inputOutput.active(ESP32s2Pin.GPIO_18));
    Truth.assertThat(e).hasMessageThat().contains(ESP32s2Pin.GPIO_18.toString());
    inOrder.verify(reader).active(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).active(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerAndWriterAvailable() {
    Mockito.doReturn(true).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(true).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.available(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(reader).available(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).available(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerAvailableWriterNotAvailable() {
    Mockito.doReturn(true).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(false).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(reader).available(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).available(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerNotAvailableWriterAvailable() {
    Mockito.doReturn(false).when(reader).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(reader).available(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerAndWriterNotAvailable() {
    Mockito.doReturn(false).when(reader).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.available(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(reader).available(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerAndWriterNotOffline() {
    Mockito.doReturn(false).when(reader).offline(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(false).when(writer).offline(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.offline(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(reader).offline(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).offline(ESP32s2Pin.GPIO_18);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void readerNotOfflineWriterOffline() {
    Mockito.lenient().doReturn(false).when(reader).offline(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(true).when(writer).offline(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.offline(ESP32s2Pin.GPIO_18)).isTrue();
  }

  @Test
  public void readerOfflineWriterNotOffline() {
    Mockito.lenient().doReturn(true).when(reader).offline(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(false).when(writer).offline(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.offline(ESP32s2Pin.GPIO_18)).isTrue();
  }

  @Test
  public void readerAndWriterOffline() {
    Mockito.lenient().doReturn(true).when(reader).offline(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(true).when(writer).offline(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.offline(ESP32s2Pin.GPIO_18)).isTrue();
  }

  @Test
  public void openForInputReaderAndWriterAvailable() {
    Mockito.doReturn(true).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(true).when(writer).available(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(inputPin).when(reader).open(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback);
    Truth.assertThat(inputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback)).isSameInstanceAs(inputPin);
    inOrder.verify(reader).available(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).available(ESP32s2Pin.GPIO_18);
    inOrder.verify(reader).open(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void openForInputReaderAvailableWriterNotAvailable() {
    Mockito.lenient().doReturn(true).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(false).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback)).isNull();
  }

  @Test
  public void openForInputReaderNotAvailableWriterNotAvailable() {
    Mockito.lenient().doReturn(false).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(true).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback)).isNull();
  }

  @Test
  public void openForInputReaderAndWriterNotAvailable() {
    Mockito.lenient().doReturn(false).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(false).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.openForInput(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback)).isNull();
  }

  @Test
  public void openForOutputReaderAndWriterAvailable() {
    Mockito.doReturn(true).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(true).when(writer).available(ESP32s2Pin.GPIO_18);
    Mockito.doReturn(outputPin).when(writer).open(
        ESP32s2Pin.GPIO_18, statusCallback);
    Truth.assertThat(inputOutput.openForOutput(
        ESP32s2Pin.GPIO_18, statusCallback))
            .isSameInstanceAs(outputPin);
    inOrder.verify(reader).available(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).available(ESP32s2Pin.GPIO_18);
    inOrder.verify(writer).open(ESP32s2Pin.GPIO_18, statusCallback);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void openForOutputReaderAvailableWriterNotAvailable() {
    Mockito.lenient().doReturn(true).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(false).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.openForOutput(
            ESP32s2Pin.GPIO_18, statusCallback))
        .isNull();
  }

  @Test
  public void openForOutputReaderNotAvailableWriterAvailable() {
    Mockito.lenient().doReturn(false).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(true).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.openForOutput(
            ESP32s2Pin.GPIO_18, statusCallback))
        .isNull();
  }

  @Test
  public void openForOutputReaderAndWriterNotAvailable() {
    Mockito.lenient().doReturn(false).when(reader).available(ESP32s2Pin.GPIO_18);
    Mockito.lenient().doReturn(false).when(writer).available(ESP32s2Pin.GPIO_18);
    Truth.assertThat(inputOutput.openForOutput(
            ESP32s2Pin.GPIO_18, statusCallback))
        .isNull();
  }

  @Test
  public void statusMessageToSingleInputPin() {
    IOStatusMessage<ESP32s2Pin> message =
        new IOStatusMessage<>(
            IOStatusCode.RESET_SUCCEEDED,
            StatusScope.INPUT,
            ESP32s2Pin.GPIO_18,
            (byte) 0);
    statusRouter.accept(message);
    Mockito.verify(reader).dispatchToTargetPin(message);
  }

  @Test
  public void statusMessageToSingleOutputPin() {
    IOStatusMessage<ESP32s2Pin> message =
        new IOStatusMessage<>(
            IOStatusCode.RESET_SUCCEEDED,
            StatusScope.OUTPUT,
            ESP32s2Pin.GPIO_18,
            (byte) 0);
    statusRouter.accept(message);
    Mockito.verify(writer).dispatchToTargetPin(message);
  }

  @Test
  public void statusMessageToSingleInputAndOutputPin() {
    IOStatusMessage<ESP32s2Pin> message =
        new IOStatusMessage<>(
            IOStatusCode.RESET_SUCCEEDED,
            StatusScope.INPUT_OUTPUT,
            ESP32s2Pin.GPIO_18,
            (byte) 0);
    statusRouter.accept(message);
    Mockito.verify(reader).dispatchToTargetPin(message);
    Mockito.verify(writer).dispatchToTargetPin(message);
  }

  @Test
  public void statusMessageToAllInputAndOutputPins() {
    IOStatusMessage<ESP32s2Pin> message =
        new IOStatusMessage<>(
            IOStatusCode.RESET_SUCCEEDED,
            StatusScope.SERVER,
            ESP32s2Pin.GPIO_18,
            (byte) 0);
    statusRouter.accept(message);
    Mockito.verify(reader).dispatchToAllPins(message);
    Mockito.verify(writer).dispatchToAllPins(message);
  }
}
