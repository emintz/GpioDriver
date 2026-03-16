/*
 * GpioReader.java
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

class GpioReader<T extends Enum<? extends GpioPinNumber>>
    extends BaseGpioIO<T, InputPinProxy>
    implements PinValueConsumer  {

  private static final BiFunction<
      Integer, OutputChannel, InputPinProxy> PIN_FACTORY =
      (integer, outputChannel) ->
          new InputPinProxy(integer.byteValue(), outputChannel);

  GpioReader(
      OutputChannel outputChannel,
      PhysicalToGpioPin<T> physicalToGpioPin) {
    super(
        outputChannel,
        physicalToGpioPin,
        PIN_FACTORY);
  }

  @VisibleForTesting
  GpioReader(
      Map<T, InputPinProxy> pins,
      PhysicalToGpioPin<T> physicalToGpioPin) {
    super(physicalToGpioPin, pins);
  }

  /**
   * Decodes an incoming mutation and forwards it to the
   * appropriate input pin. Discards the mutation if it cannot be delivered.
   * Note that this is meant to be a callback. Applications must not invoke
   * this method.
   *
   * @param mutation the pin value, an 8-bit byte whose
   * sign bit indicates the applied voltage (0 --> LOW, 1 --> HIGH)
   * and whose remaining bots provide the physical GPIO pin number.
   */
  @Override
  public void accept(byte mutation) {
    var pin = pin((byte) (mutation & 0x7E));
    if (pin != null) {
      pin.receiveMutation(
          (mutation & 0x80) == 0
          ? (byte) 0
          : (byte) 1);
    }
  }

  InputPin open(
      T pinId,
      PullMode resistorConfiguration,
      PinLevelConsumer levelConsumer,
      Consumer<IOStatusCode> statusCallback) {
    var pin = pin(pinId);
    return
        pin != null
        ? pin.open(
            resistorConfiguration,
            levelConsumer,
            statusCallback)
        : null;
  }

  Level value(T gpioPin) {
    var pin = pin(gpioPin);
    return pin != null ?
        (pin.value() == 0
            ? Level.LOW
            : Level.HIGH)
        : Level.LOW;
  }
}
