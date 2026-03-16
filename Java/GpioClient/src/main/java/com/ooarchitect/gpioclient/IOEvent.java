/*
 * IOEvent.java
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
 * Characterizes Input-output-related events.
 */
public enum IOEvent {
  /** Client has asked the server to close the pin. */
  CLOSE_REQUESTED,
  /** Server has closed the pin. */
  CLOSE_SUCCEEDED,
  /** Pin could not be closed. */
  CLOSE_FAILED,
  /** Client has asked the server to openXXX the pin. */
  OPEN_REQUESTED,
  /**  Pin has been opened and is active. */
  OPEN_SUCCEEDED,
  /** OPEN_FAILED pin could not be opened. */
  OPEN_FAILED,
  /** The pin does not support the requested function. */
  UNSUPPORTED,
  /** Pin reset command failed to send or the server failed to send the command. */
  RESET_FAILED,
  /** The client has asked the server to reset the pin. */
  RESET_REQUESTED,
  /** The server has reset the pin. */
  RESET_SUCCEEDED,
  /** NO_SUCH_PIN the server has no such pin */
  NO_SUCH_PIN,
  /** IN_USE Cannot openXXX because pin is being used. */
  PIN_IN_USE,
  /** Internal server failure  */
  SERVER_FAILED,
}
