package com.shared;

import java.io.Serializable;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class User.
 */
public class User implements Serializable
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3724822216046123105L;

	/** The username. */
	private String username;

	/** The first name. */
	private String firstName;

	/** The last name. */
	private String lastName;

	/** The course list. */
	private ArrayList<String> courseList = new ArrayList<String>();

	/**
	 * Instantiates a new user.
	 */
	public User(){}

	/**
	 * Instantiates a new user.
	 *
	 * @param u the u
	 */
	public User(String u)
	{
		setUsername(u);
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * Gets the course list.
	 *
	 * @return the course list
	 */
	public ArrayList<String> getCourseList()
	{
		return courseList;
	}

	/**
	 * Sets the course list.
	 *
	 * @param courseList the new course list
	 */
	public void setCourseList(ArrayList<String> courseList)
	{
		this.courseList = courseList;
	}

	/**
	 * Serialize.
	 *
	 * @return the string
	 */
	public String serialize()
	{
		String serialString = username+"+";
		for(String course : courseList)
		{
			serialString += course+"-";
		}

		return serialString;
	}

	/**
	 * Deserialize.
	 *
	 * @param serialString the serial string
	 * @return the user
	 */
	public static User deserialize(String serialString)
	{
		User user = new User();
		String[] username = serialString.split("\\+");
		user.setUsername(username[0]);
		if(username.length > 1)
		{
			String[] courses = username[1].split("-");
			ArrayList<String> courseList = new ArrayList<String>();
			for(String course : courses)
			{
				courseList.add(course);
			}
			user.setCourseList(courseList);
		}
		return user;
	}

	/**
	 * Gets the first name.
	 *
	 * @return the first name
	 */
	public String getFirstName()
	{
		return firstName;
	}

	/**
	 * Sets the first name.
	 *
	 * @param firstName the new first name
	 */
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	/**
	 * Gets the last name.
	 *
	 * @return the last name
	 */
	public String getLastName()
	{
		return lastName;
	}

	/**
	 * Sets the last name.
	 *
	 * @param lastName the new last name
	 */
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
}
