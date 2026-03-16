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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Validate {@link GpioReader} Verifies input-specific functionality only.
 *
 * @see GpioWriterTest for base class method validation.
 */
class GpioReaderTest {

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

  private Map<ESP32s2Pin, InputPinProxy> inputPins;

  @Mock
  Consumer<IOStatusCode> statusCallback;

  @Mock
  PinLevelConsumer  levelConsumer;

  @Mock
  InputPin inputPin;

  InputPinProxy targetPin;
  private GpioReader<ESP32s2Pin> reader;
  InOrder inOrder;

  @BeforeEach
  public void beforeEachTest() {
    inputPins = new TreeMap<>(PHYSICAL_PIN_ORDER);
    for (var pin : ESP32s2Pin.values()) {
      var value = Mockito.mock(InputPinProxy.class);
      inputPins.put(
          pin,
          value);
    }
    targetPin = inputPins.get(ESP32s2Pin.GPIO_18);
    reader = new GpioReader<>(
        inputPins,
        new PhysicalToGpioPin<>(ESP32s2Pin.class));

    ArrayList<InputPinProxy> pinsInOrder = new ArrayList<>(inputPins.size());
    for (var pin : inputPins.entrySet()) {
      pinsInOrder.add(pin.getValue());
    }
    inOrder = Mockito.inOrder(pinsInOrder.toArray());
  }

  @Test
  public void openSucceeds() {
    Mockito.doReturn(inputPin).when(targetPin).open(
        PullMode.UP,
        levelConsumer,
        statusCallback);
    Truth.assertThat(reader.open(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback)).isSameInstanceAs(inputPin);
    inOrder.verify(targetPin).open(
        PullMode.UP,
        levelConsumer,
        statusCallback);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void openFails() {
    Mockito.doReturn(null).when(targetPin).open(
        PullMode.UP,
        levelConsumer,
        statusCallback);
    Truth.assertThat(reader.open(
        ESP32s2Pin.GPIO_18,
        PullMode.UP,
        levelConsumer,
        statusCallback)).isNull();
    inOrder.verify(targetPin).open(
        PullMode.UP,
        levelConsumer,
        statusCallback);
    inOrder.verifyNoMoreInteractions();

  }

  @Test
  public void valueLow() {
    Mockito.doReturn((byte) 0).when(targetPin).value();
    Truth.assertThat(reader.value(ESP32s2Pin.GPIO_18))
        .isEqualTo(Level.LOW);
    inOrder.verify(targetPin).value();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void valueHigh() {
    Mockito.doReturn((byte) 1).when(targetPin).value();
    Truth.assertThat(reader.value(ESP32s2Pin.GPIO_18))
        .isEqualTo(Level.HIGH);
    inOrder.verify(targetPin).value();
    inOrder.verifyNoMoreInteractions();
  }
}
