package io.github.joken.heatbuster;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BLEService extends Service {
	public static final String REQUEST_BLUETOOTH = "request_bluetooth";

	private Messenger mMessenger;
	private String token;
	private BluetoothAdapter mBluetoothAdapter;

	public BLEService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		token = intent.getStringExtra("token");//Tokenをセット

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
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startIntent.putExtra(REQUEST_BLUETOOTH, enableBTIntent);
			startActivity(enableBTIntent);
		}

		return START_STICKY;
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
		public void handleMessage(Message msg){}

	}
}
