/*
 * BaseIOPin.java
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
 * Base class for user-visible GPIO pin proxies.
 */
public class BaseIOPinImpl<T extends GpioPinProxy>
    implements BaseIOPin<T> {
  private final T proxy;

  protected BaseIOPinImpl(T proxy) {
    this.proxy = proxy;
  }

  @Override
  public boolean active() {
    return proxy.active();
  }

  @Override
  public void close() {
    proxy.close();
  }

  @Override
  public boolean offline() {
    return proxy.offline();
  }

  protected T proxy() {
    return proxy;
  }
}
