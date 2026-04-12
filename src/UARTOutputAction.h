/*
 * UARTOutputAction.h
 *
 *  Created on: Apr 10, 2026
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

#ifndef UARTOUTPUTACTION_H_
#define UARTOUTPUTACTION_H_

#include "Packet.h"

#include <driver/uart.h>
#include <PullQueueHT.h>
#include <stdint.h>
#include <TaskAction.h>

/**
 * @brief Task action that writes packets to a serial port.
 */
class UARTOutputAction : public TaskAction {
  uart_port_t port_;
  PullQueueHT<Packet>& packet_queue_;

public:
  /**
   * @brief Constructs an instance that listens to the specified
   *        `packet_queue` and writes to the specified UART `port`
   *
   * @param port UART output port. Specifies which UART to use.
   * @param packet_queue data source
   */
  UARTOutputAction (
      uart_port_t port,
      PullQueueHT<Packet>& packet_queue) :
        packet_queue_(packet_queue){
    port_ = port;
  }

  virtual ~UARTOutputAction () = default;

  /**
   * @brief data forwarding loop that waits for messages on the input queue
   *        and writes their contents to the UART specified at construction.
   */
  virtual void run(void) override;
};

#endif /* UARTOUTPUTACTION_H_ */
