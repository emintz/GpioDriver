/*
 * UARTDriver.h
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

#ifndef UARTDRIVER_H_
#define UARTDRIVER_H_

#include <driver/uart.h>

/**
 * Universal Asynchronous Receiver/Transmitter (UART) driver
 * manager
 */
class UARTDriver {

  UARTDriver () = default;
  virtual ~UARTDriver () = default;

public:

  /**
   * Install an unbuffered UART driver on the specified port. The
   * configured port will not have an event queue nor will
   * applciation-visible interrupts be enabled. Port is configured
   * for 8 bit characters, no parity, 1 stop bit, no flow control.
   * The caller supplies the baud rate.
   *
   * @param port the UART port to configure. Note that the ESP32-S2
   *        has two UART ports. Available ports might differ for
   *        other variants.
   * @param baud_rate transmission speed in nominal bits per second.
   *        Defaults to 115200.
   * @param transmit_pin the GPIO pin that pushes bits from this server
   *        to the client
   * @param receive_pin the GPIO pin that receives bits sent by the
   *        client
   * @return `true` if the driver is already installed or was
   *         installed successfully; `false` if installation failed.
   *         When the invocation succeeds (i.e. returns `true`), the
   *         specified port is opened for full duplex I/O.
   */
  static bool install_unbuffered(
      uart_port_t port,
      int transmit_pin,
      int receive_pin,
      uint32_t baud_rate = 115200);

  /**
   * Remove the UART driver for the specified port if a driver
   * is active (i.e. has been installed) on the port. Do nothing if a
   * driver is inactive (i.e. has not been installed) on the port.
   * @param port
   * @return
   */
  static bool remove(uart_port_t port);
};

#endif /* UARTDRIVER_H_ */
