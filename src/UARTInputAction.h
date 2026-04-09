/*
 * UARTInputAction.h
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

#ifndef UARTINPUTACTION_H_
#define UARTINPUTACTION_H_

#include <driver/uart.h>
#include <PullQueueHT.h>
#include <stdint.h>
#include <TaskAction.h>

/**
 * Task action that reads data from a UART and forwards it
 * to a queue
 */
class UARTInputAction : public TaskAction {
  uart_port_t port_;
  PullQueueHT<uint8_t>& input_queue_;
public:

  /**
   * Constructs an instance that reads from the specified port and
   * writes to the specified queue. This is one half of the server's
   * connection to the client; the other being the action that sends
   * output to the client.
   *
   * @param port UART number, the physical data source
   * @param input_queue connection to the task(s) that process
   *        received data.
   */
  UARTInputAction (
      uart_port_t port,
      PullQueueHT<uint8_t>& input_queue) :
          port_(port),
          input_queue_(input_queue) {

  }
  virtual ~UARTInputAction () = default;

  /**
   * The forwarding loop, the task action that reads from
   * the UART and writes to the queue. Be sure that the
   * UART driver has been installed for this instance's
   * port before starting the loop (i.e. starting the
   * enclosing task).
   */
  virtual void run(void) override;
};

#endif /* UARTINPUTACTION_H_ */
