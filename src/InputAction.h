/*
 * InputAction.h
 *
 *  Created on: Feb 16, 2026
 *      Author: Eric Mintz
 *
 * Task action that receives input event and status messages and
 * takes appropriate action. The production version will forward
 * the data to the host.
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

#ifndef INPUTACTION_H_
#define INPUTACTION_H_

#include "OutputPinHandler.h"

#include <PullQueueHT.h>
#include <TaskAction.h>
#include <stdint.h>

//-----------------------------------------------------------
// Debugging support. Delete before putting in production. //
//-----------------------------------------------------------

/**
 * @brief Input activity processor: marshals status and value changes
 *        and reports them to the host.
 *
 * Allows stand-alone running by echoing input pin levels to
 * corresponding output pins. Disable this for production.
 */
class InputAction : public TaskAction {
  PullQueueHT<Packet>& packet_queue_;
  OutputPinHandler& output_handler_;

public:
  /**
   * Creates an `InputAction` instance
   *
   * @param pin_change_queue carries input pin value change
   *                         notifications.
   * @param packet_queue carries input pin status change
   *                     notifications
   */
  InputAction (
      PullQueueHT<Packet>& packet_queue,
      OutputPinHandler& output_handler);
  virtual ~InputAction ();

  /**
   * Receives pin change and status notifications and forwards them
   * to the host.
   */
  virtual void run(void) override;
};

#endif /* INPUTACTION_H_ */
