/*
 * OutputPinHandler.cpp
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

#include "OutputPinHandler.h"

OutputPinHandler::OutputPinHandler (
    PullQueueHT<StatusMessage>& status_message_queue) :
        StatusReporter(status_message_queue),
        pins_(),
        byte_to_pin_() {
  SupportedPins::apply_to_write(
      OutputPinHandler::PinMapMaker(pins_, status_message_queue));
  for (const auto& [key, value] : pins_) {
    byte_to_pin_.emplace(static_cast<uint8_t>(key), key);
  }
}

bool OutputPinHandler::valid(gpio_num_t pin_number) {
  auto is_valid = pins_.contains(pin_number);
  if (!is_valid) {
    send_output_status(IOStatus::NO_SUCH_PIN, pin_number);
  }
  return is_valid;
}

bool OutputPinHandler::open_pin(
    gpio_num_t pin_number) {
  bool opened_pin = false;

  if (valid(pin_number)) {
    auto pin = pins_.at(pin_number).get();
    if (pin->offline()) {
     send_output_status(IOStatus::INVALID_STATE, pin_number);
    } else if (pin->in_use()) {
     send_output_status(IOStatus::PIN_IN_USE, pin_number);
    } else if (!pin->available()) {
     send_output_status(IOStatus::PIN_OFFLINE, pin_number);
    } else if (opened_pin = pin->open()) {
     send_output_status(IOStatus::OPEN_SUCCEEDED, pin_number);
    } else {
     send_output_status(IOStatus::OPEN_FAILED, pin_number);
    }
  }
  return opened_pin;
}

void OutputPinHandler::mutate(uint8_t mutation) {
  Serial.printf("Applying mutation: %02x.\n", mutation);
  uint8_t pin_number = mutation & 0x7F;
  if (valid(static_cast<gpio_num_t>(pin_number))) {
    auto pin_id = byte_to_pin_.at(pin_number);
    if (pins_.at(pin_id)->in_use()) {
      pins_.at(pin_id)->set_level(
          mutation & 0x80
              ? 1 : 0);
      // TODO: report output failure.
    }
  } else {
    send_output_status(
        IOStatus::NO_SUCH_PIN, static_cast<gpio_num_t>(pin_number));
  }
}

