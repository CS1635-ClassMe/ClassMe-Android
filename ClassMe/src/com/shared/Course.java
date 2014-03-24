package com.shared;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class Course.
 */
public class Course implements Serializable
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6434443948276427528L;
	
	/** The course request key. */
	private String subjectCode, courseName, courseDescription, courseRequestKey;
	
	/** The course number. */
	private int courseNumber;
	
	/**
	 * Instantiates a new course.
	 */
	public Course(){}
	
	/**
	 * Instantiates a new course.
	 *
	 * @param code the code
	 * @param num the num
	 * @param name the name
	 * @param desc the desc
	 */
	public Course(String code, int num, String name, String desc)
	{
		subjectCode = code;
		courseNumber = num;
		courseName = name;
		courseDescription = desc;
	}

	/**
	 * Gets the subject code.
	 *
	 * @return the subject code
	 */
	public String getSubjectCode()
	{
		return subjectCode;
	}

	/**
	 * Sets the subject code.
	 *
	 * @param subjectCode the new subject code
	 */
	public void setSubjectCode(String subjectCode)
	{
		this.subjectCode = subjectCode;
	}

	/**
	 * Gets the course name.
	 *
	 * @return the course name
	 */
	public String getCourseName()
	{
		return courseName;
	}

	/**
	 * Sets the course name.
	 *
	 * @param courseName the new course name
	 */
	public void setCourseName(String courseName)
	{
		this.courseName = courseName;
	}

	/**
	 * Gets the course description.
	 *
	 * @return the course description
	 */
	public String getCourseDescription()
	{
		return courseDescription;
	}

	/**
	 * Sets the course description.
	 *
	 * @param courseDescription the new course description
	 */
	public void setCourseDescription(String courseDescription)
	{
		this.courseDescription = courseDescription;
	}

	/**
	 * Gets the course number.
	 *
	 * @return the course number
	 */
	public int getCourseNumber()
	{
		return courseNumber;
	}

	/**
	 * Sets the course number.
	 *
	 * @param courseNumber the new course number
	 */
	public void setCourseNumber(int courseNumber)
	{
		this.courseNumber = courseNumber;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Course)
		{
			Course other = (Course)o;
			if(other.courseNumber == courseNumber)
				return true;
			else
				return false;
		}
		return false;
	}
	
	/**
	 * Gets the course request key.
	 *
	 * @return the course request key
	 */
	public String getCourseRequestKey()
	{
		return courseRequestKey;
	}
	
	/**
	 * Sets the course request key.
	 *
	 * @param courseRequestKey the new course request key
	 */
	public void setCourseRequestKey(String courseRequestKey)
	{
		this.courseRequestKey = courseRequestKey;
	}
}
