package com.cs1635.classme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationRowAdapter extends ArrayAdapter<String>
{
	Context context;
	String[] options;

	public NavigationRowAdapter(Context context, int textViewResourceId, String[] options)
	{
		super(context, textViewResourceId, options);
		this.context = context;
		this.options = options;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.navigation_row, null);

		TextView optionText = (TextView) v.findViewById(R.id.optionText);
		ImageView icon = (ImageView) v.findViewById(R.id.icon);

		optionText.setText(options[position]);
		if(options[position].equals("Search"))
			icon.setBackgroundResource(android.R.drawable.ic_menu_search);
		if(options[position].equals("Home"))
			icon.setBackgroundResource(R.drawable.home);
		if(options[position].equals("My Classes"))
			icon.setBackgroundResource(R.drawable.courses);

		return v;
	}
}