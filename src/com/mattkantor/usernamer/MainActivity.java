package com.mattkantor.usernamer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Submit a user to the server.
	 * @param view
	 * @throws MalformedURLException 
	 */
	public void submitUser(View view) throws MalformedURLException {
		HashMap<String, String> userData = new HashMap<String, String>();

		EditText usernameField = (EditText) findViewById(R.id.usernameField);

		String username = usernameField.getText().toString();
		String deviceType = String.format("%s Android %s", Build.MODEL, Build.VERSION.RELEASE);
		String deviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		userData.put("username", username);
		userData.put("deviceType", deviceType);
		userData.put("deviceId", deviceId);

		SubmitUserTask task = new SubmitUserTask(this, userData);
		task.execute(new URL("http://10.0.2.2:9000/users")); // FIXME: Don't hardcode this URL.
	}

	/**
	 * Display the result of user submission.
	 * @param result
	 */
	public void onUserSubmissionComplete(UserSubmissionResult result) {
		TextView resultTextView = (TextView) findViewById(R.id.resultTextView);
		// TODO: Handle special UI behavior based on result.status.
		resultTextView.setText(result.message);
	}
}
