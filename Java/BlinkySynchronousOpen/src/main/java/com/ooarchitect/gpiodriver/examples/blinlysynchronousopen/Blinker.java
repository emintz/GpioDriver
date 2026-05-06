/*
 * Blinker.java
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
package com.ooarchitect.gpiodriver.examples.blinlysynchronousopen;

import com.ooarchitect.gpioclient.Level;
import com.ooarchitect.gpioclient.OutputPin;

/**
 * Pulses an {@link OutputPin} that (presumably) has an attached LED,
 * causing the latter to blink.
 */
public class Blinker implements Runnable {

  private final OutputPin ledPin;

  /**
   * Constructor
   *
   * @param ledPin the pin (presumably) connected to the LED to
   *               be blinked.
   */
  public Blinker(OutputPin ledPin) {
    this.ledPin = ledPin;
  }

  /**
   * Waits for the bound {@link OutputPin} to become ready then
   * blinks it once/second. The initial wait illustrates the
   * need for a higher-level open method.
   */
  @Override
  public void run() {
    try (ledPin) {
      System.out.println(
          "Blinker started, waiting for the LED pin to become active or go offline.");
      while(!ledPin.active() && !ledPin.offline()) {
        Thread.sleep(10);
      }
      System.out.println("LED pin is active.");
      while(ledPin.active()) {
        ledPin.send(Level.HIGH);
        Thread.sleep(500);
        ledPin.send(Level.LOW);
        Thread.sleep(500);
      }
    } catch(InterruptedException e) {
      System.out.println("Blinker thread interrupted");
    }
  }
}
