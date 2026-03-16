/*
 * GpioDriver.ino
 *
 *  Created on: Feb 13, 2026
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
#include "Arduino.h"

#include "InputAction.h"
#include "InputPinHandler.h"
#include "PinAssignments.h"
#include "StatusMessage.h"

#include <GpioChangeDetector.h>
#include <PullQueueHT.h>
#include <TaskWithActionH.h>

static HardwareGpioChangeService gpio_change_service;
static PullQueueHT<uint8_t> pin_change_queue(1024);
static PullQueueHT<StatusMessage> status_queue(128);
static InputAction input_action(
    pin_change_queue,
    status_queue);
static InputPinHandler input_pin_handler(
    pin_change_queue,
    status_queue);
static TaskWithActionH input_processor(
    "Input",
    10,
    &input_action,
    4096);

static bool report_boolean_status(
    const char *message,
    bool status) {
  Serial.printf("%s %s.\n", message, status ? "succeeded" : "failed");
  return status;
}

static void blink_it(uint8_t pin) {
  digitalWrite(pin, HIGH);
  delay(100);
  digitalWrite(pin, LOW);
}

static void ripple_once(void) {
  blink_it(BUILTIN_LED_PIN);
  blink_it(RED_LED_PIN);
  blink_it(YELLOW_LED_PIN);
  blink_it(GREEN_LED_PIN);
}

static void ripple(int count) {
  for (int i = 0; i < count; ++i) {
    ripple_once();
  }
}

void setup() {
  Serial.begin(115200);
  Serial.printf("GPIO Driver compiled on %s at %s.\n",
      __DATE__, __TIME__);

  pinMode(BUILTIN_LED_PIN, OUTPUT);
  pinMode(RED_LED_PIN, OUTPUT);
  pinMode(YELLOW_LED_PIN, OUTPUT);
  pinMode(GREEN_LED_PIN, OUTPUT);

  digitalWrite(BUILTIN_LED_PIN, LOW);
  digitalWrite(RED_LED_PIN, LOW);
  digitalWrite(YELLOW_LED_PIN, LOW);
  digitalWrite(GREEN_LED_PIN, LOW);

  pinMode(BUILTIN_PUSH_BUTTON_PIN, INPUT_PULLUP);
  pinMode(RED_PUSH_BUTTON_PIN, INPUT_PULLUP);
  pinMode(YELLOW_PUSH_BUTTON_PIN, INPUT_PULLUP);
  pinMode(GREEN_PUSH_BUTTON_PIN, INPUT_PULLUP);

  ripple(10);

  report_boolean_status(
      "GPIO value change monitoring",
      gpio_change_service.begin());
  report_boolean_status(
      "Pin change queue startup", pin_change_queue.begin());
  report_boolean_status(
      "Status reporting queue startup", status_queue.begin());
  report_boolean_status(
      "Input pin handler startup",
      input_pin_handler.begin());
  report_boolean_status(
      "Input processor task",
      input_processor.start());

  Serial.printf("The GREEN LED control pin is %s.\n",
      input_pin_handler.in_use(static_cast<gpio_num_t>(GREEN_PUSH_BUTTON_PIN))
      ? "in use" : "available");

  digitalWrite(BUILTIN_LED_PIN, digitalRead(BUILTIN_PUSH_BUTTON_PIN));
  digitalWrite(RED_LED_PIN, digitalRead(RED_PUSH_BUTTON_PIN));
  digitalWrite(YELLOW_LED_PIN, digitalRead(YELLOW_PUSH_BUTTON_PIN));
  digitalWrite(GREEN_LED_PIN, digitalRead(GREEN_PUSH_BUTTON_PIN));

  input_pin_handler.open_pin(
      static_cast<gpio_num_t>(BUILTIN_PUSH_BUTTON_PIN),
      InputPinHandler::PullMode::UP);
  input_pin_handler.open_pin(
      static_cast<gpio_num_t>(RED_PUSH_BUTTON_PIN),
      InputPinHandler::PullMode::UP);
  input_pin_handler.open_pin(
      static_cast<gpio_num_t>(YELLOW_PUSH_BUTTON_PIN),
      InputPinHandler::PullMode::UP);
  input_pin_handler.open_pin(
      static_cast<gpio_num_t>(GREEN_PUSH_BUTTON_PIN),
      InputPinHandler::PullMode::UP);

  delay(1);
}

void loop() {
  vTaskDelay(portMAX_DELAY);
}
