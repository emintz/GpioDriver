/*
 * UARTDriver.cpp
 *
 *  Created on: Apr 1, 2026
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

#include "UARTDriver.h"

#define RX_BUFFER_SIZE 256

bool UARTDriver::install_unbuffered(
    uart_port_t port,
    uint32_t baud_rate) {
  bool result = uart_is_driver_installed(port);

  if (!result) {
    result =
        (ESP_OK == uart_driver_install(
            port,
            RX_BUFFER_SIZE,
            0,
            0,
            NULL,
            0))
        && (ESP_OK == uart_set_word_length(
            port,
            UART_DATA_8_BITS))
        && (ESP_OK == uart_set_stop_bits(
            port,
            UART_STOP_BITS_1))
        && (ESP_OK == uart_set_parity(
            port,
            UART_PARITY_DISABLE))
        && (ESP_OK == uart_set_baudrate(
            port,
            baud_rate));
  }

  return result;
}

bool UARTDriver::remove(uart_port_t port) {
  bool result = !uart_is_driver_installed(port);
  if (!result) {
    result = ESP_OK == uart_driver_delete(port);
  }
  return result;
}
