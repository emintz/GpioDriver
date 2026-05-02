/*
 * TestByteSupplier.java
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

import com.google.common.truth.Truth;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Validate {@link InputPinImpl}. Note that this does does not
 * validate the base class.
 *
 * @see OutputPinImplTest for {@link BaseIOPinImpl} validation.
 */
@ExtendWith(MockitoExtension.class)
class InputPinImplTest {

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private InputPinProxy<ESP32s2Pin> pinProxy;

  private InOrder inOrder;

  private InputPinImpl<ESP32s2Pin> pin;

  @BeforeEach
  public void beforeEachTest() {
    inOrder = Mockito.inOrder(pinProxy);
    pin = new InputPinImpl<>(pinProxy);
  }

  @Test
  public void valueIsHigh() {
    Mockito.doReturn(Level.HIGH).when(pinProxy).value();
    Truth.assertThat(pin.value()).isEqualTo(Level.HIGH);
    inOrder.verify(pinProxy).value();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void valueIsLow() {
    Mockito.doReturn(Level.LOW).when(pinProxy).value();
    Truth.assertThat(pin.value()).isEqualTo(Level.LOW);
    inOrder.verify(pinProxy).value();
    inOrder.verifyNoMoreInteractions();
  }
}
