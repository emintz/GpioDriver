/*
 * CommandInputState.h
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

#ifndef COMMANDINPUTSTATE_H_
#define COMMANDINPUTSTATE_H_

enum class CommandInputState {
  CREATED,             /**< CREATED initial state */
  HAVE_VALUE,          /**< HAVE_VALUE have an output value */
  AT_START,            /**< AT_START received lead-in byte command message start*/
  AT_COMMAND,          /**< AT_COMMAND have the command byte*/
  AT_SCOPE,            /**< AT_SCOPE have the message scope (input, output, etc.) */
  AT_PIN_NO,           /**< AT_PIN_NO have the target pin number */
  AT_RESISTOR_CONFIG,  /**< AT_RESISTOR_CONFIG have a
                        * pullup/pulldown configuration */
  HAVE_COMMAND,        /**< HAVE COMMAND have a complete command to dispatch */
  RECOVERING,          /**< RECOVERING recovering from a command overrun. */
};

bool operator<(const CommandInputState lhs, const CommandInputState rhs) {
  return static_cast<unsigned int>(lhs) < static_cast<unsigned int>(rhs);
}

#endif /* COMMANDINPUTSTATE_H_ */
