/*
 * OutputPin.h
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

#ifndef OUTPUTPINIMPL_H_
#define OUTPUTPINIMPL_H_

#include "BaseIOPin.h"
#include "PinState.h"
#include "PullMode.h"

#include <driver/gpio.h>
/**
 * Handles a single output pin.
 */
class OutputPinImpl final :
    public BaseIOPin {

public:
  /**
   * Constructor
   *
   * @param pin_number the GPIO pin identifier
   * @param status_queue transmits status messages to the client
   */
  OutputPinImpl(
      const gpio_num_t pin_number,
      PullQueueHT<StatusMessage>& status_queue) :
          BaseIOPin(
              pin_number,
              StatusScope::OUTPUT_SCOPE,
              status_queue) {
  }

  virtual ~OutputPinImpl() = default;

  /**
   * Opens the pin and configures its pullup/pulldown
   * resistors to float. The caller <em>MUST</em>
   * ensure that this pin is available.
   *
   * @return true on success, false on failure
   */
  bool open(void);

  /**
   * Set the pin level to low (0) or high (1). Send a status
   * message on error.
   *
   * @param level 0 for low, 1 for high.
   * @return true on success, false on failure.
   */
  bool set_level(uint32_t level);
};

#endif /* OUTPUTPINIMPL_H_ */
