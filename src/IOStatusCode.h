/*
 * IOStatusCode.h
 *
 *  Created on: Apr 12, 2026
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

#ifndef IOSTATUSCODE_H_
#define IOSTATUSCODE_H_

/**
 * @brief Pin I/O operation status codes. Must match declarations
 *        in `IOStatusCode.java`
 */
enum class IOStatus {
  OK,                   /**< OK Everything is fine. */
  CLOSE_SUCCEEDED,      /**< Pin successfully closed. */
  CLOSE_FAILED,         /**< Pin could not be closed. */
  OPEN_SUCCEEDED,       /**< Pin opened successfully I/O given by scope. */
  OPEN_FAILED,          /**< OPEN_FAILED pin could not be opened. */
  NO_SUCH_PIN,          /**< NO_SUCH_PIN Non-existant pin number */
  LOST_INPUT,           /**< LOST_INPUT Input has been lost */
  PIN_IN_USE,           /**< PIN_IN_USE Cannot open because pin is being used. */
  PIN_NOT_ACTIVE,       /**< PIN_NOT_ACTIVE Cannot close because pin is not active. */
  PIN_OFFLINE,          /**< PIN_OFFLINE The pin is unavailable (wedged) */
  UNSUPPORTED,          /**< UNSUPPORTED The pin does not support the requested function. */
  RESET_FAILED,         /**< Pin or server could not be reset. */
  RESET_SUCCEEDED,      /**< Pin or server reset succeeded. */
  INVALID_STATE,        /**< INVALID_STATE Internal driver failure */
};

#endif /* IOSTATUSCODE_H_ */
