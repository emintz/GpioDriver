/*
 * InputType.h
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

#ifndef INPUTVALUETYPE_H_
#define INPUTVALUETYPE_H_

#include <functional>

/**
 * @brief Value types used in incoming data
 *
 */
enum class InputValueType {
  VALUE,         /**< VALUE, a data byte, not a delimiter */            /**< VALUE */
  START_MESSAGE, /**< START_MESSAGE `0xFF`, message start delimiter */  /**< START_MESSAGE */
  END_MESSAGE,   /**< END_MESSAGE   `0x7F`, message end delimiter */    /**< END_MESSAGE */
  UNKNOWN,       /**< UNKNOWN       unknown type, should never happen *//**< UNKNOWN */
};

#endif /* INPUTVALUETYPE_H_ */
