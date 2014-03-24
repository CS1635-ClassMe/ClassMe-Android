package com.cs1635.classme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.shared.Comment;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentOptionsDialog extends AlertDialog
{
	Context context;
	Comment comment;
	ArrayList<Comment> comments;
	ListView commentList;

	protected CommentOptionsDialog(final Context context, Comment c, final ArrayList<Comment> comments, final ListView commentList)
	{
		super(context);
		this.context = context;
		this.comment = c;
		this.comments = comments;
		this.commentList = commentList;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Comment Options");
		String plusString = "+1 comment";
		if(comment.isPlusOned())
			plusString = "Remove +1";
		if(comment.getUsername().equals(PreferenceManager.getDefaultSharedPreferences(context).getString("loggedIn","")))
		{
			builder.setItems(new String[]{plusString,"Edit comment","Delete comment"}, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					if(which == 0)
						new PlusOneTask().execute(comment);
					if(which == 1)
						new EditCommentDialog(context,comment, comments, commentList);
					if(which == 2)
						new DeleteCommentTask().execute(comment);

					dialog.dismiss();
				}
			});
		}
		else
		{
			builder.setItems(new String[]{plusString,"Flag as inappropriate"}, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					if(which == 0)
						new PlusOneTask().execute(comment);
					if(which == 1)
						new FlagCommentTask().execute(comment);

					dialog.dismiss();
				}
			});
		}

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private class PlusOneTask extends AsyncTask<Comment,Void,Boolean>
	{
		@Override
		protected Boolean doInBackground(Comment...comments)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("commentKey", comments[0].getCommentKey()));
			nameValuePairs.add(new BasicNameValuePair("user", PreferenceManager.getDefaultSharedPreferences(context).getString("loggedIn","")));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/plusOne", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());
				return Boolean.valueOf(response);
			}
			catch(Exception ex){}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			comments.get(comments.indexOf(comment)).setPlusOned(success);
			if(success)
				comments.get(comments.indexOf(comment)).setPlusOnes(comments.get(comments.indexOf(comment)).getPlusOnes()+1);
			else
				comments.get(comments.indexOf(comment)).setPlusOnes(comments.get(comments.indexOf(comment)).getPlusOnes()-1);
			((CommentViewAdapter)((HeaderViewListAdapter)commentList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		}
	}

	private class DeleteCommentTask extends AsyncTask<Comment,Void,Boolean>
	{
		ProgressDialog progressDialog;
		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Deleting Comment...", true);
		}
		@Override
		protected Boolean doInBackground(Comment...comments)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("commentKey", comments[0].getCommentKey()));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/deleteComment", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());
				if(!response.equals("done"))
					return false;
			}
			catch(Exception ex){}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			progressDialog.dismiss();
			if(!success)
			{
				Toast.makeText(context,"Error while deleting comment",Toast.LENGTH_SHORT).show();
				return;
			}
			comments.remove(comment);
			((CommentViewAdapter)((HeaderViewListAdapter)commentList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		}
	}

	private class FlagCommentTask extends AsyncTask<Comment,Void,Boolean>
	{
		ProgressDialog progressDialog;
		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Sending...", true);
		}
		@Override
		protected Boolean doInBackground(Comment...comments)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("commentKey", comments[0].getCommentKey()));
			nameValuePairs.add(new BasicNameValuePair("reason", "dunno"));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/flagComment", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());
				if(!response.equals("done"))
					return false;
			}
			catch(Exception ex){}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			progressDialog.dismiss();
			if(!success)
			{
				Toast.makeText(context,"Error while flagging comment",Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}
}