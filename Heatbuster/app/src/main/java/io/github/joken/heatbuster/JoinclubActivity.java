package io.github.joken.heatbuster;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JoinclubActivity extends AppCompatActivity implements ServiceConnection{

    @BindView(R.id.joinclub)
    ListView joinView;

    private CheckboxListAdapter checkAdaper;
    private Messenger mMessenger,replyMessenger;
    private ArrayList<Clubmonitor> BLEService_ClubMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinclub);
        ButterKnife.bind(this);

        //BLEServiceと接続
        Intent intent = new Intent(JoinclubActivity.this, BLEService.class);
        bindService(intent, this, 0);
        sendClubListRequest();

        getClubList getClubListAsync = new getClubList();
        getClubListAsync.execute();

    }

    /** 参加の確定ボタン */
    @OnClick(R.id.addjoinbutton)
    public void onClick(){
        final ArrayList<CheckBoxItem> checked = new ArrayList<>();
        for(CheckBoxItem item : checkAdaper.checkBoxItemsList){
            if(item.getChecked())checked.add(item);
        }
        Intent intent = new Intent();
        intent.putExtra("joinlist",checked);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMessenger = new Messenger(iBinder);
        replyMessenger = new Messenger(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                BLEService_ClubMonitor = (ArrayList<Clubmonitor>)msg.obj;
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMessenger = null;
    }

    private void sendClubListRequest(){
        Message message = Message.obtain(null, BLEService.CLUBLIST_REQUEST);
        message.replyTo = replyMessenger;
        sendMessage(message);
    }

    private void sendMessage(Message msg){
        try{
            mMessenger.send(msg);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    class getClubList extends AsyncTask<Void, Void, Boolean> {
        OkHttpClient client;
        ArrayList<Clubmonitor> joinableclublist;

        public getClubList(){
            super();
            client = new OkHttpClient();
            joinableclublist = new ArrayList<>();
        }

        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            this.joinableclublist = joinparce(response.body().string());
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String query = "http://mofutech:4545/group/list?token="+Hawk.get("token")+"}";
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
            ArrayList<CheckBoxItem> joinclubList = new ArrayList<CheckBoxItem>();
            for (Clubmonitor club: joinableclublist){
                if (!(Arrays.asList(BLEService_ClubMonitor).contains("club"))){
                    //TODO:この上部分のclublistをなんとかしてBLEServiceからとってくる。
                    joinclubList.add(new CheckBoxItem(club.getName()));
                }
            }
            checkAdaper = new CheckboxListAdapter(JoinclubActivity.this,joinclubList);
            joinView.setAdapter(checkAdaper);
            registerForContextMenu(joinView);
        }
    }

    public ArrayList<Clubmonitor> joinparce(String jsondata){
        ArrayList<Clubmonitor> joinableclublist = new ArrayList<>();
        try {
            JSONObject item = new JSONObject(jsondata);
            if (item.getString("success") == "true"){
                JSONArray clublist = item.getJSONArray("result");
                for (int i=0; i<clublist.length(); i++){
                    JSONObject clubJson =clublist.getJSONObject(i);
                    Clubmonitor club = new Clubmonitor(clubJson.getString("gid"),clubJson.getString("name"),0.0f,TemperatureStatus.Safe);
                    joinableclublist.add(club);
                }

            }else{
                Log.d("DownClublist:","部活の取得失敗");
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return joinableclublist;
    }
}

