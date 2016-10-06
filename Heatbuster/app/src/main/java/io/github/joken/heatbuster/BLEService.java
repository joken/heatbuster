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
import android.os.RemoteException;

import com.orhanobut.hawk.Hawk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class BLEService extends Service {
	/** Key-Value方式のKeyあるいはRequest ID */
	public static final String REQUEST_BLUETOOTH = "request_bluetooth";//BTの有効化リクエストキー
	public static final String REQUEST_BLUETOOTH_ACTION = "request_bluetooth_action";//BTの有効化リクエストアクションToken
	public static final String BLE_DEVICE = "connection_devices";//接続対象デバイスリストのキー
	public static final String CLUB_INDEX = "club_index";//BT端末追加時の部活動指定キー
	public static final String JOINING_CLUB = "joining_club";//部活動追加時のlistキー
	public static final int BLE_ADD_DEVICE = 931;//BT端末追加のMessageID
	public static final int TOKEN_ADD = 514114;//Token追加のMessageID
	public static final int CLUBLIST_REQUEST = 45454545;//clubListを要求されたときのID
	public static final int JOIN_CLUB = 993;//部活動追加時のID

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
		Hawk.init(getApplicationContext()).build();
		token = Hawk.get("token");//Tokenをセット
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
					CheckBoxItem bleDeveice = new CheckBoxItem(gatt.getDevice().getName());
					byte[] raw_data = characteristic.getValue();
					byte[] templebyte0 = Arrays.copyOfRange(raw_data,1,3);
					byte[] templebyte1 = Arrays.copyOfRange(raw_data,4,5);
					byte[] templebyte = new byte[templebyte0.length+templebyte1.length];
					System.arraycopy(templebyte0,0,templebyte,0,templebyte0.length);
					System.arraycopy(templebyte1,0,templebyte,templebyte0.length,templebyte1.length);
					bleDeveice.setTemple(ByteBuffer.wrap(templebyte).order(ByteOrder.LITTLE_ENDIAN).getFloat());
					bleDeveice.setHumid(ByteBuffer.wrap(Arrays.copyOfRange(raw_data,6,10)).order(ByteOrder.LITTLE_ENDIAN).getFloat());
					bleDeveice.setEmer_flag((ByteBuffer.wrap(Arrays.copyOfRange(raw_data,3,4)).getInt() != 0));
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
				case CLUBLIST_REQUEST:
					Messenger reply = msg.replyTo;
					if(reply != null){
						try{
							reply.send(Message.obtain(null, 0, clubList));
						}catch(RemoteException e){
							e.printStackTrace();
						}
					}
					break;
				case JOIN_CLUB:
					clubList = (ArrayList<Clubmonitor>)msg.getData().getSerializable(JOINING_CLUB);
					break;
			}
		}

	}

}
