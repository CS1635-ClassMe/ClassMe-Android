package com.cs1635.classme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class LinkDialog extends AlertDialog
{
	AlertDialog dialog;
	protected LinkDialog(final Activity context, final EditText post)
	{
		super(context);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.link_dialog,null);
		final EditText link = (EditText) view.findViewById(R.id.address);
		builder.setView(view)
				.setTitle("Link")
				.setNegativeButton("Cancel", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						post.setText(post.getText().toString()+"<a href=\""+link.getText().toString()+"\">"+link.getText().toString()+"</a>");
					}
				});
		dialog = builder.create();
		dialog.show();
		link.setSelection(7);
	}
}
