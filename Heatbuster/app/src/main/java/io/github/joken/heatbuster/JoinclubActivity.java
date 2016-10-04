package io.github.joken.heatbuster;

import android.content.Intent;
import android.os.AsyncTask;
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

public class JoinclubActivity extends AppCompatActivity {

    @BindView(R.id.joinclub)
    ListView joinView;

    private CheckboxListAdapter checkAdaper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinclub);
        ButterKnife.bind(this);

        getClubList getClubListAsync = new getClubList();
        getClubList.execute(null);

        //リスト項目をクリック時に呼び出されるコールバックを登録
        joinView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            //リスト項目クリック時の処理
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finish();
            }
        });
    }

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

    class getClubList extends AsyncTask<Void, Void, Boolean> {
        OkHttpClient client = new OkHttpClient();
        ArrayList<Clubmonitor> joinableclublist = new ArrayList<>();

        public getClubList(){
            super();
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
                if (!(Arrays.asList(clublist).contains("club"))){
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

