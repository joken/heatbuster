package io.github.joken.heatbuster;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ServiceConnection,DialogTemplate.Callback{

	@BindView(R.id.clublistView)
	ListView clublistView;
	@BindView(R.id.mainactivity_layout_parent)
	CoordinatorLayout mainAcitivity_View_Parent;

	/** 部活動リスト */
	private ClubmonitorAdapter clubAdapter;
	/** 権限チェックまたはStartActivityForResult使用後にリクエストが自分のものであったか確認する定数(一意であればOK) */
	private static final int BLE_LOCATION_REQUEST_CODE = 9999;//位置情報権限リクエストID
	public static final int PAIRING_REQUEST_CODE = 1919;//BT端末追加ActivityへのリクエストID
	public static final int JOIN_REQUEST_CODE = 2525;//部活動追加ActivityへのリクエストID
	public static final int LOGIN_REQUEST_CODE = 893;//LoginリクエストID
	public static final int BLUETOOTH_ENABLE_REQUEST_CODE = 4949;//BT有効化のリクエストID
	/** ユーザーのToken */
	private String mToken;
	/** コンテキストメニュー生成時に選択された部活動のIndex */
	private int currentClub;
	/** BLEServiceとの通信時に使用 */
	private Messenger mMessenger,replyMessenger;
	private static ArrayList<Clubmonitor> BLEService_ClubMonitor;
	private static Boolean mustDownBLEClubs=false;
	private static Boolean musthogehoge=false;
	private static Boolean MyDialogSucceed=false;
	private static final int VIEW_UPDATE_DELAY = 5000;//clublistの要求間隔(msec)

	private MainActivity activity=this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		startBLEService();
		//Hawk起動
		Hawk.init(this.getApplicationContext()).build();
		//token取得
		getToken();

		//部活動リストを初期化
		initClubListView();

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplication(), JoinclubActivity.class);
				startActivityForResult(intent, JOIN_REQUEST_CODE);
			}
		});

		//BLEの諸々とした確認
		checkBLE();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(this);
	}

	private void initClubListView() {
		BLEService_ClubMonitor = new ArrayList<>();
		clubAdapter = new ClubmonitorAdapter(MainActivity.this, BLEService_ClubMonitor);

		clublistView.setAdapter(clubAdapter);
		registerForContextMenu(clublistView);
		viewUpdate();
	}

	Handler viewUpdatehandler = new Handler();
	public void viewUpdate(){
		viewUpdatehandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if( mustDownBLEClubs){sendClubListRequest(); mustDownBLEClubs=false; musthogehoge=true;}
				if(musthogehoge){sendClubListRequest();}
				if (!(BLEService_ClubMonitor.isEmpty())) {
					for (Clubmonitor club : BLEService_ClubMonitor) {
						if (club.getSelfStatus() == TemperatureStatus.Emergency){
							Intent intent = new Intent(getApplication(), WorningActivity.class);
							intent.putExtra("CLUBNAME", clubAdapter.clubmonitorsList.get(currentClub).getName());
						}
						UpdateClubView(club);
					}
				}
				viewUpdatehandler.postDelayed(this, VIEW_UPDATE_DELAY);
			}
		}, 100);
	}

	private void getToken() {
		mToken = Hawk.get("token");
		if(mToken == null){
			Intent loginIntent = new Intent(getApplication(), LoginActivity.class);
			startActivityForResult(loginIntent, LOGIN_REQUEST_CODE);
		}else{
			bindBLEService();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainactivity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_logout:
				logout();
				break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listview1_context, menu);
		//クリックインデックス取得
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		this.currentClub = info.position;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.connect:
				Intent intent = new Intent(getApplication(), PairingActivity.class);
				intent.putExtra("GID", clubAdapter.clubmonitorsList.get(currentClub).getGid());
				startActivityForResult(intent, PAIRING_REQUEST_CODE);
				return true;
			case R.id.reverce_one:
				Intent intent1 = new Intent(getApplication(), RemoveAnyDevice.class);
				intent1.putExtra("GID", clubAdapter.clubmonitorsList.get(currentClub).getGid());
				return true;
			case R.id.reverce_all:
				new DialogTemplate.Builder(activity)
						.message(clubAdapter.clubmonitorsList.get(currentClub).getName()+"の部員の全ての監視を解除してもよいですか。")
						.positive("OK")
						.negative("キャンセル")
						.show();
				if (MyDialogSucceed){
					Message message = Message.obtain(null, BLEService.BLE_ADD_DEVICE, new ArrayList<CheckBoxItem>());
					replyMessenger = new Messenger(new Handler());
					message.replyTo = replyMessenger;
					sendMessage(message);
				}
				return true;
			case R.id.delete:
				new DialogTemplate.Builder(activity)
						.message(clubAdapter.clubmonitorsList.get(currentClub).getName()+"の監視を停止してもいいですか?")
						.positive("OK")
						.negative("キャンセル")
						.show();
				if (MyDialogSucceed){
					deleteClubinServer deleteclub = new deleteClubinServer(clubAdapter.clubmonitorsList.get(currentClub));
					deleteclub.execute();
				}
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		Toast.makeText(this, "長押しで終了", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onKeyLongPress(int Keycode, KeyEvent e) {
		if (Keycode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyLongPress(Keycode, e);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onActivityResult(int requestcode, int resultcode, Intent data){
		switch (requestcode){
			case PAIRING_REQUEST_CODE:
				if(resultcode == RESULT_OK){
					ArrayList<CheckBoxItem> pairList = (ArrayList<CheckBoxItem>) data.getSerializableExtra("pairlist");
					sendPairList(pairList, currentClub);
				}
				break;
			case JOIN_REQUEST_CODE:
				if(resultcode==RESULT_OK){
					ArrayList<CheckBoxItem> joinList = (ArrayList<CheckBoxItem>) data.getSerializableExtra("joinlist");
					ArrayList<Clubmonitor> joinmonitors = new ArrayList<>();
					for(CheckBoxItem item : joinList){
						Clubmonitor itemc =convertCheckBoxToCkubMonitor(item);
						clubAdapter.clubmonitorsList.add(itemc);
						joinmonitors.add(itemc);
					}
					sendJoinList(joinmonitors);
				}
				break;
			case LOGIN_REQUEST_CODE:
				if(resultcode == RESULT_OK){
					mToken = data.getStringExtra("TOKEN");
					//Log.d("hogehogehoge",mToken);
					startBLEService();
				}else{
					finish();
				}
				break;
			case BLUETOOTH_ENABLE_REQUEST_CODE:
				if(resultcode != RESULT_OK){
					Snackbar.make(mainAcitivity_View_Parent, "BlueToothサービスが無効です。", Snackbar.LENGTH_SHORT).show();
				}
		}
	}

	private void checkBLE(){
		//そもそもBLE機能があるかどうか
		if(!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			showToast("この端末ではBLEのサポートがありません。");
			finish();
		}

		//権限チェックを投げつける(Android6以上)
		checkPermission();
	}

	private Clubmonitor convertCheckBoxToCkubMonitor(CheckBoxItem checkBoxItem){
		Clubmonitor clubmonitor = new Clubmonitor(checkBoxItem.getGid(), checkBoxItem.getSerial(), checkBoxItem.getTemple(), checkBoxItem.getselfStat());
		return clubmonitor;
	}

	private void bindBLEService() {
		Intent it = new Intent(MainActivity.this, BLEService.class);
		bindService(it, this, 0);
	}

	private void startBLEService(){
		//tokenを元にBLEServiceを呼び起こす
		Intent it = new Intent(MainActivity.this, BLEService.class);
		startService(it);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void checkPermission() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			//BLEではなぜか位置情報アクセスが必要。
			if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
				requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},BLE_LOCATION_REQUEST_CODE);
			}
		}
	}

	/** requestPermissionsの結果を受け取るコールバック */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
		//リクエストが自分のものだったか
		if(requestCode == BLE_LOCATION_REQUEST_CODE){
			//リクエストが通ったかどうか
			if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
				showToast("アクセス権限の許可が受諾されました。");
				bindBLEService();
			}else{
				showToast("アクセス権限の許可が拒否されました。");
			}
		}else {
			super.onRequestPermissionsResult(requestCode, permissions ,grantResults);
		}
	}

	private void logout(){
		Logout logoutAsync = new Logout();
		logoutAsync.execute();
		//TODO logoutする
	}

	private void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**BLEServiceとのConnectionをとる*/
	@Override
	public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
		mMessenger = new Messenger(iBinder);
		sendToken(mToken);
		sendClubListRequest();
	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {
		mMessenger = null;
	}

	public void sendToken(String token){
		Message msg = Message.obtain(null, BLEService.TOKEN_ADD, token);
		replyMessenger = new Messenger(new Handler());
		msg.replyTo=replyMessenger;
		sendMessage(msg);
	}

	public void sendPairList(ArrayList<CheckBoxItem> list, int clubindex) {
		//データ作成
		Bundle bundle = new Bundle();
		bundle.putSerializable(BLEService.BLE_DEVICE, list);
		bundle.putInt(BLEService.CLUB_INDEX ,clubindex);
		Message msg = Message.obtain(null, BLEService.BLE_ADD_DEVICE);
		msg.setData(bundle);//メッセージ化
		replyMessenger = new Messenger(new Handler());
		msg.replyTo=replyMessenger;
		sendMessage(msg);
	}

	public void sendJoinList(ArrayList<Clubmonitor> list){
		Bundle bundle = new Bundle();
		bundle.putSerializable(BLEService.JOINING_CLUB, list);
		Message msg = Message.obtain(null, BLEService.JOIN_CLUB);
		msg.setData(bundle);
		replyMessenger = new Messenger(new MessageJoinClubHandler());
		msg.replyTo=replyMessenger;
		sendMessage(msg);
	}

	private void sendMessage(Message msg) {
		try {
			mMessenger.send(msg);//送信
		} catch (RemoteException e) {
			e.printStackTrace();
			Toast.makeText(this.getApplicationContext(), "子機が正常に追加できませんでした。", Toast.LENGTH_SHORT).show();
		}
	}

	private void sendClubListRequest(){
		Message message = Message.obtain(null, BLEService.CLUBLIST_REQUEST);
		replyMessenger = new Messenger(new MessageHandler());
		message.replyTo = replyMessenger;
		sendMessage(message);
	}

	@Override
	public void onMyDialogSucceeded(int requestCode, int resultCode, Bundle params) {
		MyDialogSucceed = true;
	}

	@Override
	public void onMyDialogCancelled(int requestCode, Bundle params) {
		MyDialogSucceed = false;
	}

	static class MessageHandler extends Handler{
		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			BLEService_ClubMonitor = (ArrayList<Clubmonitor>)msg.obj;
		}
	}

	static class MessageJoinClubHandler extends Handler{
		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			mustDownBLEClubs=true;
		}
	}

	/** BLEServiceからの情報を受信する */
	class BLEBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent){
			//Bluetooth有効化の要請のとき
			if(intent.getAction().equals(BLEService.REQUEST_BLUETOOTH_ACTION)){
				Intent it = intent.getParcelableExtra(BLEService.REQUEST_BLUETOOTH);
				startActivityForResult(it, BLUETOOTH_ENABLE_REQUEST_CODE);
			}
		}

	}

	public void UpdateClubView(Clubmonitor club){
		clubAdapter.notifyDataSetChanged();
	}

	class Logout extends AsyncTask<Void, Void, Boolean>{
		OkHttpClient client;
		private DialogTemplate dialog;
		String jsondata;

		public Logout(){
			super();
			client = new OkHttpClient();
		}

		void run(String url) throws IOException {
			Request request = new Request.Builder()
					.url(url)
					.build();

			Response response = client.newCall(request).execute();
			this.jsondata = response.body().string();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new DialogTemplate.Builder(activity)
					.message("logout now...")
					.isProgress(true)
					.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String query = "http://mofutech.net:4545/logout?token="+Hawk.get("token")+"}";
			try{
				run(query);
				return true;
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success){
			Hawk.deleteAll();
			//TODO: ここにはBLEとのコネクションを全部切ってサービスをfinish()する処理が入りそう。
			dialog.dismiss();
			finish();
		}
	}

	class deleteClubinServer extends AsyncTask<Void, Void, Boolean> {
		OkHttpClient client;
		Clubmonitor club;
		private DialogTemplate dialog;

		public deleteClubinServer(Clubmonitor club) {
			super();
			client = new OkHttpClient();
			this.club = club;
		}

		void run(String url) throws IOException {
			Request request = new Request.Builder()
					.url(url)
					.build();

			Response response = client.newCall(request).execute();
			response.body().string();
			//TODO: 遅れなかった時の処理を書いていません
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			this.dialog = new DialogTemplate.Builder(activity)
					.message("部活の監視を停止しています")
					.isProgress(true)
					.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String query = "http://mofutech.net:4545/group/" + club.getGid() + "/leave?token=" + Hawk.get("token");
			try {
				run(query);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success)
		{
			this.dialog.dismiss();
			//TODO:BLEdeviceから削除する処理
		}
	}

}
