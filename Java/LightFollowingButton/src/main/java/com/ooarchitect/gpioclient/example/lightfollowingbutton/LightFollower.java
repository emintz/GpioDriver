

/*
 * LightFollower.java
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

import com.ooarchitect.gpioclient.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Class that drives output pin levels from input pins.
 */
public class LightFollower {

  private final GpioInputOutput<ESP32s2Pin> gpio;

  public LightFollower(String portDescription) {
    gpio = GpioInputOutputBuilder.create(
        ESP32s2Pin.class, portDescription);
  }

  private InputPin openButtonAndLED(
      ESP32s2Pin buttonPin,
      ESP32s2Pin ledPin) {
    try {
      var outputPin = gpio.synchronousOpenForOutput(
          ledPin,
          (_) -> {});
      var inputPin = gpio.synchronousOpenForInput(
          buttonPin, PullMode.UP,
          new MutationConsumer(outputPin),
          (_) -> {
          });
      outputPin.send(inputPin.value());
      return inputPin;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void start() {
    if (gpio.start()) {
      ArrayList<InputPin> pins = new ArrayList<>();
      pins.add(openButtonAndLED(ESP32s2Pin.GPIO_27, ESP32s2Pin.GPIO_2));
      pins.add(openButtonAndLED(ESP32s2Pin.GPIO_19, ESP32s2Pin.GPIO_12));
      pins.add(openButtonAndLED(ESP32s2Pin.GPIO_4, ESP32s2Pin.GPIO_14));
      pins.add(openButtonAndLED(ESP32s2Pin.GPIO_15, ESP32s2Pin.GPIO_13));
      System.out.println("Press any key to exit.");
      var keyboard = new InputStreamReader(System.in);
      try {
        var keyPressed = keyboard.read();
        System.out.println("Key pressed: " + keyPressed);
      } catch (IOException e) {
        System.out.println("Error reading keyboard, stopping");
      }
    }
  }
}
