package io.github.joken.bletest;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    // 10秒後にスキャンを停止
    private static final long SCAN_PERIOD = 10000;

    private final static int REQUEST_PERMISSIONS = 1;
    private final static int SDKVER_MARSHMALLOW = 23;

    private Handler mHandler = new Handler();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    boolean mScanning=false;
    private final String TAG = "hogehoge";
    private BluetoothGattCharacteristic mBluetoothCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Android6.0以降なら権限確認.
        if(Build.VERSION.SDK_INT >= SDKVER_MARSHMALLOW)
        {
            this.requestBlePermission();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    @TargetApi(SDKVER_MARSHMALLOW)
    private void requestBlePermission(){
        // 権限が許可されていない場合はリクエスト.
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
            },REQUEST_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 権限リクエストの結果を取得する.
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Succeed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onClickScan(View v) {
        // 10秒後にBLE機器のスキャンを開始します
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        }, SCAN_PERIOD);
    }

    /**
     * スキャン停止ボタンタップ時のコールバックメソッド
     * @param v
     */
    public void onClickStop(View v) {
        // BLE機器のスキャンを停止します
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    /*
    private void scanLeDevice(final boolean enable){
        if (enable) {
            //Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }*/

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            // スキャンできた端末の情報をログ出力
            // なおuuidはnullの模様(使わないからOK)
            ParcelUuid[] uuids = device.getUuids();
            String uuid = "";
            if (uuids != null) {
                for (ParcelUuid puuid : uuids) {
                    uuid += puuid.toString() + " ";
                }
            }
            String msg = "name=" + device.getName() + ", bondStatus="
                    + device.getBondState() + ", address="
                    + device.getAddress() + ", type" + device.getType()
                    + ", uuids=" + uuid;
            getApplicationContext();
            Log.d("BLEActivity", msg);
            mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.d("connect!!!!!!!!!!!!!!!",msg);
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //接続された
            if (newState == BluetoothProfile.STATE_CONNECTED){
                Log.i(TAG, "Connected to GATT server.");
                // サービスを検索する
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                // 切断された
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            Log.d(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "poyo!");
                //サービスのリストを取得
                BluetoothGattService service = gatt.getService(UUID.fromString("1d180fbd-dd5b-4ca1-ac1b-abbb699afb46"));
                //サービスからCharacteristicのリストを取得
                List<BluetoothGattCharacteristic> charastic = service.getCharacteristics();
                //CharacteristicにNotificationの受信要求を設定

                for (BluetoothGattCharacteristic characteristic : charastic) {
                    Log.d(TAG, "ここまではきたきたきた");
                    mBluetoothCharacteristic = characteristic;
                }

                gatt.setCharacteristicNotification(mBluetoothCharacteristic, true);
                List<BluetoothGattDescriptor> descriptors = mBluetoothCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor descriptor:descriptors) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Characteristicの読込成功
                byte[] read_data = characteristic.getValue();
                Log.i(TAG, "data = "  + "温度[1]" +read_data[1]+"温度[2]" +read_data[2]+"emergency"+read_data[3]+"指数部byte"+read_data[4]
                        +"水蒸気量データ[6]"+read_data[6]+"水蒸気量データ[7]"+read_data[7]+"水蒸気量データ[8]"+read_data[8]+"指数部byte"+read_data[9]);
                Log.i(TAG, "data = "  +read_data[1]+"-" +read_data[2]+"-"+read_data[3]+"-"+read_data[4]
                        +"-"+read_data[6]+"-"+read_data[7]+"-"+read_data[8]+"-"+read_data[9]);
            }
        }

        //Notification/Indicateの受信コールバック
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            Log.d(TAG, "onCharacteristicChanged");
            // Characteristicの値更新通知
            byte[] raw_data = characteristic.getValue();
            Log.i(TAG, "data = "  + "温度[1]" +raw_data[1]+"温度[2]" +raw_data[2]+"emergency"+raw_data[3]+"指数部byte"+raw_data[4]
            +"水蒸気量データ[6]"+raw_data[6]+"水蒸気量データ[7]"+raw_data[7]+"水蒸気量データ[8]"+raw_data[8]+"指数部byte"+raw_data[9]);
            Log.i(TAG,""+raw_data[2]);
            Log.i(TAG,""+raw_data[1]);
            String piyo = ""+raw_data[2]+raw_data[1];
            Log.i(TAG,String.valueOf(Integer.parseInt(piyo,16)));
            byte[] templebyte0 = Arrays.copyOfRange(raw_data,1,3);
            byte[] templebyte1 = Arrays.copyOfRange(raw_data,4,5);
            byte[] templebyte = new byte[templebyte0.length+templebyte1.length];
            System.arraycopy(templebyte0,0,templebyte,0,templebyte0.length);
            System.arraycopy(templebyte1,0,templebyte,templebyte0.length,templebyte1.length);
            try {
                Log.i("温度",Float.toString(ByteBuffer.wrap(templebyte).order(ByteOrder.LITTLE_ENDIAN).getFloat()));
                Log.i("湿度",Float.toString(ByteBuffer.wrap(Arrays.copyOfRange(raw_data,6,10)).order(ByteOrder.LITTLE_ENDIAN).getFloat()));
                Log.i("Emergency",Boolean.toString(ByteBuffer.wrap(Arrays.copyOfRange(raw_data,3,4)).getInt() != 0));
            }catch (Exception e) {
                e.printStackTrace();
            }

        }

    };

    public void onClickBreak(View v){
        mBluetoothGatt.readCharacteristic(mBluetoothCharacteristic);
        Log.d(TAG,"ここまでは来ました");
    }
}
