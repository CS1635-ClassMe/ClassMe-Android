package com.cs1635.classme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MyImageGetter implements Html.ImageGetter
{
	Context context;
	TextView textView;
	static LruCache<String,Drawable> cache = new LruCache<String, Drawable>(5);

	public MyImageGetter(TextView t, Context c)
	{
		context = c;
		textView = t;
	}

	@Override
	public Drawable getDrawable(String source)
	{
		Drawable d;
		if((d = cache.get(source)) != null)
			return d;

		d = context.getResources().getDrawable(R.drawable.ic_launcher);
		new LoadImage().execute(source, d);

		return d;
	}

	class LoadImage extends AsyncTask<Object, Void, Bitmap> {

		private Drawable mDrawable;
		private String source;

		@Override
		protected Bitmap doInBackground(Object... params) {
			source = (String) params[0];
			mDrawable = (Drawable) params[1];
			try {
				InputStream is = new URL(source).openStream();
				return BitmapFactory.decodeStream(is);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				mDrawable = new BitmapDrawable(context.getResources(),bitmap);
				mDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
				cache.put(source,mDrawable);
				// i don't know yet a better way to refresh TextView
				// mTv.invalidate() doesn't work as expected
				CharSequence t = textView.getText();
				textView.setText(t);
			}
		}
	}
}