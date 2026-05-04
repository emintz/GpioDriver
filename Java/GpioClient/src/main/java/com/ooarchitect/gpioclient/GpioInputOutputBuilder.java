

/*
 * GpioInputOutputFactory.java
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

import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * A factory that creates {@link GpioInputOutput} instances
 */
public class GpioInputOutputBuilder {
  private static final int BAUD_RATE = 115200;

  private GpioInputOutputBuilder() {}

  public static <T extends Enum<T> & GpioPinNumber> GpioInputOutput<T> create(
      Class<T> pinType,
      String portDescription) {
    GpioInputOutput<T> ret = null;
    var path = FileSystems.getDefault().getPath(portDescription);
    if (Files.exists(path)) {
      var port = SerialPort.getCommPort(portDescription);
      port.setComPortParameters(
          BAUD_RATE,
          8,
          SerialPort.ONE_STOP_BIT,
          SerialPort.NO_PARITY);
      port.setComPortTimeouts(
          SerialPort.TIMEOUT_READ_BLOCKING,
          Integer.MAX_VALUE,
          Integer.MAX_VALUE);
      int timeout = port.getReadTimeout();
      System.out.println("Read timeout : " + timeout + " milliseconds.");
      if (port.openPort()) {
        ret = new GpioInputOutputImpl<>(
            pinType,
            new Receiver(port),
            new Transmitter<>(port));
      }
    }
    return ret;
  }
}
