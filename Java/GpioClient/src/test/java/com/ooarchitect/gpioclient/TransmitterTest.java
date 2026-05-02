package com.ooarchitect.gpioclient;/*
 * TestByteSupplierTest.java
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

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Validate the {@link Transmitter}
 */
@ExtendWith(MockitoExtension.class)
public class TransmitterTest {

  @Mock
  private SerialPort serialPort;

  private InOrder inOrder;

  private Transmitter<ESP32s2Pin> transmitter;

  @BeforeEach
  public void beforeEachTest() {
    transmitter = new Transmitter<>(serialPort);
    inOrder = Mockito.inOrder(serialPort);
  }

  @Test
  public void transmitMutation() {
    transmitter.sendMutation(ESP32s2Pin.GPIO_5, 1);
    transmitter.sendMutation(ESP32s2Pin.GPIO_2, 0);
    byte[] firstTransmission = {(byte) 0x85};
    inOrder.verify(serialPort).writeBytes(firstTransmission, 1, 0);
    byte[] secondTransmission = {(byte) 0x02};
    inOrder.verify(serialPort).writeBytes(secondTransmission, 1, 0);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void transmitCommand() {
    transmitter.sendCommand(
        ConfigurationCommandCode.OPEN,
        StatusScope.INPUT,
        ESP32s2Pin.GPIO_5,
        PullMode.UP);
    byte[] expectedFirstTransmission = {
        (byte) 0XFF,
        (byte) ConfigurationCommandCode.OPEN.ordinal(),
        (byte) StatusScope.INPUT.ordinal(),
        ESP32s2Pin.GPIO_5.number(),
        (byte) PullMode.UP.ordinal(),
        (byte) 0x7F};
    transmitter.sendCommand(
        ConfigurationCommandCode.RESET,
        StatusScope.INPUT_OUTPUT,
        ESP32s2Pin.GPIO_2,
        PullMode.FLOAT);

    byte[] expectedSecondTransmission = {
        (byte) 0xFF,
        (byte) ConfigurationCommandCode.RESET.ordinal(),
        (byte) StatusScope.INPUT_OUTPUT.ordinal(),
               ESP32s2Pin.GPIO_2.number(),
        (byte) PullMode.FLOAT.ordinal(),
        (byte) 0x7F};

    inOrder.verify(serialPort).writeBytes(expectedFirstTransmission, 6, 0);
    inOrder.verify(serialPort).writeBytes(expectedSecondTransmission, 6, 0);
    inOrder.verifyNoMoreInteractions();
  }
}
