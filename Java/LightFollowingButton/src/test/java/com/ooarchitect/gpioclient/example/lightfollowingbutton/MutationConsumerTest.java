package com.ooarchitect.gpioclient.example.lightfollowingbutton;/*
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

import com.ooarchitect.gpioclient.Level;
import com.ooarchitect.gpioclient.OutputPin;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Verifies {@link MutationConsumer}
 */
@ExtendWith(MockitoExtension.class)
class MutationConsumerTest {
  @Rule
  public MockitoRule initRule = MockitoJUnit.rule();

  @Mock
  private OutputPin ledPin;

  private MutationConsumer consumer;

  @BeforeEach
  void beforeEach() {
    consumer = new MutationConsumer(ledPin);
  }

  @Test
  void testConsumerLow() {
    consumer.accept(Level.LOW);
    Mockito.verify(ledPin).send(Level.LOW);
  }

  @Test
  void testConsumerHigh() {
    consumer.accept(Level.HIGH);
    Mockito.verify(ledPin).send(Level.HIGH);
  }
}
