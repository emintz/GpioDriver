/*
 * StatusReporter.h
 *
 *  Created on: Mar 19, 2026
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

#ifndef STATUSREPORTER_H_
#define STATUSREPORTER_H_

#include "StatusMessage.h"

#include <PullQueueHT.h>

/**
 * Mix-in that enqueues operation status reports for
 * transmission to the client
 */
class StatusReporter {
  PullQueueHT<StatusMessage>& status_message_queue_;

protected:
  StatusReporter(PullQueueHT<StatusMessage>& status_message_queue) :
    status_message_queue_(status_message_queue) {
  }

public:
  virtual ~StatusReporter() = default;

protected:

  /**
   * Enqueue a status message for a pin
   *
   * @param pin_number the reporting pin
   * @param status the pin status
   * @param side_data status-dependent data, zero if none.
   */
  void send_status(
      IOStatus status,
      StatusScope scope,
      gpio_num_t pin_number,
      uint8_t side_data = 0) {
    StatusMessage message = {
        .status_ = status,
        .scope_ = scope,
        .pin_number_ = pin_number,
        .side_data_ = side_data,
    };

    status_message_queue_.send_message(&message);
  }

  void send_status_from_ISR(
      IOStatus status,
      StatusScope scope,
      gpio_num_t pin_number,
      uint8_t side_data = 0) {
    StatusMessage message = {
        .status_ = status,
        .scope_ = scope,
        .pin_number_ = pin_number,
        .side_data_ = side_data,
    };
    status_message_queue_.send_message_from_ISR(&message);
  }
};


#endif /* STATUSREPORTER_H_ */
