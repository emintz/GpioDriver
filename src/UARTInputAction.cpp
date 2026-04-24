/*
 * UARTInputAction.cpp
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

#include "UARTInputAction.h"

void UARTInputAction::run(void) {
  uint8_t byte = 0;
  int count = 0;
  for (;;) {
    if (1 == uart_read_bytes(port_, &byte, 1, portMAX_DELAY)) {
//      count = (count + 1) % 32;
//      if (!count) {
//        Serial.println();
//      }
//      Serial.printf("%02x ", static_cast<int>(byte));
      input_queue_.send_message(&byte);
    }
  }
}
