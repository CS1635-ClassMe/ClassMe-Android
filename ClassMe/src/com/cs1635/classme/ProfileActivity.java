package com.cs1635.classme;

import android.app.Activity;
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
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.shared.Achievement;
import com.shared.Post;
import com.shared.User;
import com.shared.UserProfile;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProfileActivity extends ActionBarActivity
{
	Activity context = this;
	SharedPreferences prefs;
	DrawerLayout drawerLayout;
	ActionBarDrawerToggle drawerToggle;
	ActionBar actionBar;
	ListView drawerList, profileList;
	SupportMenuItem refresh;
	UserProfile userProfile;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_activity);

		Gson gson = new Gson();
		userProfile = gson.fromJson((String)getIntent().getExtras().get("userProfile"),UserProfile.class);

		actionBar = getSupportActionBar();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		profileList = (ListView)findViewById(R.id.profileList);
		profileList.addHeaderView(makeProfileHeader(),null,false);
		profileList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new String[]{}));

		if(userProfile.getUsername().equals(prefs.getString("loggedIn","")))
		{
			drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
			{
				/** Called when a drawer has settled in a completely closed state. */
				public void onDrawerClosed(View view)
				{
					ActivityCompat.invalidateOptionsMenu(context); // creates call to onPrepareOptionsMenu()
				}

				/** Called when a drawer has settled in a completely open state. */
				public void onDrawerOpened(View drawerView)
				{
					ActivityCompat.invalidateOptionsMenu(context); // creates call to onPrepareOptionsMenu()
				}
			};
			drawerLayout.setDrawerListener(drawerToggle);
			drawerList = (ListView) findViewById(R.id.left_drawer);

			// Set the adapter for the list view
			String[] strings = {"Home", "My Classes", "SearchActivity"};
			drawerList.addHeaderView(makeHeader());
			drawerList.setAdapter(new NavigationRowAdapter(context,0,strings));
			// Set the list's click listener
			drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					if(position == 1) //home
					{
						Intent intent = new Intent(context, StreamActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					if(position == 2) //classes
					{

					}
					if(position == 3) //search
					{
						Intent intent = new Intent(context, OldSearchActivity.class);
						startActivity(intent);
					}
				}
			});
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if(userProfile.getName() != null)
			actionBar.setTitle(userProfile.getName());
		else
			actionBar.setTitle(userProfile.getUsername());
	}

	private View makeProfileHeader()
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.profile_header, null);
		if(layout != null)
		{
			ImageView profileImage = (ImageView) layout.findViewById(R.id.profileImage);
			UrlImageViewHelper.setUrlDrawable(profileImage,"https://studentclassnet.appspot.com/addendum/getImage?username="+userProfile.getUsername());

			TextView realName = (TextView) layout.findViewById(R.id.realName);
			realName.setText(userProfile.getName());

			TextView username = (TextView) layout.findViewById(R.id.username);
			username.setText(userProfile.getUsername());

			TextView about = (TextView) layout.findViewById(R.id.about);
			about.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					resetUnderline(layout,"About");
				}
			});
			TextView trophies = (TextView) layout.findViewById(R.id.trophies);
			trophies.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					resetUnderline(layout,"Trophies");
					profileList.setAdapter(new TrophyAdapter(context,0,new ArrayList<Achievement>()));
					new TrophyTask(context,profileList,refresh).execute(userProfile.getUsername());
				}
			});
			TextView posts = (TextView) layout.findViewById(R.id.posts);
			posts.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					resetUnderline(layout,"Posts");
					profileList.setAdapter(new PostViewAdapter(context,0,new ArrayList<Post>()));
					new PostSearchTask(context,profileList,refresh,1).execute("username:"+userProfile.getUsername());
				}
			});
			TextView classes = (TextView) layout.findViewById(R.id.classes);
			classes.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					resetUnderline(layout,"Classes");
					profileList.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, userProfile.getCourseList()));
				}
			});
		}

		resetUnderline(layout,"About");
		return layout;
	}

	private void resetUnderline(View layout, String clicked)
	{
		View aboutUnderline = layout.findViewById(R.id.aboutUnderline);
		View trophiesUnderline = layout.findViewById(R.id.trophiesUnderline);
		View postsUnderline = layout.findViewById(R.id.postsUnderline);
		View classesUnderline = layout.findViewById(R.id.classesUnderline);

		aboutUnderline.setVisibility(View.INVISIBLE);
		trophiesUnderline.setVisibility(View.INVISIBLE);
		postsUnderline.setVisibility(View.INVISIBLE);
		classesUnderline.setVisibility(View.INVISIBLE);

		if(clicked.equals("About"))
			aboutUnderline.setVisibility(View.VISIBLE);
		if(clicked.equals("Trophies"))
			trophiesUnderline.setVisibility(View.VISIBLE);
		if(clicked.equals("Posts"))
			postsUnderline.setVisibility(View.VISIBLE);
		if(clicked.equals("Classes"))
			classesUnderline.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);

		refresh = (SupportMenuItem) menu.findItem(R.id.refresh);
		refresh.setVisible(false);

		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if(drawerToggle != null)
			drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		if(drawerToggle != null)
			drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(drawerToggle != null && drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}

		switch(item.getItemId())
		{
			case android.R.id.home:
			{
				finish();
			}
			default:
				return super.onOptionsItemSelected(item);
		}
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
				drawerLayout.closeDrawers();
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

	class TrophyTask extends AsyncTask<String,Void,ArrayList<Achievement>>
	{
		Context context;
		ListView postList;
		SupportMenuItem refresh;

		public TrophyTask(Context c, ListView list, SupportMenuItem r)
		{
			context = c;
			postList = list;
			refresh = r;
		}

		@Override
		protected void onPreExecute()
		{
			if(refresh != null)
			{
				refresh.setVisible(true);
				refresh.setActionView(R.layout.actionbar_indeterminate_progress);
			}
		}

		@Override
		protected ArrayList<Achievement> doInBackground(String... params)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("type", "earned"));
			nameValuePairs.add(new BasicNameValuePair("username",params[0]));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/getAchievements", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());

				Gson gson = new Gson();
				Type collectionType = new TypeToken<Collection<Achievement>>(){}.getType();
				return gson.fromJson(response, collectionType);
			}
			catch (Exception e)
			{
				Log.e("addendum", e.getMessage() + "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(final ArrayList<Achievement> achievements)
		{
			if(refresh != null)
			{
				refresh.setVisible(false);
				refresh.setActionView(null);
			}

			RelativeLayout layout = (RelativeLayout)postList.getParent();
			TextView empty = (TextView) layout.findViewById(R.id.empty);

			if(achievements != null && achievements.size() > 0)
			{
				if(postList.getAdapter() != null)
				{
					TrophyAdapter adapter = null;
					if(postList.getAdapter() instanceof  TrophyAdapter)
						adapter = (TrophyAdapter)postList.getAdapter();
					else if(postList.getAdapter() instanceof HeaderViewListAdapter)
						adapter = (TrophyAdapter) ((HeaderViewListAdapter) postList.getAdapter()).getWrappedAdapter();
					if(adapter == null)
						return;

					empty.setVisibility(View.GONE);
					postList.setVisibility(View.VISIBLE);
					adapter.clear();
					for(Achievement achievement : achievements) //adapter.addAll requires API level 11
						adapter.add(achievement);
					adapter.notifyDataSetChanged();
				}
				else
					postList.setAdapter(new TrophyAdapter(context, R.layout.post, achievements));
			}
		}
	}
}
