package com.example.nutandroid.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutandroid.R;
import com.example.nutandroid.util.NutLogger;

enum Mode
{
	ONE2ONE, MORE2MORE, MORE2ONE, MORE2ONENR
}

public class MTWrite extends Activity implements OnClickListener, OnCheckedChangeListener
{

	private Button btnBegin;
	private TextView tvLog;
	private ScrollView scroll;
	private RadioGroup radioGroup;
	private Mode mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mtwrite);
		btnBegin = (Button) findViewById(R.id.btnBegin);
		tvLog = (TextView) findViewById(R.id.tvLog);
		scroll = (ScrollView) tvLog.getParent();

		btnBegin.setOnClickListener(this);

		radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		radioGroup.setOnCheckedChangeListener(this);

		radioGroup.check(R.id.rbSingle);
	}

	@Override
	public void onClick(View v)
	{
		if (btnBegin == v)
		{
			btnBeginOnClick();
		}
	}

	private void btnBeginOnClick()
	{
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			Toast.makeText(this, "device not ready." + Environment.getExternalStorageState(), Toast.LENGTH_LONG).show();
			return;
		}
		btnBegin.setEnabled(false);
		new ThreadWriteTask(mMode, this).execute();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		switch (checkedId)
		{
		case R.id.rbSingle:
			mMode = Mode.ONE2ONE;
			break;
		case R.id.rbMTMultiFiles:
			mMode = Mode.MORE2MORE;
			break;
		case R.id.rbMTOneFile:
			mMode = Mode.MORE2ONE;
			break;
		case R.id.rbMTOneFileNR:
			mMode = Mode.MORE2ONENR;
			break;
		}

	}

	public void onTaskFinished(Mode mode, Long timeConsumed)
	{
		ShowLog("Time:" + timeConsumed + "\tmode:" + mode);
		btnBegin.setEnabled(true);
	}

	private void ShowLog(String msg)
	{
		tvLog.append(msg + "\r\n");
		tvLog.post(new Runnable()
		{

			@Override
			public void run()
			{
				scroll.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

}

class ThreadWriteTask extends AsyncTask<Void, Void, Long>
{
	private static final NutLogger logger = NutLogger.getLogger(ThreadWriteTask.class);

	private static final ExecutorService service = Executors.newFixedThreadPool(2);
	private Mode mode;
	private MTWrite activity;
	private static final int mWriteTimes = 50000;

	private File mDirectory;

	ThreadWriteTask(Mode mode, MTWrite activity)
	{
		this.mode = mode;
		this.activity = activity;
		logger.info("writeTask mode:{}", mode);

		mDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if (!mDirectory.exists())
		{
			logger.info("dir {} not exists,create it", mDirectory.getAbsolutePath());
			boolean mkdirs = mDirectory.mkdirs();
			logger.info("created:{}", mkdirs);
		}
	}

	private void preparePath(String path)
	{
		File file = new File(path);
		if (file.exists())
		{
			logger.info("file {} exists,delete it.", path);
			boolean delete = file.delete();
			logger.info("deleted:{}", delete);
		}
	}

	@Override
	protected Long doInBackground(Void... params)
	{
		switch (mode)
		{
		case MORE2MORE:
			return more2MoreWrite();
		case MORE2ONE:
			return more2OneWrite();
		case ONE2ONE:
			return one2OneWrite();
		case MORE2ONENR:
			return more2OneWriteNR();
		default:
			return 0L;
		}
	}

	private Long more2OneWriteNR()
	{
		String path1 = mDirectory.getAbsolutePath() + File.separator + "nutTest" + mode + ".dat.part1";
		String path2 = mDirectory.getAbsolutePath() + File.separator + "nutTest" + mode + ".dat.part2";
		preparePath(path1);
		preparePath(path2);

		final int writeTimes1 = mWriteTimes / 2, writeTimes2 = mWriteTimes - writeTimes1;

		byte[] buffer1 = new byte[1024], buffer2 = new byte[1024];
		WriteTask2 task1 = new WriteTask2(path1, writeTimes1, 0, buffer1,false);
		WriteTask2 task2 = new WriteTask2(path2, writeTimes2, 0, buffer2,false);
		long totalTime = 0;
		ExecutorCompletionService<Long> completionService = new ExecutorCompletionService<Long>(service);
		Future<Long> future1 = completionService.submit(task1);
		Future<Long> future2 = completionService.submit(task2);
		for (int i = 0; i < 2; i++)
		{
			try
			{
				Future<Long> future = completionService.take();
				Long time = future.get();
				if (time == -1)
				{
					logger.warn("time is -1");
					future1.cancel(true);
					future2.cancel(true);
					return -1L;
				}
				else
				{
					totalTime += time;
				}
			}
			catch (InterruptedException e)
			{
				logger.error("interuppted", e);
				return -1L;
			}
			catch (ExecutionException e)
			{
				logger.error("execute exception", e);
				return -1L;
			}
		}
		logger.info("begin to merge");
		long startTime = System.nanoTime();
		try
		{
			FileOutputStream fos = new FileOutputStream(path1,true);
			FileChannel outChannel = fos.getChannel();
			FileInputStream fis = new FileInputStream(path2);
			FileChannel inChannel = fis.getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
			inChannel.close();
			outChannel.close();
			fis.close();
			fos.close();
			logger.info("merge complete");
		}
		catch (IOException e)
		{
			logger.error("merge error", e);
			return -1L;
		}
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		totalTime += duration;
		
		return totalTime;
	}

	private Long one2OneWrite()
	{
		String path = mDirectory.getAbsolutePath() + File.separator + "nutTest" + mode + ".dat";
		preparePath(path);
		byte[] buffer = new byte[1024];
		WriteTask2 task = new WriteTask2(path, mWriteTimes, 0, buffer, false);
		return task.call();
	}

	private Long more2OneWrite()
	{
		String path = mDirectory.getAbsolutePath() + File.separator + "nutTest" + mode + ".dat";
		preparePath(path);
		final int writeTimes1 = mWriteTimes / 2, writeTimes2 = mWriteTimes - writeTimes1;

		byte[] buffer1 = new byte[1024], buffer2 = new byte[1024];
		WriteTask2 task1 = new WriteTask2(path, writeTimes1, 0, buffer1, false);
		WriteTask2 task2 = new WriteTask2(path, writeTimes2, writeTimes1 * buffer1.length, buffer2, false);
		ExecutorCompletionService<Long> completionService = new ExecutorCompletionService<Long>(service);
		Future<Long> future1 = completionService.submit(task1);
		Future<Long> future2 = completionService.submit(task2);
		long totalTime = 0;
		for (int i = 0; i < 2; i++)
		{
			try
			{
				Future<Long> future = completionService.take();
				Long time = future.get();
				if (time == -1)
				{
					logger.warn("time is -1");
					future1.cancel(true);
					future2.cancel(true);
					return -1L;
				}
				else
				{
					totalTime += time;
				}
			}
			catch (InterruptedException e)
			{
				logger.error("interuppted", e);
				return -1L;
			}
			catch (ExecutionException e)
			{
				logger.error("execute exception", e);
				return -1L;
			}
		}
		return totalTime;
	}

	private Long more2MoreWrite()
	{
		String path1 = mDirectory.getAbsolutePath() + File.separator + "nutTest" + mode + ".dat.part1";
		String path2 = mDirectory.getAbsolutePath() + File.separator + "nutTest" + mode + ".dat.part2";
		preparePath(path1);
		preparePath(path2);

		final int writeTimes1 = mWriteTimes / 2, writeTimes2 = mWriteTimes - writeTimes1;

		byte[] buffer1 = new byte[1024], buffer2 = new byte[1024];
		WriteTask2 task1 = new WriteTask2(path1, writeTimes1, 0, buffer1,true);
		WriteTask2 task2 = new WriteTask2(path2, writeTimes2, 0, buffer2,true);
		long totalTime = 0;
		ExecutorCompletionService<Long> completionService = new ExecutorCompletionService<Long>(service);
		Future<Long> future1 = completionService.submit(task1);
		Future<Long> future2 = completionService.submit(task2);
		for (int i = 0; i < 2; i++)
		{
			try
			{
				Future<Long> future = completionService.take();
				Long time = future.get();
				if (time == -1)
				{
					logger.warn("time is -1");
					future1.cancel(true);
					future2.cancel(true);
					return -1L;
				}
				else
				{
					totalTime += time;
				}
			}
			catch (InterruptedException e)
			{
				logger.error("interuppted", e);
				return -1L;
			}
			catch (ExecutionException e)
			{
				logger.error("execute exception", e);
				return -1L;
			}
		}
		return totalTime;
	}

	@Override
	protected void onPostExecute(Long result)
	{
		this.activity.onTaskFinished(mode, result);
	}

}

class WriteTask2 implements Callable<Long>
{
	private static final NutLogger logger = NutLogger.getLogger(WriteTask2.class);

	private int mTaskWriteTimes;
	private long mTaskPosition;

	private String mPath;

	private byte[] mBuffer;

	private boolean mIsNR;

	public WriteTask2(String path, int writeTimes, long position, byte[] buffer, boolean isNR)
	{
		mPath = path;
		mTaskWriteTimes = writeTimes;
		mTaskPosition = position;
		mBuffer = buffer;
		mIsNR = isNR;
	}

	@Override
	public Long call()
	{
		logger.debug("enter call");
		long startTime = System.nanoTime();
		try
		{
			if (mIsNR)
			{
				writeFileNR();
			}
			else
			{
				writeFile();
			}
		}
		catch (IOException e)
		{
			logger.error("error task1", e);
			return -1L;
		}
		long endTime = System.nanoTime();
		logger.debug("leave call");
		return endTime - startTime;
	}

	private void writeFile() throws IOException
	{
		logger.info("enter write file");
		RandomAccessFile raf = null;
		FileChannel channel = null;
		try
		{
			raf = new RandomAccessFile(mPath, "rw");
			channel = raf.getChannel();
			MappedByteBuffer mappedByteBuffer = channel.map(MapMode.READ_WRITE, mTaskPosition, mBuffer.length * mTaskWriteTimes);
			for (int i = 0; i < mTaskWriteTimes; i++)
			{
				mappedByteBuffer.put(mBuffer, 0, mBuffer.length);
			}
		}
		finally
		{
			try
			{
				if (raf != null)
				{
					raf.close();
				}
			}
			finally
			{
				if (channel != null)
				{
					channel.close();
				}
			}

		}
		logger.info("leave write file");
	}

	private void writeFileNR() throws IOException
	{
		logger.info("enter write file NR");
		FileOutputStream fos = new FileOutputStream(mPath, false);
		FileChannel channel = fos.getChannel();
		ByteBuffer bb = ByteBuffer.allocate(mBuffer.length);
		for (int i = 0; i < mTaskWriteTimes; i++)
		{
			bb.clear();
			bb.put(mBuffer, 0, mBuffer.length);
			channel.write(bb);
		}
		channel.close();
		fos.close();
		logger.info("leave write file NR");
	}

}
