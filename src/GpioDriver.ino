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
#include <Arduino.h>

#include "CommandDispatcher.h"
#include "ConfigurationCommandCode.h"
#include "InputAction.h"
#include "InputPinHandler.h"
#include "IOStatusCode.h"
#include "OutputPinHandler.h"
#include "PinAssignments.h"
#include "PullMode.h"
#include "StatusScope.h"
#include "UARTDriver.h"

#include <driver/uart.h>
#include <GpioChangeDetector.h>
#include <PullQueueHT.h>
#include <TaskWithActionH.h>

#define COMMAND_START 0xFF
#define COMMAND_END 0x7F

static HardwareGpioChangeService gpio_change_service;
static PullQueueHT<uint8_t> input_queue(1024);
static PullQueueHT<Packet> packet_queue(128);
static OutputPinHandler output_pin_handler(packet_queue);
static InputAction input_action(
    packet_queue,
    output_pin_handler);
static InputPinHandler input_pin_handler(
    packet_queue);
static TaskWithActionH input_processor(
    "Input",
    10,
    &input_action,
    4096);
static CommandDispatcher command_dispatcher(
    packet_queue,
    input_queue,
    input_pin_handler,
    output_pin_handler);
static TaskWithActionH dispatcher_task(
    "Dispatcher",
    11,
    &command_dispatcher,
    4096);

/**
 * Write a pass/fail status message
 *
 * @param message message to write
 * @param status true indicates success, false failure
 * @return status, for chaining.
 */
static bool report_boolean_status(
    const char *message,
    bool status) {
  Serial.printf("%s %s.\n", message, status ? "succeeded" : "failed");
  return status;
}

/**
 * Blink (raise and lower) the specified pin.
 *
 * @param pin gpio pin to blink
 */
static void blink_it(uint8_t pin) {
  digitalWrite(pin, HIGH);
  delay(100);
  digitalWrite(pin, LOW);
}

/**
 * Ripple (i.e. blink each once) the LEDs.
 */
static void ripple_once(void) {
  blink_it(BUILTIN_LED_PIN);
  blink_it(RED_LED_PIN);
  blink_it(YELLOW_LED_PIN);
  blink_it(GREEN_LED_PIN);
}

/**
 * Display ripple pattern
 *
 * @param count number of times to display the pattern.
 */
static void ripple(int count) {
  for (int i = 0; i < count; ++i) {
    ripple_once();
  }
}

/**
 * Sends a command to the server. Debug only.
 *
 * @param command_code what to do
 * @param scope_code in what scope, INPUT, OUTPUT, etc.
 * @param pin_number_code target GPIO pin or 0 if N/A.
 * @param resistor_configuration_code pullup/pulldown configuration, or
 *        PullMode.FLOAT if N/A.
 */
static void send_command(
    ConfigurationCommandCode command_code,
    StatusScope scope_code,
    gpio_num_t pin_number_code,
    PullMode resistor_configuration_code) {
  uint8_t lead_in(COMMAND_START);
  uint8_t command_value = static_cast<uint8_t>(command_code);
  uint8_t scope_value = static_cast<uint8_t>(scope_code);
  uint8_t pin_number_value = static_cast<uint8_t>(pin_number_code);
  uint8_t resistor_configuration_value =
      static_cast<uint8_t>(resistor_configuration_code);
  uint8_t lead_out(COMMAND_END);

  input_queue.send_message(&lead_in);
  input_queue.send_message(&command_value);
  input_queue.send_message(&scope_value);
  input_queue.send_message(&pin_number_value);
  input_queue.send_message(&resistor_configuration_value);
  input_queue.send_message(&lead_out);
}

/**
 * Send commands to open the output (LED) pins. Debug only.
 */
static void open_pins_with_commands(void) {
  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::INPUT_SCOPE,
    static_cast<gpio_num_t>(BUILTIN_PUSH_BUTTON_PIN),
    PullMode::UP);
  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::INPUT_SCOPE,
    static_cast<gpio_num_t>(RED_PUSH_BUTTON_PIN),
    PullMode::UP);
  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::INPUT_SCOPE,
    static_cast<gpio_num_t>(YELLOW_PUSH_BUTTON_PIN),
    PullMode::UP);
  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::INPUT_SCOPE,
    static_cast<gpio_num_t>(GREEN_PUSH_BUTTON_PIN),
    PullMode::UP);

  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::OUTPUT_SCOPE,
    static_cast<gpio_num_t>(BUILTIN_LED_PIN),
    PullMode::FLOAT);
  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::OUTPUT_SCOPE,
    static_cast<gpio_num_t>(RED_LED_PIN),
    PullMode::FLOAT);
  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::OUTPUT_SCOPE,
    static_cast<gpio_num_t>(YELLOW_LED_PIN),
    PullMode::FLOAT);
  send_command(
    ConfigurationCommandCode::OPEN,
    StatusScope::OUTPUT_SCOPE,
    static_cast<gpio_num_t>(GREEN_LED_PIN),
    PullMode::FLOAT);

    delay(1);
}

/**
 * Print a status message indicating a UART driver's installation
 * status.
 *
 * @param port UART port.
 */
static void check_uart_driver(uart_port_t port) {
  Serial.printf(
      "UART port %d driver %s installed.\n",
      static_cast<int>(port),
      uart_is_driver_installed(port) ? "is" : "is not");
}

void setup() {
  Serial.begin(115200);
  Serial.printf("GPIO Driver compiled on %s at %s.\n",
      __DATE__, __TIME__);

  check_uart_driver(UART_NUM_0);
  check_uart_driver(UART_NUM_1);
  Serial.printf("Hardware FIFO buffer length for port 1 is: %d.\n",
      UART_HW_FIFO_LEN(UART_NUM_1));

  report_boolean_status(
      "Installation of UART driver for port 1",
      UARTDriver::install_unbuffered(UART_NUM_1));
  check_uart_driver(UART_NUM_1);

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
      "Input queue (carries data from server) startup",
      input_queue.begin());
  report_boolean_status(
      "GPIO value change monitoring",
      gpio_change_service.begin());
  report_boolean_status(
      "Status reporting queue startup",
      packet_queue.begin());

  report_boolean_status(
      "Input processor task startup",
      input_processor.start());
  report_boolean_status(
      "Command dispatcher task startup",
      dispatcher_task.start());

  Serial.printf("The GREEN LED control pin is %s.\n",
      input_pin_handler.in_use(static_cast<gpio_num_t>(GREEN_PUSH_BUTTON_PIN))
      ? "in use" : "available");

  digitalWrite(BUILTIN_LED_PIN, digitalRead(BUILTIN_PUSH_BUTTON_PIN));
  digitalWrite(RED_LED_PIN, digitalRead(RED_PUSH_BUTTON_PIN));
  digitalWrite(YELLOW_LED_PIN, digitalRead(YELLOW_PUSH_BUTTON_PIN));
  digitalWrite(GREEN_LED_PIN, digitalRead(GREEN_PUSH_BUTTON_PIN));

  Serial.println("Opening GPIO pins.");
  open_pins_with_commands();
}

void loop() {
  vTaskDelay(portMAX_DELAY);
}
