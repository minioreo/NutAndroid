package com.example.nutandroid.http.download;

public interface HttpDownloadListener
{
	void onDownloadBegin(long totalBytes);

	void onProgressChanged(long finishedBytes);

	void onDownloadFinished();

	void onFailed(Throwable error);
}
