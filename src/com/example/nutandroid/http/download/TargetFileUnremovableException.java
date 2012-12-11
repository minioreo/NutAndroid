package com.example.nutandroid.http.download;

public class TargetFileUnremovableException extends Exception
{

	private static final long serialVersionUID = 1L;

	public TargetFileUnremovableException(String filePath)
	{
		this.filePath = filePath;
	}

	String filePath;

	public String getFilePath()
	{
		return filePath;
	}
}
