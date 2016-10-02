package io.github.joken.heatbuster;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import java.util.ArrayList;

public class BLEService extends Service {
	/** Key-Value方式のKeyあるいはRequest ID */
	public static final String REQUEST_BLUETOOTH = "request_bluetooth";//BTの有効化リクエストキー
	public static final String REQUEST_BLUETOOTH_ACTION = "request_bluetooth_action";//BTの有効化リクエストアクションToken
	public static final String BLE_DEVICE = "connection_devices";//接続対象デバイスリストのキー
	public static final String CLUB_INDEX = "club_index";//BT端末追加時の部活動指定キー
	public static final int BLE_ADD_DEVICE = 931;//BT端末追加のMessageID
	public static final int TOKEN_ADD = 514114;//Token追加のMessageID

	private Messenger mMessenger;//メッセンジャー
	private static String token;//LoginToken
	private static ArrayList<Clubmonitor> clubList;//監視リスト
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGattCallback mGattCallback;

	public BLEService() {
		initGattCallBack();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		token = intent.getStringExtra("token");//Tokenをセット
		clubList = new ArrayList<>();

		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Ensures Bluetooth is available on the device and it is enabled. If not,
		// displays a dialog requesting user permission to enable Bluetooth.
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			//MainActivityにBluetooth有効化を促す
			Intent startIntent = new Intent();
			startIntent.setAction(REQUEST_BLUETOOTH_ACTION);
			startIntent.putExtra(REQUEST_BLUETOOTH, enableBTIntent);
			sendBroadcast(startIntent);
		}

		startBLEGatt();

		return START_STICKY;
	}

	private void initGattCallBack(){
		mGattCallback = new BluetoothGattCallback() {
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				super.onConnectionStateChange(gatt, status, newState);
			}

			@Override
			public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
				if(status == BluetoothGatt.GATT_SUCCESS){
					byte[] raw_data = characteristic.getValue();
					//TODO パースして結果をなにかに格納する
				}
			}
		};
	}

	private void startBLEGatt(){
		//TODO 別スレッドで監視する
	}

	@Override
	public void onCreate(){
		super.onCreate();
		mMessenger = new Messenger(new MessageHandler(this.getApplicationContext()));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return this.mMessenger.getBinder();
	}

	static class MessageHandler extends Handler{
		private Context mContext;

		public MessageHandler(Context ct){
			this.mContext = ct;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg){
			switch (msg.what){
				case BLE_ADD_DEVICE:
					clubList.get(msg.getData().getInt(CLUB_INDEX)).setDeviceList((ArrayList<CheckBoxItem>)msg.getData().getSerializable(BLE_DEVICE));
					break;
				case TOKEN_ADD:
					token = (String)msg.obj;
					break;
			}
		}

	}

}
