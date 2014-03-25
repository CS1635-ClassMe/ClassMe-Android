package com.cs1635.classme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.shared.Post;
import com.shared.User;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StreamActivity extends ActionBarActivity implements ActionBar.OnNavigationListener
{
	ListView postList;
	ActionBarActivity context = this;
	SharedPreferences prefs;
	ActionBar actionBar;
	SupportMenuItem refresh;
	String streamLevel = "all";
	DrawerLayout drawerLayout;
	ListView drawerList;
	ActionBarDrawerToggle drawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream);

		actionBar = getSupportActionBar();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		fillNavigationMenu();

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view)
			{
				getSupportActionBar().setTitle("");
				ActivityCompat.invalidateOptionsMenu(context); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView)
			{
				getSupportActionBar().setTitle("Addendum Mobile");
				ActivityCompat.invalidateOptionsMenu(context); // creates call to onPrepareOptionsMenu()
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		drawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		String[] strings = {"Home", "My Classes", "Search", "Chat"};
		drawerList.addHeaderView(makeHeader());
		drawerList.setAdapter(new NavigationRowAdapter(this, android.R.layout.simple_dropdown_item_1line, strings));
		// Set the list's click listener
		drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if(position == 1) //home
				{
					drawerLayout.closeDrawers();
					actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
					getSupportActionBar().setSelectedNavigationItem(0);
				}
				if(position == 2) //classes
				{
					Intent intent = new Intent(context, ClassSearchActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
				if(position == 3) //search
				{
					Intent intent = new Intent(context, SearchActivity.class);
					startActivity(intent);
				}
				if(position == 4) //chat
				{
					Intent intent = new Intent(context, ChatActivity.class);
					intent.putExtra("username","Aamir");
					startActivity(intent);
				}
			}
		});

		actionBar.setTitle("");
		postList = (ListView) findViewById(R.id.postList);

		new PostsAsyncTask().execute("all");
	}

	private View makeHeader()
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.drawer_header, null);
		layout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new ProfileTask(context).execute(prefs.getString("loggedIn", ""));
			}
		});

		RelativeLayout nameLayout = (RelativeLayout) layout.findViewById(R.id.nameLayout);
		Drawable nameBackground = nameLayout.getBackground();
		nameBackground.setAlpha(80);

		ImageView background = (ImageView) layout.findViewById(R.id.background);
		try
		{
			background.setImageBitmap(BitmapFactory.decodeStream(getResources().getAssets().open("pitt.jpg")));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		ImageView profileImage = (ImageView) layout.findViewById(R.id.profileImage);
		UrlImageViewHelper.setUrlDrawable(profileImage, "https://studentclassnet.appspot.com/addendum/getImage?username=" + prefs.getString("loggedIn", "default"));

		TextView realName = (TextView) layout.findViewById(R.id.realName);
		Gson gson = new Gson();
		User user = gson.fromJson(prefs.getString("userObject", ""), User.class);
		realName.setText(user.getFirstName() + " " + user.getLastName());
		TextView username = (TextView) layout.findViewById(R.id.username);
		username.setText(prefs.getString("loggedIn", "default"));

		return layout;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == 1)
			fillNavigationMenu();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		new PostsAsyncTask().execute(streamLevel);
	}

	private void fillNavigationMenu()
	{
		Gson gson = new Gson();
		User user = gson.fromJson(prefs.getString("userObject", ""), User.class);
		ArrayList<String> courseList = user.getCourseList();
		courseList.add(0, "All Classes");
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(new ArrayAdapter<String>(context, R.layout.actionbar_spinner_dropdown, courseList), this);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId)
	{
		Gson gson = new Gson();
		User user = gson.fromJson(prefs.getString("userObject", ""), User.class);
		ArrayList<String> courseList = user.getCourseList();
		courseList.add(0, "All Classes");

		if(courseList.get(itemPosition).equals("All Classes"))
			streamLevel = "all";
		else
			streamLevel = courseList.get(itemPosition);

		new PostsAsyncTask().execute(streamLevel);
		return true;
	}

	private class PostsAsyncTask extends AsyncTask<String, Void, ArrayList<Post>>
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
			nameValuePairs.add(new BasicNameValuePair("username", prefs.getString("loggedIn", "default")));
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
					postList.setAdapter(new PostViewAdapter(context, R.layout.post, posts));

				postList.setClickable(true);
				postList.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						Intent intent = new Intent(context, SinglePostActivity.class);
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		menu.findItem(R.id.refresh).setVisible(!drawerOpen);
		menu.findItem(R.id.newPost).setVisible(!drawerOpen);
		if(drawerOpen)
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		else
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		return super.onPrepareOptionsMenu(menu);
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
		if(drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		switch(item.getItemId())
		{
			case R.id.refresh:
			{
				new PostsAsyncTask().execute(streamLevel);
				return true;
			}
			case R.id.action_settings:
			{
				Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT).show();
				return true;
			}
			case R.id.logout:
			{
				SharedPreferences.Editor edit = prefs.edit();
				edit.remove("loggedIn");
				edit.commit();
				startActivity(new Intent(context, LoginActivity.class));
				finish();
				return true;
			}
			case R.id.newPost:
			{
				startActivity(new Intent(this, NewPost.class));
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
