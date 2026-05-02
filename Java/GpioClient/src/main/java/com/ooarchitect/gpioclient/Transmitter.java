/*
 * Transmitter.java
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
 * Sends data to the server.
 */
public class  Transmitter<T extends Enum<T> & GpioPinNumber> {
  private static final byte LEAD_IN = (byte) 0xFF;
  private static final byte LEAD_OUT = (byte) 0x7F;
  private final SerialPort serialPort;

  public Transmitter(SerialPort serialPort) {
    this.serialPort = serialPort;
  }

  boolean sendMutation(byte mutation) {
    var buffer = new byte[] { mutation };
    int bytesWritten;
    synchronized (serialPort) {
      bytesWritten = serialPort.writeBytes(buffer, buffer.length, 0);
    }
    return 0 <= bytesWritten;
  }

  boolean sendMutation(T pin, int value) {
    var concretePin = (GpioPinNumber) pin;
    var pinNumber = concretePin.number();
    var mutation = pinNumber
        | ((value == 0) ? 0x00 :  0x80);
    return sendMutation((byte) mutation);
  }

  boolean sendCommand(
      ConfigurationCommandCode commandCode,
      StatusScope scope,
      T pin,
      PullMode resistorConfiguration) {
    var concretePin = (GpioPinNumber) pin;
    var buffer = new byte[6];
    buffer[0] = LEAD_IN;
    buffer[1] = (byte) commandCode.ordinal();
    buffer[2] = (byte) scope.ordinal();
    buffer[3] = concretePin.number();
    buffer[4] = (byte) resistorConfiguration.ordinal();
    buffer[5] = LEAD_OUT;
    int bytesWritten;
    synchronized (serialPort) {
      bytesWritten = serialPort.writeBytes(buffer, buffer.length, 0);
    }
    return 0 <= bytesWritten;
  }
}
