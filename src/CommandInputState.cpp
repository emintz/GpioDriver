/*
 * CommandInputState.cpp
 *
 *  Created on: Mar 31, 2026
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

#include "CommandInputState.h"

/**
 * Strictly less than operator for `CommandInputState` values. Invocations
 * evaluate the expression `lhs < rhs`. The invocation compares instance
 * cardinality, i.e. its value when cast to an `unsigned int`. This
 * function provides the ordering required for `CommandInputState` values
 * to be keys in a `std::map`.
 *
 * @param lhs left hand side of the comparison
 * @param rhs right hand side of the comparison
 * @return `true` if the left hand side cardinality is strictly less
 *         than that of the right hand side, `false` otherwise. Note that
 *         comparing equal values produces `false`.
 */
bool operator<(const CommandInputState lhs, const CommandInputState rhs) {
  return static_cast<unsigned int>(lhs) < static_cast<unsigned int>(rhs);
}
