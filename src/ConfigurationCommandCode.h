/*
 * ConfigurationCommandCode.h
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

#ifndef CONFIGURATIONCOMMANDCODE_H_
#define CONFIGURATIONCOMMANDCODE_H_

/**
 * @brief Commands to configure the server.
 *
 * Note that commands run within a scope: Input, Output,
 * Input and Output, and Server-wide. Must match
 * declaration in `ConfigurationCommandCode.java`
 */
enum class ConfigurationCommandCode {
  RESET, /**< RESET Reset the server and refresh all inputs */
  OPEN,  /**< OPEN Open for input or output, depending on scope. */
  CLOSE, /**< CLOSE Close for input or output, depending on scope.. */
};

#endif /* CONFIGURATIONCOMMANDCODE_H_ */
