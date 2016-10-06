package io.github.joken.heatbuster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.orhanobut.hawk.Hawk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A login screen that offers login via ID/password.
 */
public class LoginActivity extends Activity {

	/** UI references.*/
	@BindView(R.id.login_progress)
	ProgressBar mProgressView;
	@BindView(R.id.user_ID)
	EditText mIDView;
	@BindView(R.id.password)
	EditText mPasswordView;
	@BindView(R.id.login_form)
	ScrollView mLoginFormView;

	/**Keep track of the login task to ensure we can cancel it if requested.*/
	private UserLoginTask mAuthTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		BootstrapButton mIDSignInButton = (BootstrapButton) findViewById(R.id.email_sign_in_button);
		mIDSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid ID, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mIDView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String ID = mIDView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid ID address.
		if (TextUtils.isEmpty(ID)) {
			mIDView.setError(getString(R.string.error_field_required));
			focusView = mIDView;
			cancel = true;
		} else if (!isIDValid(ID)) {
			mIDView.setError(getString(R.string.error_invalid_email));
			focusView = mIDView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask(ID, password);
			mAuthTask.execute((Void) null);
		}
	}

	private boolean isIDValid(String ID) {
		//TODO: Replace this with your own logic
		return ID.length() > 4;
	}

	private boolean isPasswordValid(String password) {
		//TODO: Replace this with your own logic
		return password.length() > 6;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mID;
		private final String mPassword;
		private final OkHttpClient mclient;
		private String token;

		private static final String loginApi = "http://mofutech.net:4545/login";

		UserLoginTask(String ID, String password) {
			mID = ID;
			mPassword = password;
			mclient = new OkHttpClient();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String query = loginApi+"?username"+mID+"&password={"+md5(mPassword)+"}";
			String result=null;
			Request request = new Request.Builder().url(query).build();
			try {
				Response response = mclient.newCall(request).execute();
				result =response.body().string();
				//Log.d("RESULT",result);
			}catch (Exception e) {
				e.printStackTrace();
				result = null;
				return false;
			}



			return parseResult(result);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (true) {//TODO デバッグ終了後に条件文に変更する
				Intent intent = new Intent(getApplication(), MainActivity.class);
				intent.putExtra("token", token);
				setResult(RESULT_OK, intent);
				finish();
			} else {
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}

		/**
		 * 認証成否を判定する
		 *
		 * @param query サーバーからのレスポンス
		 * @return 認証成功ならtrue、そうでないならfalse
		 */
		private boolean parseResult(String query) {
			//TODO ロジックの実装
			//TODO tokenのセット(成功時)
			token = "onononon!";
			Hawk.init(getApplicationContext()).build();
			Hawk.put("token",token);
			return true;
		}
	}

	public String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++)
				hexString.append(String.format("%02x", messageDigest[i] & 0xff));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
