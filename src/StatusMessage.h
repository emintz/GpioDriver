/*
 * StatusMessage.h
 *
 *  Created on: Feb 15, 2026
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

#ifndef STATUSMESSAGE_H_
#define STATUSMESSAGE_H_

#include <stdint.h>

#include <driver/gpio.h>

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
  PIN_OFFLINE,          /**< PIN_OFFLINE The pin is unavailable (wedged) */
  UNSUPPORTED,          /**< UNSUPPORTED The pin does not support the requested function. */
  RESET_FAILED,         /**< Pin or server could not be reset. */
  RESET_SUCCEEDED,      /**< Pin or server reset succeeded. */
  INVALID_STATE,        /**< INVALID_STATE Internal driver failure */
};

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


/**
 * @brief Response to host request
 */
struct StatusMessage {
  IOStatus status_;        /**< Pin status */
  StatusScope scope_;      /**< Scope (applicability) */
  gpio_num_t pin_number_;  /**< The affected pin number */
  uint8_t side_data_;      /**< Status-specific data */
};

#endif /* STATUSMESSAGE_H_ */
