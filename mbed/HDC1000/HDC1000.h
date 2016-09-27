#ifndef HDC1000_H


#include "mbed.h"
#include "typedef.h"

#define HDC1000_H
#define HDC1000_ADDRESS 0x80
#define HDC1000_DATA    0x00
#define HDC1000_CONFIG  0x02


class HDC1000 {
  public:
    HDC1000(PinName sda, PinName scl);
    HDC1000(I2C& pin_i2c);
    void initialize();
    void get_data();
    float temperature();
    float humidity();

  protected:
    I2C i2c;
    WORD_VALUE raw_humidity;
    WORD_VALUE raw_temperature;
    char buffer[4];
};

#endif
