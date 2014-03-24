package com.cs1635.classme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shared.Post;
import com.shared.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apmem.tools.layouts.FlowLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NewPost extends ActionBarActivity
{

	Activity context = this;
	EditText post;
	Uri captureUri;
	ArrayList<String> attachmentNames = new ArrayList<String>(), attachmentKeys = new ArrayList<String>(), deleteKeys = new ArrayList<String>();
	FlowLayout attachmentsPanel;
	Post editPost;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_post);

		Bundle bundle = getIntent().getBundleExtra("bundle");
		if(bundle != null)
			editPost = (Post) bundle.getSerializable("post");

		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "temp.jpg");
		captureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		getSupportActionBar().hide();

		Gson gson = new Gson();
		final User user = gson.fromJson(PreferenceManager.getDefaultSharedPreferences(this).getString("userObject", ""), User.class);

		final AlphaAnimation alphaUp = new AlphaAnimation(.5f, 1f); //hack for view.setAlpha in api < 11
		alphaUp.setDuration(0);
		alphaUp.setFillAfter(true);

		final AlphaAnimation alphaDown = new AlphaAnimation(1f, .5f);
		alphaDown.setDuration(0);
		alphaDown.setFillAfter(true);

		final LinearLayout shareLayout = (LinearLayout) findViewById(R.id.shareLayout);
		shareLayout.startAnimation(alphaDown);

		post = (EditText) findViewById(R.id.postText);
		post.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				if(post.getText().length() > 0)
					shareLayout.startAnimation(alphaUp);
				else
					shareLayout.startAnimation(alphaDown);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
			}
		});
		if(editPost != null)
			post.setText(editPost.getPostContent());

		LinearLayout photoLayout = (LinearLayout) findViewById(R.id.photoLayout);
		photoLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new PhotoDialog(context, captureUri);
			}
		});
		LinearLayout linkLayout = (LinearLayout) findViewById(R.id.linkLayout);
		linkLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new LinkDialog(context, post);
			}
		});
		LinearLayout fileLayout = (LinearLayout) findViewById(R.id.fileLayout);
		fileLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent chooseFile;
				Intent intent;
				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("file/*");
				intent = Intent.createChooser(chooseFile, "Choose a file");
				startActivityForResult(intent, 3);
			}
		});

		final Spinner spinner = (Spinner) findViewById(R.id.classSpinner);
		ArrayAdapter<String> adapter = null;
		if(editPost == null)
		{
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, user.getCourseList());
			adapter.insert("Everyone", 0);
		}
		else
		{
			ArrayList<String> course = new ArrayList<String>();
			if(editPost.getStreamLevel().equals("all"))
				course.add("Everyone");
			else
				course.add(editPost.getStreamLevel());
			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, course);
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		LinearLayout cancelLayout = (LinearLayout) findViewById(R.id.cancelLayout);
		cancelLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(editPost != null && editPost.getPostContent().equals(post.getText().toString()))
					finish();
				else if(post.getText().toString().length() != 0)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(context)
							.setTitle("Addendum Mobile")
							.setMessage("Do you want to discard this post?")
							.setNegativeButton("No", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									dialog.dismiss();
								}
							})
							.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									context.finish();
								}
							});
					builder.create().show();
				}
				else
					finish();
			}
		});
		shareLayout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(editPost != null)
				{
					post.setText(post.getText().toString().replaceAll("\n","<br>"));
					editPost.setPostContent(post.getText().toString());
					editPost.setAttachmentKeys(attachmentKeys);
					editPost.setAttachmentNames(attachmentNames);
					new EditPostTask(editPost).execute();
				}
				else
				{
					if(post.getText().length() > 0)
						new PostUploadTask().execute(post.getText().toString().replaceAll("\n", "<br>"), user.getUsername(), spinner.getSelectedItem().toString());
				}
			}
		});

		attachmentsPanel = (FlowLayout) findViewById(R.id.attachmentsPanel);
		if(editPost != null)
		{
			attachmentNames = editPost.getAttachmentNames();
			attachmentKeys = editPost.getAttachmentKeys();

			for(int i = 0; i < attachmentKeys.size(); i++)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View layout = inflater.inflate(R.layout.attachment, null);
				final TextView name = (TextView) layout.findViewById(R.id.fileName);
				name.setText(attachmentNames.get(i));
				ImageView delete = (ImageView) layout.findViewById(R.id.delete);
				delete.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						for(int i = 0; i < attachmentNames.size(); i++)
						{
							if(attachmentNames.get(i).equals(name.getText()))
							{
								attachmentNames.remove(i);
								String key = attachmentKeys.remove(i);
								attachmentsPanel.removeView(layout);
								new DeleteAttachmentsTask().execute(key);
							}
						}
					}
				});
				attachmentsPanel.addView(layout);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != Activity.RESULT_OK)
			return;

		String filePath = null;
		Uri uri = null;
		if(requestCode == 0)
			uri = captureUri;
		if(requestCode == 1 || requestCode == 3)
			uri = data.getData();
		if(requestCode == 0 || requestCode == 1 || requestCode == 3)
		{
			try
			{
				String[] proj = {MediaStore.Images.Media.DATA};
				Cursor cursor = managedQuery(uri, proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				filePath = cursor.getString(column_index);
			}
			catch(Exception e)
			{
				filePath = uri.getPath();
			}
		}

		new ImageUploadTask(requestCode == 3).execute(filePath);
	}

	private class PostUploadTask extends AsyncTask<String, Void, Boolean>
	{
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Sending...", true);
		}

		@Override
		protected Boolean doInBackground(String... params)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("text", params[0]));
			nameValuePairs.add(new BasicNameValuePair("username", params[1]));
			nameValuePairs.add(new BasicNameValuePair("level", params[2].equals("Everyone") ? "all" : params[2]));
			Gson gson = new Gson();
			nameValuePairs.add(new BasicNameValuePair("time", gson.toJson(new Date())));
			nameValuePairs.add(new BasicNameValuePair("attachmentKeys", gson.toJson(attachmentKeys)));
			nameValuePairs.add(new BasicNameValuePair("attachmentNames", gson.toJson(attachmentNames)));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/uploadPost", nameValuePairs);
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
			if(success)
			{
				for(String key : deleteKeys)
					new DeleteAttachmentsTask().execute(key);
				context.finish();
			}
			else
			{
				Toast.makeText(context, "There was a problem uploading your post.  Please try again.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class ImageUploadTask extends AsyncTask<String, Void, String>
	{
		ProgressDialog progressDialog;
		boolean isAttachment;
		ImageUploadTask task = this;

		public ImageUploadTask(boolean isAttachment)
		{
			this.isAttachment = isAttachment;
		}

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Uploading photo to server, please wait...", true);
		}

		@Override
		protected String doInBackground(String... arg0)
		{
			if(arg0[0] == null)
				return null;

			HttpResponse response = null;
			String resp = null;

			String responseUrl = null;
			try
			{
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);

				// execute request
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/imageUpload", nameValuePairs);
				HttpEntity entity = urlResponse.getEntity();
				if(entity != null)
				{
					responseUrl = EntityUtils.toString(entity);
					responseUrl = responseUrl.substring(0, responseUrl.lastIndexOf("/") + 1);
				}
			}
			catch(Exception ex)
			{
				Log.e("Addendum", "" + ex.getMessage());
			}

			if(responseUrl == null) return null;

			try
			{
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(responseUrl);
				MultipartEntity entity = new MultipartEntity();
				File image = new File(arg0[0]);
				String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(arg0[0]));
				entity.addPart("data", new FileBody(image, mimeType));
				httppost.setEntity(entity);
				response = httpclient.execute(httppost);
				resp = EntityUtils.toString(response.getEntity());
				if(isAttachment && !isCancelled())
				{
					attachmentNames.add(image.getName());
					attachmentKeys.add(resp);
				}
			}
			catch(IOException e)
			{
				Log.e("Addendum", e.getMessage() + "");
			}
			return resp;
		}

		@Override
		public void onPostExecute(String response)
		{
			progressDialog.dismiss();
			if(response != null)
			{
				if(isAttachment)
				{
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					final View layout = inflater.inflate(R.layout.attachment, null);
					final TextView name = (TextView) layout.findViewById(R.id.fileName);
					name.setText(attachmentNames.get(attachmentNames.size() - 1));
					ImageView delete = (ImageView) layout.findViewById(R.id.delete);
					delete.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							for(int i = 0; i < attachmentNames.size(); i++)
							{
								if(attachmentNames.get(i).equals(name.getText()))
								{
									attachmentNames.remove(i);
									String key = attachmentKeys.remove(i);
									attachmentsPanel.removeView(layout);
									deleteKeys.add(key);
								}
							}
						}
					});
					attachmentsPanel.addView(layout);
				}
				else
					post.setText(post.getText() + "<img src=\"http://studentclassnet.appspot.com/addendum/getImage?key=" + response + "\">");
			}
			else
				Toast.makeText(context, "Upload error, please check your connection and try again", Toast.LENGTH_SHORT).show();
		}
	}

	class DeleteAttachmentsTask extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... params)
		{
			String responseUrl = null;
			try
			{
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("key", params[0]));
				AppEngineClient.makeRequest("/addendum/deleteAttachment", nameValuePairs);
			}
			catch(Exception ex)
			{
				Log.e("Addendum", "" + ex.getMessage());
			}
			return null;
		}
	}

	private class EditPostTask extends AsyncTask<Void,Void,Boolean>
	{
		ProgressDialog progressDialog;
		Post editPost;

		public EditPostTask(Post editPost)
		{
			this.editPost = editPost;
		}	
		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Sending...", true);
		}
		@Override
		protected Boolean doInBackground(Void...v)
		{
			Gson gson = new Gson();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("html", editPost.getPostContent()));
			nameValuePairs.add(new BasicNameValuePair("postKey", editPost.getPostKey()));
			nameValuePairs.add(new BasicNameValuePair("attachmentKeys", gson.toJson(attachmentKeys)));
			nameValuePairs.add(new BasicNameValuePair("attachmentNames", gson.toJson(attachmentNames)));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/editPost", nameValuePairs);
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
			if(!success)
			{
				Toast.makeText(context, "Error while uploading post", Toast.LENGTH_SHORT).show();
				return;
			}
			else
			{
				for(String key : deleteKeys)
					new DeleteAttachmentsTask().execute(key);

				Bundle bundle = new Bundle();
				bundle.putSerializable("post",editPost);
				Intent result = new Intent();
				result.putExtras(bundle);
				setResult(Activity.RESULT_OK, result);
				context.finish();
			}
		}
	}
}
