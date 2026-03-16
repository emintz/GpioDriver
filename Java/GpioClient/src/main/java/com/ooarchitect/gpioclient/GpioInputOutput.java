/*
 * GpioInputOutput.java
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
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Main user interface with the BPIO I/O system. Provides basic control
 * and
 */
public class GpioInputOutput<T extends Enum<? extends GpioPinNumber>> {
  private final GpioReader<T> reader;
  private final GpioWriter<T> writer;
  // The dispatcher is a bridge between the input and
  // the reader and the writer. We park it here
  // for convenience. Please leave it even though
  // this class doesn't reference it.
  @SuppressWarnings("unused")
  private final ResponseDispatcher<T> dispatcher;
  private Thread dispatchThread;

  /**
   * Routes incoming status messages to their handlers. Routing
   * is based on the message scope.
   *
   * @param <T> Server microcontroller type
   *
   * @see StatusScope
   */
  @VisibleForTesting
  static class StatusRouter<T extends Enum<? extends GpioPinNumber>>
      implements Consumer<IOStatusMessage<T>> {

    private final GpioReader<T> reader;
    private final GpioWriter<T> writer;

    /**
     * Creates a {@link StatusRouter}
     *
     * @param reader handles input-related status messages
     * @param writer handles output-related status messages
     */
    StatusRouter(GpioReader<T> reader, GpioWriter<T> writer) {
      this.reader = reader;
      this.writer = writer;
    }

    /**
     * Routes a status message to its handler
     *
     * @param statusMessage message to route.
     */
    @Override
    public void accept(IOStatusMessage<T> statusMessage) {
      switch (statusMessage.scope()) {
        case INPUT -> reader.dispatchToTargetPin(statusMessage);
        case OUTPUT -> writer.dispatchToTargetPin(statusMessage);
        case INPUT_OUTPUT -> {
          reader.dispatchToTargetPin(statusMessage);
          writer.dispatchToTargetPin(statusMessage);
        }
        case SERVER -> {
          reader.dispatchToAllPins(statusMessage);
          writer.dispatchToAllPins(statusMessage);
        }
      }
    }
  }

  GpioInputOutput (
      GpioReader<T> reader,
      GpioWriter<T> writer,
      ResponseDispatcher<T> dispatcher) {
    this.reader = reader;
    this.writer = writer;
    this.dispatcher = dispatcher;
    dispatchThread = null;
  }

  public boolean active(T pin) {
    var readerActive = reader.active(pin);
    var writerActive = writer.active(pin);
    if (readerActive && writerActive) {
      throw new IllegalStateException(
          "Both reader and writer active on pin: " + pin);
    }
    return readerActive || writerActive;
  }

  public boolean available(T pin) {
    return reader.available(pin) && writer.available(pin);
  }

  public boolean offline(T pin) {
    return reader.offline(pin) || writer.offline(pin);
  }

  @Nullable
  public InputPin openForInput(
      T pin,
      PullMode resistorConfiguration,
      PinLevelConsumer levelConsumer,
      Consumer<IOStatusCode> statusCallback) {
    return available(pin)
        ? reader.open(pin, resistorConfiguration, levelConsumer, statusCallback)
        : null;
  }

  @Nullable
  public OutputPin openForOutput(
      T pinId,
      Consumer<IOStatusCode> statusCallback) {
    return available(pinId)
        ? writer.open(pinId, statusCallback)
        : null;
  }

 synchronized  public void start() {
    if (dispatchThread == null) {
      dispatchThread = new Thread(dispatcher);
      dispatchThread.start();
    }
  }

  synchronized void stop() {
    if (dispatchThread != null) {
      dispatcher.stop();
      try {
        dispatchThread.join();
        dispatchThread = null;
      } catch (InterruptedException expected) {
        // Normal exit
      }
    }
  }

  public static <T extends Enum<? extends GpioPinNumber>> GpioInputOutput<T> create(
      Class<T> pinClass,
      OutputChannel outputChannel,
      ByteSupplier inputChannel) {
    PhysicalToGpioPin<T> physicalToGpioPin = new PhysicalToGpioPin<>(pinClass);
    var reader = new GpioReader<>(outputChannel, physicalToGpioPin);
    var writer = new GpioWriter<>(outputChannel, physicalToGpioPin);
    var statusRouter = new StatusRouter<>(reader, writer);
    var responseDispatcher = new ResponseDispatcher<>(
        inputChannel,
        reader,
        statusRouter,
        physicalToGpioPin);
    return new GpioInputOutput<>(
        reader,
        writer,
        responseDispatcher);
  }
}
