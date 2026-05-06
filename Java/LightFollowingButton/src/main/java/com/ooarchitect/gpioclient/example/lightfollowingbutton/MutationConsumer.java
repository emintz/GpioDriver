

/*
 * MutationConsumer.java
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

import com.ooarchitect.gpioclient.Level;
import com.ooarchitect.gpioclient.OutputPin;

import java.util.function.Consumer;

/**
 * Receives button events and turns an LED on or off accordingly.
 * The LED is controlled by an InputPin that is bound at
 * construction.
 */
public class MutationConsumer
    implements Consumer<Level> {

  private final OutputPin ledPin;

  MutationConsumer(OutputPin ledPin) {
    this.ledPin = ledPin;
  }

  @Override
  public void accept(Level level) {
    ledPin.send(level);
  }
}
