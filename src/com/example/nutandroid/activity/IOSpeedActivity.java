package com.example.nutandroid.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.WriteAbortedException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.example.nutandroid.R;
import com.example.nutandroid.util.NutLogger;

enum WRITE_MODE
{
	MODE_RANDOM, MODE_CHANNEL, MODE_MEMORY_MAP
}

public class IOSpeedActivity extends Activity implements OnClickListener, OnCheckedChangeListener
{
	private static final NutLogger logger = NutLogger.getLogger(IOSpeedActivity.class);

	private WRITE_MODE mMode;
	private Button btnBegin;
	private RadioGroup radioGroup;

	private TextView tvLog;

	private ScrollView scroll;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_iospeed);

		btnBegin = (Button) findViewById(R.id.btnBegin);
		tvLog = (TextView) findViewById(R.id.tvLog);
		scroll = (ScrollView) tvLog.getParent();

		btnBegin.setOnClickListener(this);

		logger.debug("type:{}", findViewById(R.id.radioGroup1).getClass());
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		radioGroup.setOnCheckedChangeListener(this);

		radioGroup.check(R.id.rbRandom);

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
		WriteTask task = new WriteTask(mMode, this);
		task.execute();
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

	public void writeFinished(WRITE_MODE mode, long time)
	{
		ShowLog("time:" + time + "  mode:" + mode.toString());
		btnBegin.setEnabled(true);
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1)
	{
		switch (arg1)
		{
		case R.id.rbRandom:
			btnBegin.setText("Begin RandomAccessFile");
			mMode = WRITE_MODE.MODE_RANDOM;
			break;
		case R.id.rbChannel:
			btnBegin.setText("Begin Channel");
			mMode = WRITE_MODE.MODE_CHANNEL;
			break;
		case R.id.rbMemoryMap:
			btnBegin.setText("Begin Memory Map");
			mMode = WRITE_MODE.MODE_MEMORY_MAP;
			break;
		default:
			break;
		}
	}
}

class WriteTask extends AsyncTask<Void, Void, Long>
{
	private static final NutLogger logger = NutLogger.getLogger(WriteTask.class);

	private WRITE_MODE mMode;
	private IOSpeedActivity mActivity;
	private byte[] mBuffer;

	private String mPath;
	private final static int writeTimes = 10240;

	public WriteTask(WRITE_MODE mode, IOSpeedActivity activity)
	{
		mMode = mode;
		mActivity = activity;
		mBuffer = new byte[1024];
		File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if (!directory.exists())
		{
			logger.info("dir {} not exists,create it", directory.getAbsolutePath());
			boolean mkdirs = directory.mkdirs();
			logger.info("created:{}", mkdirs);
		}
		mPath = directory.getAbsolutePath() + File.separator + "nutTest" + mode + ".dat";
		File file = new File(mPath);
		if (file.exists())
		{
			logger.info("file {} exists,delete it.", mPath);
			boolean delete = file.delete();
			logger.info("deleted:{}", delete);
		}
	}

	@Override
	protected Long doInBackground(Void... params)
	{
		try
		{
			long startTime = System.nanoTime();
			switch (mMode)
			{
			case MODE_RANDOM:

				randomWrite();

				break;
			case MODE_CHANNEL:
				channelWrite();
				break;
			case MODE_MEMORY_MAP:
				memoryMapWrite();
				break;
			}
			long endTime = System.nanoTime();
			return endTime - startTime;
		}
		catch (Exception e)
		{
			logger.error("task error", e);
			return -1L;
		}

	}

	@Override
	protected void onPostExecute(Long result)
	{
		mActivity.writeFinished(mMode, result);
	}

	private void randomWrite() throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(mPath, "rw");
		for (int i = 0; i < writeTimes; i++)
		{
			raf.write(mBuffer);
		}
		raf.close();
	}

	private void channelWrite() throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(mPath, "rw");
		FileChannel channel = raf.getChannel();
		for (int i = 0; i < writeTimes; i++)
		{
			ByteBuffer buffer = ByteBuffer.wrap(mBuffer);
			channel.write(buffer);
		}
		channel.close();
	}

	private void memoryMapWrite() throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(mPath, "rw");
		FileChannel channel = raf.getChannel();
		MappedByteBuffer mappedByteBuffer = channel.map(MapMode.READ_WRITE, 0, mBuffer.length * writeTimes);
		for (int i = 0; i < writeTimes; i++)
		{
			mappedByteBuffer.put(mBuffer);
		}
		channel.close();
	}
}
