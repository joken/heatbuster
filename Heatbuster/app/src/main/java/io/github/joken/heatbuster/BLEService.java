package io.github.joken.heatbuster;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

public class BLEService extends Service {
	private Messenger mMessenger;

	public BLEService() {
	}

	@Override
	public void onCreate(){
		super.onCreate();
		mMessenger = new Messenger(new MessageHandler(this.getApplicationContext()));
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	static  class MessageHandler extends Handler{
		private Context mContext;

		public MessageHandler(Context ct){
			this.mContext = ct;
		}

	}
}
