package com.cs1635.classme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.shared.Comment;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditCommentDialog extends AlertDialog
{
	Context context;
	Comment comment;
	ArrayList<Comment> comments;
	ListView commentList;
	AlertDialog dialog = this;

	protected EditCommentDialog(Context con, Comment comm, ArrayList<Comment> comme, ListView commList)
	{
		super(con);
		context = con;
		comment = comm;
		comments = comme;
		commentList = commList;

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.edit_dialog, null);
		builder.setView(view);
		builder.setTitle("Edit Comment");

		final EditText editText = (EditText) view.findViewById(R.id.editText);
		editText.setText(comment.getContent());

		builder.setPositiveButton("Confirm",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				comment.setContent(editText.getText().toString());
				comment.setLastEdit(new Date());
				new EditCommentTask(context,comment,comments,commentList,dialog).execute(comment);
			}
		}).setNegativeButton("Cancel",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.show();
	}

	private class EditCommentTask extends AsyncTask<Comment,Void,Boolean>
	{
		ProgressDialog progressDialog;
		Context context;
		ArrayList<Comment> comments;
		ListView commentList;
		Comment comment;
		DialogInterface dialog;

		public EditCommentTask(Context con, Comment comme, ArrayList<Comment> comm, ListView comList, DialogInterface d)
		{
			context = con;
			comment = comme;
			comments = comm;
			commentList = comList;
			dialog = d;
		}
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
			nameValuePairs.add(new BasicNameValuePair("commentText", comments[0].getContent()));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/editComment", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());
				if(!response.equals("done"))
					return false;
			}
			catch(Exception ex)
			{
				Log.e("addendum", ex.getMessage() + "");
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success)
		{
			progressDialog.dismiss();
			dialog.dismiss();
			if(!success)
			{
				Toast.makeText(context, "Error while uploading comment", Toast.LENGTH_SHORT).show();
				return;
			}
			comments.set(comments.indexOf(comment), comment);
			((CommentViewAdapter)((HeaderViewListAdapter)commentList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		}
	}
}
