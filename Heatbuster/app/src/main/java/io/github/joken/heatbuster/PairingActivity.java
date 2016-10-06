package io.github.joken.heatbuster;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PairingActivity extends AppCompatActivity {

    @BindView(R.id.pairinglistview)
    ListView pairingList;

	/** 発見されたBLEデバイス名を格納する */
	private CheckboxListAdapter checkAdaper;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothAdapter.LeScanCallback mLeScanCallBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pairing);
		ButterKnife.bind(this);

        ArrayList<CheckBoxItem> pairbleList = new ArrayList<CheckBoxItem>();
        checkAdaper = new CheckboxListAdapter(PairingActivity.this,pairbleList);

		//ListViewにAdapterを登録
        pairingList.setAdapter(checkAdaper);
        registerForContextMenu(pairingList);

		//ToolBarをActionBar化
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);


		// Initializes Bluetooth adapter.
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Ensures Bluetooth is available on the device and it is enabled. If not,
		// displays a dialog requesting user permission to enable Bluetooth.
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, MainActivity.BLUETOOTH_ENABLE_REQUEST_CODE);
		}

		initScanCallBack();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBluetoothAdapter.startLeScan(mLeScanCallBack);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBluetoothAdapter.startLeScan(mLeScanCallBack);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode){
			case MainActivity.BLUETOOTH_ENABLE_REQUEST_CODE:
				if(resultCode != RESULT_OK){
					Toast.makeText(this.getApplicationContext(), "Bluetoothの使用が拒否されました。", Toast.LENGTH_SHORT).show();
					setResult(RESULT_CANCELED);
					finish();
				}
		}
	}

	private void initScanCallBack(){
		mLeScanCallBack = new BluetoothAdapter.LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
				ParcelUuid[] uuids = bluetoothDevice.getUuids();
				StringBuilder builder = new StringBuilder();
				for(int n = 0; n < uuids.length; n++){
					builder.append(uuids[n].toString());
					if(n < uuids.length - 1){
						builder.append("-");
					}
				}
				String parsedUUID = builder.toString();
				CheckBoxItem item = new CheckBoxItem(parsedUUID);
				item.setDevice(bluetoothDevice);
				if(!checkAdaper.checkBoxItemsList.contains(item)){
					checkAdaper.checkBoxItemsList.add(item);
				}
			}
		};
	}

	@OnClick(R.id.addpairbutton)
    public void onClick() {
        final ArrayList<CheckBoxItem> checked = new ArrayList<>();
        for(CheckBoxItem item : checkAdaper.checkBoxItemsList){
            if(item.getChecked())checked.add(item);
        }
		if(checked.isEmpty()){
			Toast.makeText(this.getApplicationContext(),"1つ以上の子機を選択してください。",Toast.LENGTH_LONG).show();
			return;
		}
		new AddSerialServer(getIntent().getStringExtra("GID"), MakingJson(checked)).execute();
        Intent intent = new Intent();
        intent.putExtra("pairlist",checked);
        setResult(RESULT_OK,intent);
        finish();
    }

	/** Serverに指定された部活の端末データを全て送信する **/
	class AddSerialServer extends AsyncTask<Void, Void, Boolean> {
		public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		OkHttpClient client = new OkHttpClient();
		private String gid;
		private String json;

		AddSerialServer(String gid,String json){
			this.gid=gid;
			this.json=json;
		}

		String post(String url) throws IOException {
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
			String query = "http://mofutech.net:4545/group/"+gid+"/mod/add";
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
	public String MakingJson(ArrayList<CheckBoxItem> DeviceList){
		// jsonデータの作成
		JSONObject jsonOneData;

		try {
			jsonOneData = new JSONObject();
			jsonOneData.put("token", Hawk.get("token"));

			JSONArray itemArray = new JSONArray();
			for (CheckBoxItem Device : DeviceList) {
				jsonOneData = new JSONObject();
				jsonOneData.put("mac", Device.getSerial());
				itemArray.put(jsonOneData);
			}
			jsonOneData.put("element",itemArray);

		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
		return jsonOneData.toString();
	}
}
