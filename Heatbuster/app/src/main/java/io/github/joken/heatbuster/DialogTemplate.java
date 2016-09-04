package io.github.joken.heatbuster;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Dialog作成用のテンプレートクラス
 */

public class DialogTemplate extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
				.create();
	}

	@Override
	public void onPause(){
		super.onPause();

		//メモリリーク対策
		dismiss();
	}

}
