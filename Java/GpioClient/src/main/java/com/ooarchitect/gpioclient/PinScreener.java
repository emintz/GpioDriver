

/*
 * PinFactory.java
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

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * Screens GPIO pin instances for availability. An instance is bound
 * to a pin proxy, then a pin is inserted. When the proxy state changes,
 * the screener either returns the pin (the open succeeded) or
 * {@code null} if the open failed.
 *
 * @param <P> the pin type.
 */
class PinScreener<P extends BaseIOPin>
    implements Consumer<PinState> {
  private final Semaphore semaphore;
  private boolean status;

  PinScreener() {
    semaphore = new Semaphore(0);
    status = false;
  }

  P awaitStatus(P pin) throws InterruptedException {
    semaphore.acquire();
    return status ? pin : null;
  }

  @Override
  public void accept(PinState pinState) {
    switch (pinState) {
      case INACTIVE:
        status = false;
        semaphore.release();
        break;
      case OPEN_PENDING:
        break;
      case ACTIVE:
        status = true;
        semaphore.release();
        break;
      case CLOSE_PENDING:
        break;
      case OFFLINE:
        status = false;
        semaphore.release();
        break;
      case RESET_PENDING:
        break;
    }
  }
}
