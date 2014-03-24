package com.cs1635.classme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.shared.Achievement;

import java.util.ArrayList;

public class TrophyAdapter extends ArrayAdapter<Achievement>
{
	Context context;
	ArrayList<Achievement> achievements;

	public TrophyAdapter(Context context, int textViewResourceId, ArrayList<Achievement> achievements)
	{
		super(context, textViewResourceId, achievements);
		this.achievements = achievements;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.achievement_row, null);

		TextView name = (TextView) v.findViewById(R.id.name);
		TextView description = (TextView) v.findViewById(R.id.description);

		name.setText(achievements.get(position).getName());
		description.setText(achievements.get(position).getDescriptionText());

		return v;
	}
}