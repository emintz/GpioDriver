/*
 * ConfigurationCommandMessage.cpp
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

#include "ConfigurationCommandMessage.h"

ConfigurationCommandMessage::ConfigurationCommandMessage (
    uint8_t command,
    uint8_t scope,
    uint8_t pin,
    uint8_t resistor_config) :
      command_(static_cast<ConfigurationCommandCode>(command)),
      scope_(static_cast<StatusScope>(scope)),
      pin_(static_cast<gpio_num_t>(pin)),
      resistor_config_(static_cast<PullMode>(resistor_config)) {
}

