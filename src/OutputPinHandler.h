/*
 * OutputPinHandler.h
 *
 *  Created on: Mar 19, 2026
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

#ifndef OUTPUTPINHANDLER_H_
#define OUTPUTPINHANDLER_H_

#include "IOStatusCode.h"
#include "OutputPinImpl.h"
#include "StatusReporter.h"
#include "SupportedPins.h"

#include <driver/gpio.h>
#include <map>
#include <memory>
#include <PullQueueHT.h>
#include <stdint.h>

class OutputPinHandler final : protected StatusReporter {
  std::map<gpio_num_t, std::unique_ptr<OutputPinImpl>> pins_;
  std::map<uint8_t, gpio_num_t> byte_to_pin_;

  class PinMapMaker {
    friend class OutputPinHandler;

    std::map<gpio_num_t, std::unique_ptr<OutputPinImpl>>& pins_;
    PullQueueHT<Packet>& status_message_queue_;

    PinMapMaker(
        std::map<gpio_num_t, std::unique_ptr<OutputPinImpl>>& pins,
        PullQueueHT<Packet>& status_message_queue) :
          pins_(pins),
          status_message_queue_(status_message_queue) {
    }

  public:

    void operator() (gpio_num_t pin) {
      pins_.emplace(
          pin,
          std::make_unique<OutputPinImpl>(
              pin,
              status_message_queue_)
              );
    }
  };

  void send_output_status(
      IOStatus status,
      gpio_num_t pin_number,
      uint8_t side_data = 0) {
    send_status(status, StatusScope::OUTPUT_SCOPE, pin_number, side_data);
  }

public:
  OutputPinHandler (PullQueueHT<Packet>& status_message_queue);
  virtual ~OutputPinHandler () = default;

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
        valid(pin)
        && pins_.at(pin)->available();
  }

  /**
   * Close the specified pin
   *
   * @param pin the pin to close
   * @return `true` on success, `false` on failure.
   */
  bool close(gpio_num_t pin) {
    return
        in_use(pin)
        && pins_.at(pin)->close();
  }

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
   * Write the mutation to its target pin. Send
   * a status report if the write fails.
   *
   * @param mutation contains the pin number and value.
   *        The most significant bit contains the value,
   *        either 0 or 1. The remaining bits contain the
   *        pin number, which must be in [0 .. -x7E]
   */
  void mutate(uint8_t mutation);

  /**
   * Open the specified pin for write (output). Send a status
   * message indicating success or failure.
   *
   * @param pin_number the pin to open
   *
   * @return `true` on success, `false` on failure
   */
  bool open_pin(gpio_num_t pin_number);

  /**
   * Reset the specified pin. Note that pins in any state are reset.
   * A successful reset restores the pin to availability, meaning that
   * the pin will become available (i.e. will be closed) if it is active
   * (i.e. is open).
   *
   * @param pin_number the pin to reset
   *
   * @return `true` on success, `false` on failure.
   */
  bool reset(gpio_num_t pin_number) {
    return
        valid(pin_number)
        && pins_.at(pin_number)->reset();
  }

  bool valid(gpio_num_t pin_number);
};

#endif /* OUTPUTPINHANDLER_H_ */
