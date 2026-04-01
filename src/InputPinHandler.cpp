/*
 * InputPinHandler.cpp
 *
 *  Created on: Mar 31, 2026
 *      Author: Eric Mintz
 *
 * Copyright (c) 2026, Eric Mintz
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

#include "InputPinHandler.h"
#include "SupportedPins.h"

InputPinHandler::InputPinHandler (
    PullQueueHT<uint8_t>& pin_change_queue,
    PullQueueHT<StatusMessage>& status_queue) :
        StatusReporter(status_queue),
        status_queue_(status_queue),
        pins_(),
        byte_to_pin_() {
  Serial.println("Creating pin map.");
  SupportedPins::apply_to_read(
      InputPinHandler::PinMapMaker(pins_, pin_change_queue, status_queue_));
  for (const auto& [key, value] : pins_) {
    byte_to_pin_.emplace(static_cast<uint8_t>(key), key);
  }
}

bool InputPinHandler::valid(gpio_num_t pin_number) {
  bool is_valid = pins_.contains(pin_number);
  if (!is_valid) {
    send_input_status(IOStatus::NO_SUCH_PIN, pin_number);
  }
  return is_valid;
}

bool InputPinHandler::close_pin(gpio_num_t pin) {
  auto result = in_use(pin);
  if (result) {
    auto to_close = pins_.at(pin).get();
    to_close->stop();
    result = to_close->close();
  }
  return result;
}

bool InputPinHandler::open_pin(
    gpio_num_t pin_number,
    PullMode mode) {
  bool opened_pin = false;

  if (valid(pin_number)) {
    auto pin = pins_.at(pin_number).get();
    if (pin->offline()) {
     send_input_status(IOStatus::INVALID_STATE, pin_number);
    } else if (pin->in_use()) {
     send_input_status(IOStatus::PIN_IN_USE, pin_number);
    } else if (!pin->available()) {
     send_input_status(
         IOStatus::PIN_OFFLINE, pin_number);
    } else if (
        opened_pin =
            pin->open(mode)
            && pin->start()) {
     send_input_status(IOStatus::OPEN_SUCCEEDED, pin_number, gpio_get_level(pin_number));
    } else {
     send_input_status(IOStatus::OPEN_FAILED, pin_number);
     pin->reset();
    }
  }
  return opened_pin;
}

bool InputPinHandler::reset(gpio_num_t pin_number) {
  return
      valid(pin_number)
      && pins_.at(pin_number)->reset();
}
