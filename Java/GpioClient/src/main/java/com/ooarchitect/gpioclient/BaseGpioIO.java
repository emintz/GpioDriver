/*
 * BaseGpioIO.java
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
import java.util.function.BiFunction;

/**
 * Base GPIO I/O class that implements direction-agnostic functionality.
 *
 * @param <T> GPIO pin identifier class
 * @param <P> Concrete GPIO pin class
 *
 * @see GpioPinNumber
 * @see GpioPinProxy
 */
abstract class BaseGpioIO<
    T extends Enum<? extends GpioPinNumber>,
    P extends GpioPinProxy> {

  private static class PinPopulator<
      T extends Enum<? extends GpioPinNumber>,
      P extends GpioPinProxy>
      implements BiConsumer<Integer, T> {

    private final OutputChannel outputChannel;
    private final BiFunction<Integer, OutputChannel, P> pinFactory;
    private final Map<T, P> pins;

    private PinPopulator(
        OutputChannel outputChannel,
        BiFunction<Integer, OutputChannel, P> pinFactory,
        Map<T, P> pins) {
      this.outputChannel = outputChannel;
      this.pinFactory = pinFactory;
      this.pins = pins;
    }

    @Override
    public void accept(Integer integer, T t) {
      pins.put(t, pinFactory.apply(integer, outputChannel));
    }
  }

  private final PhysicalToGpioPin<T> physicalToGpioPin;
  private final Map<T, P> pins;

  /**
   * Constructs an instance from the specified parameters
   *
   * @param outputChannel     connection to the GPIO I/O server
   * @param physicalToGpioPin maps physical GPIO  numbers
   *                          to enumerated pin IDs.
   * @param pinFactory        creates instances of {@code P}
   */
  protected BaseGpioIO(
      OutputChannel outputChannel,
      PhysicalToGpioPin<T> physicalToGpioPin,
      BiFunction<Integer, OutputChannel, P> pinFactory) {
    this.physicalToGpioPin = physicalToGpioPin;
    pins = new HashMap<>();
    this.physicalToGpioPin.forEach(
        new PinPopulator<>(outputChannel, pinFactory, pins));
  }

  /**
   * Constructor for tests, which allows tests to inject mock
   * pins.
   *
   * @param physicalToGpioPin maps physical pin IDs to their logical
   *                          counterparts
   * @param pins              test GPIO pins
   */
  @VisibleForTesting
  protected BaseGpioIO(
      PhysicalToGpioPin<T> physicalToGpioPin,
      Map<T, P> pins) {
    this.physicalToGpioPin = physicalToGpioPin;
    this.pins = pins;
  }

  /**
   * Checks if the subclass can openXXX the specified pin for
   * input or output
   *
   * @param gpioPin pin to check
   * @return {code true} if {@code gpioPin} identifies a valid
   *         pin that is not in use.
   */
  public boolean available(T gpioPin) {
    var pin = pins.get(gpioPin);
    return null  != pin && pin.available();
  }

  /**
   * Checks if the subclass can read or write (as appropriate)
   * from or to the specified ipn.
   *
   * @param gpioPin identifies the pin to check
   * @return {@code true} if {@code gpioPin} identifies a pin
   *         that is accepting output or providing input.
   */
  public boolean active(T gpioPin) {
    var pin = pins.get(gpioPin);
    return null != pin && pin.active();
  }

  /**
   * Sends a request to close the specified pin.
   *
   * @param gpioPin identifies the pin to be closed
   * @return {@code true} if the request was sent, {@code false}
   *         if transmission failed. Note that a successful
   *         invocation <em>DOES NOT</em> guarantee that the
   *         pin was closed. It status remains unknown until
   *         the server replies to the request.
   */
  boolean close(T gpioPin) {
    var pin = pins.get(gpioPin);
    return null != pin && pin.close();
  }

  /**
   * Dispatches (i.e. sends) the specified status message to all pins.
   *
   * @param ioStatus message to dispatch.
   */
  void dispatchToAllPins(IOStatusMessage<T> ioStatus) {
    pins.forEach((_, gpioPin) ->
      gpioPin.receivePinConfigurationStatus(ioStatus.status(), ioStatus.side_data()));
  }

  /**
   * Dispatches (i.e. sends) the specified status message to the specified pin
   *
   * @param ioStatus message to dispatch
   */
  void dispatchToTargetPin(IOStatusMessage<T> ioStatus) {
    var targetPin = pins.get(ioStatus.gpioPin());
    if (targetPin != null) {
      targetPin.receivePinConfigurationStatus(ioStatus.status(), ioStatus.side_data());
    }
  }

  /**
   * Checks if the specified pin has been taken offline
   *
   * @param gpioPin the pin to check.
   * @return {@code true} if {code gpioPin} is valid or identifies
   *         a pin that has been taken offline (i.e. out of service)
   *         due to a fatal error.
   */
  public boolean offline(T gpioPin) {
    var pin = pins.get(gpioPin);
    return null == pin || pin.offline();
  }

  /**
   * Resets the specified pin. If the pin is openXXX or offline, it will be closed
   *
   * @param gpioPin identifies the pin to be reset
   * @return {@code true} if {@code gpioPin} identifies a valid pin and
   *         the server accepted the reset request, {@code false} otherwise
   *         Note that a successful invocation <em>DOES NOT</em> imply
   *         that the pin has been reset. It will remain offline until
   *         the server confirms that the reset succeeded.
   */
  boolean reset(T gpioPin) {
    var pin = pins.get(gpioPin);
    return pin != null && pin.reset();
  }

  /**
   * Reset all pins
   *
   * @return {@code true} if all reset requests were sent to the server,
   *         {@code false} if any reset fails. Note that the method
   *         <em>always</em> attempts to reset all pins even when a
   *         transmission fails.
   *         Note that a successful invocation <em>DOES NOT</em> imply
   *         that the pins have been reset. Pins will remain offline until
   *         the server confirms that the reset succeeded.
   */
  boolean resetAll() {
    boolean result = true;
    for (T gpioPin : pins.keySet()) {
      var pin = pins.get(gpioPin);
      var resetStatus = pin != null && pin.reset();
      result = result && resetStatus;
    }
    return result;
  }

  @VisibleForTesting
  Map<T, P> pinMap() {
    return pins;
  }

  /**
   * Retrieves the pin having the provided physical ID number.
   *
   * @param physicalPinNo the ID number
   * @return the pin, if found, or {@code null}.
   */
  @Nullable
  protected P pin(byte physicalPinNo) {
    T pinId = physicalToGpioPin.toLogical(physicalPinNo);
    return pinId == null ? null : pins.get(pinId);
  }

  /**
   * Retrieves the pin having the specified logical ID.
   *
   * @param pinId identifies the desired pin. Note that
   *              {@code pinId} <em>MUST</em> be
   *              valid, i.e. in [0x00 .. 0x7E]. Do
   *              <em>NOT</em> pass a raw mutation.
   * @return the matching pin, if found, {@code null} otherwise
   */
  @Nullable
  protected P pin(T pinId) {
    return pins.get(pinId);
  }
}
