#include "HDC1000.h"


HDC1000::HDC1000(PinName sda, PinName scl) : i2c(sda, scl) {
  initialize();
}

HDC1000::HDC1000(I2C& pin_i2c) : i2c(pin_i2c) {
  initialize();
}

// Set configuration
void HDC1000::initialize() {
  wait_ms(15);
  buffer[0] = HDC1000_CONFIG;
  buffer[1] = 0x10;
  buffer[2] = 0x00;
  i2c.write(HDC1000_ADDRESS, buffer, 3);
}

// get data
void HDC1000::get_data() {
  buffer[0] = HDC1000_DATA;
  i2c.write(HDC1000_ADDRESS, buffer, 1);
  wait_ms(20);
  i2c.read( HDC1000_ADDRESS, buffer, 4);
}

// get temperature from data
float HDC1000::temperature() {
  get_data();
  raw_temperature.byte.HB=buffer[0];
  raw_temperature.byte.LB=buffer[1];
  return ((((float)raw_temperature.word_value / 65536) * 165) - 40);
}

// get humidity from data
float HDC1000::humidity() {
  get_data();
  raw_humidity.byte.HB=buffer[2];
  raw_humidity.byte.LB=buffer[3];
  return (((float)raw_humidity.word_value / 65536) * 100);
}
