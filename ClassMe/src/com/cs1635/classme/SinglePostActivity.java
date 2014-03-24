package com.cs1635.classme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.shared.Comment;
import com.shared.Post;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SinglePostActivity extends ActionBarActivity
{
	SinglePostActivity context = this;
	Post post;
	ListView commentList;
	View postView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_post);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //no idea why the keyboard appears, this hides it
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle bundle = getIntent().getExtras();
		if(bundle == null)
		{
			Toast.makeText(this, "Post no longer available", Toast.LENGTH_SHORT).show();
			finish();
		}

		Gson gson = new Gson();
		post = gson.fromJson(bundle.getString("post"), Post.class);
		postView = setupPost(post);
		final EditText commentBox = (EditText) findViewById(R.id.addComment);
		ImageView send = (ImageView) findViewById(R.id.send);
		commentList = (ListView) findViewById(R.id.commentList);

		commentList.setClickable(true);
		commentList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				new CommentOptionsDialog(context, post.getComments().get(position - 1), post.getComments(), commentList); //position-1 because of the header view
			}
		});

		send.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new CommentUploadTask(post.getComments(), commentList, commentBox).execute(commentBox.getText().toString(), post.getPostKey());
			}
		});

		commentList.addHeaderView(postView);
		commentList.setAdapter(new CommentViewAdapter(this, R.id.commentList, post.getComments(), post));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != Activity.RESULT_OK)
			return;

		if(requestCode == 1) //we just edited the post
		{
			post = (Post) data.getExtras().getSerializable("post");
			commentList.removeHeaderView(postView);
			commentList.addHeaderView(setupPost(post));
		}
	}

	public View setupPost(final Post post)
	{
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.single_post, null);

		TextView title = (TextView) v.findViewById(R.id.title);
		WebView content = (WebView) v.findViewById(R.id.content);
		TextView username = (TextView) v.findViewById(R.id.username);
		ImageView profileImage = (ImageView) v.findViewById(R.id.profileImage);
		TextView time = (TextView) v.findViewById(R.id.time);
		TextView score = (TextView) v.findViewById(R.id.score);
		TextView numComments = (TextView) v.findViewById(R.id.numComments);

		String streamLevel = post.getStreamLevel();
		if(streamLevel.equals("all"))
			streamLevel = "Everyone";
		title.setText(streamLevel);

		content.loadDataWithBaseURL(null, post.getPostContent(), "text/html", "utf-8", null);
		username.setText(post.getUsername());
		String timeFormatString = "h:mm a";
		String editFormatString = "h:mm a";
		Date now = new Date(System.currentTimeMillis());
		if(post.getPostTime().getDate() != now.getDate())
			timeFormatString = "MMM d, yyyy";
		if(post.getLastEdit() != null && post.getLastEdit().getDate() != now.getDate())
			editFormatString = "MMM d, yyyy";
		String timeString = String.valueOf(android.text.format.DateFormat.format(timeFormatString, post.getPostTime()));
		if(post.getLastEdit() != null)
			timeString += "(last edit - " + String.valueOf(android.text.format.DateFormat.format(editFormatString, post.getLastEdit())) + ")";
		time.setText(timeString);
		UrlImageViewHelper.setUrlDrawable(profileImage, "https://studentclassnet.appspot.com/addendum/getImage?username=" + post.getUsername());
		String scoreString = post.getUpvotes() - post.getDownvotes() + "";
		if(post.getUpvotes() - post.getDownvotes() > 0)
			scoreString = "+" + scoreString;
		score.setText(scoreString);
		numComments.setText(String.valueOf(post.getComments().size()));

		profileImage.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new ProfileTask(context).execute(post.getUsername());
			}
		});

		if(post.getAttachmentKeys().size() > 0)
		{
			LinearLayout attachmentsLayout = (LinearLayout) v.findViewById(R.id.attachmentsLayout);
			attachmentsLayout.setVisibility(View.VISIBLE);

			for(int i=0; i<post.getAttachmentNames().size(); i++)
			{
				String key = post.getAttachmentKeys().get(i);
				String name = post.getAttachmentNames().get(i);
				TextView attachment = new TextView(context);
				attachment.setText(Html.fromHtml("<a href=\"https://studentclassnet.appspot.com/addendum/getImage?key=" + key + "\">" + name + "</a>"));
				attachment.setMovementMethod(LinkMovementMethod.getInstance());
				attachmentsLayout.addView(attachment);
			}
		}

		return v;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.single_post, menu);
		SupportMenuItem edit = (SupportMenuItem) menu.findItem(R.id.edit);
		SupportMenuItem delete = (SupportMenuItem) menu.findItem(R.id.delete);
		SupportMenuItem flag = (SupportMenuItem) menu.findItem(R.id.flag);

		if(PreferenceManager.getDefaultSharedPreferences(this).getString("loggedIn", "").equals(post.getUsername()))
			flag.setVisible(false);
		else
		{
			edit.setVisible(false);
			delete.setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.edit:
			{
				Intent intent = new Intent(this,NewPost.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("post",post);
				intent.putExtra("bundle",bundle);
				startActivityForResult(intent, 1);
				return true;
			}
			case R.id.delete:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Delete")
						.setMessage("Do you want to delete this post?")
						.setPositiveButton("OK", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
								new DeletePostTask().execute(post);
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								dialog.dismiss();
							}
						}).show();
				return true;
			}
			case R.id.flag:
			{
				return true;
			}
			case android.R.id.home:
			{
				finish();
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class CommentUploadTask extends AsyncTask<String, Void, Comment>
	{
		ArrayList<Comment> comments;
		ListView commentList;
		ProgressDialog progressDialog;
		EditText commentBox;

		public CommentUploadTask(ArrayList<Comment> c, ListView l, EditText cb)
		{
			comments = c;
			commentList = l;
			commentBox = cb;
		}

		@Override
		protected Comment doInBackground(String... params)
		{
			Comment comment = new Comment();
			comment.setCommentTime(new Date());
			comment.setContent(params[0]);
			comment.setUsername(PreferenceManager.getDefaultSharedPreferences(context).getString("loggedIn", ""));
			Gson gson = new Gson();

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("comment", gson.toJson(comment)));
			nameValuePairs.add(new BasicNameValuePair("postKey", params[1]));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/uploadComment", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());
				if(response != null)
					comment.setCommentKey(response);
				else
					return null;
			}
			catch(Exception ex)
			{
				Log.e("Addendum", ex.getMessage() + "");
			}

			return comment;
		}

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Sending...", true);
		}

		@Override
		protected void onPostExecute(Comment comment)
		{
			progressDialog.hide();

			if(comment != null)
			{
				commentBox.setText("");
				comments.add(comment);
				((CommentViewAdapter) ((HeaderViewListAdapter) commentList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
			}
			else
				Toast.makeText(context, "Problem uploading your comment", Toast.LENGTH_SHORT).show();
		}
	}

	private class DeletePostTask extends AsyncTask<Post, Void, Boolean>
	{
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Deleting Post...", true);
		}

		@Override
		protected Boolean doInBackground(Post... post)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("postKey", post[0].getPostKey()));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/deletePost", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());
				if(!response.equals("done"))
					return false;
			}
			catch(Exception ex)
			{
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			progressDialog.dismiss();
			if(!success)
			{
				Toast.makeText(context, "Error while deleting post", Toast.LENGTH_SHORT).show();
				return;
			}
			context.finish();
		}
	}
}