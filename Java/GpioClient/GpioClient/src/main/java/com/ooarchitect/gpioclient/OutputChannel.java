/*
 * ByteConsumer.java
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
 * API that consumes primitive {@code byte}  and {@code byte[]} values.
 */
public interface ByteConsumer {
  /**
   * Consumes and processes the provided {@code byte} value.
   * Can generate arbitrary side effects and take arbitrarily
   * long to complete. Blocking semantics are not specified,
   * so processing <em>MIGHT</em> not be complete upon
   * return.
   *
   * @param value {@code byte} to consume and process.
   */
  void accept(byte value);

  /**
   * Consumes and processes the provided {@code byte} array.
   * Can generate arbitrary side effects and take arbitrarily
   * long to complete. Blocking semantics are not specified,
   * so processing <em>MIGHT</em> not be complete upon
   * return.
   *
   * @param values byte array to consume and process
   */
  void accept(byte[] values);
}
