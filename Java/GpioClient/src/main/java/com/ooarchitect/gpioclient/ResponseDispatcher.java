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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * <p>Receives data from the GPIO I/O Server, decodes it, and forwards
 * the result to the appropriate handler. Two data types arrive:
 * mutations, which carry input values, and configuration
 * responses, which provide the results of configuration commands.</p>
 *
 * <p>A mutation is a single byte whose most significant bit
 * indicates the applied voltage: 1 if high, 0 if low. The
 * remaining bits indicate the physical GPIO number
 * between 0 and 0x7E inclusive. Valid GPIO numbers
 * vary by ESP-32 model. Please refer to the chip
 * documentation for details.</p>
 *
 * <p>Status messages have the following format, where each value
 * occupies one byte.
 *
 * <ol>
 *   <li>{@code 0xFF}, the lead-in byte.</li>
 *   <li>Physical GPIO number, which must correspond to an element
 *       of type {@code T}</li>
 *   <li>Pin status, the ordinal of an {@link IOStatusCode} element</li>
 *   <li>Status-specific side data. Must be 0 if not used. Note
 *       that side data can contain any value, including the
 *       normally reserved {@code 0xFF} and {0X7F}.</li>
 * </ol>
 * </p>
 */
class ResponseDispatcher<T extends Enum<? extends GpioPinNumber>>
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
     * Have the operation status and GPIO number, the first
     * two elements.
     */
    HAVE_PIN_NUMBER,
    /**
     * Have the operation status, the first s.OUTPUTtatus element
     */
    HAVE_PIN_STATUS,
    /**
     * Have the input-output scope
     */
    HAVE_INPUT_OUTPUT_SCOPE,
    /**
     * Have all four elements, waiting for the end
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

  /**
   * A message contains:
   *
   * <ol>
   *   <li>Start message: {@code oxFF}</li>
   *   <li>Physical GPIO number</li>
   *   <li>Status code</li>
   *   <li>I/O scope TODO</li>
   *   <li>Side data</li>
   *   <li>End message: 0xFF</li>
   * </ol>
   */
  private static final TransitionTable<State, InputType> TRANSITION_TABLE =
      TransitionTable.builder(State.RECOVERING, InputType.class)
          .add(State.CREATED, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)
          .add(State.CREATED, InputType.VALUE, State.MUTATING)

          .add(State.MUTATING, InputType.VALUE, State.MUTATING)
          .add(State.MUTATING, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)

          .add(State.RECEIVING_STATUS_MESSAGE, InputType.VALUE, State.HAVE_PIN_NUMBER)

          .add(State.HAVE_PIN_NUMBER, InputType.VALUE, State.HAVE_PIN_STATUS)

          .add(State.HAVE_PIN_STATUS, InputType.VALUE, State.HAVE_INPUT_OUTPUT_SCOPE)

          .add(State.HAVE_INPUT_OUTPUT_SCOPE, InputType.VALUE, State.HAVE_SIDE_DATA)
          .add(State.HAVE_INPUT_OUTPUT_SCOPE, InputType.START_STATUS, State.HAVE_SIDE_DATA)
          .add(State.HAVE_INPUT_OUTPUT_SCOPE, InputType.END_STATUS, State.HAVE_SIDE_DATA)

          .add(State.HAVE_SIDE_DATA, InputType.END_STATUS, State.STATUS_RECEIVED)

          .add(State.STATUS_RECEIVED, InputType.VALUE, State.MUTATING)
          .add(State.STATUS_RECEIVED, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)

          .add(State.RECOVERING, InputType.END_STATUS, State.IDLE)

          .add(State.IDLE, InputType.START_STATUS, State.RECEIVING_STATUS_MESSAGE)
          .add(State.IDLE, InputType.VALUE, State.MUTATING)

          .build();

  private final ByteSupplier inputSource;
  private final PinValueConsumer valueConsumer;
  Consumer<IOStatusMessage<T>> statusConsumer;

  private State currentState;
  private byte rawScope;
  private byte rawStatus;
  private byte physicalPinNumber;
  private byte sideData;
  private final PhysicalToGpioPin<T> pinConversion;
  private final AtomicBoolean keepRunning;

  /**
   * Determine an input byte's type, which will be
   *
   * <ul>
   *   <li>Start of message: {@code 0xFF}</li>
   *   <li>End of message: {@code -x7F}</li>
   *   <li>Mutation: all other values, where the sign bit represents
   *       the voltage level ({@code LOW} or {@code HIGH}
   *       and the remaining bits give the physical lin
   *       number in [0x00 ... ox7E]</li>
   * </ul>
   *
   * @param input the incoming byte
   * @return the corresponding byte type
   */
  private static InputType typeOf(byte input) {
    return switch (input) {
      case (byte) 0xFF -> InputType.START_STATUS;
      case (byte) 0x7F -> InputType.END_STATUS;
      default -> InputType.VALUE;
    };
  }

  ResponseDispatcher(
      ByteSupplier inputSource,
      PinValueConsumer valueConsumer,
      Consumer<IOStatusMessage<T>> statusConsumer,
      PhysicalToGpioPin<T> physicalToGpioPin) {
    this.inputSource = inputSource;
    this.valueConsumer = valueConsumer;
    this.statusConsumer = statusConsumer;

    currentState = State.CREATED;
    rawStatus = 0;
    physicalPinNumber = 0;
    sideData = 0;
    this.pinConversion = physicalToGpioPin;
    keepRunning = new AtomicBoolean(true);
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
  void processOneByte() throws InterruptedException {
    byte input = inputSource.get();
    switch (currentState =
        TRANSITION_TABLE.onReceipt(currentState, typeOf(input))) {
      case CREATED:
        // Cannot happen.
        break;
      case MUTATING:
        valueConsumer.accept(input);
        break;
      case RECEIVING_STATUS_MESSAGE:
        break;
      case HAVE_PIN_NUMBER:
        physicalPinNumber = input;
        break;
      case HAVE_PIN_STATUS:
        rawStatus = input;
        break;
        case HAVE_INPUT_OUTPUT_SCOPE:
          rawScope = input;
          break;
      case HAVE_SIDE_DATA:
        sideData = input;
        break;
      case STATUS_RECEIVED:
        var receivedStatus = new IOStatusMessage<>(
            IOStatusCode.values()[rawStatus],
            StatusScope.values()[rawScope],
            pinConversion.toLogical(physicalPinNumber),
            sideData);
        statusConsumer.accept(receivedStatus);
        break;
      case RECOVERING:
        break;
      case IDLE:
        break;
    }
  }

  @Override
  public void run() {
    try {
      while (keepRunning.get()) {
        processOneByte();
      }
    } catch (InterruptedException expected) {
      // Normal exit
    }
  }

  /**
   * Stop the run loop. This is useful for testing, but needs
   * beefing up for production.
   */
  public void stop() {
    ;Thread.currentThread().interrupt();
  }
}
