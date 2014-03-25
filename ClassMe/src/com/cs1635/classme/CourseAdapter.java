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
		View layout;
		if(convertView != null)
			layout = convertView;
		else
			layout = inflater.inflate(R.layout.class_row, null);

		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		final Course course = courses.get(position);

		TextView courseName = (TextView) layout.findViewById(R.id.courseName);
		TextView unread = (TextView) layout.findViewById(R.id.unread);

		courseName.setText(course.getCourseName());
		unread.setText(course.getCourseDescription());

		String courseString = course.getSubjectCode()+course.getCourseNumber();
		Gson gson = new Gson();
		User user = gson.fromJson(prefs.getString("userObject",""),User.class);

		return layout;
	}
}