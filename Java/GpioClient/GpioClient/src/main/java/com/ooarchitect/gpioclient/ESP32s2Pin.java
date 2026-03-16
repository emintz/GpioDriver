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
 * for details.
 */
public enum ESP32s2Pin implements GpioPinNumber {
    /** Built-in LED */
    GPIO_2(2),
    GPIO_4(4),
    GPIO_5(5),
    /** Strapping pin, must be low or floating at boot. */
    GPIO_12(12),
    GPIO_13(13),
    GPIO_14(14),
    GPIO_15(15),
    /** Pin 16 is reserved for UART 2 RX  */
    /** Pin 17 is reserved for UART 2 TX */
    ;

    public int getPinNumber() {
        return pinNumber;
    }

    private final int pinNumber;

    ESP32s2Pin(int pinNumber) {
        this.pinNumber = pinNumber;
    }

    public int pinNumber() {
        return pinNumber;
    }
}
