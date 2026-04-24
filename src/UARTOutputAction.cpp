/*
 * UARTOutputAction.cpp
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

#include "UARTOutputAction.h"

#include <Arduino.h>

void UARTOutputAction::run(void) {
  Packet packet;
  for (;;) {
    if(packet_queue_.pull_message(&packet)) {
//      Serial.printf("Writing %d bytes.\n", packet.length());
      if (auto sent = packet.length() != uart_write_bytes(
          port_,
          packet.data(),
          packet.length())) {
        Serial.printf("ERROR: only %d bytes of %d sent!\n",
            sent,
            static_cast<int>(packet.length()));
      }
//      else {
//        Serial.printf("Successfully sent %d bytes.\n", static_cast<int>(packet.length()));
//      }
    }
  }
}
