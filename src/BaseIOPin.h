/*
 * BaseIOPin.h
 *
 *  Created on: Mar 21, 2026
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

#ifndef BASEIOPIN_H_
#define BASEIOPIN_H_

#include "IOStatusCode.h"
#include "Packet.h"
#include "PinState.h"
#include "PullMode.h"
#include "StatusReporter.h"

#include <driver/gpio.h>
#include <PullQueueHT.h>

/**
 * Base class for input and output pins.
 */
class BaseIOPin : public StatusReporter {

  const gpio_num_t pin_number_;
  const StatusScope scope_;
  PinState state_;


protected:
  BaseIOPin (
      const gpio_num_t pin_number,
      const StatusScope scope,
      PullQueueHT<Packet>& packet_queue);


  /**
   * Configure the pin's pullup/pulldown resistors and
   * send a status message on failure.
   *
   * @param pull_mode resistor configuration
   * @return true if successful, false otherwise.
   */
  bool configure_pull_mode(PullMode pull_mode);

  /**
   * Physical GPIO pin accessor
   *
   * @return the physical pin number
   */
  gpio_num_t pin_number() {
    return pin_number_;
  }

  /**
   * Send a esp_err_t status code to the client. Do nothing
   * if the code is ESP_OK.
   *
   * @param status
   * @param side_data
   */
  bool send_esp_status(
      esp_err_t esp_status,
      uint8_t side_data = 0);


  /**
   * Enqueue a status message for a pin
   *
   * @param status the pin status
   * @param side_data status-dependent data, zero if none.
   */
  void send_pin_status(
      IOStatus status,
      uint8_t side_data = 0) {
    send_status(
        status,
        scope_,
        pin_number_,
        side_data);
  }

  /**
   * Sets the pin direction (input or output)
   *
   * @param direction how the data flows: in or out
   * @return true on success, false on failure
   */
  bool set_direction(gpio_mode_t direction);

  /**
   * Sets the pin state to the specified value.
   *
   * @param state the state to set.
   */
  void set_state(PinState state) {
    state_ = state;
  }

public:
  virtual ~BaseIOPin() = default;

  /**
   * Query this pin for availability. A pin must be
   * available before it can be opened for output
   *
   * @return true if the pin is available, false otherwise.
   */
  bool available() {
    return PinState::CLOSED == state_;
  }

  /**
   * Close this pin.
   *
   * @return true if the pin was closed successfully, false
   *         if the close could not be performed for any reason,
   *         including the pin already being closed.
   */
  bool close();

  /**
   * Query this pin for being opened for output
   *
   * @return true if the pin is open for output, false
   *         otherwise. Note that a non-opened pin
   *         might not openable as it could be offline.
   */
  bool in_use() {
    return PinState::OPEN == state_;
  }


  /**
   * Query this pin for being offline. Note that an
   * offline pin is hosed and cannot be opened for
   * output
   *
   * @return true if the pin is offline for output, false
   *         otherwise.
   */
  bool offline() {
    return PinState::OFFLINE == state_;
  }

  /**
   * Rests the pin. Disables input and output, reset
   * the resistor configuration, etc. Post a status
   * message on error.
   *
   * @return true on success, false on failure.
   */
  bool reset(void);

};

#endif /* BASEIOPIN_H_ */
