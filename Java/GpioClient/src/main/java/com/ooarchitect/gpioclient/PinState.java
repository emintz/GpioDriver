/*
 * PinState.java
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

/**
 * States that an input pin can occupy.
 */
enum PinState {
  /**
   * Pin not in use.
   */
  INACTIVE,
  /**
   * The application wants this pin to openXXX.
   */
  OPEN_PENDING,
  /**
   * Pin is openXXX and providing input.
   */
  ACTIVE,
  /**
   * Close request sent to the server; awaiting response.
   */
  CLOSE_PENDING,
  /**
   * Pin is broken, unavailable, out to lunch, or on strike.
   */
  OFFLINE,
  /**
   * Pin reset requested; awaiting response.
   */
  RESET_PENDING,
}
