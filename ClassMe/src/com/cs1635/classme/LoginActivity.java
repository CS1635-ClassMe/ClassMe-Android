package com.cs1635.classme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.shared.User;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity
{
	Context context = this;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String username;
	private String mPassword;

	// UI references.
	private EditText usernameView;
	private EditText passwordEdit;

	private EditText firstName, lastName, registerEmail, registerUsername, registerPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(true || prefs.contains("loggedIn")) //TODO: temporary for demo
		{
			Intent intent = new Intent(this, HomeActivity.class);
			startActivity(intent);
			finish();
		}

		setContentView(R.layout.activity_login);

		// Set up the login form.
		usernameView = (EditText) findViewById(R.id.username);
		passwordEdit = (EditText) findViewById(R.id.password);
		passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
			{
				if(id == R.id.login || id == EditorInfo.IME_NULL)
				{
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				attemptLogin();
			}
		});

		firstName = (EditText) findViewById(R.id.firstName);
		lastName = (EditText) findViewById(R.id.lastName);
		registerEmail = (EditText) findViewById(R.id.registerEmail);
		registerUsername = (EditText) findViewById(R.id.registerUsername);
		registerPassword = (EditText) findViewById(R.id.registerPassword);
		registerPassword.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
			{
				if(id == R.id.register || id == EditorInfo.IME_NULL)
				{
					attemptRegister();
					return true;
				}
				return false;
			}
		});
		Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				attemptRegister();
			}
		});
	}

	private void attemptRegister()
	{
		if(firstName.getText().length() < 1)
		{
			firstName.setError("Cannot be empty");
			firstName.requestFocus();
			return;
		}
		if(lastName.getText().length() < 1)
		{
			lastName.setError("Cannot be empty");
			lastName.requestFocus();
			return;
		}
		if(registerEmail.getText().length() < 1)
		{
			registerEmail.setError("Cannot be empty");
			registerEmail.requestFocus();
			return;
		}
		if(registerUsername.getText().length() < 1)
		{
			registerUsername.setError("Cannot be empty");
			registerUsername.requestFocus();
			return;
		}
		if(registerPassword.getText().length() < 1)
		{
			registerPassword.setError("Cannot be empty");
			registerPassword.requestFocus();
			return;
		}
		new NewUserTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin()
	{
		if(mAuthTask != null)
		{
			return;
		}

		// Reset errors.
		usernameView.setError(null);
		passwordEdit.setError(null);

		// Store values at the time of the login attempt.
		username = usernameView.getText().toString();
		mPassword = passwordEdit.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if(TextUtils.isEmpty(mPassword))
		{
			passwordEdit.setError(getString(R.string.error_field_required));
			focusView = passwordEdit;
			cancel = true;
		}

		// Check for a valid email address.
		if(TextUtils.isEmpty(username))
		{
			usernameView.setError(getString(R.string.error_field_required));
			focusView = usernameView;
			cancel = true;
		}

		if(cancel)
		{
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else
		{
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, User>
	{
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(context,"","Signing In...",true);
		}

		@Override
		protected User doInBackground(Void... params)
		{

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", mPassword));

			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/doLogin", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());

				Gson gson = new Gson();
				User user = gson.fromJson(response, User.class);
				return user;
			}
			catch(Exception ex)
			{
				return null;
			}
		}

		@Override
		protected void onPostExecute(User user)
		{
			mAuthTask = null;
			progressDialog.dismiss();

			if(user != null)
			{
				SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
				edit.putString("loggedIn", user.getUsername());
				Gson gson = new Gson();
				edit.putString("userObject", gson.toJson(user));
				edit.commit();
				Intent intent = new Intent(context, StreamActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			else
			{
				passwordEdit.setError(getString(R.string.error_incorrect_password));
				passwordEdit.requestFocus();
			}
		}
	}

	private class NewUserTask extends AsyncTask<Void, Void, String>
	{
		ProgressDialog dialog;

		@Override
		protected void onPreExecute()
		{
			dialog = ProgressDialog.show(context, "", "Sending...", true);
		}

		@Override
		protected String doInBackground(Void... params)
		{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("username", registerUsername.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("email", registerEmail.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("password", registerPassword.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("firstName", firstName.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("lastName", lastName.getText().toString()));


			try
			{
				HttpResponse urlResponse = AppEngineClient.makeRequest("/addendum/createUser", nameValuePairs);
				String response = EntityUtils.toString(urlResponse.getEntity());

				return response;
			}
			catch(Exception e)
			{
				Log.e("addendum", e.getMessage() + "");
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result)
		{
			dialog.hide();
			if(result != null)
			{
				if(result.equals("success"))
				{
					firstName.setText("");
					lastName.setText("");
					registerEmail.setText("");
					registerUsername.setText("");
					registerPassword.setText("");
					usernameView.requestFocus();
					Toast.makeText(context, "Please check your email for a confirmation link", Toast.LENGTH_SHORT);
				}
				if(result.equals("user_exists"))
				{
					registerUsername.setError("Username already exists");
					registerUsername.requestFocus();
				}
				if(result.equals("email_exists"))
				{
					registerEmail.setError("Email address already in use");
					registerEmail.requestFocus();
				}
			}
		}
	}
}
