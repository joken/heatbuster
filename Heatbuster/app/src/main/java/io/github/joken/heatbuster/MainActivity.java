package io.github.joken.heatbuster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listview1_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.connect_many:
                return true;
            case R.id.connect_one:
                return true;
            case R.id.reverce_all:
                return true;
            case R.id.reverce_one:
                return true;
            case R.id.delete:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
