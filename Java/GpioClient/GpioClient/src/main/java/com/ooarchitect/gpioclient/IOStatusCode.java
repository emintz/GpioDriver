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
 * The status of a pin mutation. The enumeration names and values
 * <em>MUST</em> match the {@code IOStatus} {@code class enum}
 * in {@code SatusMessage.h} <em>PRECISELY</em>. The code takes
 * no quarter.
 */
public enum PinConfigStatusCode {
  /** OK Everything is fine. */
  OK,
  /** OPEN_SUCCEEDED Pin has been opened and is active. */
  OPEN_SUCCEEDED,
  /** OPEN_FOR_OUTPUT Pin has been opened for output. */
  @Deprecated
  OPEN_FOR_OUTPUT,
  /** OPEN_FAILED pin could not be opened. */
  OPEN_FAILED,
  /** INPUT_OPEN_FAILED pin could not be opened for input. */
  @Deprecated
  INPUT_OPEN_FAILED,
  /** NO_SUCH_PIN Non-existent pin */
  NO_SUCH_PIN,
  /** LOST_INPUT Input has been lost */
  LOST_INPUT,
  /** IN_USE Cannot open because pin is being used. */
  PIN_IN_USE,
  /** UNSUPPORTED The pin does not support the requested function. */
  UNSUPPORTED,
  /** UNCATEGORIZED Uncategorized error */
  UNCATEGORIZED,
  /** Pin was reset -- used for error recovery. */
  RESET,
  /** INVALID_STATE Internal driver failure */
  INVALID_STATE,
}
