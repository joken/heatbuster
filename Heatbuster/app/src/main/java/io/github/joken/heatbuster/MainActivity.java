package io.github.joken.heatbuster;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements  ServiceConnection{

	@BindView(R.id.clublistView)
	ListView clublistView;

	/** 部活動リスト */
	private ClubmonitorAdapter clubAdapter;
	/** 権限チェック後にリクエストが自分のものであったか確認する定数(値に意味はない) */
	private static int BLE_LOCATION_REQUEST_CODE = 9999;
	public static final int PAIRING_REQUEST_CODE = 1919;
	public static final int JOIN_REQUEST_CODE = 2525;
	/** ユーザーのToken */
	private String mToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		ArrayList<Clubmonitor> personList = new ArrayList<Clubmonitor>();
		personList.add(new Clubmonitor("野球部", 41.2f, TemperatureStatus.Emergency));
		personList.add(new Clubmonitor("サッカー部", 27.4f, TemperatureStatus.Safe));
		personList.add(new Clubmonitor("テニス部", 26.7f, TemperatureStatus.Safe));
		personList.add(new Clubmonitor("女子バレー部", 28.9f, TemperatureStatus.Warning));
		personList.add(new Clubmonitor("卓球部", 27.2f, TemperatureStatus.Safe));
		clubAdapter = new ClubmonitorAdapter(MainActivity.this, personList);

		clublistView.setAdapter(clubAdapter);
		registerForContextMenu(clublistView);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplication(), JoinclubActivity.class);
				startActivityForResult(intent, JOIN_REQUEST_CODE);
			}
		});

		//tokenを取得しておく
		mToken = this.getIntent().getStringExtra("token");

		//BLEの諸々とした確認
		checkBLE();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listview1_context, menu);
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
				Intent intent1 = new Intent(getApplication(),WorningActivity.class);
				startActivity(intent1);
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
					sendPairList(pairList);
				}
				break;
			case JOIN_REQUEST_CODE:
				if(resultcode==RESULT_OK){
					ArrayList<CheckBoxItem> joinList = (ArrayList<CheckBoxItem>) data.getSerializableExtra("joinlist");
					//TODO ちゃんとclubを追加する
				}
				break;
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

	private void startBLEservice(){
		//tokenを元にBLEServiceを呼び起こす
		Intent it = new Intent(MainActivity.this, BLEService.class);
		it.putExtra("token", mToken);
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
				startBLEservice();
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

	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {

	}

	public void sendPairList(ArrayList<CheckBoxItem> list) {
		//TODO リストをServiceに放り投げる
	}

}
