/*
 * Packet.cpp
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

#include "Packet.h"

#include "string.h"

Packet::Packet() {
  length_ = 0;
  memset(data_, 0, sizeof(data_));
}

Packet::Packet (uint8_t mutation) {
  memset(data_, 0, sizeof(data_));
  length_ = 1;
  data_[0] = mutation;
}

Packet::Packet(
      IOStatus status,
      StatusScope scope,
      gpio_num_t pin_number,
      uint8_t side_data) {
  length_ = 6;
  data_[0] = static_cast<uint8_t>(0xFF);
  data_[1] = static_cast<uint8_t>(status);
  data_[2] = static_cast<uint8_t>(scope);
  data_[3] = static_cast<uint8_t>(pin_number);
  data_[4] = side_data;
  data_[5] = static_cast<uint8_t>(0x7F);
}

 Packet::Packet(const Packet& copy_me) {
  this->length_ = copy_me.length_;
  memcpy(this->data_, copy_me.data_, sizeof(data_));
}

Packet& Packet::operator=(const Packet& assign_me) {
  this->length_ = assign_me.length_;
  memcpy(this->data_, assign_me.data_, sizeof(data_));
  return *this;
}
