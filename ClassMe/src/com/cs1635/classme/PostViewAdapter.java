package com.cs1635.classme;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

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

public class PostViewAdapter extends ArrayAdapter<Post>
{
	Context context;
	ArrayList<Post> posts;

	public PostViewAdapter(Context context, int textViewResourceId, ArrayList<Post> posts)
	{
		super(context, textViewResourceId, posts);
		this.posts = posts;
		this.context = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.post, null);

		TextView title = (TextView) v.findViewById(R.id.title);
		TextView content = (TextView) v.findViewById(R.id.content);
		TextView username = (TextView) v.findViewById(R.id.username);
		ImageView profileImage = (ImageView) v.findViewById(R.id.profileImage);
		TextView time = (TextView) v.findViewById(R.id.time);
		TextView score = (TextView) v.findViewById(R.id.score);
		ImageView voteUp = (ImageView) v.findViewById(R.id.voteUp);
		ImageView voteDown = (ImageView) v.findViewById(R.id.voteDown);
		TextView numComments = (TextView) v.findViewById(R.id.numComments);

		String streamLevel = posts.get(position).getStreamLevel();
		if(streamLevel.equals("all"))
			streamLevel = "Everyone";
		title.setText(streamLevel);

		content.setText(Html.fromHtml(posts.get(position).getPostContent(),new MyImageGetter(content,context),null));
		//content.loadDataWithBaseURL(null, posts.get(position).getPostContent(), "text/html", "utf-8", null);
		username.setText(posts.get(position).getUsername());
		String timeFormatString = "h:mm a";
		String editFormatString = "h:mm a";
		Date now = new Date(System.currentTimeMillis());
		if(posts.get(position).getPostTime().getDate() != now.getDate())
			timeFormatString = "MMM d, yyyy";
		if(posts.get(position).getLastEdit() != null && posts.get(position).getLastEdit().getDate() != now.getDate())
			editFormatString = "MMM d, yyyy";
		String timeString = String.valueOf(android.text.format.DateFormat.format(timeFormatString, posts.get(position).getPostTime()));
		if(posts.get(position).getLastEdit() != null)
			timeString += "(last edit - " + String.valueOf(android.text.format.DateFormat.format(editFormatString, posts.get(position).getLastEdit())) + ")";
		time.setText(timeString);
		UrlImageViewHelper.setUrlDrawable(profileImage, "https://studentclassnet.appspot.com/addendum/getImage?username="+posts.get(position).getUsername());

		String scoreString = posts.get(position).getUpvotes()-posts.get(position).getDownvotes() + "";
		if(posts.get(position).getUpvotes()-posts.get(position).getDownvotes() > 0)
			scoreString = "+" + scoreString;
		score.setText(scoreString);
		if(posts.get(position).isDownvoted())
			voteDown.setImageResource(R.drawable.voted_down);
		else
			voteDown.setImageResource(R.drawable.default_down);
		if(posts.get(position).isUpvoted())
			voteUp.setImageResource(R.drawable.voted_up);
		else
			voteUp.setImageResource(R.drawable.default_up);

		View.OnClickListener voteListener = new VoteListener(voteUp,voteDown,posts.get(position),v);
		voteUp.setOnClickListener(voteListener);
		voteDown.setOnClickListener(voteListener);

		numComments.setText(String.valueOf(posts.get(position).getComments().size()));

		profileImage.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new ProfileTask(context).execute(posts.get(position).getUsername());
			}
		});

		if(posts.get(position).getComments().size() > 0)
		{
			ViewFlipper commentFlipper = (ViewFlipper) v.findViewById(R.id.commentFlipper);
			commentFlipper.setVisibility(View.VISIBLE);
			LinearLayout commentProfileLayout = (LinearLayout) v.findViewById(R.id.commenterProfileLayout);
			final ArrayList<View> profileImages = new ArrayList<View>(3);
			ArrayList<Comment> comments = posts.get(position).getComments();

			int start = comments.size()-3;
			if(start < 0)
				start = 0;
			for(int i=start; i<comments.size(); i++)
			{
				View commentView = vi.inflate(R.layout.single_comment, null);
				TextView commentUsername = (TextView) commentView.findViewById(R.id.commentUsername);
				TextView commentContent = (TextView) commentView.findViewById(R.id.commentContent);
				commentUsername.setText(comments.get(i).getUsername());
				commentContent.setText(Html.fromHtml(comments.get(i).getContent(), new MyImageGetter(commentContent,context),null));
				commentFlipper.addView(commentView);

				View imageLayout = vi.inflate(R.layout.profile_image,null);
				ImageView image = (ImageView)imageLayout.findViewById(R.id.image);
				UrlImageViewHelper.setUrlDrawable(image,"https://studentclassnet.appspot.com/addendum/getImage?username="+comments.get(i).getUsername());
				profileImages.add(imageLayout);
				commentProfileLayout.addView(imageLayout);
			}

			profileImages.get(0).findViewById(R.id.underline).setVisibility(View.VISIBLE);
			if(comments.size()>1)
			{
				commentFlipper.setInAnimation(AnimationUtils.loadAnimation(context,android.R.anim.fade_in));
				commentFlipper.setOutAnimation(AnimationUtils.loadAnimation(context,android.R.anim.fade_out));
				commentFlipper.setFlipInterval(4000);
				commentFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
					int count = 1;

					public void onAnimationStart(Animation animation)
					{
						if(count == 0)
						{
							profileImages.get(count).findViewById(R.id.underline).setVisibility(View.VISIBLE);
							profileImages.get(profileImages.size()-1).findViewById(R.id.underline).setVisibility(View.GONE);
						}
						else
						{
							profileImages.get(count).findViewById(R.id.underline).setVisibility(View.VISIBLE);
							profileImages.get((count-1)).findViewById(R.id.underline).setVisibility(View.GONE);
						}
						count++;
						if(count == profileImages.size())
							count = 0;
					}
					public void onAnimationRepeat(Animation animation) {}
					public void onAnimationEnd(Animation animation) {}
				});
				commentFlipper.startFlipping();
			}
		}
		return v;
	}

	private class VoteListener implements View.OnClickListener
	{
		ImageView voteUp, voteDown;
		Post post;
		View parent;

		public VoteListener(ImageView up, ImageView down, Post p, View parent)
		{
			voteDown = down;
			voteUp = up;
			post = p;
			this.parent = parent;
		}

		@Override
		public void onClick(View v)
		{
			if(v == voteUp)
				new VoteTask(parent,post).execute(post.getPostKey(),PreferenceManager.getDefaultSharedPreferences(context).getString("loggedIn",""),"up");
			else
				new VoteTask(parent,post).execute(post.getPostKey(),PreferenceManager.getDefaultSharedPreferences(context).getString("loggedIn",""),"down");
		}
	}

	private class VoteTask extends AsyncTask<String,Void,Boolean>
	{
		View parent;
		Post post;
		String direction;

		public VoteTask(View parentLayout, Post post)
		{
			parent = parentLayout;
			this.post = post;
		}

		@Override
		protected Boolean doInBackground(String... params)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("postKey", params[0]));
			nameValuePairs.add(new BasicNameValuePair("user",params[1]));
			nameValuePairs.add(new BasicNameValuePair("direction",params[2]));
			direction = params[2];

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/vote", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());
				return Boolean.valueOf(response);
			}
			catch (Exception e)
			{
				Log.e("addendum", e.getMessage() + "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			ImageView downArrow = (ImageView) parent.findViewById(R.id.voteDown);
			ImageView upArrow = (ImageView) parent.findViewById(R.id.voteUp);
			TextView scoreLabel = (TextView) parent.findViewById(R.id.score);
			int upDownVotes = Integer.valueOf(scoreLabel.getText().toString());

			if(direction.equals("up"))
			{
				if(result)//user has not upvoted before or they have downvoted before
				{
					if(post.isDownvoted())
						upDownVotes++;
					post.setDownvoted(false);
					downArrow.setImageResource(R.drawable.default_down);
					if(post.isUpvoted())
					{
						scoreLabel.setText(String.valueOf(--upDownVotes));
						post.setUpvoted(false);
						upArrow.setImageResource(R.drawable.default_up);
					}
					else
					{
						scoreLabel.setText(String.valueOf(++upDownVotes));
						post.setUpvoted(true);
						upArrow.setImageResource(R.drawable.voted_up);
					}
				}
				else
				//user has upvoted before - undo it
				{
					scoreLabel.setText(String.valueOf(--upDownVotes));
					post.setUpvoted(false);
					upArrow.setImageResource(R.drawable.default_up);
				}
			}
			else
			{
				if(result)
				{
					if(post.isUpvoted())
						upDownVotes--;
					post.setUpvoted(false);
					upArrow.setImageResource(R.drawable.default_up);
					if(post.isDownvoted())
					{
						scoreLabel.setText(String.valueOf(++upDownVotes));
						post.setDownvoted(false);
						downArrow.setImageResource(R.drawable.default_down);
					}
					else
					{
						scoreLabel.setText(String.valueOf(--upDownVotes));
						post.setDownvoted(true);
						downArrow.setImageResource(R.drawable.voted_down);
					}
				}
				else
				{
					post.setDownvoted(false);
					scoreLabel.setText(String.valueOf(++upDownVotes));
					downArrow.setImageResource(R.drawable.default_down);
				}
			}
		}
	}
}