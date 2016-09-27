package io.github.joken.heatbuster;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class BLEService extends Service {
	private Messenger mMessenger;
	private String token;

	public BLEService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		token = intent.getStringExtra("token");
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
