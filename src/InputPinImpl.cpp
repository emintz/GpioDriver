/*
 * InputPinImpl.cpp
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

#include "InputPinImpl.h"

InputPinImpl::InputPinImpl (
    const gpio_num_t pin_number,
    PullQueueHT<Packet>& packet_queue) :
        BaseIOPin(
            pin_number,
            StatusScope::INPUT_SCOPE,
            packet_queue),
         packet_queue_(packet_queue),
         pin_change_detector_(
             pin_number,
             GpioChangeType::ANY_CHANGE,
             this) {
}

void InputPinImpl::apply(void) {
  uint8_t pin_level =
      gpio_get_level(pin_number()) << 7
      | pin_number();
  Packet packet(pin_level);
  if (!packet_queue_.send_message_from_ISR(&packet)) {
     send_status_from_ISR(
         IOStatus::LOST_INPUT,
         StatusScope::INPUT_SCOPE,
         pin_number());
  }
}

bool InputPinImpl::open(PullMode mode) {
  auto status =
      set_direction(GPIO_MODE_INPUT)
      && configure_pull_mode(mode);
      set_state(status ? PinState::OPEN : PinState::OFFLINE);
      return status;
}
