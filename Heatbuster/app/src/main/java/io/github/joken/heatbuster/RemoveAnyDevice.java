package io.github.joken.heatbuster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RemoveAnyDevice extends AppCompatActivity {

    @BindView(R.id.removeAnyDevice)
    ListView removeAnyView;

    private CheckboxListAdapter checkAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_any_device);
        ButterKnife.bind(this);
    }

    public void redistMenu(ArrayList<CheckBoxItem> devicelist){
        checkAdapter = new CheckboxListAdapter(RemoveAnyDevice.this,devicelist);
        removeAnyView.setAdapter(checkAdapter);
        registerForContextMenu(removeAnyView);
    }
}
