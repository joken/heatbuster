package io.github.joken.heatbuster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class JoinclubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinclub);

        //ListViewのセット
        ListView listView = new ListView(this);
        setContentView(listView);

        //データの追加
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter.add("忍道");
        adapter.add("華道");
        adapter.add("香道");

        listView.setAdapter(adapter);
        //リスト項目をクリック時に呼び出されるコールバックを登録
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            //リスト項目クリック時の処理
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finish();
            }
        });
    }
}
