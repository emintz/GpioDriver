

/*
 * ResponseHandler.java
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

import com.google.common.annotations.VisibleForTesting;
import com.ooarchitect.statemachine.TransitionTable;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Handles responses from the GPIO I/O Server.
 */
class ResponseHandler <T extends Enum<? extends GpioPinNumber<T>>>
    implements Runnable {

  /**
   * The arriving byte type.
   */
  @VisibleForTesting
  private enum InputType {
    /** Arbitrary value */
    VALUE,
    /** 0xFF, indicates status message start */
    START_STATUS,
    /** 0x7F indicates status message end */
    END_STATUS,
  }

  @VisibleForTesting
  enum State {
    /** Newly created */
    CREATED,
    /** Processing input pin value changes: LOW -> HIGH and HIGH -> LOW. */
    MUTATING,
    /**
     * At the start of a status message
     */
    RECEIVING_STATUS_MESSAGE,
    /**
     * Have the operation status, the first status element
     */
    HAVE_PIN_STATUS,
    /**
     * Have the operation status and GPIO number, the first
     * two elements.
     */
    HAVE_PIN_NUMBER,
    /**
     * Have all three elements, waiting for the end
     * response byte
     */
    HAVE_SIDE_DATA,
    /**
     * Received a complete, well-formed status message
     */
    STATUS_RECEIVED,
    /**
     * Recovering from unexpected input.
     */
    RECOVERING,
    /**
     *  Waiting for input after recovering from an error. *.
     */
    IDLE,
  }

  private static final TransitionTable<State, InputType> TRANSITION_TABLE =
      TransitionTable.builder(State.RECOVERING, InputType.class)
          .add(State.CREATED, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)
          .add(State.CREATED, InputType.VALUE, State.MUTATING)

          .add(State.MUTATING, InputType.VALUE, State.MUTATING)
          .add(State.MUTATING, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)

          .add(State.RECEIVING_STATUS_MESSAGE, InputType.VALUE, State.HAVE_PIN_STATUS)

          .add(State.HAVE_PIN_STATUS, InputType.VALUE, State.HAVE_PIN_NUMBER)

          .add(State.HAVE_PIN_NUMBER, InputType.VALUE, State.HAVE_SIDE_DATA)
          .add(State.HAVE_PIN_NUMBER, InputType.START_STATUS, State.HAVE_SIDE_DATA)
          .add(State.HAVE_PIN_NUMBER, InputType.END_STATUS, State.HAVE_SIDE_DATA)

          .add(State.HAVE_SIDE_DATA, InputType.END_STATUS, State.STATUS_RECEIVED)

          .add(State.STATUS_RECEIVED, InputType.VALUE, State.MUTATING)
          .add(State.STATUS_RECEIVED, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)

          .add(State.RECOVERING, InputType.END_STATUS, State.IDLE)

          .add(State.IDLE, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)
          .add(State.IDLE, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)

          .build();

  private final ByteSupplier inputSource;
  private final PinValueConsumer valueConsumer;
  Consumer<PinConfigStatus<T>> statusConsumer;

  private State currentState;
  private byte rawStatus;
  private byte physicalPinNumber;
  private byte sideData;
  private final HashMap<Integer, T> physicalToLogical;

  private static InputType typeOf(byte input) {
    return switch (input) {
      case (byte) 0xFF -> InputType.START_STATUS;
      case (byte) 0x7F -> InputType.END_STATUS;
      default -> InputType.VALUE;
    };
  }

  @SuppressWarnings("unchecked")
  ResponseHandler(
      Class<T> gpioPinType,
      ByteSupplier inputSource,
      PinValueConsumer valueConsumer,
      Consumer<PinConfigStatus<T>> statusConsumer) {
    this.inputSource = inputSource;
    this.valueConsumer = valueConsumer;
    this.statusConsumer = statusConsumer;

    currentState = State.CREATED;
    rawStatus = 0;
    physicalPinNumber = 0;
    sideData = 0;
    physicalToLogical = new HashMap<>();
    for (T logicalGPIO : gpioPinType.getEnumConstants()) {
      int number = ((GpioPinNumber<T>)logicalGPIO).number();
      physicalToLogical.put(number, logicalGPIO);
    }
  }

  @VisibleForTesting
  State currentState() {
    return currentState;
  }

  /**
   * Wait for a byte to arrive then process it. We isolate processing
   * from the run loop so that tests don't need to synchronize with
   * a free-running server -- no threads, no fuss, no muss,
   * no bother.
   */
  @VisibleForTesting
  void processOneByte() {
    byte input = inputSource.get();
    switch (currentState = TRANSITION_TABLE.onReceipt(currentState, typeOf(input))) {
      case CREATED:
        // Cannot happen.
        break;
      case MUTATING:
        valueConsumer.accept(input);
        break;
      case RECEIVING_STATUS_MESSAGE:
        break;
      case HAVE_PIN_STATUS:
        rawStatus = input;
        break;
      case HAVE_PIN_NUMBER:
        physicalPinNumber = input;
        break;
      case HAVE_SIDE_DATA:
        sideData = input;
        break;
      case STATUS_RECEIVED:
        var receivedStatus = new PinConfigStatus<>(
            PinMutationStatus.values()[rawStatus],
            physicalToLogical.get((int) physicalPinNumber),
            sideData);
        statusConsumer.accept(receivedStatus);
        break;
      case RECOVERING:
        break;
      case IDLE:
        break;
    }
  }

  public void run() {
    for (;;) {
      processOneByte();
    }
  }
}
