package com.cs1635.classme;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.internal.view.SupportMenuItem;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
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

class PostSearchTask extends AsyncTask<String,Void,ArrayList<Post>>
{
	Context context;
	ListView postList;
	SupportMenuItem refresh;
	int headerOffset;

	public PostSearchTask(Context c, ListView list, SupportMenuItem r, int offset)
	{
		context = c;
		postList = list;
		refresh = r;
		headerOffset = offset;
	}

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
		nameValuePairs.add(new BasicNameValuePair("requestingUser", PreferenceManager.getDefaultSharedPreferences(context).getString("loggedIn", "default")));
		nameValuePairs.add(new BasicNameValuePair("searchText",params[0]));

		try
		{
			HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/postSearch", nameValuePairs);
			String response = EntityUtils.toString(urlResponse.getEntity());

			Gson gson = new Gson();
			Type collectionType = new TypeToken<Collection<Post>>(){}.getType();
			return gson.fromJson(response, collectionType);
		}
		catch (Exception e)
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

		RelativeLayout layout = (RelativeLayout)postList.getParent();
		TextView empty = (TextView) layout.findViewById(R.id.empty);

		if(posts != null && posts.size() > 0)
		{
			Collections.sort(posts, Post.PostScoreComparator);
			if(postList.getAdapter() != null)
			{
				PostViewAdapter adapter = null;
				if(postList.getAdapter() instanceof  PostViewAdapter)
					adapter = (PostViewAdapter)postList.getAdapter();
				else if(postList.getAdapter() instanceof HeaderViewListAdapter)
					adapter = (PostViewAdapter) ((HeaderViewListAdapter) postList.getAdapter()).getWrappedAdapter();
				if(adapter == null)
					return;

				empty.setVisibility(View.GONE);
				postList.setVisibility(View.VISIBLE);
				adapter.clear();
				adapter.addAll(posts);
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
					Intent intent = new Intent(context,SinglePostActivity.class);
					Bundle bundle = new Bundle();
					Gson gson = new Gson();
					bundle.putString("post",gson.toJson(posts.get(position-headerOffset)));
					intent.putExtras(bundle);
					context.startActivity(intent);
				}
			});
		}
		else if(empty != null)
		{
			postList.setVisibility(View.GONE);
			empty.setVisibility(View.VISIBLE);
		}
	}
}