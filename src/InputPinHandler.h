/*
 * InputPinHandler.h
 *
 *  Created on: Feb 15, 2026
 *      Author: Eric Mintz
 *
 * Copyright (c) 2026, Eric Mintz
 * All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <https://www.gnu.org/licenses/>.
 */

#ifndef INPUTPINHANDLER_H_
#define INPUTPINHANDLER_H_

#include "StatusMessage.h"

#include <GpioChangeDetector.h>
#include <memory>
#include <PullQueueHT.h>
#include <stdint.h>
#include <vector>
#include <VoidFunction.h>

#include <driver/gpio.h>

/**
 * @brief Pin input handler
 *
 * The pin input handler manages the global input activity. Users
 * can open and close pins. When it opens or closes a pin, the handler
 * enqueues the operation results for transmission to the host. When
 * an open pin value changes, the handler enqueues a change notification.
 */
class InputPinHandler final {

  /**
   * Handler state
   */
  enum class State {
    STOPPED,  /**< Handler is not running and can be started. */
    RUNNING,  /**< Handler is running */
  };

public:
  /**
   * Pullup/Pulldown resistor modes
   */
  enum class PullMode {
    UP,     /**< Pull the input to Vcc */
    DOWN,   /**< Pull the input to ground */
    BOTH,   /**< Both (but why?) */
    FLOAT,  /**< Neither pull up or pull down resistor */
  };
private:

  /**
   * Pin change handler, a `VoidFunction` implementation that
   * sends pin change notifications to the host. Transmission
   * is delegated to the containing `InputPinHandler`. Note
   * that the functions runs in *interrupt* mode.
   */
  class OnPinChange final : public VoidFunction {
    gpio_num_t pin_number_;
    InputPinHandler& pin_handler_;


  public:
    /**
     * Creates a pin change handler and binds it to the
     * specified pin.
     *
     * @param pin_number pin to watch. Note that the pin must
     *                   be properly configured for input. If
     *                   pullup or pulldown resistors are needed,
     *                   they must be set up as well. Please use
     *                   the native ESP32
     *                   GPIOxx[https://docs.espressif.com/projects/esp-idf/en/stable/esp32/api-reference/peripherals/gpio.html]
     *                   symbolic pin declarations for best results.
     *                   The pin number must be between 0 and 126 inclusive
     *                   because pin 127 indicates an error.
     *
     * @param pin_handler containing pin handling service, which
     *                    sends messages and maintains status.
     */
    OnPinChange(
        gpio_num_t pin_number,
        InputPinHandler& pin_handler) :
          pin_number_(pin_number),
          pin_handler_(pin_handler) {
    }

    virtual ~OnPinChange(void) = default;

    /**
     * Invoked when a pin changes value.
     */
    virtual void apply(void) override {
      pin_handler_.pin_changed(pin_number_);
    }

    /**
     * Retrieves the watched pin number
     *
     * @return the watched pin number
     */
    gpio_num_t pin_number(void) {
      return pin_number_;
    }
  };

  /**
   * Manages pin input. The class aggregates a detector that
   * that fires on all pin level changes and the pin change
   * handler that notifies the dispatcher of the change.
   */
  class InputPinManager final {

    std::unique_ptr<InputPinHandler::OnPinChange> on_pin_change_;
    std::unique_ptr<GpioChangeDetector> change_detector_;

  public:
    /**
     * Constructor
     *
     * @param pin_number identifies the GPIO pin being watched. Pin
     *                   numbers are unsigned integers < 128.
     * @param handler    Containing input pin handler, the class that
     *                   does all the work.
     */
    InputPinManager(gpio_num_t pin_number, InputPinHandler& handler);
    ~InputPinManager() = default;

    /**
     * Start the input service. Be sure that the service is closed.
     *
     * @return `true` if the open succeeded, `false` otherwise.
     */
    bool start(void) {
      gpio_num_t pin_number = on_pin_change_->pin_number();
      return change_detector_->start();
    }

    /**
     * Stops the service if it is running. Does nothing if the service
     * is not running.
     */
    void stop(void) {
      return change_detector_->stop();
    }
  };

  friend class InputPinHandler::OnPinChange;

  PullQueueHT<uint8_t>& pin_change_queue_;
  PullQueueHT<StatusMessage>& status_queue_;

  HardwareGpioChangeService gpio_change_service_;
  State state_;
  std::vector<std::unique_ptr<InputPinManager>> watched_pins_;

  /**
   * Examine an esp_err_t value and post (enqueue for future delivery)
   * an error status message if an error occurred -- that is, if
   * `ESP_OK != status`.
   *
   * @param pin_number reporting pin number
   * @param status low-level ESP32 status value
   * @return `true` if  `ESP_OK == status`, `false` otherwise
   */
  bool post_esp32_status(gpio_num_t pin_number, esp_err_t status);

  /**
   * Set the specified pin to input mode and notify the host on
   * failure
   *
   * @param pin_number pin to set
   * @return `true` if the operation succeeded, `false` otherwise.
   */
  bool set_as_input(gpio_num_t pin_number);

  /**
   * Configures pullup and pulldown resistors on the specified pin
   * and notify the host of any failure.
   *
   * @param pin_number the pin to configure
   * @param mode the configuration
   * @return `true` if the mode was set successfully, `false` otherwise.
   */
  bool set_pull_mode(
      gpio_num_t pin_number,
      InputPinHandler::PullMode mode);

  /**
   * Start watching the pin and report any errors that occur. If
   * the invocation succeeds, input will begin to stream to the host
   * starting with the current pin value. The pin must be fully
   * configured prior to invocation.
   *
   * @param pin_number the pin to watch.
   * @return `true` if the operation succeeded, `false` otherwise
   */
  bool start_pin_watch(gpio_num_t pin_number);

  /**
   * Enqueues a pin changed message for the specified pin. Note
   * that this method runs in *interrupt* mode.
   *
   * @param pin_number identifies the changed pin.
   */
  void pin_changed(gpio_num_t pin_number);

  /**
   * Enqueue a status message for a pin
   *
   * @param pin_number the reporting pin
   * @param status the pin status
   * @param side_data status-dependent data, zero if none.
   */
  void send_status(
      IOStatus status,
      StatusScope scope,
      gpio_num_t pin_number,
      uint8_t side_data = 0) {
    StatusMessage message = {
        .status_ = status,
        .scope_ = scope,
        .pin_number_ = pin_number,
        .side_data_ = side_data,
    };

    status_queue_.send_message(&message);
  }

public:
  /**
   * Constructor
   *
   * @param pin_change_queue transmits pin change notifications. The
   *        invoker is responsible for configuring and starting the
   *        queue.
   * @param status_queue transmits status change messages. The
   *                    invoker is responsible for configuring
   *                    and starting the queue.
   */
  InputPinHandler(
      PullQueueHT<uint8_t>& pin_change_queue,
      PullQueueHT<StatusMessage>& status_queue);
  virtual ~InputPinHandler();

  /**
   * Starts the input pin manager by starting all required
   * services. Calls are idempotent.
   *
   * @return `true` if startup succeeded; `false` otherwise.
   */
  bool begin(void);

  /**
   * Ends the input pin manager  by shutting down all required
   * services. Does nothing if the service is not open. Calls
   * are idempotent. Note that the handler can be restarted.
   */
  void end(void);

  /**
   * Determines if a pin is being used for input
   *
   * @param pin_number the pin to check
   * @return `true` if the pin is being used for input, `false` otherwise.
   */
  bool in_use(gpio_num_t pin_number) {
    return pin_number < watched_pins_.size()
        && static_cast<bool>(watched_pins_[pin_number]);
  }

  /**
   * Opens the specified pin for input and reports the operation
   * status to the caller. The status is placed on the status queue
   * for transmission to the host.
   *
   * @param pin_number the pin to configure. The pin *MUST NOT* be
   *                   in use. The invoker must check both input and
   *                   output.
   */
  void open_pin(gpio_num_t pin_number, InputPinHandler::PullMode mode);

private:

};

#endif /* INPUTPINHANDLER_H_ */
