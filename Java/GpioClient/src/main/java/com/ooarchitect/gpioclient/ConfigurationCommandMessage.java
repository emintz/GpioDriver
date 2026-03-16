/*
 * ConfigurationMessage.java
 *
 * Copyright (C) 2026  Eric Mintz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ooarchitect.gpioclient;

/**
 * Message that orders the server to configure a pin.
 *
 * @param command               how to configure the pin
 * @param ioDirection           input or output
 * @param pinNumber             the target pin (logical value)
 * @param resistorConfiguration pullup/pulldown resistor configuration
 */
record ConfigurationCommandMessage<T extends Enum<? extends GpioPinNumber>>(
    ConfigurationCommandCode command,
    StatusScope ioDirection,
    GpioPinNumber pinNumber,
    PinPullMode resistorConfiguration) {
}
