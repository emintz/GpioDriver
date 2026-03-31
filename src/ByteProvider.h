/*
 * ByteProvider.h
 *
 *  Created on: Mar 24, 2026
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
#ifndef BYTEPROVIDER_H_
#define BYTEPROVIDER_H_

#include <stdint.h>

/**
 * @brief a source of `uint8_t` (i.e. byte) values.
 *
 * This class is a proxy for bytes read from the client.
 * It is useful for testing.
 */
class ByteProvider {
protected:
  ByteProvider() = default;

public:
  virtual ~ByteProvider() = default;

  virtual uint8_t operator() () = 0;
};

#endif /* BYTEPROVIDER_H_ */
