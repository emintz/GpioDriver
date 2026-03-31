/*
 * StateTransitionTable.h
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

#ifndef STATETRANSITIONTABLE_H_
#define STATETRANSITIONTABLE_H_

#include <map>

/**
 * @brief a user configurable state transition table
 *
 * @tparam E event type
 * @tparam S state type
 */
template<class E, class S>
class StateTransitionTable {

  std::map<S, std::map<E, S>> table;

  const S default_state_;

public:
  StateTransitionTable(S default_state) :
    default_state_(default_state) {
  }

  ~StateTransitionTable() = default;

  /**
   * @brief add a transition to the table
   *
   * Specifies a transition. If the current state is
   * `from` and the event is `event`, then the the
   * enclosing state machine must transition into
   * `to`.
   *
   * @param from current state
   * @param event transition trigger
   * @param to resulting state
   * @return `*this` to support call chaining.
   */
  StateTransitionTable& add(S from, E event, S to) {
    if (!table.contains(from)) {
      table.emplace(from, std::map<E, S>());
    }
    table[from][event] = to;
    return *this;
  }

  S to(S current, E event) {
    return
        table.contains(current) && table[current].contains(event)
        ? table[current][event]
        : default_state_;
  }
};

#endif /* STATETRANSITIONTABLE_H_ */
