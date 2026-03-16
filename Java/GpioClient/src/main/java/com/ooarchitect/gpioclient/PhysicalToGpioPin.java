/*
 * PhysicalToGpioPin.java
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

import com.google.common.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Self configuring physical to logical GPIO pin converter.
 *
 * @param <T> GPIO pin enumeration that matches the ESP-32
 *           server model.
 */
class PhysicalToGpioPin<T extends Enum<? extends GpioPinNumber>> {
  // TODO: in an EnumMap<T, V> T must extend Enum<T> which ours
  //       does not, so we cheat and use a HashMap.
  // TODO: consider replacing with a factory that returns an ImmutableMap.
  private final HashMap<Integer, T> physicalToLogical;

  /**
   * Constructs and populates a conversion table.
   *
   * @param gpioPinType ESP32 pin enumeration
   */
  PhysicalToGpioPin(Class<T> gpioPinType) {
    physicalToLogical = new HashMap<>();
    for (T logicalGPIO : gpioPinType.getEnumConstants()) {
      int number = ((GpioPinNumber)logicalGPIO).number();
      physicalToLogical.put(number, logicalGPIO);
    }
  }

  @Nullable
  T toLogical(byte physical) {
    return physicalToLogical.get((int) physical);
  }

  void forEach(BiConsumer<Integer, T> consumer) {
    physicalToLogical.forEach(consumer);
  }

  @VisibleForTesting
  Map<Integer, T> gpioMap() {
    return physicalToLogical;
  }
}
