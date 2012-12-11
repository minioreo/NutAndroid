package com.example.nutandroid.http.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.AbstractHttpMessage;

import android.util.FloatMath;

import com.example.nutandroid.http.HttpContextEx;
import com.example.nutandroid.http.HttpFactory;
import com.example.nutandroid.util.NutLogger;

class DownloadThread extends Thread
{
	enum DownloadState
	{
		NOT_BEGIN_YET, DOWNLOADING, ERROR, WAIT_RETRY, SUCCESS, CANCELED
	};

	public static final long MIN_DOWNLOAD_BYTES_PER_THREAD = 1 * 1024 * 1024;

	private static final NutLogger logger = NutLogger.getLogger(DownloadThread.class);

	private boolean mRunning = true;
	private String mURL;
	private boolean mIsPartial;
	private long mBytesFrom;

	public long getmBytesFrom()
	{
		return mBytesFrom;
	}

	public long getmBytesTo()
	{
		return mBytesTo;
	}

	private long mBytesTo;
	private String mSavedPath;

	private int mFinishedBytes;
	private Throwable mException;
	private boolean canRetry = false;
	private int mExecuteCount = 0;
	private static final int MAX_RETRY_COUNT = 3;

	private DownloadState mDownloadState = DownloadState.NOT_BEGIN_YET;

	public DownloadThread(String url, String savedPath)
	{
		mURL = url;
		mSavedPath = savedPath;
		mIsPartial = false;
	}

	public DownloadThread(String url, String savedPath, long bytesFrom, long bytesTo)
	{
		mURL = url;
		mSavedPath = savedPath;
		mIsPartial = true;
		mBytesFrom = bytesFrom;
		mBytesTo = bytesTo;
	}

	public DownloadThread(DownloadThread thread)
	{
		mURL = thread.mURL;
		mSavedPath = thread.mSavedPath;
		mIsPartial = thread.mIsPartial;
		mBytesFrom = thread.mBytesFrom;
		mBytesTo = thread.mBytesTo;
		mExecuteCount = thread.mExecuteCount;
		setName(thread.getName());
		setDaemon(thread.isDaemon());
	}

	@Override
	public void run()
	{
		mExecuteCount++;
		logger.info("thread[{}] begin,execute count:{}", getName(), mExecuteCount);
		mDownloadState = DownloadState.DOWNLOADING;
		HttpClient httpClient = HttpFactory.getHttpClient();
		HttpContextEx context = HttpFactory.getSharedHttpContext();
		final HttpGet get = new HttpGet(mURL);
		setHeaders(get);
		if (mIsPartial)
		{
			get.setHeader("Range", "bytes=" + mBytesFrom + "-" + mBytesTo);
		}
		RandomAccessFile file = null;
		FileChannel channel = null;
		HttpResponse response = null;
		InputStream inputStream = null;
		try
		{
			file = new RandomAccessFile(mSavedPath, "rw");
			channel = file.getChannel();
			MappedByteBuffer byteBuffer = channel.map(MapMode.READ_WRITE, mBytesFrom, mBytesTo - mBytesFrom + 1);
			response = httpClient.execute(get, context);
			logger.info("thread[{}] response status:{}", getName(), response.getStatusLine());
			Header[] headers = response.getAllHeaders();
			logger.debug("thread[{}] print responese headers begin", getName());
			for (int i = 0; i < headers.length; i++)
			{
				logger.debug("thread[{}] {}:{}", getName(), headers[i].getName(), headers[i].getValue());
			}
			logger.debug("thread[{}] print responese headers finish", getName());
			inputStream = response.getEntity().getContent();
			byte[] buffer = new byte[4096];
			int count = 0;
			while (true)
			{
				if (!mRunning)
				{
					get.abort();
					break;
				}
				count = inputStream.read(buffer);
				if (count == -1)
				{
					inputStream.close();
					channel.close();
					file.close();
					break;
				}
				byteBuffer.put(buffer, 0, count);
				mFinishedBytes += count;
			}
			if (mRunning)
			{
				mDownloadState = DownloadState.SUCCESS;
			}
			else
			{
				mDownloadState = DownloadState.CANCELED;
			}
			long expectTotal = mBytesTo - mBytesFrom + 1;
			if (expectTotal != mFinishedBytes)
			{
				logger.error("thread[{}] finished,state:{},from:{},to:{},expectTotal:{},actualTotal:{}", getName(), mDownloadState, mBytesFrom, mBytesTo, expectTotal, mFinishedBytes);
				canRetry = true;
				mDownloadState = DownloadState.ERROR;
			}
			else
			{
				logger.info("thread[{}] finished,state:{},from:{},to:{},expectTotal:{},actualTotal:{}", getName(), mDownloadState, mBytesFrom, mBytesTo, expectTotal, mFinishedBytes);
			}

		}
		catch (ClientProtocolException e)
		{
			mException = e;
			mDownloadState = DownloadState.ERROR;
			logger.error("thread " + getName(), e);
			get.abort();
			return;
		}
		catch (IOException e)
		{
			mException = e;
			logger.error(getName(), e);
			logger.info("thread:{},exception:{},execute count:{},maxRetry count:{}", getName(), e.getClass(), mExecuteCount, MAX_RETRY_COUNT);
			if (mExecuteCount <= MAX_RETRY_COUNT && (e instanceof SocketTimeoutException))
			{
				logger.debug("{} can retry", getName());
				canRetry = true;
			}
			mDownloadState = DownloadState.ERROR;
			get.abort();
			return;
		}
		catch (NullPointerException e)
		{
			logger.error(getName(), e);
			StackTraceElement[] stackTrace = e.getStackTrace();
			// httpclient bug:
			// http://code.google.com/p/android/issues/detail?id=5255
			if (stackTrace != null && stackTrace.length > 0 && stackTrace[0].getClassName().equals("org.apache.http.impl.client.DefaultRequestDirector"))
			{
				logger.warn("apache httpclient bug found.need catch and retry,thread {}", getName());
				mException = e;
				canRetry = true;
				mDownloadState = DownloadState.ERROR;
				get.abort();
			}
			else
			{
				throw e;
			}
		}
	}

	private void setHeaders(AbstractHttpMessage msg)
	{
		Header[] headers = HttpFactory.getCommonHeaders();
		for (int i = 0; i < headers.length; i++)
		{
			msg.setHeader(headers[i]);
		}
	}

	public void cancel()
	{
		mRunning = false;
		logger.debug("thread[{}] has been canceled", getName());
	}

	public int getFinishedBytes()
	{
		return mFinishedBytes;
	}

	public DownloadState getDownloadState()
	{
		return mDownloadState;
	}

	public Throwable getError()
	{
		return mException;
	}

	public int getExecuteCount()
	{
		return mExecuteCount;
	}

	public boolean canRetry()
	{
		return mDownloadState == DownloadState.ERROR && canRetry;
	}

	public static DownloadThread[] splitTask(long totalBytes, int threadCount, String url, String savePath)
	{
		int partNum = threadCount;
		long bytesPerThread = (long) FloatMath.ceil((float) totalBytes / partNum);
		if (bytesPerThread < MIN_DOWNLOAD_BYTES_PER_THREAD)
		{
			partNum = (int) FloatMath.ceil((float) totalBytes / MIN_DOWNLOAD_BYTES_PER_THREAD);
			bytesPerThread = (long) FloatMath.ceil((float) totalBytes / partNum);
		}
		DownloadThread[] threads = new DownloadThread[partNum];
		for (int i = 0; i < threads.length; i++)
		{
			long fromByte = bytesPerThread * i;
			long toByte = i == partNum - 1 ? totalBytes - 1 : fromByte + bytesPerThread - 1;
			DownloadThread thread = new DownloadThread(url, savePath, fromByte, toByte);
			threads[i] = thread;
		}
		return threads;
	}
}