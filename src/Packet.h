/*
 * Packet.h
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

#ifndef PACKET_H_
#define PACKET_H_

#include <stdint.h>

#include <driver/gpio.h>

enum class IOStatus;
enum class StatusScope;

/**
 * @brief Serialized data ready for transmission to the client.
 *
 * A packet contains a single unit of work for the client to process,
 * e.g. a pin value change.
 */
class Packet {
  uint8_t length_;
  uint8_t data_[6];

public:

  /**
   * @brief Constructs an empty `Packet`. Note that `length()` will
   *        return 0.
   *
   */
  Packet();

  /**
   * @brief Constructs a packet containing the specified `mutation`.
   *
   * Note that this constructor is invoked only when an input
   * pin level changes.
   *
   * @param mutation pin level and pin number in mutation format.
   */
  Packet (uint8_t mutation);

  /**
   * @brief Writes an operation status to the bus.
   *
   * Note that this method sets the wire format of an operation
   * status. The wire format is the following byte sequence :
   *
   * 1. `0xFF`, the lead-in code
   * 2. Status code.
   * 3. I/O scope
   * 4. Physical GPIO number
   * 5. Status-specific side data. Must be 0 if N/A.
   * 6. `0x7F`, lead-out
   *
   * @param status status code, such as in indication that a pin
   *        was opened successfully
   * @param scope how the message pertains to the server, e.g. input,
   *        output, server-level, etc.
   * @param pin_number the physical pin number being reported, or 0
   *        if N/A
   * @param side_data status-specific side data. Must be 0 if N/A.
   */
  Packet(
      IOStatus status,
      StatusScope scope,
      gpio_num_t pin_number,
      uint8_t side_data = 0);
  Packet(const Packet& copy_me);

  Packet& operator=(const Packet& assign_me);

  virtual ~Packet () = default;

  /**
   * @return the data length in bytes. Only bytes
   *         `[0 .. length())` are guaranteed to be valid.
   */
  uint8_t length(void) const {
    return length_;
  }

  /**
   * @return the data to be serialized on the USRT.
   */
  const uint8_t *data(void) const {
    return data_;
  }
};

#endif /* PACKET_H_ */
