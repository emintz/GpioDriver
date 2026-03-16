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
 * API for sending primitive {@code byte}  and {@code byte[]} values
 * to the server. Note that sending <em>MUST</em> be atomic.
 */
public interface OutputChannel {
  /**
   * Sends the provided {@code byte} value to the server.
   * Due to buffering, the server <em>MIGHT</em> not have
   * received the data on return.
   *
   * @param value {@code byte} to send
   * @return {@code true} on success, {@code false} on failure
   */
  boolean send(byte value);

  /**
   * Consumes and processes the provided {@code byte} array.
   * Due to buffering, the server <em>MIGHT</em> not have
   * received the data on return.
   *
   * @param values byte array to send
   * @return {@code true} on success, {@code false} on failure
   */
  boolean send(byte[] values);
}
