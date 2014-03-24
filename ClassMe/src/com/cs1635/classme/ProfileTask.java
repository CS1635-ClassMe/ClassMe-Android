package com.cs1635.classme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.shared.UserProfile;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

class ProfileTask extends AsyncTask<String,Void,UserProfile>
{
	Context context;
	ProgressDialog progressDialog;
	Gson gson;

	public ProfileTask(Context c)
	{
		context = c;
		gson = new Gson();
	}

	@Override
	protected void onPreExecute()
	{
		progressDialog = ProgressDialog.show(context, "", "Fetching Profile...", true);
	}

	@Override
	protected UserProfile doInBackground(String... params)
	{
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("username",params[0]));

		try
		{
			HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/getUserProfile", nameValuePairs);
			String response = EntityUtils.toString(urlResponse.getEntity());

			return gson.fromJson(response, UserProfile.class);
		}
		catch (Exception e)
		{
			Log.e("addendum", e.getMessage() + "");
		}

		return null;
	}

	@Override
	protected void onPostExecute(UserProfile userProfile)
	{
		progressDialog.dismiss();
		Intent intent = new Intent(context,ProfileActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("userProfile",gson.toJson(userProfile));
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
}
