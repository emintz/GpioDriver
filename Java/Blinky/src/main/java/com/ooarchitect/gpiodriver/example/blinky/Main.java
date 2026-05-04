/*
 * Main.java
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
import com.ooarchitect.gpioclient.GpioInputOutput;
import com.ooarchitect.gpioclient.GpioInputOutputBuilder;

import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * <p>Application that uses the GPIO library to blink the built-in LED
 * on a standard ESP32s2 development board. By convention, the
 * built-in LED is connected to pin 2.</p>
 *
 * <p>The application uses the lowest level GPIO API. In addition to
 * providing an example of use, it illustrates the API's shortcoming
 * with respect to opening GPIO pins. The low-level pin open
 * methods return a pin in the OPEN_PENDING state, meaning that
 * the API has asked the server to open the pin, but the server
 * has yet to confirm success. A convenience layer is urgently
 * needed.
 */
public class Main {
  private final GpioInputOutput<ESP32s2Pin> gpio;


  /**
   * Validates the command line arguments. The argument array is valid if
   * and only if it contains a single argument that specifies a valid serial
   * device. The method verifies that the device exists.
   *
   * @param args the complete list of command line arguments.
   * @return {@code true} if the argument array is valid, {@code false}
   *         otherwise.
   */
  private static boolean checkArguments(String[] args) {
    boolean result = args.length == 1;
    if (!result) {
      System.err.println("Usage:");
      System.err.println();
      System.err.println("   GpioTestApplication <serial port device>");
      System.err.println();
      System.err.println("Example (Linux-centric):");
      System.err.println();
      System.err.println("   GpioTestApplication /dev/ttyUSB1");
    } else {
      var path = FileSystems.getDefault().getPath(args[0]);
      result = Files.exists(path);
      if (result) {
        System.out.print("Found serial device: ");
        System.out.println(args[0]);
      } else {
        System.err.println("File not found: " + path);
      }
    }
    return result;
  }

  /**
   * Application entry point
   *
   * @param args command line arguments.
   * @see #checkArguments(String[])
   */
  public static void main(String[] args) {
    if (checkArguments(args)) {
      var theApplication = new Main(args[0]);
      if (theApplication.init()) {
        theApplication.blink();
      }
    }
  }

  /**
   * Constructor
   *
   * @param portDescription serial device specification. The device
   *                        <em>MUST</em> be connected to an ESP32s2
   *                        that is running the GPIO control sketch.
   */
  private Main(String portDescription) {
    gpio =  GpioInputOutputBuilder.create(
        ESP32s2Pin.class, portDescription);
  }

  /**
   * Starts the input thread.
   *
   * @return {@code true} if the input thread started successfully, {@code false}
   *         otherwise. The GPIO API becomes usable if and only if the
   *         invocation succeeds.
   */
  private boolean init() {
    return gpio.start();
  }

  /**
   * Blinks the built-in LED.
   */
  void blink() {
    var writeToBuiltinLed = gpio.openForOutput(ESP32s2Pin.GPIO_2, new IOStatusConsumer());
    var blinker = new Blinker(writeToBuiltinLed);
    blinker.run();
  }
}
