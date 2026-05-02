/*
 * GPInputOutput.java
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

import java.util.function.Consumer;

/**
 * General Purpose Pin Input/Output API.
 *
 * @param <T> Pin enumeration type. Note that each supported
 *            enumeration supports a specific ESP-32 variant.
 */
public interface GpioInputOutput<T extends Enum<T> & GpioPinNumber>  {

  /**
   * Queries a pin's activity. A pin is active if and only if it is open for
   * input or output (but never both). A pin is either active or inactive.
   * Inactive pins can be either available or off-line.
   *
   * @param pin the GPIO pin to query
   * @return {@code true} if the pin is active or {@code false} if it is
   *         inactive.
   */
  boolean active(T pin);

  /**
   * Queries a pin's availability. A pin is available if and only if it can be
   * opened for input or output (but not both). An unavailable pin is either
   * in use (i.e. open) or off-line (i.e. in an invalid state)
   *
   * @param pin the GPIO pin to query
   * @return {@code true} if the pin is available; {@code false} otherwise.
   */
  boolean available(T pin);

  /**
   * Queries a pin's state. A pin is offline if and only if it is in an
   * invalid state (a.k.a. "wedged"). Note that a pin is either offline
   * or online. An online pin can be either in use or available.
   *
   * @param pin the GPIO pin to query
   * @return {@code true} if the pin is off-line ("wedged") and {@code false} otherwise.
   */
  boolean offline(T pin);

  /**
   * Opens the specified pin for input
   *
   * @param pin                   the GPIO pin to open
   * @param resisterConfiguration Pullup/Pulldown resistor configuration
   * @param levelConsumer         Called when the pin level changes
   * @param statusCallback        Called when the pin reports status
   * @return an {@link InputPin} bound to the specified {@code pin} if successful,
   * {@code null} otherwise.
   */
  InputPin openForInput(
      T pin,
      PullMode resisterConfiguration,
      Consumer<Level> levelConsumer,
      Consumer<IOStatusCode> statusCallback);

  /**
   * Opens the specified pin for output
   *
   * @param pin            the GPIO pin to open
   * @param statusCallback invoked when the pin reports its status
   * @return an {@link OutputPin} bound to the specified {@code pin} if successful,
   * {@code null} otherwise.
   */
  OutputPin openForOutput(
      T pin,
      Consumer<IOStatusCode> statusCallback);

  /**
   * Builds out the class and starts processing input.
   *
   * @return {@code true} if processing started successfully,
   *         {@code false} otherwise.
   */
  boolean start();
}
