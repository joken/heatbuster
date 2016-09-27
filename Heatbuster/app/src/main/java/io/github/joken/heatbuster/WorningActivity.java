package io.github.joken.heatbuster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WorningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worning);
    }

    public void onClickOK(View v){
        finish();
    }
}
