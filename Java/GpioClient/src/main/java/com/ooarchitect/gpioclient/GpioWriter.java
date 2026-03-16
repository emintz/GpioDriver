/*
 * GpioWriter.java
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

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

class GpioWriter<T extends Enum<? extends GpioPinNumber>>
    extends BaseGpioIO<T, OutputPinProxy> {

  /**
   * Populates the pin map with {@link OutputPinProxy} instances.
   */
  private static class PinPopulator implements BiFunction<Integer, OutputChannel, OutputPinProxy> {

    private PinPopulator() {
    }

    @Override
    public OutputPinProxy apply(Integer integer, OutputChannel outputChannel) {
      return new OutputPinProxy(integer.byteValue(), outputChannel);
    }
  }

  GpioWriter(
      OutputChannel outputChannel,
      PhysicalToGpioPin<T> physicalToGpioPin) {
    super(
        outputChannel,
        physicalToGpioPin,
        new PinPopulator());
  }

  @VisibleForTesting
  GpioWriter(
      Map<T, OutputPinProxy> pins,
      PhysicalToGpioPin<T> physicalToGpioPin) {
    super(physicalToGpioPin, pins);
  }

  OutputPin open(
      T pinId,
      Consumer<IOStatusCode> statusCallback) {
    var pin = pin(pinId);
    return pin != null
        ? pin.open(statusCallback)
        : null;
  }

  /**
   * Sends a level change commend to the server.
   *
   * @param gpioPin pin to change
   * @param value the desired level, high or low
   * @return {@code true} if the request was sent, {@code false}
   *         on failure.
   */
  public boolean send(T gpioPin, Level value) {
    var pin = pin(gpioPin);
    return pin != null && pin.send(value);
  }
}
