package io.github.joken.heatbuster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PairingActivity extends AppCompatActivity {

    @BindView(R.id.pairinglistview)
    ListView pairingList;

	/** 発見されたBLEデバイス名を格納する */
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

		//ListViewにAdapterを登録
        pairingList.setAdapter(checkAdaper);
        registerForContextMenu(pairingList);

		//ToolBarをActionBar化
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

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
        Intent intent = new Intent();
        intent.putExtra("pairlist",checked);
        setResult(RESULT_OK,intent);
        finish();
    }
}
