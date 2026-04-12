/*
 * InputPinImpl.h
 *
 *  Created on: Mar 26, 2026
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

#ifndef INPUTPINIMPL_H_
#define INPUTPINIMPL_H_
#include "BaseIOPin.h"

#include "IOStatusCode.h"
#include "Packet.h"
#include "StatusScope.h"

#include <driver/gpio.h>
#include <GpioChangeDetector.h>
#include <PullQueueHT.h>
#include <VoidFunction.h>


class InputPinImpl :
    public BaseIOPin,
    public VoidFunction {
  PullQueueHT<Packet>& packet_queue_;
  GpioChangeDetector pin_change_detector_;

public:
  InputPinImpl(
      const gpio_num_t pin_number,
      PullQueueHT<Packet>& packet_queue);
  virtual ~InputPinImpl () = default;

  /**
   * Invoked by the pin change detector whenever voltage
   * changes on the pin.
   */
  virtual void apply(void) override;

  /**
   * Open this pin for write (output)
   *
   * @param mode pullup/pulldown resistor configuration
   *
   * @return `true` if successful, `false` otherwise.
   */
  bool open(PullMode mode);

  /**
   * Starts monitoring the pin level. Should be invoked
   * immediately after the pin was successfully opened.
   * The pin must be taken off-line if this call fails.
   *
   * @return true if monitoring started successfully, false
   *         otherwise.
   */
  bool start(void) {
    return pin_change_detector_.start();
  }

  /**
   * Stops monitoring the pin level. Should be invoked
   * immediately before closing the pin. The pin must be
   * taken off-line if this call fails.
   */
  void stop() {
    pin_change_detector_.stop();
  }
};

#endif /* INPUTPINIMPL_H_ */
