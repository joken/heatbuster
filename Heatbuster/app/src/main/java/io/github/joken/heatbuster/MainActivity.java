package io.github.joken.heatbuster;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView)findViewById(R.id.clublistView);
        ArrayList<Clubmonitor> personList = new ArrayList<Clubmonitor>();
        personList.add(new Clubmonitor("野球部", 29.3f,R.drawable.ic_sad));
        personList.add(new Clubmonitor("サッカー部", 27.4f,R.drawable.ic_smile));
        personList.add(new Clubmonitor("テニス部", 41.2f,R.drawable.ic_die));
        personList.add(new Clubmonitor("女子バレー部", 28.9f,R.drawable.ic_sad));
        personList.add(new Clubmonitor("卓球部", 40.6f,R.drawable.ic_die));
        ClubmonitorAdapter adapter = new ClubmonitorAdapter(MainActivity.this, personList);

        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), JoinclubActivity.class);
                startActivity(intent);
            }
        });
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
            case R.id.connect:
                Intent intent = new Intent(getApplication(), PairingActivity.class);
                startActivity(intent);
                return true;
            case R.id.reverce_one:
                return true;
            case R.id.reverce_all:
                return true;
            case R.id.delete:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        Toast.makeText(this, "長押しで終了", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyLongPress(int Keycode, KeyEvent e){
        if (Keycode==KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return super.onKeyLongPress(Keycode,e);
    }
}
