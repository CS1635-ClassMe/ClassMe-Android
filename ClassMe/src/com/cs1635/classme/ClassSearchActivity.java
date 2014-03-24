package com.cs1635.classme;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shared.Course;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClassSearchActivity extends ActionBarActivity implements SearchView.OnQueryTextListener
{
	SharedPreferences prefs;
	ClassSearchActivity context = this;
	ListView courseList;
	long lastSearch;
	String searchString;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.class_search);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		courseList = (ListView) findViewById(R.id.courseList);

		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		new CourseTask().execute(prefs.getString("loggedIn",""));
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
			{
				finish();
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.course_search, menu);
		MenuItem search = menu.findItem(R.id.search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
		if (searchView != null)
		{
			searchView.setQueryHint("Search for courses");
			searchView.setOnQueryTextListener(this);
		}

		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String s)
	{
		if(System.currentTimeMillis()-lastSearch < 2000) //workaround for event being fired on both ACTION_DOWN and ACTION_UP
			return false;

		lastSearch = System.currentTimeMillis();
		searchString = s;

		return true;
	}

	@Override
	public boolean onQueryTextChange(String s)
	{
		return false;
	}

	private class CourseTask extends AsyncTask<String,Void,ArrayList<Course>>
	{
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Retrieving Courses...", true);
		}

		@Override
		protected ArrayList<Course> doInBackground(String... params)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("username",params[0]));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/getUserCourses", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());

				Gson gson = new Gson();
				Type collectionType = new TypeToken<Collection<Course>>(){}.getType();
				return gson.fromJson(response, collectionType);
			}
			catch (Exception e)
			{
				Log.e("addendum", e.getMessage() + "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<Course> courses)
		{
			progressDialog.dismiss();

			RelativeLayout layout = (RelativeLayout)courseList.getParent();
			TextView empty = (TextView) layout.findViewById(R.id.empty);

			if(courses != null && courses.size() > 0)
			{
				empty.setVisibility(View.GONE);
				courseList.setVisibility(View.VISIBLE);
				courseList.setAdapter(new CourseAdapter(context, R.layout.post, courses));
			}
			else
			{
				empty.setVisibility(View.VISIBLE);
				courseList.setVisibility(View.GONE);
			}
		}
	}
}
