/*
 * IOStatusConsumer.java
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
package com.ooarchitect.gpiodriver.example.blinky;

import com.ooarchitect.gpioclient.ESP32s2Pin;
import com.ooarchitect.gpioclient.IOStatusMessage;

import java.util.function.Consumer;

/**
 * Server status callback that merely dumps the received
 * message.
 */
public class IOStatusConsumer
    implements Consumer<IOStatusMessage<ESP32s2Pin>> {
  @Override
  public void accept(IOStatusMessage ioStatusMessage) {
    System.out.println("Server response: " + ioStatusMessage);
  }
}
