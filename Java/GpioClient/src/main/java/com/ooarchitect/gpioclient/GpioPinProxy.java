/*
 * TestByteSupplier.java
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

import java.util.EnumMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Base low level GPIO pin class
 *
 * @param <T> the logical GPIO ID enumeration class
 */
public abstract class GpioPinProxy<T extends Enum<T> & GpioPinNumber> {

  private static final byte LOW = 0;

  private final T pinNumber;
  private final Transmitter<T> outputChannel;
  private final StatusScope scope;
  private final ReentrantLock lock;
  private Consumer<IOStatusCode> statusCallback;
  private PinState state;

  /**
   * Constructor: instantiates a GpioPin instance from the provided
   * parameters
   *
   * @param pinNumber the physical GPIO number on the server
   * @param outputChannel connection to the server
   * @param scope I/O type, either {@link StatusScope#INPUT}
   *              or {@link StatusScope#OUTPUT}
   */
  protected GpioPinProxy(
      T pinNumber,
      Transmitter<T> outputChannel,
      StatusScope scope) {
    this.pinNumber = pinNumber;
    this.outputChannel = outputChannel;
    this.scope = scope;
    this.lock = new ReentrantLock();
    state = PinState.INACTIVE;
    clearStatusCallback();
  }

  /**
   * Queries the pin's ability to perform I/O
   *
   * @return {@code true} if the pin can perform I/O,
   *         {@code false} otherwise. Note that available
   *         pins are not active.
   *
   * @see #available()
   * @see #offline()
   */
  public boolean active() {
    try {
      lock.lock();
      return state == PinState.ACTIVE;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Queries the pin's availability, that is, if the pin can be
   * opened for input or output.
   *
   * @return {@code true} if the pin is available to be opened,
   *         {@code false} otherwise. Note that active pins are
   *         not available.
   *
   * @see #active()
   * @see #offline()
   */
  public boolean available() {
    try {
      lock.lock();
      return PinState.INACTIVE == state;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Unbinds the current status callback and substitutes a no-op
   * instance.
   */
  private void clearStatusCallback() {
    statusCallback = (_)->{};
  }

  boolean close() {
    try {
      lock.lock();
      boolean status = active();
      if (status) {
        status = transition(IOEvent.CLOSE_REQUESTED, LOW);
        if (!status) {
          transition(IOEvent.CLOSE_FAILED, LOW);
          statusCallback.accept(IOStatusCode.CLOSE_FAILED);
        }
      }
      return status;
    } finally {
      lock.unlock();
    }
  }

  /**
   * State entry action for {@link PinState#CLOSE_PENDING}, which sends a
   * close request to the serve
   *
   * @return {@code true} if the command was sent, {@code false}
   *         if the command failed. Note that success <em>DOES NOT</em>
   *         imply that the pin has been closed. The pin state is
   *         undetermined until the server sends its completion status.
   */
  private boolean closeRequested() {
    return sendConfigurationCommand(ConfigurationCommandCode.CLOSE, PullMode.FLOAT);
  }

  /**
   * Takes this GPIO pin offline when a fault is detected.
   *
   * @return {@code false} always because it is only invoked on failure.
   */
  protected abstract boolean goOffline();

  /**
   * Claims the next level of reentrant locking.
   */
  protected void lock() {
    lock.lock();
  }

  /**
   * Queries if this pin is offline
   *
   * @return {@code true} if this pin os offline, {@code false}
   *         otherwise. Note that an on-line pin might be in any
   *         valid state.
   */
  boolean offline() {
    try {
      lock.lock();
      return PinState.OFFLINE == state;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Action to perform when the server acknowledges that it has successfully
   * opened a pin for I/O
   *
   * @param sideData status-specific data
   * @return {@code true} on success, {@code false} if an error occurs.
   */
  protected abstract boolean onOpenSucceeded(byte sideData);

  /**
   * Clean up before sending a reset request.
   */
  protected void onReset() {
  }

  protected T pinNumber() {
    return pinNumber;
  }

  protected byte rawPinNumber() {
    return pinNumber.number();
  }

  /**
   * Process a deconstructed status message
   *
   * @param code status code, result of configuration operation
   * @param sideData status code-specific data
   */
  boolean receivePinConfigurationStatus(
      IOStatusCode code,
      byte sideData) {
    Consumer<IOStatusCode> activeStatusCallback = statusCallback;
    boolean result;
    try {
      lock.lock();
      result = PinTransitionTable.causesTransition(code);
      if (result) {
        result = transition(PinTransitionTable.toIOEvent(code), sideData);
        if (!result) {
          code = CORRESPONDING_FAILURE.get(code);
        }
      }
    } finally {
      lock.unlock();
    }
    activeStatusCallback.accept(code);
    return result;
  }

  /**
   * Sends a reset request to the server. Does nothing if a reset
   * request is pending. Note that this method forces the pin
   * offline until the server acknowledges the reset.
   *
   * @return {@code true} on success; {@code false} it the request
   *         cannot be set. Returns {@code true} if a reset request
   *         is outstanding.
   */
  boolean reset() {
    try {
      lock.lock();
      boolean result = true;
      if (state != PinState.RESET_PENDING) {
        onReset();
        result = transition(IOEvent.RESET_REQUESTED, LOW);
        if (!result) {
          result = transition(IOEvent.RESET_FAILED, LOW);
        }
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sends a reset request to the server.
   *
   * @return {@code true} if the request was sent successfully,
   *         {@code false} should the send fail. Note that
   *         success <em>DOES NOT</em> imply that the pin has been
   *         reset. Reset remains pending until we receive a
   *         {@link IOStatusCode#RESET_SUCCEEDED} message.
   */
  private boolean resetRequested() {
    boolean result;
    try {
      lock.lock();
      result = sendConfigurationCommand(ConfigurationCommandCode.RESET, PullMode.FLOAT);
    } finally {
      lock.unlock();
    }
    return result;
  }

  /**
   * Sends a configuration command to the server.
   *
   * @param commandCode the action for the server to take.
   * @param resistorConfiguration pullup/pulldown resistor configuration
   *                              if applicable. Pass {@link PullMode#FLOAT}
   *                              when irrelevant.
   * @return {@code true} if the command was sent successfully,
   *         {@code false} if the send attempt failed. Note that success
   *         <em>DOES NOT</em> imply that the command ran successfully or
   *         that it ran at all. The status remains unknown until the server
   *         replies.
   */
  protected boolean sendConfigurationCommand(
      ConfigurationCommandCode commandCode,
      PullMode resistorConfiguration) {
    return outputChannel.sendCommand(
        commandCode,
        scope,
        pinNumber,
        resistorConfiguration
    );

  }

  protected boolean sendMutation(byte mutation) {
    return
        active()
        && outputChannel.sendMutation(mutation);
  }

  /**
   * Send an "openXXX pin" request to the server.
   *
   * @return {@code true} if the command was sent successfully,
   *         {@code false} on failure. Note that a successful
   *         invocation <em>DOES NOT</em> imply that the pin
   *         has been opened. Its status is unknown until the
   *         server informs us.
   */
  protected abstract boolean sendOpenRequest();

  /**
   * Sets the status callback. For testing and internal use only, users
   * <em>MUST NOT</em> invoke.
   *
   * @param statusCallback receives status notifications
   */
  @VisibleForTesting
  void setStatusCallback(Consumer<IOStatusCode> statusCallback) {
    this.statusCallback = statusCallback;
  }

  /**
   * Modified
   * <a href='https://en.wikipedia.org/wiki/Moore_machine'>
   * Moore state machine transition table for GPIO pins</a>.
   * The implementation takes action upon state <em>entry</em>,
   * which differs from the Moore's original design.
   *
   * @param event incoming I/O event
   * @param sideData event-specific data
   * @return the result of the entered state's action:
   *         {@code true} if the action succeeded,
   *         {@code false} if it failed.
   */
  protected boolean transition(IOEvent event, byte sideData) {
    boolean result = true;
    try {
      lock.lock();
      switch(state = PinTransitionTable.PIN_TRANSITION_TABLE.onReceipt(state, event)) {
        case INACTIVE -> statusCallback = (_) -> {};
        case OPEN_PENDING -> result = sendOpenRequest();
        case ACTIVE -> result = onOpenSucceeded(sideData);
        case CLOSE_PENDING -> result = closeRequested();
        case OFFLINE -> result = goOffline();
        case RESET_PENDING -> result = resetRequested();
      }
      return result;
    } finally {
      lock.unlock();
    }
    // Return not needed. See end of try block.
  }

  /**
   * Releases one level of reentrant locking.
   */
  protected void unlock() {
    lock.unlock();
  }

  // Test support. Note that these methods are not
  // thread-safe.
  @VisibleForTesting
  PinState getState() {
    return state;
  }

  @VisibleForTesting
  void setState(PinState state) {
    this.state = state;
  }

  private static final EnumMap<IOStatusCode, IOStatusCode> CORRESPONDING_FAILURE;

  static {
    CORRESPONDING_FAILURE = new EnumMap<>(IOStatusCode.class);
    CORRESPONDING_FAILURE.put(IOStatusCode.OK, IOStatusCode.INVALID_STATE);
    CORRESPONDING_FAILURE.put(IOStatusCode.CLOSE_SUCCEEDED, IOStatusCode.CLOSE_FAILED);
    CORRESPONDING_FAILURE.put(IOStatusCode.CLOSE_FAILED, IOStatusCode.CLOSE_FAILED);
    CORRESPONDING_FAILURE.put(IOStatusCode.OPEN_SUCCEEDED, IOStatusCode.OPEN_FAILED);
    CORRESPONDING_FAILURE.put(IOStatusCode.OPEN_FAILED, IOStatusCode.OPEN_FAILED);
    CORRESPONDING_FAILURE.put(IOStatusCode.NO_SUCH_PIN, IOStatusCode.NO_SUCH_PIN);
    CORRESPONDING_FAILURE.put(IOStatusCode.LOST_INPUT, IOStatusCode.LOST_INPUT);
    CORRESPONDING_FAILURE.put(IOStatusCode.PIN_IN_USE, IOStatusCode.PIN_IN_USE);
    CORRESPONDING_FAILURE.put(IOStatusCode.UNSUPPORTED, IOStatusCode.UNSUPPORTED);
    CORRESPONDING_FAILURE.put(IOStatusCode.RESET_SUCCEEDED, IOStatusCode.RESET_FAILED);
    CORRESPONDING_FAILURE.put(IOStatusCode.RESET_FAILED, IOStatusCode.RESET_FAILED);
    CORRESPONDING_FAILURE.put(IOStatusCode.INVALID_STATE, IOStatusCode.INVALID_STATE);
  }
}
