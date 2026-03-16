/*
 * InputPinHandler.cpp
 *
 *  Created on: Feb 15, 2026
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

#include "InputPinHandler.h"

InputPinHandler::InputPinHandler(
    PullQueueHT<uint8_t>& pin_change_queue,
    PullQueueHT<StatusMessage>& status_queue) :
    pin_change_queue_(pin_change_queue),
    status_queue_(status_queue),
    state_(InputPinHandler::State::STOPPED) {
}

InputPinHandler::~InputPinHandler() {
  end();
}

bool InputPinHandler::begin (void)  {
  bool result = true;
  if (InputPinHandler::State::RUNNING == state_) {
    Serial.println("Starting the GPIO change watcher.");
    result = gpio_change_service_.begin();
    Serial.println("GPIO change watcher is running.");
  }
  return result;
}

void InputPinHandler::end(void) {
  if (InputPinHandler::State::STOPPED != state_) {
    for (auto i = 0; i < watched_pins_.size(); ++i) {
      watched_pins_[i]->stop();
      watched_pins_[i].release();
    }
    gpio_change_service_.end();
    state_ = InputPinHandler::State::STOPPED;
  }
}

void InputPinHandler::open_pin(
    gpio_num_t pin_number,
    InputPinHandler::PullMode mode) {
  if (in_use(pin_number)) {
    send_status(pin_number, IOStatus::INVALID_STATE);
  } else {
    if (
        set_as_input(pin_number)
        && set_pull_mode(pin_number, mode)
        && start_pin_watch(pin_number)) {
      send_status(
          pin_number,
          IOStatus::OPEN_FOR_INPUT,
          static_cast<uint8_t>(gpio_get_level(pin_number)));
    }
  }
}

void InputPinHandler::pin_changed(gpio_num_t pin_number) {
  uint8_t result = gpio_get_level(pin_number) << 7 | pin_number;
  if (!pin_change_queue_.send_message_from_ISR(&result)) {
  } else {

  }
}

bool InputPinHandler::post_esp32_status(gpio_num_t pin_number, esp_err_t status) {
  bool result = true;
  switch (status) {
  case ESP_OK:
    break;
  case ESP_ERR_INVALID_ARG:
    result = false;
    send_status(pin_number, IOStatus::NO_SUCH_OUTPUT_PIN);
    break;
  default:
    result = false;
    send_status(pin_number, IOStatus::UNCATEGORIZED);
    break;
  }
  return result;
}

bool InputPinHandler::set_as_input(gpio_num_t pin_number) {
  return post_esp32_status(pin_number, gpio_set_direction(pin_number, GPIO_MODE_INPUT));
}

bool InputPinHandler::set_pull_mode(gpio_num_t pin_number, InputPinHandler::PullMode mode) {
  gpio_pull_mode_t esp32_pull_mode;
  switch (mode) {
  case InputPinHandler::PullMode::UP:
    esp32_pull_mode = GPIO_PULLUP_ONLY;
    break;
  case InputPinHandler::PullMode::DOWN:
    esp32_pull_mode = GPIO_PULLDOWN_ONLY;
    break;
  case InputPinHandler::PullMode::BOTH:
    esp32_pull_mode = GPIO_PULLUP_PULLDOWN;
    break;
  case InputPinHandler::PullMode::FLOAT:
    esp32_pull_mode = GPIO_FLOATING;
    break;
  }
  return post_esp32_status(
      pin_number,
      gpio_set_pull_mode(pin_number, esp32_pull_mode));
}

bool InputPinHandler::start_pin_watch(gpio_num_t pin_number) {
  watched_pins_.reserve(static_cast<size_t>(pin_number));  // TODO: fix this!
  watched_pins_[pin_number].release();
  // TODO: can we use std::make_unique() instead?
  watched_pins_[pin_number].reset(
      new InputPinHandler::InputPinManager(pin_number, *this));
  bool result = watched_pins_[pin_number]->start();
  if (!result) {
    send_status(pin_number, IOStatus::OUTPUT_OPEN_FAILED);
  }
  return result;
}

InputPinHandler::InputPinManager::InputPinManager(
    gpio_num_t pin_number, InputPinHandler& handler) :
      on_pin_change_(std::make_unique<OnPinChange>(pin_number, handler)),
      change_detector_(std::make_unique<GpioChangeDetector>(
          pin_number,
          GpioChangeType::ANY_CHANGE,
          on_pin_change_.get())) {
}
