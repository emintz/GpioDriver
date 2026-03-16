

/*
 * InputPinV2.java
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

import java.util.function.Consumer;

/**
 * Refactored input pin based on GpioPin.
 */
final class InputPin extends GpioPin {

  private static final byte LOW = 0;

  private byte value;
  private PinLevelConsumer levelConsumer;
  private OutputPinConfiguration pendingConfiguration;

  private record OutputPinConfiguration(
      PullMode resistorConfiguration,
      PinLevelConsumer levelConsumer,
      Consumer<IOStatusCode> statusCallback) {
  }


  InputPin(
    byte pinNumber,
    OutputChannel outputChannel) {
    super(pinNumber, outputChannel, StatusScope.INPUT);
    value = LOW;
    levelConsumer = null;
  }

  /**
   * Takes this input pin offline. The pin <em>MUST</em> be locked
   * prior to invocation. Since the state machine is the only invoker,
   * this is assured.
   *
   * @return {@code false}, since an error occurred.
   */
  @Override
  protected boolean goOffline() {
    value = LOW;
    return false;
  }

  @Override
  protected void onReset() {
    value = LOW;
  }

  boolean open(
      PullMode resistorConfiguration,
      PinLevelConsumer levelConsumer,
      Consumer<IOStatusCode> statusCallback) {
    boolean result = false;
    try {
      lock();
      if (available()) {
        pendingConfiguration =
            new OutputPinConfiguration(
                resistorConfiguration,
                levelConsumer,
                statusCallback);
        result = transition(IOEvent.OPEN_REQUESTED, LOW);
        if (!result) {
          transition(IOEvent.OPEN_FAILED, LOW);
        }
      }
    } finally {
      unlock();
    }
    return result;
  }

  /**
   * State entry action for {@link PinState#ACTIVE} that sets the pin's
   * initial value.
   *
   * @param sideData status-specific data
   * @return {@code true} because it always succeeds.
   */
  @Override
  protected boolean openSucceeded(byte sideData) {
    value = sideData;
    return true;
  }

  /**
   * Receive a GPIO mutation (i.e. a changed voltage level), record its
   * value, and pass it to the bound {@link PinLevelConsumer}. Note that
   * the level consumer is <em>NOT</em> single threaded.
   *
   * @param mutation the new GPIO voltage level, LOW (0) or HIGH (1). The
   *                 level is ignored when the pin is inactive.
   */
  void receiveMutation(byte mutation) {
    // Note that active() is atomic so locking is not needed.
    // In fact locking would be undesirable since the pin must
    // be unlocked when it invokes the consumer.
    if (active()) {
      levelConsumer.consume(value = mutation);
    }
  }

  @Override
  protected boolean sendOpenRequest() {
    boolean result =
        pendingConfiguration != null
            && sendConfigurationCommand(ConfigurationCommandCode.OPEN,
            pendingConfiguration.resistorConfiguration);
    if (result) {
      levelConsumer = pendingConfiguration.levelConsumer();
      setStatusCallback(pendingConfiguration.statusCallback());
    }
    return result;
  }

  byte value() {
    try {
      lock();
      return value;
    } finally {
      unlock();
    }
  }

  // Test support
  @VisibleForTesting
  void setValue(byte value) {
    this.value = value;
  }
}
