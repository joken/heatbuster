package io.github.joken.heatbuster;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
	public static final int DEVICELIST_REQUEST = 1270;//デバイスリスト要求時のID
	public static final int GET_DEVICE = 3423;

	private static final int SERVER_CONNECT_DELAY = 10000;//鯖と通信する頻度(msec)
	private static final int BLE_CONNECT_DELAY = 10000;//BLE端末と通信する頻度(msec)
	public static final String BLE_THREAD = "BLE-Thread";//スレッドにつく名前(Tag)
	private static final String SERVICE_UUID = "1d180fbd-dd5b-4ca1-ac1b-abbb699afb46";

	private Messenger mMessenger;//メッセンジャー
	private Handler mBackGroundHandler;//Timer用 runnableをpost(送信)するブツ
	private HandlerThread mHandlerThread;//runnableをrun(実行)するブツ 別スレッド実行
	private static String token;//LoginToken
	private static ArrayList<Clubmonitor> clubList;//監視リスト(Group)
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
	private BluetoothGattDescriptor mDescritor;
	private BluetoothGattCallback mGattCallback;

	public BLEService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Hawk.init(getApplicationContext()).build();
		token = Hawk.get("token");//Tokenをセット
		if(Hawk.get("clublist") == null){
			clubList = new ArrayList<>();
		}else{
			clubList = Hawk.get("clublist");
		}

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

		startBLEGatt();//BLEのconnect操作Task(Background)

		StartUpLoadData();//ServerへのUploadTask(
		DownloadatServer();//ServerへのDownloadTask(Background)

		return START_STICKY;
	}

	private void initGattCallBack(){
		mGattCallback = new BluetoothGattCallback() {
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				super.onConnectionStateChange(gatt, status, newState);
				if (newState == BluetoothProfile.STATE_CONNECTED){
					//接続に成功したらサービスを検索にする
					gatt.discoverServices();
				}else if(newState==BluetoothProfile.STATE_DISCONNECTED){
					//接続が切れたらGATTを空にする
					mGattCallback=null;
				}
			}

			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status){
				// サービスが見つかったら実行.
				if (status == BluetoothGatt.GATT_SUCCESS) {
					// UUIDが同じかどうかを確認する.
					BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
					if (service != null) {
						// 指定したUUIDを持つキャラクタリスティックを確認する.
						for (BluetoothGattCharacteristic characteristic:service.getCharacteristics()){
							mBluetoothGattCharacteristic = characteristic;
						}

						if (mBluetoothGattCharacteristic != null) {
							// キャラクタリスティックが見つかったら、Notificationをリクエスト.
							boolean registered = gatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true);

							for (BluetoothGattDescriptor descriptor:mBluetoothGattCharacteristic.getDescriptors()){
								mDescritor = descriptor;
							}
							mDescritor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
							gatt.writeDescriptor(mDescritor);
						}
					}
				}
			}

			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
				CheckBoxItem bleDeviceData = new CheckBoxItem(gatt.getDevice().getName());
				byte[] raw_data = characteristic.getValue();
				Float temple = ((float)Integer.parseInt(""+raw_data[2]+raw_data[1],16))*0.1f;
				Float wid = ((float) Integer.parseInt(""+raw_data[8]+raw_data[7]+raw_data[6],16))*0.1f;
				Boolean emer = (Integer.parseInt(""+raw_data[3],16)==1);
				bleDeviceData.setTemple(temple);
				bleDeviceData.setHumid(wid);
				bleDeviceData.setEmer_flag(emer);

				for(Clubmonitor monitor : clubList){
					for(CheckBoxItem item : monitor.getDeviceList()){
						if(item.getSerial().equals(bleDeviceData.getSerial())){//bleDeviceDataのある場所をさがす
							clubList.get(clubList.indexOf(monitor)).getDeviceList()//該当clubのデバイスリスト
									.set(monitor.getDeviceList().indexOf(item), bleDeviceData);//該当デバイスのDataを置換
						}
					}
				}
			}
		};
	}

	private void startBLEGatt(){
		mBackGroundHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(clubList.isEmpty())return;
				for(Clubmonitor monitor : clubList){
					if(monitor.getDeviceList() == null)return;
					for(CheckBoxItem item : monitor.getDeviceList()){
						item.getDevice().connectGatt(getApplicationContext(), true, mGattCallback).connect();
					}
				}
			}
		}, BLE_CONNECT_DELAY);
	}

	/**
	 * Serverに対して一定期間ごとにclublist全体の情報を送信する
	 * */
	private void StartUpLoadData(){
		mBackGroundHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for(Clubmonitor monitor : clubList){
					if(monitor.getDeviceList() == null)return;
					UploadtoServer(monitor);
				}
			}
		}, SERVER_CONNECT_DELAY);
	}

	/**
	 * serverに対してクラブごとに全てのBLEの情報をおくる
	 * メインスレッドで実行しないこと!!!(ネットワーク通信が発生します)
	 * */
	private void UploadtoServer(final Clubmonitor club){
		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		OkHttpClient client = new OkHttpClient();
		String url = "http://mofutech.net:4545/group/"+club.getGid()+"/mod/update";
		RequestBody body = 	RequestBody.create(JSON,MakingJson(club.getDeviceList()));
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		try {
			Response response = client.newCall(request).execute();
			response.body().string();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**1部活のデバイスリストを受け取ると送信用のJSONを吐く機械**/
	private String MakingJson(ArrayList<CheckBoxItem> devicelist){
		// jsonデータの作成
		JSONObject jsonOneData;

		try {
			jsonOneData = new JSONObject();
			jsonOneData.put("token", Hawk.get("token"));

			JSONArray itemArray = new JSONArray();
			for (CheckBoxItem conDevice : devicelist) {
				jsonOneData = new JSONObject();
				jsonOneData.put("mac", conDevice.getSerial());
				jsonOneData.put("temp", conDevice.getTemple());
				jsonOneData.put("wet", conDevice.getHumid());
				jsonOneData.put("stat", conDevice.getStat());
				itemArray.put(jsonOneData);
			}
			jsonOneData.put("elements",itemArray);

		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
		return jsonOneData.toString();
	}

	/**serverから定期的に部活ごとに平均温度と湿度、STATを受け取ってClublistのClubmonitorそれぞれに入れ込む処理**/
	private void DownloadatServer(){
		mBackGroundHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (Clubmonitor club: clubList) {
					OkHttpClient client = new OkHttpClient();
					String url = "http://mofutech.net:4545/group/" + club.getGid() + "/mod/status?token=" + Hawk.get("token");
					try {
						Request request = new Request.Builder()
								.url(url)
								.build();

						Response response = client.newCall(request).execute();
						String responseJson = response.body().string();
						Log.i("結果",responseJson);
						JsonInputtoClub(club, responseJson);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, SERVER_CONNECT_DELAY);
	}
	/**1部活ごとに平均温度と湿度、STATを受け取って引数の部活に入れ込んであげる処理**/
	private void JsonInputtoClub(Clubmonitor club,String jsondata){
		try {
			JSONObject item = new JSONObject(jsondata);
			if (item.getString("success").equals("true")){
				JSONObject item2 = item.getJSONObject("result");
				club.setClubTemp(Float.valueOf(item2.getString("temp")));
				club.setTempIncreaseRate(Float.valueOf(item2.getString("wet")));
				switch (item2.getString("stat")){
					case "EMER":
						club.setSelfStatus(TemperatureStatus.Emergency);
						Intent i = new Intent(getApplicationContext(),WorningActivity.class);
						i.putExtra("CLUBNAME",club.getName());
						startActivity(i);
						break;
					case "WARN":
						club.setSelfStatus(TemperatureStatus.Warning);
						break;
					case "SAFE":
						club.setSelfStatus(TemperatureStatus.Safe);
						break;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * clubのGIDに紐付いたデバイスリストを返す。
	 * @param gid 探したいclubのgid
	 * @return gidに該当するclubのデバイスリスト。該当するものがなければnull
	 * */
	private ArrayList<CheckBoxItem> getDevicesByGID(String gid){
		for(Clubmonitor monitor : clubList){
			if(monitor.getGid().equals(gid)){
				return monitor.getDeviceList();
			}
		}
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		mMessenger = new Messenger(new MessageHandler(this.getApplicationContext()));
		initGattCallBack();
		mHandlerThread = new HandlerThread(BLE_THREAD);
		mHandlerThread.start();//startしておかないとgetLooper()でnullが返る。
		mBackGroundHandler = new Handler(mHandlerThread.getLooper());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Hawk.put("clublist", clubList);
		mHandlerThread.quit();
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
					ArrayList<CheckBoxItem> devices = (ArrayList<CheckBoxItem>)msg.getData().getSerializable(BLE_DEVICE);
					clubList.get(msg.getData().getInt(CLUB_INDEX)).setDeviceList(devices);
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
					Messenger reply1 = msg.replyTo;
					if (reply1 != null){
						try{
							reply1.send(Message.obtain(null,0));
						}catch (RemoteException e){
							e.printStackTrace();
						}
					}
					break;
			}
		}

	}

}
