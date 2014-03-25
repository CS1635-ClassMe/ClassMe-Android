package com.cs1635.classme;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shared.Post;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class CourseStreamActivity extends ActionBarActivity
{
	CourseStreamActivity activity = this;
	SupportMenuItem refresh;
	ListView postList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_page);

		Button membersButton = (Button) findViewById(R.id.membersButton);
		membersButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(activity, Members.class);
				startActivity(intent);
			}
		});

		postList = (ListView) findViewById(R.id.postList);
		new PostsAsyncTask().execute("all");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stream, menu);
		refresh = (SupportMenuItem) menu.findItem(R.id.refresh);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == R.id.action_settings)
		{
			return true;
		}
		if(id == R.id.newPost)
		{
			Intent intent = new Intent(this, NewPost.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	class PostsAsyncTask extends AsyncTask<String, Void, ArrayList<Post>>
	{
		@Override
		protected void onPreExecute()
		{
			if(refresh != null)
				refresh.setActionView(R.layout.actionbar_indeterminate_progress);
		}

		@Override
		protected ArrayList<Post> doInBackground(String... params)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("username", "rom66"));//prefs.getString("loggedIn", "default")));
			nameValuePairs.add(new BasicNameValuePair("level", params[0]));
			nameValuePairs.add(new BasicNameValuePair("sort", "Popular"));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/getPosts", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());

				Gson gson = new Gson();
				Type collectionType = new TypeToken<Collection<Post>>()
				{
				}.getType();
				return gson.fromJson(response, collectionType);
			}
			catch(Exception e)
			{
				Log.e("addendum", e.getMessage() + "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(final ArrayList<Post> posts)
		{
			if(refresh != null)
				refresh.setActionView(null);

			RelativeLayout layout = (RelativeLayout) postList.getParent();
			TextView empty = (TextView) layout.findViewById(R.id.empty);

			if(posts != null && posts.size() > 0)
			{
				empty.setVisibility(View.GONE);
				postList.setVisibility(View.VISIBLE);
				Collections.sort(posts, Post.PostScoreComparator);
				if(postList.getAdapter() != null)
				{
					PostViewAdapter adapter = (PostViewAdapter) postList.getAdapter();
					adapter.clear();
					for(Post post : posts) //addAll() requires API level 11
						adapter.add(post);
					adapter.notifyDataSetChanged();
				}
				else
					postList.setAdapter(new PostViewAdapter(activity, R.layout.post, posts));

				postList.setClickable(true);
				postList.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						Intent intent = new Intent(activity, SinglePostActivity.class);
						Bundle bundle = new Bundle();
						Gson gson = new Gson();
						bundle.putString("post", gson.toJson(posts.get(position)));
						intent.putExtras(bundle);
						startActivity(intent);
					}
				});
			}
			else
			{
				empty.setVisibility(View.VISIBLE);
				postList.setVisibility(View.GONE);
			}
		}
	}
}
