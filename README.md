# heatbuster
## ファイル構成
Heatbuster/mbed   
|-mbed(ソースコード)   
| |-HDC1000   
| | |-HDC1000.h(HDC1000ライブらるのヘッダファイル)   
| | |-HDC1000.cpp(HDC1000ライブラリのソースファイル)   
| | |-typedef.h(型名置換ヘッダファイル)   
| |-main.cpp(本プログラムのソースファイル)   
| |-main.h(本プログラムのヘッダファイル)   
|-Heatbuster_TY51822r3.hex(mbed用実行ファイル)   
|-README.md(本ファイル)   
   

## main.cpp概要
### define
WAIT_TIME: 情報更新間隔(秒)   
WARNING_HEAT_LEVEL:emergencyとする体温(°C)　　　
DEVICE_NAME:半角英数6文字までの任意値   
   
### 関数定義
DataWrittenCalBack:データ書込要求(emergency取下要求)が来た場合の実行コード   
DisconnectionCallBack:Centralとの通信が不能になった場合の実行コード
onConnectionCallBack:Centralとの通信が可能になった場合の実行コード   
PeriodicCallBack:WAIT_TIME毎に実行されるコード   
UpdateEmergencyState:emergency状態に関する実行コード   
UpdateServiceValue:各値を更新するコード   
Float2IEEE11073:符号付少数からリトルエンディアン符号付浮動小数点数への変換コード   
AmountOfWaterVapor:相対湿度から水蒸気量(g/m^3)を求めるコード   
   
### 変数定義
health_value:体温センサ   
normal_Value:気温センサ   
emergency:危険報告LED   
power:特になし   
flag[0]:emergency報告用フラグ   
advertising_service_id:advertising用service UUID   
uuid_base:送出UUID   
temperature_payload /   
characteristic_temperature /   
characteristic_heat_value /   
heat_value_service /   
connection_parameters:BLEセットアップ   