package com.ooarchitect.gpioclient;/*
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

import com.google.common.truth.Truth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Confirms {@link PinScreener}
 */
@ExtendWith(MockitoExtension.class)
public class PinScreenerTest {

  static class TestHarness implements Runnable {
    private final OutputPin rawPin;
    private OutputPin screenedPin;
    private PinScreener<OutputPin> screener;

    TestHarness(OutputPin rawPin) {
      this.rawPin = rawPin;
      screener = new PinScreener<>();
    }

    @Override
    public void run() {
      try {
        screenedPin = screener.awaitStatus(rawPin);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    PinScreener<OutputPin> getScreener() {
      return screener;
    }

    OutputPin getScreenedPin() {
      return screenedPin;
    }
  }

  @Mock
  private OutputPin outputPin;

  private TestHarness testHarness;

  private Thread harnessThread;

  @BeforeEach
  public void beforeEachTest() {
    testHarness = new TestHarness(outputPin);
    harnessThread = Thread.ofPlatform().start(testHarness);
  }

  @Test
  public void toActive() throws InterruptedException {
    testHarness.getScreener().accept(PinState.ACTIVE);
    harnessThread.join();
    Truth.assertThat(testHarness.getScreenedPin()).isSameInstanceAs(outputPin);
  }

  @Test
  public void toOffline() throws InterruptedException {
    testHarness.getScreener().accept(PinState.OFFLINE);
    harnessThread.join();
    Truth.assertThat(testHarness.getScreenedPin()).isNull();
  }

  @Test
  public void toInactive() throws InterruptedException {
    testHarness.getScreener().accept(PinState.INACTIVE);
    harnessThread.join();
    Truth.assertThat(testHarness.getScreenedPin()).isNull();
  }
}
