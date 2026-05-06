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
package com.ooarchitect.gpiodriver.example.readpin;

import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * Main application class.
 */
public class Main {

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

  public static void main(String[] args) {
    if (checkArguments(args)) {
      new PinReader(args[0]).run();
    }
  }
}
