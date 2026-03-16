# GpioDriver
The GPIO Server that connects to masters via serial USB. The
server supports digital I/O, pins can be high or low.
Please refer to the ESP32
[documentation}(https://docs.espressif.com/projects/esp-idf/en/latest/esp32/get-started/)
for details.

## Features
The GPIO Server allows users to configure pin states, write to
output pins, and receive data from input pins.

The Server presents itself as a USB serial device. Depending on
the ESP32 board, it could present as `/dev/ttyUSBx`, where `x`
is 0, 1, ... . Please Mac and Windows documentation for their
specific details.





