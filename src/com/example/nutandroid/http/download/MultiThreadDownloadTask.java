package com.example.nutandroid.http.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;

import com.example.nutandroid.content.ModernAsyncTask2;
import com.example.nutandroid.http.HttpContextEx;
import com.example.nutandroid.http.HttpFactory;
import com.example.nutandroid.util.NutLogger;

public class MultiThreadDownloadTask extends ModernAsyncTask2<Void, Long, Void>
{
	private static final NutLogger logger = NutLogger.getLogger(MultiThreadDownloadTask.class);

	private HttpDownloadListener mListener;
	private String mNetworkFilePath;
	private String mSavedPath;
	private static final String contentLength = "Content-Length";
	private static final String acceptRanges = "Accept-Ranges";
	private static final int sMaxThreadCount = 125;
	private ArrayList<DownloadThread> mDownloadThreads = new ArrayList<DownloadThread>();
	private ArrayList<DownloadThread> mFailedThreads = new ArrayList<DownloadThread>();
	private ArrayList<DownloadThread> mFinishedThreads = new ArrayList<DownloadThread>();
	private long mDonePartBytes;
	// private static final ExecutorService executor =
	// Executors.newCachedThreadPool();
	private static final ExecutorService executor = new CustomThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	private int mThreadCount = 5;
	private static final long FLAG_BEGIN = 0, FLAG_DOWNLOADING = 1, FLAG_SUCCESS = 2, FLAG_ERROR = 3, FLAG_CANCELED = 4;
	private long mCurrentFlag = FLAG_BEGIN;

	private boolean mSupportPartial;

	private boolean mRefuseThreadNotification = false;

	private Throwable mError;

	private long mTotalBytes;

	private long mFinishedBytes;

	private int mTotalParts;

	private int mWakeCount;

	public MultiThreadDownloadTask(String netWorkFilePath, String savedPath, HttpDownloadListener listener)
	{
		mNetworkFilePath = netWorkFilePath;
		mSavedPath = savedPath;
		mListener = listener;
	}

	public MultiThreadDownloadTask(String netWorkFilePath, String savedPath, HttpDownloadListener listener, int threadCount)
	{
		this(netWorkFilePath, savedPath, listener);
		if (threadCount > sMaxThreadCount)
		{
			mThreadCount = sMaxThreadCount;
		}
		else if (threadCount > 0)
		{
			mThreadCount = threadCount;
		}
	}

	@Override
	protected Void doInBackground(Void... params)
	{

		try
		{
			createFile();
		}
		catch (TargetFileUnremovableException e)
		{
			mError = e;
			publishProgress(FLAG_ERROR);
			return null;
		}

		try
		{
			getTotalBytes();
		}
		catch (Exception ex)
		{
			logger.error("fail to get resource size", ex);
			mError = ex;
			publishProgress(FLAG_ERROR);
			return null;
		}

		// 任务分配还待优化，下载过程中部分线程已经下载完，有的线程还没下载完，剩下的部分就只有少部分线程在下载，降低了速度。如果分配粒度过大，这段时间就会比较长，需要将任务分为很多的小块儿，分发给所有线程，下载完的线程继续获取未下载的小块儿，这样可以保持大部分时间都是满线程下载
		dispatchDownloads();
		logger.info("after doInBackGround");
		return null;
	}

	private void createFile() throws TargetFileUnremovableException
	{
		File f = new File(mSavedPath);
		File parentFile = f.getParentFile();
		if(!parentFile.exists())
		{
			boolean mkdirs = parentFile.mkdirs();
			logger.info("parent({}) do not exist,create return:{}", parentFile.getAbsolutePath(),mkdirs);
		}
		if (f.exists())
		{
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			// String timeStampString =
			// sdf.format(Calendar.getInstance().getTime());
			// int lastSeperator = mSavedPath.lastIndexOf(File.separatorChar);
			// String newPath = mSavedPath.substring(0, lastSeperator) +
			// timeStampString + mSavedPath.substring(lastSeperator);
			// logger.info("file {} already exist,rename to :{}", mSavedPath,
			// newPath);
			// boolean renameRet = f.renameTo(new File(newPath));
			// if (!renameRet)
			// {
			// logger.info("rename failed,try to delete");
			// }
			logger.info("file {} exist,delete it first", mSavedPath);
			boolean deleteRet = f.delete();
			if (!deleteRet)
			{
				throw new TargetFileUnremovableException(mSavedPath);
			}
		}
	}

	@Override
	protected void onProgressUpdate(Long... values)
	{
		if (mRefuseThreadNotification)
		{
			return;
		}

		long flag = values[0];
		if (FLAG_BEGIN == flag)
		{

			mListener.onDownloadBegin(values[1]);
		}
		else if (FLAG_DOWNLOADING == flag)
		{
			mListener.onProgressChanged(values[1]);
		}
		else if (FLAG_ERROR == flag)
		{

			mListener.onFailed(mError);
			mRefuseThreadNotification = true;
		}
		else if (FLAG_SUCCESS == flag)
		{
			mListener.onDownloadFinished();
		}
	}

	private void getTotalBytes() throws ClientProtocolException, IOException
	{
		HttpClient httpClient = HttpFactory.getHttpClient();
		HttpContextEx context = HttpFactory.getSharedHttpContext();
		HttpHead head = new HttpHead(mNetworkFilePath);
		HttpResponse response = httpClient.execute(head, context);
		Header[] allHeaders = response.getAllHeaders();
		logger.info("finish do http head,print headers begin");
		for (int i = 0; i < allHeaders.length; i++)
		{
			Header header = allHeaders[i];
			if (contentLength.equals(header.getName()))
			{
				mTotalBytes = Long.parseLong(header.getValue());
				logger.info("got total bytes:{}", mTotalBytes);
			}
			else if (acceptRanges.equals(header.getName()) && "bytes".equals(header.getValue()))
			{
				mSupportPartial = true;
				logger.info("find support partial");
			}

			logger.debug("{}:{}", header.getName(), header.getValue());
		}
		logger.info("print headers finish");
	}

	private void dispatchDownloads()
	{
		publishProgress(FLAG_BEGIN, mTotalBytes);
		if (!mSupportPartial)
		{
			logger.info("partial not supported");
			DownloadThread t = new DownloadThread(mNetworkFilePath, mSavedPath);
			t.setName(Thread.currentThread().getName() + "#single");
			t.setDaemon(true);
			mDownloadThreads.add(t);
			executor.execute(t);

			logger.info("begin download");
		}
		else
		{
			logger.info("begin partial download with {} threads", mThreadCount);

			DownloadThread[] downloadThreads = DownloadThread.splitTask(mTotalBytes, mThreadCount, mNetworkFilePath, mSavedPath);
			mTotalParts = downloadThreads.length;
			logger.info("split task into {} parts", downloadThreads.length);
			for (int i = 0; i < downloadThreads.length; i++)
			{
				DownloadThread t = downloadThreads[i];
				t.setName(Thread.currentThread().getName() + "#" + i);
				t.setDaemon(true);
				mDownloadThreads.add(t);
				executor.execute(t);
				logger.info("start thread {} to get bytes from {} to {}.", t.getName(), t.getmBytesFrom(), t.getmBytesTo());
			}
		}
		publishProgress(FLAG_DOWNLOADING, 0L);
		mCurrentFlag = FLAG_DOWNLOADING;
		while (mCurrentFlag == FLAG_DOWNLOADING)
		{
			if (isCancelled())
			{
				logger.debug("{} has been canceled", Thread.currentThread().getName());
				for (int i = 0; i < mDownloadThreads.size(); i++)
				{
					DownloadThread thread = mDownloadThreads.get(i);
					logger.debug("begin cancel {}", thread.getName());
					thread.cancel();
				}
				mCurrentFlag = FLAG_CANCELED;
				return;
			}
			calculateProgress();
			if (mCurrentFlag != FLAG_DOWNLOADING)
			{
				break;
			}
			try
			{
				Thread.sleep(20);
			}
			catch (InterruptedException e)
			{
				logger.warn("downloadTask Thread interrupted", e);
				break;
			}
		}
		logger.info("dispatchDownloads finished");
	}

	private void calculateProgress()
	{
		long totalBytes = mDonePartBytes;
		boolean errorExit = false;
		Throwable error = null;
		LABLE_LOOP: for (int i = 0; i < mDownloadThreads.size(); i++)
		{
			DownloadThread t = mDownloadThreads.get(i);
			switch (t.getDownloadState())
			{
			case ERROR:
				logger.info("thread[{}] fail,canRetry:{}.", t.getName(), t.canRetry());
				if (t.canRetry())
				{
					mDownloadThreads.remove(i);
					i--;
					mFailedThreads.add(t);
					break;
				}
				else
				{
					errorExit = true;
					error = t.getError();
					break LABLE_LOOP;
				}
			case DOWNLOADING:
				totalBytes += t.getFinishedBytes();
				break;
			case NOT_BEGIN_YET:
				break;
			case SUCCESS:
				mDonePartBytes += t.getFinishedBytes();
				totalBytes += t.getFinishedBytes();
				mDownloadThreads.remove(i);
				i--;
				mFinishedThreads.add(t);
				mWakeCount++;
				logger.info("find {} success doneBytes:{} finishedBytes:{} wakeCount changed to {}, {} tasks finished", t.getName(), mDonePartBytes, t.getFinishedBytes(), mWakeCount,
						mFinishedThreads.size());
				break;
			default:
				break;
			}

		}
		if (errorExit)
		{
			logger.warn("download error");
			for (int j = 0; j < mDownloadThreads.size(); j++)
			{
				mDownloadThreads.get(j).cancel();
			}
			mCurrentFlag = FLAG_ERROR;
			mError = error;
			publishProgress(FLAG_ERROR);
			return;
		}
		for (int j = mWakeCount; mFailedThreads.size() > 0 && j > 0; j++)
		{
			DownloadThread thread = new DownloadThread(mFailedThreads.remove(0));
			mDownloadThreads.add(thread);
			executor.execute(thread);
			if (thread.getExecuteCount() == 1)
			{
				mWakeCount--;
			}
			logger.info("retry thread:{},wakeCount {}", thread.getName(), mWakeCount);
		}

		if (totalBytes > mFinishedBytes)
		{
			mFinishedBytes = totalBytes;
			publishProgress(FLAG_DOWNLOADING, totalBytes);
		}

		if (mFinishedThreads.size() == mTotalParts)
		{
			logger.info("downloadComplete,totalBytes:{}", totalBytes);
			mCurrentFlag = FLAG_SUCCESS;
			publishProgress(FLAG_SUCCESS);
		}
		if (mFailedThreads.size() == mTotalParts)
		{
			logger.error("totally failed,{} failed", mFailedThreads.size());
			mCurrentFlag = FLAG_ERROR;
			mError = mFailedThreads.get(0).getError();
			publishProgress(FLAG_ERROR);
		}

	}
}

class CustomThreadPoolExecutor extends ThreadPoolExecutor
{
	private static final NutLogger logger = NutLogger.getLogger(CustomThreadPoolExecutor.class);

	public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
		if (r instanceof Thread)
		{
			String name = ((Thread) r).getName();
			logger.debug("new name:{}", name);
			t.setName(name);
		}
		else
		{
			logger.debug("not a thread {}", r.getClass());
		}
	}
}
