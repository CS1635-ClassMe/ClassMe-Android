package com.shared;

import java.io.Serializable;

public class Achievement implements Serializable
{
	private static final long serialVersionUID = -8457244121843347327L;
	private String name, descriptionText;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescriptionText()
	{
		return descriptionText;
	}

	public void setDescriptionText(String descriptionText)
	{
		this.descriptionText = descriptionText;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Achievement)
		{
			Achievement other = (Achievement)o;
			return other.getName().equals(getName());
		}
		
		return false;
	}
}
