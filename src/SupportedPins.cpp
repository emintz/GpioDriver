/*
 * SupportedPins.cpp
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

#include "SupportedPins.h"

#include <cstdint>
#include <driver/gpio.h>
#include <map>
#include <set>

static std::set<gpio_num_t> input_pins(
    {
      GPIO_NUM_2,   // Built in LED
      GPIO_NUM_4,
      GPIO_NUM_5,
      GPIO_NUM_12,  // Stapping pin, must be LOW or floating at boot
      GPIO_NUM_13,
      GPIO_NUM_14,
      GPIO_NUM_15,
      // GPIO 16 reserved for UART 2 RX
      // GPIO 17 reserved for UART 2 TX
      GPIO_NUM_18,
      GPIO_NUM_19,
      GPIO_NUM_21,
      GPIO_NUM_22,
      GPIO_NUM_23,
      GPIO_NUM_25,
      GPIO_NUM_26,
      GPIO_NUM_27,
      GPIO_NUM_32,
      GPIO_NUM_33,
      GPIO_NUM_34,  // Read only, no pull up/down resistors
      GPIO_NUM_35,  // Read only, no pull up/down resistors
      GPIO_NUM_36,  // Read only, no pull up/down resistors
    });

static std::set<gpio_num_t> output_pins({
  GPIO_NUM_2,   // Built in LED
  GPIO_NUM_4,
  GPIO_NUM_5,
  GPIO_NUM_12,  // Stapping pin, must be LOW or floating at boot
  GPIO_NUM_13,
  GPIO_NUM_14,
  GPIO_NUM_15,
  // GPIO 16 reserved for UART 2 RX
  // GPIO 17 reserved for UART 2 TX
  GPIO_NUM_18,
  GPIO_NUM_19,
  GPIO_NUM_21,
  GPIO_NUM_22,
  GPIO_NUM_23,
  GPIO_NUM_25,
  GPIO_NUM_26,
  GPIO_NUM_27,
  GPIO_NUM_32,
  GPIO_NUM_33,
  // GPIO_NUM_34 is read-only.
  // GPIO_NUM_35 is read only.
  // GPIO_NUM_36 is read only.
});

static std::set<gpio_num_t> read_only_pins(
    {
      GPIO_NUM_34,  // Read only, no pull up/down resistors
      GPIO_NUM_35,  // Read only, no pull up/down resistors
      GPIO_NUM_36,  // Read only, no pull up/down resistors
    });

static gpio_num_t smallest_pin = *(input_pins.begin());
static gpio_num_t largest_pin = *(std::prev(input_pins.end()));

static bool pin_number_in_range(uint8_t pin_number) {
  return
      static_cast<uint8_t>(smallest_pin) <= pin_number
      && static_cast<uint8_t>(largest_pin) >= pin_number;
}

void SupportedPins::apply_to_read(
    std::function<void(gpio_num_t)> function) {
  if (function) {
    for (auto pin : input_pins) {
      function(pin);
    }
  }
}

void SupportedPins::apply_to_write(
  std::function<void(gpio_num_t)> function) {
  if (function) {
    for (auto pin : output_pins) {
      function(pin);
    }
  }
}

bool SupportedPins::for_read(gpio_num_t pin) {
  return input_pins.contains(pin);
}

bool SupportedPins::for_read(uint8_t pin_number) {
  return
      pin_number_in_range(pin_number)
      && for_read(static_cast<gpio_num_t>(pin_number));
}

bool SupportedPins::for_write(gpio_num_t pin) {
  return
      output_pins.contains(pin);
}

bool SupportedPins::for_write(uint8_t pin_number) {
  return
      pin_number_in_range(pin_number)
      && for_write(static_cast<gpio_num_t>(pin_number));
}
