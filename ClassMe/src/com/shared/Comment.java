package com.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class Comment.
 */
public class Comment implements Serializable, Comparable<Comment>
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3763260169521173359L;

	/** The content. */
	private String username, content;

	/** The last edit. */
	private Date commentTime, lastEdit;

	/** The comment key. */
	private String commentKey;

	/** The plus ones. */
	private int plusOnes;

	/** The plus oned. */
	private boolean plusOned;

	/** If this comment has been accepted as an answer by the post's author. */
	private boolean isAccepted;

	/** The attachment keys. */
	private ArrayList<String> attachmentKeys = new ArrayList<String>();

	/** The attachment names. */
	private ArrayList<String> attachmentNames = new ArrayList<String>();

	/**
	 * Checks if is plus oned.
	 *
	 * @return true, if is plus oned
	 */
	public boolean isPlusOned()
	{
		return plusOned;
	}

	/**
	 * Sets the plus oned.
	 *
	 * @param plusOned the new plus oned
	 */
	public void setPlusOned(boolean plusOned)
	{
		this.plusOned = plusOned;
	}

	/**
	 * Gets the plus ones.
	 *
	 * @return the plus ones
	 */
	public int getPlusOnes()
	{
		return plusOnes;
	}

	/**
	 * Sets the plus ones.
	 *
	 * @param plusOnes the new plus ones
	 */
	public void setPlusOnes(int plusOnes)
	{
		this.plusOnes = plusOnes;
	}

	/**
	 * Instantiates a new comment.
	 */
	public Comment(){}

	/**
	 * Instantiates a new comment.
	 *
	 * @param username the username
	 * @param content the content
	 */
	public Comment(String username, String content)
	{
		this.username = username;
		this.content = content;
		commentTime = new Date();
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
	 * Gets the content.
	 *
	 * @return the content
	 */
	public String getContent()
	{
		return content;
	}

	/**
	 * Sets the content.
	 *
	 * @param content the new content
	 */
	public void setContent(String content)
	{
		this.content = content;
	}

	/**
	 * Gets the comment time.
	 *
	 * @return the comment time
	 */
	public Date getCommentTime()
	{
		return commentTime;
	}

	/**
	 * Sets the comment time.
	 *
	 * @param commentTime the new comment time
	 */
	public void setCommentTime(Date commentTime)
	{
		this.commentTime = commentTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Comment other)
	{
		return commentTime.compareTo(other.commentTime);
	}

	/**
	 * Gets the comment key.
	 *
	 * @return the comment key
	 */
	public String getCommentKey()
	{
		return commentKey;
	}

	/**
	 * Sets the comment key.
	 *
	 * @param commentKey the new comment key
	 */
	public void setCommentKey(String commentKey)
	{
		this.commentKey = commentKey;
	}

	/**
	 * Gets the last edit.
	 *
	 * @return the last edit
	 */
	public Date getLastEdit()
	{
		return lastEdit;
	}

	/**
	 * Sets the last edit.
	 *
	 * @param lastEdit the new last edit
	 */
	public void setLastEdit(Date lastEdit)
	{
		this.lastEdit = lastEdit;
	}

	public boolean isAccepted()
	{
		return isAccepted;
	}

	public void setAccepted(boolean isAccepted)
	{
		this.isAccepted = isAccepted;
	}

	public ArrayList<String> getAttachmentKeys()
	{
		return attachmentKeys;
	}

	public void setAttachmentKeys(ArrayList<String> attachmentKeys)
	{
		this.attachmentKeys = attachmentKeys;
	}

	public ArrayList<String> getAttachmentNames()
	{
		return attachmentNames;
	}

	public void setAttachmentNames(ArrayList<String> attachmentNames)
	{
		this.attachmentNames = attachmentNames;
	}
}
