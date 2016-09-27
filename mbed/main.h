#include "mbed.h"
#include "HDC1000.h"
#include "BLE.h"

#define CONSOLE_DEBUG 1

#if CONSOLE_DEBUG
Serial pc(USBTX, USBRX);
#define DEBUG(...) {pc.printf(__VA_ARGS__);}

#else
#define DEBUG(...)

#endif

#define WAIT_TIME 1
#define WARNING_HEAT_LEVEL 33.0
#define DEVICE_NAME "afjio3"


void DataWrittenCallback(const GattWriteCallbackParams *params);
void DisconnectionCallBack(const Gap::DisconnectionCallbackParams_t *parameters);
void onConnectionCallBack(const Gap::ConnectionCallbackParams_t *parameters);
void PeriodicCallBack(void);
void UpdateEmergenceState(float temperature);
void UpdateServiceValue(void);
uint32_t Float2IEEE11073(float temperature);
float AmountOfWaterVapor(float temperature, float humidity);


BLEDevice ble;
HDC1000 health_value(I2C_SDA0, I2C_SCL0);
HDC1000 normal_value(I2C_SDA1, I2C_SCL1);
DigitalOut emergency(LED1);

/*
  flag[] is boolean variables for notify some states.
  flag[0]: heat emeagence state
*/
bool flag[1] = {false};
static volatile bool  sensor_polling = true;
uint8_t advertising_service_id[16] = {
  0x46, 0xfb, 0x9a, 0x69, 0xbb, 0xab, 0x1b, 0xac,
  0xa1, 0x4c, 0x5b, 0xdd, 0xbd, 0x0f, 0x18, 0x1d
};
uint8_t uuid_base[16] = {
  0x1d, 0x18, 0x0f, 0xbd, 0xdd, 0x5b, 0x4c, 0xa1,
  0xac, 0x1b, 0xab, 0xbb, 0x69, 0x9a, 0xfb, 0x46
};
uint8_t temperature_payload[10] = {};
GattCharacteristic characteristic_temperature(
  uuid_base,
  temperature_payload,
  10,
  10,
  GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY | GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_WRITE
);
GattCharacteristic *characteristic_heat_value[] = {
  &characteristic_temperature
};
GattService heat_value_service(
  uuid_base,
  characteristic_heat_value,
  sizeof(characteristic_heat_value) / sizeof(GattCharacteristic *)
);
static Gap::ConnectionParams_t connection_parameters;
