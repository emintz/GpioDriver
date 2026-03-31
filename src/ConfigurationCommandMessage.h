/*
 * ConfigurationCommandMessage.h
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

#ifndef CONFIGURATIONCOMMANDMESSAGE_H_
#define CONFIGURATIONCOMMANDMESSAGE_H_

/**
 * @brief Command that orders the server to configure a GPIO pin
 *
 * Must match `ConfigurationCommandMessage.java`
 */

#include "ConfigurationCommandCode.h"
#include "PullMode.h"
#include "StatusScope.h"

#include <driver/gpio.h>
#include <stdint.h>

/**
 * @brief deserialized configuration command
 *
 * A command to configure a GPIO pin. The wire format
 * is
 *
 * 1. `0xFF`, signaling message start.
 * 2. The action to take.
 * 3. Specifies where to apply the command, e.g.
 *    to an input pin, an output pin, etc.
 * 4. The physical GPIO pin number
 * 5. Pullup/Pulldown resistor configuration. Set to
 *    `FLOATING` when N/A.
 * 6. `0x7F`, signaling message end.
 */
class ConfigurationCommandMessage {
  ConfigurationCommandCode command_;
  StatusScope scope_;
  gpio_num_t pin_;
  PullMode resistor_config_;

public:
  ConfigurationCommandMessage (
      uint8_t command,
      uint8_t scope,
      uint8_t pin,
      uint8_t resistor_config);
  virtual ~ConfigurationCommandMessage () = default;

  ConfigurationCommandCode command() const {
    return command_;
  }

  gpio_num_t pin() const {
    return pin_;
  }

  PullMode resistor_config() const {
    return resistor_config_;
  }

  StatusScope scope() const {
    return scope_;
  }


};

#endif /* CONFIGURATIONCOMMANDMESSAGE_H_ */
