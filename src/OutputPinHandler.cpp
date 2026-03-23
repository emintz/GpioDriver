/*
 * OutputPinHandler.cpp
 *
 *  Created on: Mar 19, 2026
 *      Author: eric
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
      StatusReporter(status_message_queue) {
    SupportedPins::apply_to_write(
        OutputPinHandler::PinMapMaker(pins_, status_message_queue));
    for (const auto& [key, value] : pins_) {
      byte_to_pin_.emplace(static_cast<uint8_t>(key), key);
    }
  }

  void OutputPinHandler::mutate(uint8_t mutation) {
    Serial.printf("Applying mutation: %02x.\n", mutation);
    uint8_t pin_number = mutation & 0x7F;
    if (byte_to_pin_.contains(pin_number)) {
      Serial.printf("Found pin number %02d.\n", pin_number);
      auto pin_id = byte_to_pin_.at(pin_number);
      if (pins_.at(pin_id)->in_use()) {
        pins_.at(pin_id)->set_level(
            mutation & 0x80
                ? 1 : 0);
      }
    }
  }

bool OutputPinHandler::open_pin(
    gpio_num_t pin_number) {
  bool opened_pin = false;

  if (!pins_.contains(pin_number)) {
    send_output_status(IOStatus::NO_SUCH_PIN, pin_number);
  } else{
    auto pin = pins_.at(pin_number).get();
    if (pin->offline()) {
     send_output_status(IOStatus::INVALID_STATE, pin_number);
    } else if (pin->in_use()) {
     send_output_status(IOStatus::PIN_IN_USE, pin_number);
    } else if (!pin->available()) {
     send_output_status(IOStatus::PIN_OFFLINE, pin_number);
    } else if (pin->open()) {
     opened_pin = true;
     send_output_status(IOStatus::OPEN_SUCCEEDED, pin_number);
    } else {
     send_output_status(IOStatus::OPEN_FAILED, pin_number);
    }
  }
  return opened_pin;
}

