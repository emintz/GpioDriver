/*
 * PinTransitionTable.java
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

import com.ooarchitect.statemachine.TransitionTable;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;

/**
 * Provides a state transition table for input and output pins/
 */
public class PinTransitionTable {
  private static final EnumMap<IOStatusCode, IOEvent> STATUS_TO_EVENT =
      new EnumMap<>(IOStatusCode.class);

  static {
    STATUS_TO_EVENT.put(IOStatusCode.CLOSE_FAILED, IOEvent.CLOSE_FAILED);
    STATUS_TO_EVENT.put(IOStatusCode.CLOSE_SUCCEEDED, IOEvent.CLOSE_SUCCEEDED);
    STATUS_TO_EVENT.put(IOStatusCode.OPEN_FAILED, IOEvent.OPEN_FAILED);
    STATUS_TO_EVENT.put(IOStatusCode.OPEN_SUCCEEDED, IOEvent.OPEN_SUCCEEDED);
    STATUS_TO_EVENT.put(IOStatusCode.NO_SUCH_PIN, IOEvent.NO_SUCH_PIN);
    STATUS_TO_EVENT.put(IOStatusCode.PIN_IN_USE, IOEvent.PIN_IN_USE);
    STATUS_TO_EVENT.put(IOStatusCode.UNSUPPORTED, IOEvent.UNSUPPORTED);
    STATUS_TO_EVENT.put(IOStatusCode.RESET_FAILED, IOEvent.RESET_FAILED);
    STATUS_TO_EVENT.put(IOStatusCode.RESET_SUCCEEDED, IOEvent.RESET_SUCCEEDED);
    STATUS_TO_EVENT.put(IOStatusCode.INVALID_STATE, IOEvent.SERVER_FAILED);
  };

  static final TransitionTable<PinState, IOEvent> PIN_TRANSITION_TABLE =
      TransitionTable.builder(PinState.OFFLINE, IOEvent.class)
          .add(PinState.INACTIVE, IOEvent.OPEN_REQUESTED, PinState.OPEN_PENDING)
          .add(PinState.OPEN_PENDING, IOEvent.OPEN_SUCCEEDED, PinState.ACTIVE)
          .add(PinState.ACTIVE, IOEvent.CLOSE_REQUESTED, PinState.CLOSE_PENDING)
          .add(PinState.CLOSE_PENDING, IOEvent.CLOSE_SUCCEEDED, PinState.INACTIVE)
          .add(PinState.INACTIVE, IOEvent.CLOSE_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.OPEN_PENDING, IOEvent.CLOSE_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.CLOSE_PENDING, IOEvent.CLOSE_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.OFFLINE, IOEvent.CLOSE_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.RESET_PENDING, IOEvent.RESET_SUCCEEDED, PinState.INACTIVE)
          .add(PinState.INACTIVE, IOEvent.RESET_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.OPEN_PENDING, IOEvent.RESET_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.ACTIVE, IOEvent.RESET_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.CLOSE_PENDING, IOEvent.RESET_REQUESTED, PinState.RESET_PENDING)
          .add(PinState.OFFLINE, IOEvent.RESET_REQUESTED, PinState.RESET_PENDING)
          .build();

  @Nullable
  static IOEvent toIOEvent(IOStatusCode ioStatusCode) {
    return STATUS_TO_EVENT.get(ioStatusCode);
  }

  static boolean causesTransition(IOStatusCode ioStatusCode) {
    return STATUS_TO_EVENT.containsKey(ioStatusCode);
  }
}
