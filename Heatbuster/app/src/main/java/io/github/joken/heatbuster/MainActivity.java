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

public class MainActivity extends AppCompatActivity implements ServiceConnection{

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
	private Messenger mMessenger;

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
		//TODO GroupListの取得で部活動を追加する
		ArrayList<Clubmonitor> personList = new ArrayList<>();
		personList.add(new Clubmonitor("1","野球部", 41.2f, TemperatureStatus.Emergency));
		personList.add(new Clubmonitor("2","サッカー部", 27.4f, TemperatureStatus.Safe));
		personList.add(new Clubmonitor("3","テニス部", 26.7f, TemperatureStatus.Safe));
		personList.add(new Clubmonitor("4","女子バレー部", 28.9f, TemperatureStatus.Warning));
		personList.add(new Clubmonitor("5","卓球部", 27.2f, TemperatureStatus.Safe));
		clubAdapter = new ClubmonitorAdapter(MainActivity.this, personList);

		clublistView.setAdapter(clubAdapter);
		registerForContextMenu(clublistView);
	}

	private void getToken() {
		mToken = Hawk.get("token", null);
		if(mToken == null){
			Intent loginIntent = new Intent(getApplication(), LoginActivity.class);
			startActivityForResult(loginIntent, LOGIN_REQUEST_CODE);
		}else{
			bindBLEService();
		}
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
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.connect:
				Intent intent = new Intent(getApplication(), PairingActivity.class);
				startActivityForResult(intent, PAIRING_REQUEST_CODE);
				return true;
			case R.id.reverce_one:
				return true;
			case R.id.reverce_all:
				return true;
			case R.id.delete:
				Intent intent_delete = new Intent(getApplication(),DeleteClubActivity.class);
				startActivity(intent_delete);
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
					sendJoinList(joinList);
				}
				break;
			case LOGIN_REQUEST_CODE:
				if(resultcode == RESULT_OK){
					mToken = data.getStringExtra("token");
					sendToken(mToken);
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

	private void showToast(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**BLEServiceとのConnectionをとる*/
	@Override
	public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
		mMessenger = new Messenger(iBinder);
	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {
		mMessenger = null;
	}

	public void sendToken(String token){
		Message msg = Message.obtain(null, BLEService.TOKEN_ADD, token);
		sendMessage(msg);
	}

	public void sendPairList(ArrayList<CheckBoxItem> list, int clubindex) {
		//データ作成
		Bundle bundle = new Bundle();
		bundle.putSerializable(BLEService.BLE_DEVICE, list);
		bundle.putInt(BLEService.CLUB_INDEX ,clubindex);
		Message msg = Message.obtain(null, BLEService.BLE_ADD_DEVICE);
		msg.setData(bundle);//メッセージ化
		sendMessage(msg);
	}

	public void sendJoinList(ArrayList<CheckBoxItem> list){
		Bundle bundle = new Bundle();
		bundle.putSerializable(BLEService.JOINING_CLUB, list);
		Message msg = Message.obtain(null, BLEService.JOIN_CLUB);
		msg.setData(bundle);
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

	/** Serverに指定された部活の端末データを全て送信する **/
	class UploadClubJSON extends AsyncTask<Void, Void, Boolean> {
		public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		OkHttpClient client = new OkHttpClient();
		private String gid;
		private String json;

		UploadClubJSON(String gid,String json){
			this.gid=gid;
			this.json=json;
		}

		String post(String url) throws IOException{
			RequestBody body = RequestBody.create(JSON,json);
			Request request = new Request.Builder()
					.url(url)
					.post(body)
					.build();
			Response response = client.newCall(request).execute();
			return response.body().string();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String query = "http://mofutech:4545/group/"+gid+"/mod/update";
			try{
				post(query);
				return true;
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
	}

	/** 送信用のJSONを作成する **/
	public String MakingJson(ArrayList<CheckBoxItem> conDeviceList){
		// jsonデータの作成
		JSONObject jsonOneData;

		try {
			jsonOneData = new JSONObject();
			jsonOneData.put("token", Hawk.get("token"));

			JSONArray itemArray = new JSONArray();
			for (CheckBoxItem conDevice : conDeviceList) {
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

	/**Serverからクラブごとの平均値と部内温度最高評価を受信する**/
	class DownloadClubStats extends AsyncTask<Void, Void, Boolean> {
		OkHttpClient client = new OkHttpClient();

		private String gid;
		private Clubmonitor club;

		DownloadClubStats(String gid,Clubmonitor club){
			this.gid=gid;
			this.club=club;
		}

		String run(String url) throws IOException {
			Request request = new Request.Builder()
					.url(url)
					.build();

			Response response = client.newCall(request).execute();
			return response.body().string();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String query = "http://mofutech:4545/group/"+gid+"/mod/status";
			try{
				downparce(club,run(query));
				return true;
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
	}

	/** serverから受信したJSONの解析及びオブジェクト(部活)の更新 **/
	public void downparce(Clubmonitor club,String jsondata){
		try {
			JSONObject item = new JSONObject(jsondata);
			if (item.getString("success") == "true"){
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
			}else{
				Log.d("DownloadStat:","何一つ得られませんでした");
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
