package io.github.joken.heatbuster;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
}
