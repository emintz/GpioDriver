

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
package com.ooarchitect.gpioclient.example.lightfollowingbutton;

import java.nio.file.FileSystems;
import java.nio.file.Files;

public class Main {
  private static boolean checkArguments(String[] args) {
    boolean result = args.length == 1;
    if (!result) {
      System.err.println("Usage:");
      System.err.println();
      System.err.println("   GpioTestApplication <serial port device>");
      System.err.println();
      System.err.println("Example:");
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

  public static void main(String[] args) throws Exception {
    if (checkArguments(args)) {
      System.out.println("Starting GpioTestApplication to read from: " + args[0]);
      var follower = new LightFollower(args[0]);
      follower.start();
      System.out.println("Stopping. Thank you for using the button follower.");
    }
  }
}
