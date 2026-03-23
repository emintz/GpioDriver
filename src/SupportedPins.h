/*
 * SupportedPins.h
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

#ifndef SUPPORTEDPINS_H_
#define SUPPORTEDPINS_H_

#include <driver/gpio.h>
#include <functional>

/**
 * Supported ESP32-S2 pins. TODO: Generalize for ESP32-S3 &c,
 * perhaps via #ifdef
 */

class SupportedPins {

  SupportedPins() = default;
  ~SupportedPins () = default;

public:

  /**
   * Apply the specified function to every input pin. Does
   * nothing if the provided function is null.
   *
   * @param function function to apply, ignored if
   *        null
   */
  static void apply_to_read(
      std::function<void(gpio_num_t)> function);


  /**
   * Apply the specified function to every output pin. Does
   * nothing if the provided function is null.
   *
   * @param function function to apply, ignored if
   *        null
   */
  static void apply_to_write(
      std::function<void(gpio_num_t)> function);

  static bool for_read(gpio_num_t pin);

  static bool for_read(uint8_t pin_number);

  static bool for_write(gpio_num_t pin);

  static bool for_write(uint8_t pin);
};

#endif /* SUPPORTEDPINS_H_ */
