/*
 * GpioInputOutputImpl.java
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class GpioInputOutputImpl<T extends Enum<T> & GpioPinNumber>
    implements GpioInputOutput<T> {

  private final EnumMap<T, InputPinProxy<T>> inputPinProxies;
  private final EnumMap<T, OutputPinProxy<T>> outputPinProxies;
  private final Map<Integer, T> physicalOutputPins;
  private final Class<T> pinType;
  private final Receiver receiver;
  private final Transmitter<T> transmitter;
  private ResponseDispatcher<T> dispatcher;
  private Thread dispatcherThread;

  /**
   * Constructor
   *
   * @param pinType pin enumerator class
   * @param receiver receives bytes from the server. This is exposed to
   *                 simplify testing.
   * @param transmitter sends bytes to the server. This is exposed to
   *                    simplify testing.
   */
  GpioInputOutputImpl(
      Class<T> pinType,
      Receiver receiver,
      Transmitter<T> transmitter) {
    this.pinType = pinType;
    this.receiver = receiver;
    this.transmitter = transmitter;
    inputPinProxies = new EnumMap<>(pinType);
    outputPinProxies = new EnumMap<>(pinType);
    physicalOutputPins = new HashMap<>();
    dispatcherThread = null;
  }

  /**
   * Accesses a specified pin's activity state. A pin is active
   * if and only if it is open for input or output.
   *
   * @param pin the GPIO pin to query
   * @return {@code true} if the pin is active, {@code false} otherwise
   */
  @Override
  public boolean active(T pin) {
    return inputPinProxies.get(pin).active()
    || outputPinProxies.get(pin).active();
  }

  /**
   * Accesses a specified pin's availability. A pin is
   * available if and only if it can be opened. If
   * available, the pin is not in use and is in a valid
   * state.
   *
   * @param pin the GPIO pin to query
   * @return {@code true} if the pin is available, {@code false}
   *         otherwise
   */
  @Override
  public boolean available(T pin) {
    return inputPinProxies.get(pin).available()
        && outputPinProxies.get(pin).available();
  }

  /**
   * If the dispatcher thread exists (i.e. the caller has successfully
   * invoked {@link #start()}), join the thread and wait indefinitely.
   * Otherwise, return immediately.
   *
   * @return {@code true} if the thread has been joined, {@code false}
   *         otherwise. Note that a successful return will be delayed
   *         indefinitely.
   * @throws InterruptedException if the thread is interrupted.
   */
  public boolean joinReadThread() throws InterruptedException {
    boolean result = dispatcherThread != null;
    if (result) {
      dispatcherThread.join();
    }
    return result;
  }

  /**
   * Accesses the pin's validity. If the pin is offline, the
   * pin is wedged in an invalid state. Resetting it
   * <em>might</em> make it available, but this is not
   * guaranteed. An online pin is either available or
   * active.
   *
   * @param pin the GPIO pin to query
   * @return {@code true} if the pin is in an invalid state,
   *         {@code false} otherwise. Note that the pin is
   *         usable if and only if this method returns {@code false}.
   */
  @Override
  public boolean offline(T pin) {
    return inputPinProxies.get(pin).offline()
        || outputPinProxies.get(pin).offline();
  }

  @Override
  public InputPin openForInput(
      T pin,
      PullMode resistorConfiguration,
      Consumer<Level> levelConsumer,
      Consumer<IOStatusMessage<T>> statusCallback) {
    InputPin result = null;
    if (available(pin))  {
      result = inputPinProxies.get(pin).open(resistorConfiguration, levelConsumer, statusCallback);
    }
    return result;
  }

  @Override
  public OutputPin openForOutput(T pin, Consumer<IOStatusMessage<T>> statusCallback) {
    OutputPin result = null;
    if (available(pin)) {
      result = outputPinProxies.get(pin).open(statusCallback);
    }
    return result;
  }

  void populateFields() {
    for (T pin : pinType.getEnumConstants()) {
      inputPinProxies.put(pin, new InputPinProxy<>(pin, transmitter));
      outputPinProxies.put(pin, new OutputPinProxy<>(pin, transmitter));
      physicalOutputPins.put((int) pin.number(), pin);
    }

    var mutationConsumer = new BiConsumer<T, Level>() {
      @Override
      public void accept(T pin, Level level) {
        inputPinProxies.get(pin).receiveMutation(level);
      }
    };

    var statusConsumer = new Consumer<IOStatusMessage<T>>() {
      @Override
      public void accept(IOStatusMessage<T> message) {
        switch (message.scope()) {
          case INPUT:
            inputPinProxies.get(message.gpioPin())
                .receivePinConfigurationStatus(
                    message.status(), message.side_data());
            break;
            case OUTPUT:
              outputPinProxies.get(message.gpioPin())
                  .receivePinConfigurationStatus(
                      message.status(), message.side_data());
              break;
          case INPUT_OUTPUT:
            System.err.println("Received INPUT_OUTPUT scoped status.");
            break;
          case SERVER:
            System.err.println("Received SERVER scoped status.");
            break;
        }
      }
    };

    dispatcher = new ResponseDispatcher<>(
        receiver,
        mutationConsumer,
        statusConsumer,
        physicalOutputPins
    );
  }

  @Override
  public boolean start() {
    boolean ret = true;
    populateFields();
    try {
      dispatcherThread = new Thread(dispatcher, "GPIO Dispatcher");
      dispatcherThread.start();
    } catch (Exception e) {
      e.printStackTrace();
      ret = false;
    }
    return ret;
  }

  @Override
  public InputPin synchronousOpenForInput(
      T pin,
      PullMode resistorConfiguration,
      Consumer<Level> levelConsumer,
      Consumer<IOStatusMessage<T>> statusCallback)
          throws InterruptedException {
    InputPin result = null;
    if (available(pin))  {
      var proxy = inputPinProxies.get(pin);
      try {
        var screener = new PinScreener<InputPin>();
        proxy.setTransitionCallback(screener);
        result = screener.awaitStatus(proxy.open(
            resistorConfiguration, levelConsumer, statusCallback));
      } finally {
        proxy.clearTransitionCallback();
      }
    }
    return result;
  }

  @Override
  public OutputPin synchronousOpenForOutput(
      T pin,
      Consumer<IOStatusMessage<T>> statusCallback)
          throws InterruptedException {
    OutputPin result = null;
    if (available(pin)) {
      var proxy = outputPinProxies.get(pin);
      try {
        var screener = new PinScreener<OutputPin>();
        proxy.setTransitionCallback(screener);
        result = screener.awaitStatus(proxy.open(statusCallback));
      } finally {
        proxy.clearTransitionCallback();
      }
    }
    return result;
  }

  @VisibleForTesting
  ResponseDispatcher<T> dispatcher() {
    return dispatcher;
  }

  @VisibleForTesting
  InputPinProxy<T> inputPinProxy(T pin) {
    return inputPinProxies.get(pin);
  }

  @VisibleForTesting
  OutputPinProxy<T> outputPinProxy(T pin) {
    return outputPinProxies.get(pin);
  }
}
