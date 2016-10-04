package io.github.joken.heatbuster;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorningActivity extends AppCompatActivity {

	@BindView(R.id.worning_mes0)
	TextView worning_mes0;
	@BindView(R.id.worning_mes1)
	TextView worning_mes1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_worning);
		ButterKnife.bind(this);
		Intent i = getIntent();
		String clubname = i.getStringExtra("CLUBNAME");
		worning_mes0.setText(clubname + "熱中症が発生しました。\n");
		worning_mes1.setMovementMethod(ScrollingMovementMethod.getInstance());
		worning_mes1.setText(clubname + "では現在、熱中症の疑いがある生徒が検知されています。すぐさま部活動を中断し、疑いの見られる生徒を保健室に連れていき、他の部員に水分補給及び日陰での休憩を薦めます。\n\n");
		//警告音を鳴らす
		ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
		toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE);
	}

	public void onClickOK(View v) {
		finish();
	}
}
