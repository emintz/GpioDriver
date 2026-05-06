/*
 * PinReader.java
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
package com.ooarchitect.gpiodriver.example.readpin;

import com.ooarchitect.gpioclient.*;

public class PinReader implements Runnable {

  GpioInputOutput<ESP32s2Pin> gpio;
  InputPin pin;

  public PinReader(String portDescription) {
    gpio = GpioInputOutputBuilder.create(ESP32s2Pin.class, portDescription);
    pin = null;
  }

  private boolean openPin() {
    if (gpio != null) {
      pin = gpio.openForInput(
          ESP32s2Pin.GPIO_4,
          PullMode.UP,
          level -> System.out.println("Pin level: " + level),
          esp32s2PinIOStatusMessage ->
              System.out.println("Pin status: " + esp32s2PinIOStatusMessage));
    }
    return pin != null;
  }

  @Override
  public void run() {
    if (gpio.start() && openPin()) {
      System.out.println("Reading pin...");
      try {
        gpio.joinReadThread();
      } catch (InterruptedException e) {
        System.err.println("Pin read interrupted.");
        e.printStackTrace();
      }
    }
  }
}
