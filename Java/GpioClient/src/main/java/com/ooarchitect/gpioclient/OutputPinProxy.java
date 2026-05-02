/*
 * OutputPinV2.java
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

import java.util.function.Consumer;

/**
 * Proxy for a server GPIO that is openXXX for output.
 */
public class OutputPinProxy<T extends Enum<T> & GpioPinNumber>
    extends GpioPinProxy<T> {

  private static final byte LOW = 0;

  private Consumer<IOStatusCode> pendingStatusCodeCallback;

  OutputPinProxy(
      T pinNumber,
      Transmitter<T> outputChannel) {
    super(
        pinNumber,
        outputChannel,
        StatusScope.OUTPUT);
    pendingStatusCodeCallback = null;
  }

  @Override
  protected boolean goOffline() {
    return false;
  }

  boolean doOpen(Consumer<IOStatusCode> statusCallback) {
    boolean result = false;
    try {
      lock();
      pendingStatusCodeCallback = statusCallback;
      result = transition(IOEvent.OPEN_REQUESTED, LOW);
      if (!result) {
        result = transition(IOEvent.CLOSE_FAILED, LOW);
      }
    } finally {
      unlock();
    }
    return result;
  }

  @Override
  protected boolean onOpenSucceeded(byte sideData) {
    return true;
  }

  OutputPin open(Consumer<IOStatusCode> statusCallback) {
    return doOpen(statusCallback)
        ? new OutputPinImpl<>(this)
        : null;
  }

  /**
   * Sends the specified value to this output pin
   *
   * @param value pin value
   * @return status code, {@code true} if successful,
   *         {@code false} otherwise. The most common
   *         cause is send before successful openXXX.
   */
  boolean send(Level value) {
    boolean result = active();
    if (result) {
      result = sendMutation((byte) (rawPinNumber() | value.value()));
    }
    return result;
  }

  @Override
  protected boolean sendOpenRequest() {
    boolean result =
        pendingStatusCodeCallback != null
        && sendConfigurationCommand(ConfigurationCommandCode.OPEN,
            PullMode.FLOAT);
    if (result) {
      setStatusCallback(pendingStatusCodeCallback);
    }
    pendingStatusCodeCallback = null;
    return result;
  }
}
