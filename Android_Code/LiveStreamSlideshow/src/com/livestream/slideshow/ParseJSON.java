package com.livestream.slideshow;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.livestream.slideshow.SettingsDialog.SettingsUpdatedListener;

public class ParseJSON extends AsyncTask<String, String, JSONObject> { 
	
	SettingsUpdatedListener listener;

	
	@Override
	protected JSONObject doInBackground(String... params) {
		String url = params[0];
		
		return getJson(url);
	}

	public JSONObject getJson(String url) {

		InputStream is = null;
		String result = "";
		JSONObject jsonObject = null;

		// HTTP
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e) {
			Log.e("Exception", e.toString());
			return null;
		}

		// Read response to string
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is, "utf-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			return null;
		}

		// Convert string to object
		try {
			jsonObject = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("Exception", e.toString());
			return null;
		}

		return jsonObject;

	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		
		super.onPostExecute(result);
		//{"presetTimeFormat":"seconds","durationTimeFormat":"seconds","presetTime":"1","durationTime":"1"}
		try {
			String presetTimeFormat = result.getString("presetTimeFormat");
			String durationTimeFormat = result.getString("durationTimeFormat");
			Settings settings = new Settings(result.getInt("durationTime"), result.getInt("presetTime"),
					presetTimeFormat, durationTimeFormat);
			this.listener.settingsChanged(settings);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}	

	public void setSettingsListener(SettingsUpdatedListener updates){
		this.listener = updates;
	}

}
