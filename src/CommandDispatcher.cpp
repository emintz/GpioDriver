/*
 * CommandDispatcher.cpp
 *
 *  Created on: Mar 24, 2026
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

#include "CommandDispatcher.h"

CommandDispatcher::CommandDispatcher (
    PullQueueHT<Packet>& packet_queue,
    PullQueueHT<uint8_t>& input_provider,
    InputPinHandler& input_handler,
    OutputPinHandler& output_handler) :
        StatusReporter(packet_queue),
        input_provider_(input_provider),
        input_handler_(input_handler),
        output_handler_(output_handler),
        transition_table_(CommandInputState::RECOVERING),
        state_(CommandInputState::CREATED),
        command_byte_(0),
        scope_byte_(0),
        pin_byte_(0),
        resistor_config_byte_(0) {
  transition_table_
      .add(CommandInputState::CREATED, InputValueType::VALUE, CommandInputState::HAVE_VALUE)
      .add(CommandInputState::CREATED, InputValueType::START_MESSAGE, CommandInputState::AT_START)

      .add(CommandInputState::HAVE_VALUE, InputValueType::VALUE, CommandInputState::HAVE_VALUE)
      .add(CommandInputState::HAVE_VALUE, InputValueType::START_MESSAGE, CommandInputState::AT_START)

      .add(CommandInputState::AT_START, InputValueType::VALUE, CommandInputState::AT_COMMAND)

      .add(CommandInputState::AT_COMMAND, InputValueType::VALUE, CommandInputState::AT_SCOPE)

      .add(CommandInputState::AT_SCOPE, InputValueType::VALUE, CommandInputState::AT_PIN_NO)

      .add(CommandInputState::AT_PIN_NO, InputValueType::VALUE, CommandInputState::AT_RESISTOR_CONFIG)
      .add(CommandInputState::AT_RESISTOR_CONFIG, InputValueType::END_MESSAGE, CommandInputState::HAVE_COMMAND)

      .add(CommandInputState::HAVE_COMMAND, InputValueType::VALUE, CommandInputState::HAVE_VALUE)
      .add(CommandInputState::HAVE_COMMAND, InputValueType::START_MESSAGE, CommandInputState::AT_START)
      ;
}

InputValueType CommandDispatcher::type_of(uint8_t input) {
  InputValueType input_type = InputValueType::UNKNOWN;
  switch (input) {
  case (uint8_t) 0xFF:
    input_type = InputValueType::START_MESSAGE;
    break;
  case (uint8_t) 0x7F:
    input_type = InputValueType::END_MESSAGE;
    break;
  default:
    input_type = InputValueType::VALUE;
    break;
  }

  return input_type;
}

void CommandDispatcher::dispatch_command(void) {
  Serial.printf("Dispatching Command: %02x scope: %02x pin: %02x, resistor config: %02x\n",
      command_byte_,
      scope_byte_,
      pin_byte_,
      resistor_config_byte_);
  switch (static_cast<StatusScope>(scope_byte_)) {
  case StatusScope::INPUT_SCOPE:
    dispatch_to_input();
    break;
  case StatusScope::INPUT_OUTPUT_SCOPE:
    dispatch_to_input();
    dispatch_to_output();
    break;
  case StatusScope::OUTPUT_SCOPE:
    dispatch_to_output();
    break;
  case StatusScope::SERVER_SCOPE:
    dispatch_to_server();
    break;
  }
}

bool CommandDispatcher::check_availability(
    StatusScope command_scope) {
  gpio_num_t pin = static_cast<gpio_num_t>(pin_byte_);
  auto is_available =
      input_handler_.available(pin)
      && output_handler_.available(pin);
  if (!is_available) {
    send_status(
        IOStatus::PIN_IN_USE,
        command_scope,
        pin);
  }
  return is_available;
}

void CommandDispatcher::dispatch_to_input(void) {
  gpio_num_t pin = static_cast<gpio_num_t>(pin_byte_);
  switch (static_cast<ConfigurationCommandCode>(command_byte_)) {
  case ConfigurationCommandCode::CLOSE:
    input_handler_.close_pin(pin);
    break;
  case ConfigurationCommandCode::OPEN:
    if (check_availability(StatusScope::INPUT_SCOPE)) {
      input_handler_.open_pin(pin, static_cast<PullMode>(resistor_config_byte_));
    }
    break;
  case ConfigurationCommandCode::RESET:
    input_handler_.reset(pin);
    break;
  }
}

void CommandDispatcher::dispatch_to_output(void) {
  gpio_num_t pin = static_cast<gpio_num_t>(pin_byte_);
  switch (static_cast<ConfigurationCommandCode>(command_byte_)) {
  case ConfigurationCommandCode::CLOSE:
    output_handler_.close(pin);
    break;
  case ConfigurationCommandCode::OPEN:
    if (check_availability(StatusScope::OUTPUT_SCOPE)) {
      output_handler_.open_pin(pin);
    }
    break;
  case ConfigurationCommandCode::RESET:
    output_handler_.reset(pin);
    break;
  }
}

void CommandDispatcher::dispatch_to_server(void) {
  // TODO: implement. What da heck should we do here?
}

void CommandDispatcher::run(void) {
  uint8_t input = 0;
  for (;;) {
    input_provider_.pull_message(&input);
    auto input_type = type_of(input);
    state_ = transition_table_.to(state_, input_type);
    switch(state_) {
    case CommandInputState::CREATED:
      // Should never happen
      break;
    case CommandInputState::HAVE_VALUE:
      output_handler_.mutate(input);
      break;
    case CommandInputState::AT_START:
      break;
    case CommandInputState::AT_COMMAND:
      command_byte_ = input;
      break;
    case CommandInputState::AT_SCOPE:
      scope_byte_ = input;
      break;
    case CommandInputState::AT_PIN_NO:
      pin_byte_ = input;
      break;
    case CommandInputState::AT_RESISTOR_CONFIG:
      resistor_config_byte_ = input;
      break;
    case CommandInputState::HAVE_COMMAND:
      dispatch_command();
      break;
    case CommandInputState::RECOVERING:
      // Nothing to do. Let input run out until we find
      // the start of a message.
      break;
    }
  }
}

