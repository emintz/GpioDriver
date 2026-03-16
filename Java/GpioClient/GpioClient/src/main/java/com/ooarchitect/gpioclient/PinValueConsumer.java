/*
 * PinMutationConsumer.java
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
 * Consumes input pin value changes (a.k.a. "mutations").
 */
public interface PinMutationConsumer {
  /**
   * Accept and process the specified mutation.
   *
   * @param mutation the pin value, an 8-bit byte whose
   * sign bit indicates the applied voltage (0 --> LOW, 1 --> HIGH)
   * and whose remaining bots provide the physical pin number.
   */
  void accept(byte mutation);
}
