/*
 * InputPinHandler.h
 *
 *  Created on: Mar 31, 2026
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

#include "InputPinImpl.h"
#include "IOStatusCode.h"
#include "Packet.h"
#include "PullMode.h"
#include "StatusReporter.h"
#include "StatusScope.h"

#include <driver/gpio.h>
#include <map>
#include <memory>
#include <PullQueueHT.h>
#include <stdint.h>

/**
 * @brief Command handler for input pins
 *
 * Processes commands that configure pins for input and maintains
 * a list of open input pins. Output pins are handled separately;
 * callers must prevent conflicts.
 */
class InputPinHandler : protected StatusReporter {

  PullQueueHT<Packet>& packet_queue_;
  std::map<gpio_num_t, std::unique_ptr<InputPinImpl>> pins_;
  std::map<uint8_t, gpio_num_t> byte_to_pin_;

  class PinMapMaker {
    friend class InputPinHandler;

    std::map<gpio_num_t, std::unique_ptr<InputPinImpl>>& pins_;
    PullQueueHT<Packet>& packet_queue_;

    PinMapMaker(
        std::map<gpio_num_t, std::unique_ptr<InputPinImpl>>& pins,
        PullQueueHT<Packet>& packet_queue) :
            pins_(pins),
            packet_queue_(packet_queue) {
    }

  public:
    void operator() (gpio_num_t pin) {
      pins_.emplace(
          pin,
          std::make_unique<InputPinImpl>(
              pin,
              packet_queue_));
    }
  };

  bool valid(gpio_num_t pin_number);

public:
  InputPinHandler (
      PullQueueHT<Packet>& packet_queue);
  virtual ~InputPinHandler () = default;

  /**
   * Determine if a pin can be opened for output.
   * @param pin
   *
   * @return `true` if the pin can be opened,
   *         `false` if it is in use or off line. Note
   *         that an unknown pin is always not available.
   */
  bool available(gpio_num_t pin) {
    return
        pins_.contains(pin)
        && pins_.at(pin)->available();
  }

  /**
   * Close the specified pin
   *
   * @param pin the pin to close
   * @return `true` on success, `false` on failure.
   */
  bool close_pin(gpio_num_t pin);

  /**
   * Determine if a pin us currently open for output
   *
   * @param pin pin to query.
   *
   * @return `true` if the pin is open for output,
   *         `false` otherwise. Note that an unknown
   *         pin is always not open
   */
  bool in_use(gpio_num_t pin) {
    return
       valid(pin)
        && pins_.at(pin)->in_use();
  }

  /**
   * Determine if a pin is off line, i.e. is in an
   * an error state (possibly wedged)
   *
   * @param pin pin to query
   * @return `true` if the pin is off line, `false`
   *         otherwise. Note that an unknown pin is
   *         always off line.
   */
  bool offline(gpio_num_t pin) {
    return
        !valid(pin)
        || pins_.at(pin)->offline();
  }

  /**
   * Open the specified pin for read (input). Send a status
   * message indicating success or failure. The caller
   * MUST determine that the pin is available for write
   * (output) before invoking this method.
   *
   * @param pin_number the pin to open
   * @param mode the pullup/pulldown resistor configuration.
   *
   * @return `true` on success, `false` on failure
   */
  bool open_pin(
      gpio_num_t pin_number,
      PullMode node);

  /**
   * Resets the specified pin. On success, the pin becomes available;
   * if it was active, it will be closed. On failure, the pin goes off-line.
   *
   * @param pin_number the pin to reset
   * @return 'true' on success; `false` on failure
   */
  bool reset(gpio_num_t pin_number);

  /**
   * Enqueue a status message for transmission to the client. Note that
   * the message's scope will be `INPUT_SCOPE`.
   *
   * @param status the status code
   * @param pin_number the pin number
   * @param side_data side data; defaults to 0 if not provided
   */
  void send_input_status(
      IOStatus status,
      gpio_num_t pin_number,
      uint8_t side_data = 0) {
    send_status(status, StatusScope::INPUT_SCOPE, pin_number, side_data);
  }
};

#endif /* INPUTPINHANDLER_H_ */
