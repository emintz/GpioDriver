/*
 * BaseIOPin.cpp
 *
 *  Created on: Mar 21, 2026
 *      Author: Eric Mintz
 *
 * Copyright (c) 2026, Eric Mintz
 * All Rights Reserved
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <https://www.gnu.org/licenses/>.
 */

#include "BaseIOPin.h"

BaseIOPin::BaseIOPin (
    const gpio_num_t pin_number,
    const StatusScope scope,
    PullQueueHT<Packet>& packet_queue) :
        StatusReporter(packet_queue),
        pin_number_(pin_number),
        scope_(scope),
        state_(PinState::CLOSED) {
}

bool BaseIOPin::configure_pull_mode(PullMode mode) {
  gpio_pull_mode_t esp32_pull_mode;
  switch (mode) {
  case PullMode::UP:
    esp32_pull_mode = GPIO_PULLUP_ONLY;
    break;
  case PullMode::DOWN:
    esp32_pull_mode = GPIO_PULLDOWN_ONLY;
    break;
  case PullMode::BOTH:
    esp32_pull_mode = GPIO_PULLUP_PULLDOWN;
    break;
  case PullMode::FLOAT:
    esp32_pull_mode = GPIO_FLOATING;
    break;
  }
  return send_esp_status(
      gpio_set_pull_mode(pin_number_, esp32_pull_mode),
      0);
}

bool BaseIOPin::send_esp_status(
    esp_err_t esp_status,
    uint8_t side_data) {
  auto result = ESP_OK == esp_status;
  switch (esp_status) {
  case ESP_OK:
    // All is well. There is nothing to do.
    break;
  case ESP_ERR_INVALID_ARG:
      send_pin_status(IOStatus::NO_SUCH_PIN, 0);
      break;
  default:
    send_pin_status(IOStatus::INVALID_STATE, 0);
  }
  return result;
}

bool BaseIOPin::set_direction(gpio_mode_t direction) {
  return send_esp_status(
      gpio_set_direction(
          pin_number_,
          direction),
      0);
}

bool BaseIOPin::close(void) {
  bool result = !in_use();
  if (result) {
    result = send_esp_status(
        gpio_reset_pin(pin_number_),
        0);
    state_ = result ? PinState::CLOSED : PinState::OFFLINE;
  } else {
    send_pin_status(IOStatus::PIN_NOT_ACTIVE, 0);
  }
  return result;
}

bool BaseIOPin::reset(void) {
  auto result = send_esp_status(
      gpio_reset_pin(pin_number_),
      0);
  state_ = result ? PinState::CLOSED : PinState::OFFLINE;
  return result;
}
