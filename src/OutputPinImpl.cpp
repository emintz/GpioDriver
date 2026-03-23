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

#include "OutputPinImpl.h"

bool OutputPinImpl::open(void) {
  auto status =
      set_direction(GPIO_MODE_OUTPUT)
      && configure_pull_mode(PullMode::FLOAT);
  if (status) {
    set_state(PinState::OPEN);
  }
  return status;
}

bool OutputPinImpl::set_level(uint32_t level) {
  Serial.printf("Setting pin %d to %d.\n",
      static_cast<int>(pin_number()), level);
  return send_esp_status(
      gpio_set_level(pin_number(), level),
      0);
}
