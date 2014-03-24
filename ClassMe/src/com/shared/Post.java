package com.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class Post.
 */
public class Post implements Serializable
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6112430428377944014L;
	
	/** The report reason. */
	private String postContent, username, streamLevel, postKey, reportReason;
	
	/** The last edit. */
	private Date postTime, lastEdit;
	
	/** The downvotes. */
	private int upvotes, downvotes;
	
	/** The score. */
	private double score;
	
	/** The comments. */
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	
	/** The attachment keys. */
	private ArrayList<String> attachmentKeys = new ArrayList<String>();
	
	/** The attachment names. */
	private ArrayList<String> attachmentNames = new ArrayList<String>();
	
	/** The reported. */
	private boolean upvoted, downvoted, reported;

	/**
	 * Checks if is upvoted.
	 *
	 * @return true, if is upvoted
	 */
	public boolean isUpvoted()
	{
		return upvoted;
	}

	/**
	 * Sets the upvoted.
	 *
	 * @param upvoted the new upvoted
	 */
	public void setUpvoted(boolean upvoted)
	{
		this.upvoted = upvoted;
	}

	/**
	 * Checks if is downvoted.
	 *
	 * @return true, if is downvoted
	 */
	public boolean isDownvoted()
	{
		return downvoted;
	}

	/**
	 * Sets the downvoted.
	 *
	 * @param downvoted the new downvoted
	 */
	public void setDownvoted(boolean downvoted)
	{
		this.downvoted = downvoted;
	}

	/** The Post time comparator. */
	public static Comparator<Post> PostTimeComparator = new Comparator<Post>()
	{
		@Override
		public int compare(Post post1, Post post2)
		{
			return(post2.getPostTime().compareTo(post1.getPostTime()));
		}
	};
	
	/** The Post score comparator. */
	public static Comparator<Post> PostScoreComparator = new Comparator<Post>()
	{
		@Override
		public int compare(Post post1, Post post2)
		{
			return (int) (post2.getScore()*10000-post1.getScore()*10000);
		}
	};

	/**
	 * Gets the comments.
	 *
	 * @return the comments
	 */
	public ArrayList<Comment> getComments()
	{
		return comments;
	}

	/**
	 * Sets the comments.
	 *
	 * @param comments the new comments
	 */
	public void setComments(ArrayList<Comment> comments)
	{
		this.comments = comments;
	}

	/**
	 * Gets the post content.
	 *
	 * @return the post content
	 */
	public String getPostContent()
	{
		return postContent;
	}

	/**
	 * Gets the upvotes.
	 *
	 * @return the upvotes
	 */
	public int getUpvotes()
	{
		return upvotes;
	}

	/**
	 * Sets the upvotes.
	 *
	 * @param upvotes the new upvotes
	 */
	public void setUpvotes(int upvotes)
	{
		this.upvotes = upvotes;
	}

	/**
	 * Gets the downvotes.
	 *
	 * @return the downvotes
	 */
	public int getDownvotes()
	{
		return downvotes;
	}

	/**
	 * Sets the downvotes.
	 *
	 * @param downvotes the new downvotes
	 */
	public void setDownvotes(int downvotes)
	{
		this.downvotes = downvotes;
	}

	/**
	 * Sets the post content.
	 *
	 * @param postContent the new post content
	 */
	public void setPostContent(String postContent)
	{
		this.postContent = postContent;
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
	 * Gets the post time.
	 *
	 * @return the post time
	 */
	public Date getPostTime()
	{
		return postTime;
	}

	/**
	 * Sets the post time.
	 *
	 * @param postTime the new post time
	 */
	public void setPostTime(Date postTime)
	{
		this.postTime = postTime;
	}

	/**
	 * Gets the stream level.
	 *
	 * @return the stream level
	 */
	public String getStreamLevel()
	{
		return streamLevel;
	}

	/**
	 * Sets the stream level.
	 *
	 * @param streamLevel the new stream level
	 */
	public void setStreamLevel(String streamLevel)
	{
		this.streamLevel = streamLevel;
	}

	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	public double getScore()
	{
		return score;
	}

	/**
	 * Sets the score.
	 *
	 * @param score the new score
	 */
	public void setScore(double score)
	{
		this.score = score;
	}

	/**
	 * Gets the post key.
	 *
	 * @return the post key
	 */
	public String getPostKey()
	{
		return postKey;
	}

	/**
	 * Sets the post key.
	 *
	 * @param postKey the new post key
	 */
	public void setPostKey(String postKey)
	{
		this.postKey = postKey;
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

	/**
	 * Gets the report reason.
	 *
	 * @return the report reason
	 */
	public String getReportReason()
	{
		return reportReason;
	}

	/**
	 * Sets the report reason.
	 *
	 * @param reportReason the new report reason
	 */
	public void setReportReason(String reportReason)
	{
		this.reportReason = reportReason;
	}

	/**
	 * Checks if is reported.
	 *
	 * @return true, if is reported
	 */
	public boolean isReported()
	{
		return reported;
	}

	/**
	 * Sets the reported.
	 *
	 * @param reported the new reported
	 */
	public void setReported(boolean reported)
	{
		this.reported = reported;
	}

	/**
	 * Gets the attachment keys.
	 *
	 * @return the attachment keys
	 */
	public ArrayList<String> getAttachmentKeys()
	{
		return attachmentKeys;
	}

	/**
	 * Sets the attachment keys.
	 *
	 * @param attachmentKeys the new attachment keys
	 */
	public void setAttachmentKeys(ArrayList<String> attachmentKeys)
	{
		this.attachmentKeys = attachmentKeys;
	}

	/**
	 * Gets the attachment names.
	 *
	 * @return the attachment names
	 */
	public ArrayList<String> getAttachmentNames()
	{
		return attachmentNames;
	}

	/**
	 * Sets the attachment names.
	 *
	 * @param attachmentNames the new attachment names
	 */
	public void setAttachmentNames(ArrayList<String> attachmentNames)
	{
		this.attachmentNames = attachmentNames;
	}
}
