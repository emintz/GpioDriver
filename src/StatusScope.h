/*
 * StatusScope.h
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

#ifndef STATUSSCOPE_H_
#define STATUSSCOPE_H_

/**
 * @brief scope (i.e. applicability) of the status message. Must match
 *        declarations in `StatusScope.java`
 */
enum class StatusScope {
  INPUT_SCOPE,         /**< INPUT_SCOPE, applies to an input pin */           /**< INPUT_SCOPE */
  OUTPUT_SCOPE,        /**< OUTPUT_SCOPE, applies to an output pin */         /**< OUTPUT_SCOPE */
  INPUT_OUTPUT_SCOPE,  /**< INPUT_OUTPUT_SCOPE, applies to input and output *//**< INPUT_OUTPUT_SCOPE */
  SERVER_SCOPE,        /**< SERVER_SCOPE, applies server-wide */              /**< SERVER_SCOPE */
};

#endif /* STATUSSCOPE_H_ */
