/*
 * GpioWriterTest.java
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Validate{@link GpioWriter} and {@link BaseGpioIO}
 */
@ExtendWith(MockitoExtension.class)
class GpioWriterTest {

  private static final byte LOW = 0;
  private static final byte HIGH = 1;

  /**
   * We need the mock output pins to be stored in a well-defined
   * order so that we can verify in-order invocation. Therefore,
   * we store them in a {@link TreeMap} in physical pin order.
   * The following {@link Comparator} accomplishes this.
   */
  private static final Comparator<ESP32s2Pin> PHYSICAL_PIN_ORDER =
      (esp32s2Pin, t1) -> Integer.signum(esp32s2Pin.number() - t1.number());

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  private Map<ESP32s2Pin, OutputPinProxy> outputPins;
  private OutputPinProxy targetPin;
  private GpioWriter<ESP32s2Pin> writer;
  private InOrder inOrder;

  @Mock
  Consumer<IOStatusCode> statusCallback;

  @Mock
  private OutputPin outputPin;

  @BeforeEach
  public void beforeEachTest() {
    outputPins = new TreeMap<>(PHYSICAL_PIN_ORDER);
    for (var gpioPin : ESP32s2Pin.values()) {
      outputPins.put(
          gpioPin,
          Mockito.mock(
              OutputPinProxy.class));
    }
    targetPin = outputPins.get(ESP32s2Pin.GPIO_18);
    writer = new GpioWriter<>(
        outputPins,
        new PhysicalToGpioPin<>(ESP32s2Pin.class));
    ArrayList<OutputPinProxy> pinsInOrder = new ArrayList<>(outputPins.size());
    for (var pinEntry : outputPins.entrySet()) {
      pinsInOrder.add(pinEntry.getValue());
    }
    OutputPinProxy[] pinArray = pinsInOrder.toArray(new OutputPinProxy[0]);
    inOrder = Mockito.inOrder(pinArray);
  }

  //******************************************************************
  // Methods in the base class, BaseGpioIO
  //******************************************************************

  @Test
  public void pinAvailable() {
    Mockito.doReturn(true).when(targetPin).available();
    Truth.assertThat(writer.available(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(targetPin).available();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void pinNotAvailable() {
    Mockito.doReturn(false).when(targetPin).available();
    Truth.assertThat(writer.available(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(targetPin).available();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void pinActive() {
    Mockito.doReturn(true).when(targetPin).active();
    Truth.assertThat(writer.active(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(targetPin).active();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void pinNotActive() {
    Mockito.doReturn(false).when(targetPin).active();
    Truth.assertThat(writer.active(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(targetPin).active();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void successfulClose() {
    Mockito.doReturn(true).when(targetPin).close();
    Truth.assertThat(writer.close(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(targetPin).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void failedClose() {
    Mockito.doReturn(false).when(targetPin).close();
    Truth.assertThat(writer.close(ESP32s2Pin.GPIO_18)).isFalse();
    inOrder.verify(targetPin).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void dispatchToAllPins() {
    var statusMessage = new IOStatusMessage<ESP32s2Pin>(
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.OUTPUT,
        null,  // Ignored.
        LOW);
    writer.dispatchToAllPins(statusMessage);
    for (var entry : outputPins.entrySet()) {
      inOrder.verify(entry.getValue()).receivePinConfigurationStatus(
          IOStatusCode.OPEN_SUCCEEDED, LOW);
    }
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void dispatchToTargetPin() {
    var statusMessage = new IOStatusMessage<>(
        IOStatusCode.OPEN_SUCCEEDED,
        StatusScope.OUTPUT,
        ESP32s2Pin.GPIO_18,  // Ignored.
        HIGH);
    writer.dispatchToTargetPin(statusMessage);
    inOrder.verify(targetPin).receivePinConfigurationStatus(
        IOStatusCode.OPEN_SUCCEEDED, HIGH);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void offline() {
    Mockito.doReturn(true).when(targetPin).offline();
    Truth.assertThat(writer.offline(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(targetPin).offline();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void reset() {
    Mockito.doReturn(true).when(targetPin).reset();
    Truth.assertThat(writer.reset(ESP32s2Pin.GPIO_18)).isTrue();
    inOrder.verify(targetPin).reset();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void resetAll() {
    for (var entry : outputPins.entrySet()) {
      Mockito.doReturn(true).when(entry.getValue()).reset();
    }
    Truth.assertThat(writer.resetAll()).isTrue();
    for (var entry : outputPins.entrySet()) {
      inOrder.verify(entry.getValue()).reset();
    }
    inOrder.verifyNoMoreInteractions();
  }

  //******************************************************************
  // Methods in the concrete writer, GpioWriter
  //******************************************************************

  @Test
  public void pinAccessors() {
    Truth.assertThat(writer.pin(ESP32s2Pin.GPIO_18)).isSameInstanceAs(targetPin);
    Truth.assertThat(writer.pin((byte) 18)).isSameInstanceAs(targetPin);
  }

  @Test
  public void openSucceeds() {
    Mockito.doReturn(outputPin).when(targetPin).open(statusCallback);
    Truth.assertThat(writer.open(ESP32s2Pin.GPIO_18, statusCallback))
        .isSameInstanceAs(outputPin);
  }

  @Test
  public void openFails() {
    Mockito.doReturn(null).when(targetPin).open(statusCallback);
    Truth.assertThat(writer.open(ESP32s2Pin.GPIO_18, statusCallback)).isNull();
    inOrder.verify(targetPin).open(statusCallback);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void sendLowSucceeds() {
    Mockito.doReturn(true).when(targetPin).send(Level.LOW);
    Truth.assertThat(writer.send(ESP32s2Pin.GPIO_18, Level.LOW)).isTrue();
    inOrder.verify(targetPin).send(Level.LOW);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void sendLowFails() {
    Mockito.doReturn(false).when(targetPin).send(Level.LOW);
    Truth.assertThat(writer.send(ESP32s2Pin.GPIO_18, Level.LOW)).isFalse();
    inOrder.verify(targetPin).send(Level.LOW);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void sendHighSucceeds() {
    Mockito.doReturn(true).when(targetPin).send(Level.HIGH);
    Truth.assertThat(writer.send(ESP32s2Pin.GPIO_18, Level.HIGH)).isTrue();
    inOrder.verify(targetPin).send(Level.HIGH);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void sendHighFails() {
    Mockito.doReturn(false).when(targetPin).send(Level.HIGH);
    Truth.assertThat(writer.send(ESP32s2Pin.GPIO_18, Level.HIGH)).isFalse();
    inOrder.verify(targetPin).send(Level.HIGH);
    inOrder.verifyNoMoreInteractions();
  }
}
