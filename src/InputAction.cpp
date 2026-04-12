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

#include "IOStatusCode.h"
#include "InputAction.h"
#include "PinAssignments.h"

#include <Arduino.h>

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

static void dump_packet(const Packet& packet) {
  auto length = static_cast<unsigned int>(packet.length());
  auto data = packet.data();
  for (unsigned int index = 0; index < length; ++index) {
    dump_hex(*data++);
  }
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
    PullQueueHT<Packet>& packet_queue,
    OutputPinHandler& output_handler) :
        packet_queue_(packet_queue),
        output_handler_(output_handler) {
}

InputAction::~InputAction () {
}

void InputAction::run(void) {
  uint8_t pin_change = 0;
  Packet packet;
  for (;;) {
    if (packet_queue_.pull_message(&packet)) {
      if (1 == packet.length()) {
        auto pin_change = *packet.data();
        int input_pin = pin_change & 0x7f;
        gpio_num_t output_pin = to_output_pin(pin_change & 0x7f);
        uint8_t output_mutation =
            (pin_change & 0x80)
            | static_cast<uint8_t>(output_pin);
        output_handler_.mutate(output_mutation);
      } else {
        dump_packet(packet);
      }
    }
  }
}
