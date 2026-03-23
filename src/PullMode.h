/*
 * PullMode.h
 *
 *  Created on: Mar 19, 2026
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

#ifndef PULLMODE_H_
#define PULLMODE_H_

  /**
   * Pullup/Pulldown resistor modes. Must match PullMode.java
   */
  enum class PullMode {
    UP,     /**< Pull the input to Vcc */
    DOWN,   /**< Pull the input to ground */
    BOTH,   /**< Both (but why?) */
    FLOAT,  /**< Neither pull up or pull down resistor */
  };

#endif /* PULLMODE_H_ */
