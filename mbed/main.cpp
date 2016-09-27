#include "main.h"
#include <math.h>


int main(void) {

  //Device initializing
  Ticker Ticker;
  Ticker.attach(PeriodicCallBack, WAIT_TIME);
  emergency = 0;
  DEBUG("Initialising the nRF51822...\r\n");
  ble.init();
  DEBUG("Init is done.\r\n");
  
  //Event handles
  ble.gap().onDisconnection(DisconnectionCallBack);
  ble.gap().onConnection(onConnectionCallBack);
  ble.onDataWritten(DataWrittenCallback);
  ble.gap().getPreferredConnectionParams(&connection_parameters);

  //Advertising packets initializing
  ble.gap().accumulateAdvertisingPayload(
    GapAdvertisingData::BREDR_NOT_SUPPORTED |
    GapAdvertisingData::LE_GENERAL_DISCOVERABLE
  );
  ble.gap().setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED);
  ble.gap().accumulateAdvertisingPayload(
    GapAdvertisingData::COMPLETE_LIST_128BIT_SERVICE_IDS,
    (uint8_t*)advertising_service_id,
    sizeof(advertising_service_id)
  );
  ble.gap().accumulateAdvertisingPayload(
    GapAdvertisingData::COMPLETE_LOCAL_NAME,
    (const uint8_t *)DEVICE_NAME,
    sizeof(DEVICE_NAME)
  );
  ble.gap().setAdvertisingInterval(160); //Advertising interval : 100[ms]
  
  ble.gap().startAdvertising();
  DEBUG("Start Advertising.\r\n");

  ble.gattServer().addService(heat_value_service);
  DEBUG("Add Service.\r\n");

//Main loop
  while (true) {
    if (sensor_polling == true) {
      sensor_polling = false;
      UpdateServiceValue();
    } else {
      ble.waitForEvent();
    }
  }

  return 0;
}

//Callback function when data is written from central device
void DataWrittenCallback(const GattWriteCallbackParams *parameters) {
    
  emergency = 0;
  flag[0] = false;
  temperature_payload[3] = 0;
  DEBUG("Flag was recieved. The emergence mode is just exiting.\r\n");
}

//Callback function when disconnect from central device
void DisconnectionCallBack(const Gap::DisconnectionCallbackParams_t *parameters) {

  DEBUG("Disconnected! handle: %u, reason: %u\r\n", parameters->handle, parameters->reason);
  DEBUG("Restarting the advertising process...\r\n");
  ble.gap().startAdvertising();
}

//Callback function when connect to central device
void onConnectionCallBack(const Gap::ConnectionCallbackParams_t *parameters) {

  DEBUG("Connected. handle: %u.\r\n", parameters->handle);
  connection_parameters.slaveLatency = 1;
  if (ble.gap().updateConnectionParams(parameters->handle, &connection_parameters) != BLE_ERROR_NONE) {
    DEBUG("Failed to update connection paramter.\r\n");
  }
}

//Callback function during margin of events
void PeriodicCallBack(void) {

  sensor_polling = true;
}

//Function of updating the emergence flag to true
void UpdateEmergenceState(float temperature) {

  if(temperature >= WARNING_HEAT_LEVEL)
    flag[0] = true;
  if(flag[0] == true) {
    emergency = 1;
    temperature_payload[3] = 1;
    DEBUG("Warning!! Enter the Emergence mode!\r\n");
  }
}

//Function of sending data to central device
void UpdateServiceValue(void) {

  float health_temperature = health_value.temperature();
  float health_humidity = health_value.humidity();
  float normal_temperature = normal_value.temperature();
  float normal_humidity = normal_value.humidity();
  float health_water_vapor = AmountOfWaterVapor(health_temperature, health_humidity);
  float normal_water_vapor = AmountOfWaterVapor(normal_temperature, normal_humidity);
  float sweat_value = health_water_vapor - normal_water_vapor;
  DEBUG("Temperature:%f[c]\r\n", health_temperature);
  uint32_t temp_ieee11073 = Float2IEEE11073(health_temperature);
  uint32_t sweat_ieee11073 = Float2IEEE11073(sweat_value);
  memcpy(temperature_payload + 1, &temp_ieee11073, 4);
  memcpy(temperature_payload + 6, &sweat_ieee11073, 4);
  UpdateEmergenceState(health_temperature);
  int i;
  for(i = 0; i < 10; i++)
    DEBUG("%x ", temperature_payload[i]);
  ble.gattServer().write(
    characteristic_temperature.getValueAttribute().getHandle(),
    temperature_payload,
    sizeof(temperature_payload)
  );
}

//Function of convert to IEEE11703 from float
uint32_t Float2IEEE11073(float value) {

  uint8_t exponent = 0xFF;
  uint32_t mantissa = (uint32_t)(value * 10);
  return (((uint32_t)exponent) << 24) | mantissa;
}

float AmountOfWaterVapor(float temperature, float humidity) {
  float e, a;
  e = 6.11 * powf(10, ((7.5 * temperature) / (temperature + 237.3)));
  a = 217 * e / (temperature + 273.15) * humidity / 100;
  return a;
}
