package io.github.joken.heatbuster;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PairingActivity extends AppCompatActivity {

    @BindView(R.id.pairinglistview)
    ListView pairingList;

	/** 発見されたBLEデバイス名を格納する */
	//private ArrayAdapter<String> pairAdapter;
	private CheckboxListAdapter checkAdaper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pairing);
		ButterKnife.bind(this);

        ArrayList<CheckBoxItem> pairbleList = new ArrayList<CheckBoxItem>();
        pairbleList.add(new CheckBoxItem("k4z9yc"));
        pairbleList.add(new CheckBoxItem("z8mwf4"));
        pairbleList.add(new CheckBoxItem("8xsdxg"));
        pairbleList.add(new CheckBoxItem("zp29gy"));
        pairbleList.add(new CheckBoxItem("jawfyf"));
        checkAdaper = new CheckboxListAdapter(PairingActivity.this,pairbleList);

        pairingList.setAdapter(checkAdaper);
        registerForContextMenu(pairingList);

		//ToolBarをActionBar化
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        //ListViewにAdapterを登録
        //setPairAdapter();

	}


	private void setPairAdapter(){
		//pairAdapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_list_item_1);
		//pairingList.setAdapter(pairAdapter);
	}

    @OnClick(R.id.addpairbutton)
    public void onClick() {
        final ArrayList<CheckBoxItem> checked = new ArrayList<>();
        for(CheckBoxItem item : checkAdaper.checkBoxItemsList){
            if(item.getChecked())checked.add(item);
        }
        Intent intent = new Intent();
        intent.putExtra("pairlist",checked);
        setResult(RESULT_OK,intent);
        finish();
    }
}
