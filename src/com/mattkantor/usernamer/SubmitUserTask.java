package com.mattkantor.usernamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import android.os.AsyncTask;

/**
 * Asynchronously submit a user via HTTP post.
 * @author mkantor
 */
public class SubmitUserTask extends AsyncTask</* Params */URL, /* Progress */Integer, /* Result */String> {

	private HashMap<String, String> postData = null;
	private HttpURLConnection connection = null;
	
	// FIXME: It's kind of lame for this to not be a generic Activity. Could 
	// provide a callback interface instead, or make this abstract and have an 
	// inner class implementing onPostExecute.
	private MainActivity activity = null;
	
	public SubmitUserTask(MainActivity activity, HashMap<String, String> postData) {
		this.activity = activity;
		this.postData = postData;
	}
	
	/* Perform the HTTP request.
	 * This only supports one argument even though the superclass wants varargs.
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected String doInBackground(URL... urls) {
		URL url = urls[0];
		try {
			connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "text/plain");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();
			
			// Write post data to the connection.
			connection.getOutputStream().write(convertToQueryString(postData).getBytes());
			
			// TODO: Make this work more like the iOS version.
			int statusCode = connection.getResponseCode();
			if(statusCode >= 200 && statusCode < 300) {
				return "Success!";
			} else {
				return "Something went wrong.";
			}
		} catch(IOException exception) {
			return exception.toString();
		} finally {
			connection.disconnect();
		}
	}
	
	@Override
	protected void onPostExecute(final String responseBody) {
		activity.onUserSubmissionComplete(responseBody);
	}
	
	private static String convertToQueryString(HashMap<String, String> data) throws UnsupportedEncodingException {
		StringBuilder stringBuilder = new StringBuilder();
		for(HashMap.Entry<String, String> e : data.entrySet()) {
			if(stringBuilder.length() > 0) {
				stringBuilder.append('&');
			}
			String encodedKey = URLEncoder.encode(e.getKey(), "UTF-8");
			String encodedValue = URLEncoder.encode(e.getValue(), "UTF-8");
			stringBuilder.append(encodedKey).append('=').append(encodedValue);
		}
		
		return stringBuilder.toString();
	}
}
