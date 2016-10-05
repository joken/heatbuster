package io.github.joken.heatbuster;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.orhanobut.hawk.Hawk;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeleteClubActivity extends AppCompatActivity {

    @BindView(R.id.deleteClub)
    ListView deleteView;

    private CheckboxListAdapter checkAdaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_club);
        ButterKnife.bind(this);
    }

    public void registMenu(ArrayList<Clubmonitor> clublist){
        ArrayList<CheckBoxItem> deleteclubList = new ArrayList<CheckBoxItem>();
        for (Clubmonitor club:clublist){
            CheckBoxItem deleteclub = new CheckBoxItem(club.getName());
            deleteclub.setGid(club.getGid());
            deleteclubList.add(deleteclub);
        }
        checkAdaper = new CheckboxListAdapter(DeleteClubActivity.this,deleteclubList);
        deleteView.setAdapter(checkAdaper);
        registerForContextMenu(deleteView);
    }

    class deleteClubinServer extends AsyncTask<Void, Void, Boolean> {
        OkHttpClient client;
        ArrayList<CheckBoxItem> deleteclublist;

        public deleteClubinServer(ArrayList<CheckBoxItem> deleteclublist) {
            super();
            client = new OkHttpClient();
            this.deleteclublist = deleteclublist;
        }

        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            //TODO: 遅れなかった時の処理を書いていません
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (CheckBoxItem club : deleteclublist) {
                String query = "http://mofutech:4545/group/" + club.getGid() + "/leave?token={" + Hawk.get("token") + "}";
                try {
                    run(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
    }
}
