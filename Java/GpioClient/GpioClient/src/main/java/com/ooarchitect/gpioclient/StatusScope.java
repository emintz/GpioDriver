/*
 * IODirectionCode.java
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
 * Input/Ouytput direction code
 */
public enum IODirectionCode {
  /** The server reads the pin level and sends it to the client. */
  INPUT,
  /** The client sends a desired voltage level to the server which sets the target pin to same. */
  OUTPUT,
  /** Direction is unknown or not applicable. For reply only; invalid in responses. */
  UNKNOWN,
}
