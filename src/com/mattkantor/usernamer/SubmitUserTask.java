package com.mattkantor.usernamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;

/**
 * Asynchronously submit a user via HTTP post.
 * @author mkantor
 */
public class SubmitUserTask extends AsyncTask</* Params */URL, /* Progress */Integer, /* Result */UserSubmissionResult> {

	private HashMap<String, String> postData = null;
	private HttpURLConnection connection = null;
	
	// FIXME: It's kind of lame for this to not be a generic Activity. I could 
	// provide a callback interface instead, or make SubmitUserTask abstract 
	// and have an inner class implementing onPostExecute.
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
	protected UserSubmissionResult doInBackground(URL... urls) {
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
			
			// Figure out the status & response entity.
			int statusCode = connection.getResponseCode();
			UserSubmissionResult.Status status = null;
			InputStream responseEntityStream = null;
			if(statusCode >= 200 && statusCode < 300) {
				responseEntityStream = connection.getInputStream();
				status = UserSubmissionResult.Status.SUCCESS;
			} else {
				responseEntityStream = connection.getErrorStream();
				if(statusCode == HttpURLConnection.HTTP_CONFLICT) {
					status = UserSubmissionResult.Status.CONFLICT;
				} else {
					status = UserSubmissionResult.Status.ERROR;
				}
			}
			String responseEntity = convertToString(responseEntityStream);
			
			// Decide what message to display.
			String message = null;
			Matcher plaintextMatcher = Pattern.compile("^text/plain\\b").matcher(connection.getContentType());
			// If the response is plaintext (like we asked for), get the 
			// message from there.
			if(responseEntity.length() > 0 && plaintextMatcher.find()) {
				// Just use the first line.
				message = new Scanner(responseEntity).nextLine();
			} else { // Otherwise, just use the status string (e.g. "OK").
				message = connection.getResponseMessage();
			}
			
			return new UserSubmissionResult(status, message);
		} catch(IOException exception) {
			return new UserSubmissionResult(UserSubmissionResult.Status.ERROR, exception.toString());
		} finally {
			connection.disconnect();
		}
	}
	
	@Override
	protected void onPostExecute(final UserSubmissionResult result) {
		activity.onUserSubmissionComplete(result);
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
	
	private static String convertToString(InputStream stream) throws IOException {
		// Scanner iterates over tokens in the stream, and in this case we 
		// separate tokens using "beginning of the input boundary" (\A) thus 
		// giving us only one token for the entire stream.
		// See <http://stackoverflow.com/a/5445161/3625>.
		Scanner scanner = new Scanner(stream).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}
}
