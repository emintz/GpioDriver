/*
 * CommandDispatcher.h
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

#ifndef COMMANDDISPATCHER_H_
#define COMMANDDISPATCHER_H_

#include "InputPinHandler.h"
#include "OutputPinHandler.h"

#include "TaskAction.h"

#include "ConfigurationCommandMessage.h"
#include "CommandInputState.h"
#include "InputValueType.h"
#include "StateTransitionTable.h"
#include "StatusReporter.h"
#include "StatusScope.h"

#include <stdint.h>
#include <PullQueueHT.h>

class CommandDispatcher :
    public TaskAction,
    public StatusReporter {
  PullQueueHT<uint8_t>& input_provider_;
  InputPinHandler& input_handler_;
  OutputPinHandler& output_handler_;
  StateTransitionTable<InputValueType, CommandInputState> transition_table_;

  CommandInputState state_;
  uint8_t command_byte_;
  uint8_t scope_byte_;
  uint8_t pin_byte_;
  uint8_t resistor_config_byte_;

  InputValueType type_of(uint8_t input);

  bool check_availability(StatusScope command_scope);

  void dispatch_command(void);

  void dispatch_to_input(void);

  void dispatch_to_output(void);

  void dispatch_to_server(void);

public:
  CommandDispatcher(
      PullQueueHT<StatusMessage> status_message_queue,
      PullQueueHT<uint8_t>& input_provider,
      InputPinHandler& input_handler,
      OutputPinHandler& output_handler);
  virtual ~CommandDispatcher() = default;

  virtual void run(void) override;
};

#endif /* COMMANDDISPATCHER_H_ */
