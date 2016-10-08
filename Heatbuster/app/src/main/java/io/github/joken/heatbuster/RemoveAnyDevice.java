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
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.orhanobut.hawk.Hawk;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RemoveAnyDevice extends AppCompatActivity implements ServiceConnection,DialogTemplate.Callback{

    @BindView(R.id.removeAnyDevice)
    ListView removeAnyView;

    private CheckboxListAdapter checkAdaper;
    private CheckboxListAdapter checkAdapter;
    private Messenger mMessenger,replyMessenger;
    private String gid;
    private static ArrayList<CheckBoxItem> BLEService_CheckBoxItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_any_device);
        ButterKnife.bind(this);

        //BLEServiceと接続
        Intent intent = new Intent(RemoveAnyDevice.this, BLEService.class);
        bindService(intent, this, 0);

        BLEService_CheckBoxItem = new ArrayList<>();

        gid = getIntent().getStringExtra("GID");

        checkAdaper = new CheckboxListAdapter(RemoveAnyDevice.this, new ArrayList<CheckBoxItem>());
        removeAnyView.setAdapter(checkAdaper);
        registerForContextMenu(removeAnyView);
    }

    /** 参加の確定ボタン */
    @OnClick(R.id.deletedevicebutton)
    public void onClick(){
        final ArrayList<CheckBoxItem> unChecked = new ArrayList<>();
        for(CheckBoxItem item : checkAdaper.checkBoxItemsList){
            if(!(item.getChecked()))unChecked.add(item);
        }
        deleteDeviceListRequest(unChecked);
        finish();
    }

    public void redistMenu(ArrayList<CheckBoxItem> devicelist){
        checkAdapter = new CheckboxListAdapter(RemoveAnyDevice.this,devicelist);
        removeAnyView.setAdapter(checkAdapter);
        registerForContextMenu(removeAnyView);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mMessenger = new Messenger(iBinder);
        replyMessenger = new Messenger(new MessageDeviceListHandler());
        getDeviceListRequest();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMessenger = null;
    }

    private void getDeviceListRequest(){
        Message message = Message.obtain(null, BLEService.DEVICELIST_REQUEST, gid);
        message.replyTo = replyMessenger;
        sendMessage(message);
    }

    private void deleteDeviceListRequest(ArrayList<CheckBoxItem> uncheckedList){
        Message message = Message.obtain(null, BLEService.BLE_ADD_DEVICE, uncheckedList);
        replyMessenger = new Messenger(new Handler());
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

    class MessageDeviceListHandler extends Handler {
        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            BLEService_CheckBoxItem = (ArrayList<CheckBoxItem>)msg.obj;
            redistMenu(BLEService_CheckBoxItem);
        }
    }
}
