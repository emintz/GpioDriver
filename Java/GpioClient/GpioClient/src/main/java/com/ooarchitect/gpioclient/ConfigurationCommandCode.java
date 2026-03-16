/*
 * ConfigurationCommand.java
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
 * Commands to configure the server
 */
public enum ConfigurationCommand {
  /** Reset the server and refresh all inputs */
  RESET,
  /** Open a pin for input. */
  OPEN_FOR_INPUT,
  /** Open a pin for output. */
  CLOSE_INPUT_PIN,
  /** Open a pin for output. */
  OPEN_FOR_OUTPUT,
  /** Close an input pin. */
  CLOSE_FOR_INPUT,
}
