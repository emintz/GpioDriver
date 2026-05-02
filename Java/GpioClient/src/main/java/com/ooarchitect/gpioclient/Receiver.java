/*
 * Receiver.java
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

import com.fazecast.jSerialComm.SerialPort;

/**
 * {@link ByteSupplier) implementation that reads characters from the
 * server and returns them for processing.}
 */
class Receiver implements ByteSupplier {

  private final SerialPort serialPort;

  Receiver(SerialPort serialPort) {
    this.serialPort = serialPort;
  }

  /**
   * Reads one character from serial input and returns it to the caller.
   * The method blocks until a character arrives, so it must run on
   * dedicated input processing thread.
   *
   * @return the character, as described above
   */
  @Override
  public byte get() {
    byte[] bytes = new byte[1];
    int numRead = 0;
    while (numRead != 1) {
      try {
        numRead = serialPort.readBytes(bytes, 1, 0);
      } catch (Exception e) {
        numRead = 0;
        e.printStackTrace();
      }
    }
    return bytes[0];
  }
}
