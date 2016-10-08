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
import android.widget.ListView;

import com.orhanobut.hawk.Hawk;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeleteClubActivity extends AppCompatActivity implements ServiceConnection,DialogTemplate.Callback{

    @BindView(R.id.deleteClub)
    ListView deleteView;

    private CheckboxListAdapter checkAdaper;
    private Messenger mMessenger,replyMessenger;
    private DeleteClubActivity activity = this;
    private static ArrayList<Clubmonitor> BLEService_ClubMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_club);
        ButterKnife.bind(this);

        //BLEServiceと接続
        Intent intent = new Intent(DeleteClubActivity.this, BLEService.class);
        bindService(intent, this, 0);

        BLEService_ClubMonitor = new ArrayList<>();

        checkAdaper = new CheckboxListAdapter(DeleteClubActivity.this, new ArrayList<CheckBoxItem>());
        deleteView.setAdapter(checkAdaper);
        registerForContextMenu(deleteView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    /** 参加の確定ボタン */
    @OnClick(R.id.addjoinbutton)
    public void onClick(){
        final ArrayList<CheckBoxItem> checked = new ArrayList<>();
        for(CheckBoxItem item : checkAdaper.checkBoxItemsList){
            if(item.getChecked())checked.add(item);
        }
        deleteClubinServer deletemestoserver = new deleteClubinServer(checked);
        deletemestoserver.execute();
        Intent intent = new Intent();
        intent.putExtra("deletelist",checked);
        setResult(RESULT_OK,intent);
        finish();
    }

    public static void registMenu(ArrayList<Clubmonitor> clublist){
        ArrayList<CheckBoxItem> deleteclubList = new ArrayList<CheckBoxItem>();
        for (Clubmonitor club:clublist){
            CheckBoxItem deleteclub = new CheckBoxItem(club.getName());
            deleteclub.setGid(club.getGid());
            deleteclubList.add(deleteclub);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMessenger = new Messenger(iBinder);
        replyMessenger = new Messenger(new MessageHandler());
        sendClubListRequest();
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

    @Override
    public void onMyDialogSucceeded(int requestCode, int resultCode, Bundle params) {

    }

    @Override
    public void onMyDialogCancelled(int requestCode, Bundle params) {

    }

    static class MessageHandler extends Handler {
        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            BLEService_ClubMonitor = (ArrayList<Clubmonitor>)msg.obj;
            registMenu(BLEService_ClubMonitor);

        }
    }

    class deleteClubinServer extends AsyncTask<Void, Void, Boolean> {
        OkHttpClient client;
        ArrayList<CheckBoxItem> deleteclublist;
        private DialogTemplate dialog;

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
            response.body().string();
            //TODO: 遅れなかった時の処理を書いていません
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new DialogTemplate.Builder(activity)
                    .message("serverと通信しています")
                    .isProgress(true)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (CheckBoxItem club : deleteclublist) {
                String query = "http://mofutech.net:4545/group/" + club.getGid() + "/leave?token=" + Hawk.get("token");
                try {
                    run(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            this.dialog.dismiss();
        }
    }
}
