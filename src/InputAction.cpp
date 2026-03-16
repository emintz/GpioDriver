/*
 * InputAction.cpp
 *
 *  Created on: Feb 16, 2026
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

#include "InputAction.h"

#include <Arduino.h>

#include <PinAssignments.h>

static const char HEX_DIGITS[] = "0123456789ABCDE";

//-----------------------------------------------------------
// Debugging support. Delete before putting in production. //
//-----------------------------------------------------------

// Temporary: write hex formatted debug output.
static void dump_hex(uint8_t value) {
  Serial.print('0');
  Serial.print('x');
  Serial.printf("%02x", static_cast<int>(value));
  Serial.print(' ');
}

static void dump_status(const StatusMessage& status) {
  Serial.print('<');
  dump_hex(0xFF);
  dump_hex(status.pin_number_);
  dump_hex(static_cast<uint8_t>(status.status_));
  dump_hex(status.side_data_);
  dump_hex(0x7F);
  Serial.print('>');
}

static gpio_num_t to_output_pin(uint8_t input_pin) {
  gpio_num_t output_pin = GPIO_NUM_0;
  switch (input_pin) {
  case BUILTIN_PUSH_BUTTON_PIN:
    output_pin = static_cast<gpio_num_t>(BUILTIN_LED_PIN);
    break;
  case RED_PUSH_BUTTON_PIN:
    output_pin = static_cast<gpio_num_t>(RED_LED_PIN);
    break;
  case GREEN_PUSH_BUTTON_PIN:
    output_pin = static_cast<gpio_num_t>(GREEN_LED_PIN);
    break;
  case YELLOW_PUSH_BUTTON_PIN:
    output_pin = static_cast<gpio_num_t>(YELLOW_LED_PIN);
    break;
  }
  return output_pin;
}

//-----------------------------------------------------------
// END debugging support.                                  //
//-----------------------------------------------------------

InputAction::InputAction (
    PullQueueHT<uint8_t>& pin_change_queue,
    PullQueueHT<StatusMessage>& status_queue) :
        pin_change_queue_(pin_change_queue),
        status_queue_(status_queue) {
}

InputAction::~InputAction () {
}

void InputAction::empty_status_queue(void) {
  while (0 < status_queue_.waiting_message_count()) {
    StatusMessage status_message;
    bool read_result = status_queue_.pull_message(&status_message, 0);
    if (!read_result) {
      break;
    }
    dump_status(status_message);
  }
}

void InputAction::run(void) {
  uint8_t pin_change = 0;
  for (;;) {
    empty_status_queue();
    if (pin_change_queue_.pull_message(&pin_change, 1)) {
      empty_status_queue();
      int input_pin = pin_change & 0x7f;
      gpio_num_t output_pin = to_output_pin(pin_change & 0x7f);
      if (GPIO_NUM_0 != output_pin) {
        gpio_set_level(
            output_pin,
            (pin_change & 0x80) ? 1 : 0);
      }
    }
  }
}
