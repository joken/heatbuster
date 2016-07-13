package io.github.joken.heatbuster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView)findViewById(R.id.clublistView);
        ArrayList<Clubmonitor> personList = new ArrayList<Clubmonitor>();
        personList.add(new Clubmonitor("やきう部", 30.1f));
        personList.add(new Clubmonitor("サッカー部", 20.7f));
        personList.add(new Clubmonitor("hoge", 1145.14f));
        personList.add(new Clubmonitor("pito", 81.21f));
        personList.add(new Clubmonitor("DJRN", -23.5f));
        ClubmonitorAdapter adapter = new ClubmonitorAdapter(MainActivity.this, personList);

        listView.setAdapter(adapter);
    }
}
