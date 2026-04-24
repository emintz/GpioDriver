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
    int transmit_pin,
    int receive_pin,
    uint32_t baud_rate) {
  bool result = uart_is_driver_installed(port);

  if (!result) {
    uart_config_t uart_config = {
        .baud_rate = 115200,
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        .rx_flow_ctrl_thresh = 0,
        .source_clk = UART_SCLK_APB,
    };

    result =
        (ESP_OK == uart_driver_install(
            port,
            RX_BUFFER_SIZE,
            0,
            0,
            NULL,
            0))
        && (ESP_OK == uart_set_pin(
            port,
            transmit_pin,
            receive_pin,
            UART_PIN_NO_CHANGE,
            UART_PIN_NO_CHANGE))
        && (ESP_OK == uart_param_config(
            port,
            &uart_config));
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
