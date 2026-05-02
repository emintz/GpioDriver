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
 * Validate {@link OutputPinImpl} and {@link BaseIOPinImpl}
 */
@ExtendWith(MockitoExtension.class)
class OutputPinImplTest {

  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private OutputPinProxy<ESP32s2Pin> pinProxy;

  private InOrder inOrder;

  private OutputPinImpl<ESP32s2Pin> pin;

  @BeforeEach
  public void beforeEachTest() {
    inOrder = Mockito.inOrder(pinProxy);
    pin = new OutputPinImpl<>(pinProxy);
  }

  @Test
  void pinActive() {
    Mockito.doReturn(true).when(pinProxy).active();
    Truth.assertThat(pin.active()).isTrue();
    inOrder.verify(pinProxy).active();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void pinNotActive() {
    Mockito.doReturn(false).when(pinProxy).active();
    Truth.assertThat(pin.active()).isFalse();
    inOrder.verify(pinProxy).active();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void close() {
    Mockito.doReturn(true).when(pinProxy).close();
    pin.close();
    inOrder.verify(pinProxy).close();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void pinOffline() {
    Mockito.doReturn(true).when(pinProxy).offline();
    Truth.assertThat(pin.offline()).isTrue();
    inOrder.verify(pinProxy).offline();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void pinNotOffline() {
    Mockito.doReturn(false).when(pinProxy).offline();
    Truth.assertThat(pin.offline()).isFalse();
    inOrder.verify(pinProxy).offline();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void sendMutationsThenClose() {
    Mockito.doReturn(true).when(pinProxy).send(Level.LOW);
    Mockito.doReturn(true).when(pinProxy).send(Level.HIGH);
    Truth.assertThat(pin.send(Level.LOW)).isTrue();
    Truth.assertThat(pin.send(Level.HIGH)).isTrue();
    Truth.assertThat(pin.send(Level.LOW)).isTrue();
    Truth.assertThat(pin.send(Level.LOW)).isTrue();
    Truth.assertThat(pin.send(Level.HIGH)).isTrue();
    Truth.assertThat(pin.send(Level.LOW)).isTrue();
    Truth.assertThat(pin.send(Level.HIGH)).isTrue();
    Truth.assertThat(pin.send(Level.HIGH)).isTrue();
    Truth.assertThat(pin.send(Level.HIGH)).isTrue();
    Truth.assertThat(pin.send(Level.LOW)).isTrue();
    pin.close();

    inOrder.verify(pinProxy, Mockito.times(1)).send(Level.LOW);
    inOrder.verify(pinProxy, Mockito.times(1)).send(Level.HIGH);
    inOrder.verify(pinProxy, Mockito.times(2)).send(Level.LOW);
    inOrder.verify(pinProxy, Mockito.times(1)).send(Level.HIGH);
    inOrder.verify(pinProxy, Mockito.times(1)).send(Level.LOW);
    inOrder.verify(pinProxy, Mockito.times(3)).send(Level.HIGH);
    inOrder.verify(pinProxy, Mockito.times(1)).send(Level.LOW);
    inOrder.verify(pinProxy).close();
    inOrder.verifyNoMoreInteractions();
  }
}
