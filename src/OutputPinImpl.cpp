/*
 * OutputPin.cpp
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

#include <Arduino.h>

#include "OutputPinImpl.h"

bool OutputPinImpl::open(void) {
  // TODO: figure how to use the low level API.
//  auto status =
//      set_direction(GPIO_MODE_OUTPUT)
//      && configure_pull_mode(PullMode::FLOAT);
//  set_state(status ? PinState::OPEN : PinState::OFFLINE);
//  return status;
  pinMode(static_cast<uint8_t>(pin_number()), OUTPUT);
  set_state(PinState::OPEN);
//  gpio_dump_io_configuration(stdout, (1ULL << pin_number()));
  return true;
}

bool OutputPinImpl::set_level(uint32_t level) {
//  Serial.printf("Setting pin %d to %d.\n",
//      static_cast<int>(pin_number()), level);
  return send_esp_status(
      gpio_set_level(pin_number(), level),
      0);
}
