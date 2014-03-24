package com.cs1635.classme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shared.Course;
import com.shared.User;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends ArrayAdapter<Course>
{
	Context context;
	ArrayList<Course> courses;
	SharedPreferences prefs;
	User user;

	public CourseAdapter(Context context, int textViewResourceId, ArrayList<Course> courses)
	{
		super(context, textViewResourceId, courses);
		this.courses = courses;
		this.context = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.class_row, null);

		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		final Course course = courses.get(position);

		TextView subjectCode = (TextView) layout.findViewById(R.id.subjectCode);
		TextView courseNumber = (TextView) layout.findViewById(R.id.courseNumber);
		TextView courseName = (TextView) layout.findViewById(R.id.courseName);
		TextView description = (TextView) layout.findViewById(R.id.description);
		ImageView delete = (ImageView) layout.findViewById(R.id.deleteClass);

		subjectCode.setText(course.getSubjectCode());
		courseNumber.setText(String.valueOf(course.getCourseNumber()));
		courseName.setText(course.getCourseName());
		description.setText(course.getCourseDescription());

		String courseString = course.getSubjectCode()+course.getCourseNumber();
		Gson gson = new Gson();
		User user = gson.fromJson(prefs.getString("userObject",""),User.class);
		if(user.getCourseList().contains(courseString))
		{
			delete.setImageResource(R.drawable.delete_class);
			delete.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					new AlertDialog.Builder(context)
							.setTitle("Delete Course")
							.setMessage("Are you sure you want to remove this course from your profile?  This will hide all posts shared to this group from your stream.")
							.setPositiveButton("Yes", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									new RemoveCourseTask(position).execute(course.getSubjectCode()+course.getCourseNumber());
								}

							})
							.setNegativeButton("No", null)
							.show();
				}
			});
		}
		else
		{
			delete.setImageResource(R.drawable.add_class);
			delete.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					new AddCourseTask().execute(course.getSubjectCode()+course.getCourseNumber());
				}
			});
		}

		return layout;
	}

	private class RemoveCourseTask extends AsyncTask<String,Void,String>
	{
		int position;
		ProgressDialog progressDialog;
		String course;

		public RemoveCourseTask(int position)
		{
			this.position = position;
		}

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Removing Course...", true);
		}

		@Override
		protected String doInBackground(String... params)
		{
			course = params[0];
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("username",prefs.getString("loggedIn", "")));
			nameValuePairs.add(new BasicNameValuePair("method","delete"));
			nameValuePairs.add(new BasicNameValuePair("course",params[0]));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/addDeleteCourses", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());

				return response;
			}
			catch (Exception e)
			{
				Log.e("addendum", e.getMessage() + "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			progressDialog.dismiss();

			if(result.equals("done"))
			{
				courses.remove(position);
				notifyDataSetChanged();
				user.getCourseList().remove(course);
			}
		}
	}

	private class AddCourseTask extends AsyncTask<String,Void,String>
	{
		ProgressDialog progressDialog;
		String course;

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context, "", "Adding Course...", true);
		}

		@Override
		protected String doInBackground(String... params)
		{
			course = params[0];
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("username",prefs.getString("loggedIn", "")));
			nameValuePairs.add(new BasicNameValuePair("method","add"));
			nameValuePairs.add(new BasicNameValuePair("course",params[0]));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/addDeleteCourses", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());

				return response;
			}
			catch (Exception e)
			{
				Log.e("addendum", e.getMessage() + "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			progressDialog.dismiss();

			if(result.equals("done"))
			{
				notifyDataSetChanged();
				user.getCourseList().add(course);
				Toast.makeText(context,"Added " + course + " to your list of courses",Toast.LENGTH_SHORT).show();
			}
		}
	}
}