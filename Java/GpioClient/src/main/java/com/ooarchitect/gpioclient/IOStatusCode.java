/*
 * RemotePinStatus.java
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
 * The status of a a request. The enumeration names and values
 * <em>MUST</em> match the {@code IOStatus} {@code class enum}
 * in {@code SatusMessage.h} <em>PRECISELY</em>. The code takes
 * no quarter.
 */
public enum IOStatusCode {
  /** OK Everything is fine. */
  OK,
  /** Pin has been closed. */
  CLOSE_SUCCEEDED,
  /** Pin could not be closed. */
  CLOSE_FAILED,
  /** OPEN_SUCCEEDED Pin has been opened and is active. */
  OPEN_SUCCEEDED,
  /** OPEN_FAILED pin could not be opened. */
  OPEN_FAILED,
  /** NO_SUCH_PIN Non-existent pin */
  NO_SUCH_PIN,
  /** LOST_INPUT Input has been lost */
  LOST_INPUT,
  /** IN_USE Cannot open because pin is being used. */
  PIN_IN_USE,
  /** Cannot close or perform I/O because pin is not active. */
  PIN_NOT_ACTIVE,
  /** Pin is offline (wedged) */
  PIN_OFFLINE,
  /** UNSUPPORTED The pin does not support the requested function. */
  UNSUPPORTED,
  /** Server could not reset the pin. */
  RESET_FAILED,
  /** Pin was reset -- used for error recovery. */
  RESET_SUCCEEDED,
  /** INVALID_STATE Internal driver failure */
  INVALID_STATE,
}
