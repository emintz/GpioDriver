/*
 * PinConfigStattus.java
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
 * Completion status of a pin configuration command.
 *
 * @param <T>            logical pin enumeration for the server's ESP-32
 *                       board
 * @param status         operation status: success or reason for failure
 * @param directionCode  data flow direction (input, output, or N/A)
 * @param gpioPin        logical GPIO pin
 * @param side_data      command-specific data
 */
public record PinConfigStatusMessage<T extends Enum<? extends GpioPinNumber>>(
    PinConfigStatusCode status,
    StatusScope directionCode,
    T gpioPin,
    byte side_data) {
}
