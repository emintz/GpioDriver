/*
 * PhysicalToGpioPinTest.java
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Validates {@link PhysicalToGpioPin}
 */
class PhysicalToGpioPinTest {

  private static ArrayList<Integer> EXPECTED_KEYS;

  PhysicalToGpioPin<ESP32s2Pin> physicalToLogical;

  @BeforeAll
  public static void beforeTesting() {
    EXPECTED_KEYS = new ArrayList<>();
    for (var value : ESP32s2Pin.values()) {
      EXPECTED_KEYS.add(value.getPinNumber());
    }
  }

  @BeforeEach
  void beforeEachTest() {
    physicalToLogical = new PhysicalToGpioPin<>(ESP32s2Pin.class);
  }

  @Test
  public void keys() {
    Truth.assertThat(physicalToLogical.gpioMap().keySet())
        .containsExactlyElementsIn(EXPECTED_KEYS);
  }

  @Test
  public void values() {
    for (var logicalPin : ESP32s2Pin.values()) {
      Truth.assertWithMessage(
          "Physical GPIO %s: ",
          Integer.toString(logicalPin.getPinNumber()))
          .that(physicalToLogical.toLogical((byte) logicalPin.getPinNumber()))
          .isEqualTo(logicalPin);
    }
  }

  @Test
  public void forEach() {
    ArrayList<ESP32s2Pin> pinsImMap = new ArrayList<>();

    physicalToLogical.forEach(
        (physicalPin, logicalPin) -> pinsImMap.add(logicalPin));
    Truth.assertThat(pinsImMap).containsExactlyElementsIn(ESP32s2Pin.values());
  }
}
