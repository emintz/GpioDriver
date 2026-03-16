/*
 * ESP32_S2_PIN.java
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
 * Available GPIO pins on the ESP32-S2. Reserved pins are not defined.
 * See <a h='https://randomnerdtutorials.com/esp32-pinout-reference-gpios/'>e.g.</a>
 * for details. Read-only pins are identified by name. Note that read-only
 * pins <em>DO NOT</em></em> support pull-up or pull-down resistors
 * and <em>MUST</em> be configured as not having any.
 */
public enum ESP32s2Pin implements GpioPinNumber {

  /**
   * Built-in LED
   */
  GPIO_2(2),
  GPIO_4(4),
  GPIO_5(5),
  /**
   * Strapping pin, must be low or floating at boot.
   */
  GPIO_12(12),
  GPIO_13(13),
  GPIO_14(14),
  GPIO_15(15),
  /**
   * <p>>Pin 16 is reserved for UART 2 RX.
   *
   * <p>Pin 17 is reserved for UART 2 TX.
   */
  GPIO_18(18),
  GPIO_19(19),
  GPIO_21(21),
  GPIO_22(22),
  GPIO_23(23),
  GPIO_25(25),
  GPIO_26(26),
  GPIO_27(27),
  GPIO_32(32),
  GPIO_33(33),
  GPIO_34_READ_ONLY(34),
  GPIO_35_READ_ONLY(35),
  GPIO_36_READ_ONLY(36),
  GPIO_39_READ_ONLY(39);

  public int getPinNumber() {
    return pinNumber;
  }

  private final int pinNumber;

  ESP32s2Pin(int pinNumber) {
    this.pinNumber = pinNumber;
  }

  @Override
  public byte number() {
    return (byte) pinNumber;
  }
}
