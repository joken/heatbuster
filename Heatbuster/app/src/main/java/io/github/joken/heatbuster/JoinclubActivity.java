package io.github.joken.heatbuster;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;

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

        ArrayList<CheckBoxItem> joinclubList = new ArrayList<CheckBoxItem>();
        joinclubList.add(new CheckBoxItem("野球部"));
        joinclubList.add(new CheckBoxItem("バスケットボール部"));
        joinclubList.add(new CheckBoxItem("陸上部"));
        checkAdaper = new CheckboxListAdapter(JoinclubActivity.this,joinclubList);

        joinView.setAdapter(checkAdaper);
        registerForContextMenu(joinView);

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

    //class GetClubList extends AsyncTask<Void,Void,String>{
    //    OkHttpClient client = new OkHttpClient();
    //private static final String loginApi = "http://mofutech:4545";
    //    @Override
    //    protected String doInBackground(Void... params) {
    //       String result=null;
    //        String query = loginApi+"/group/list?token={"+Hawk.get("token")+"}";
    //        Request request = new Request.Builder()
    //                .url(query)
    //                .get()
    //                .build();
//
    //        try {
    //            Response response = client.newCall(request).execute();
    //            result = response.body().string();
    //        }catch (Exception e){
    //            e.printStackTrace();
    //        }
    //        return result;
    //    }
    //}
}

