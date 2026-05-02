/*
 * BaseIOPin.java
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

/**
 * Base user-level GPIO Input/Output API. The implementation forwards invocations
 * to a GPIO Pin proxy that interacts with the server. Both input and output
 * pins implement the API.
 */
public interface BaseIOPin extends AutoCloseable {

  /**
   * @return {@code true} if and only the pin is open for business, that is can
   * produce or consume (as appropriate) data and respond to configuration
   * requests.
   */
  boolean active();

  /**
   * Close the pin if it is open. Do nothing if the pin is closed or off-line.
   * Overrides {@link AutoCloseable#close()}
   */
  @Override
  void close();

  /**
   * @return {@code true} if and only if the pin is off-line, i.e. frozen
   *         in an invalid state a.k.a. "wedged".
   */
  boolean offline();
}
