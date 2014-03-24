package com.cs1635.classme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class PhotoDialog extends AlertDialog
{
	AlertDialog dialog = this;

	protected PhotoDialog(final Activity context, final Uri captureUri)
	{
		super(context);
		Builder builder = new Builder(context);
		View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.photo_dialog,null);
		builder.setView(view);
		LinearLayout choosePhoto = (LinearLayout) view.findViewById(R.id.choosePhoto);
		choosePhoto.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent pickPhoto = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				context.startActivityForResult(pickPhoto, 1);
				dialog.dismiss();
			}
		});
		LinearLayout takePhoto = (LinearLayout) view.findViewById(R.id.takePhoto);
		takePhoto.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
				context.startActivityForResult(intent, 0);
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.show();
	}
}
