package com.example.nutandroid.activity;

import java.io.File;
import java.text.MessageFormat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutandroid.R;
import com.example.nutandroid.http.HttpFactory;
import com.example.nutandroid.http.download.HttpDownloadListener;
import com.example.nutandroid.http.download.MultiThreadDownloadTask;
import com.example.nutandroid.util.NutLogger;

public class MultiThreadDownloadActivity extends Activity implements OnClickListener, OnItemSelectedListener, HttpDownloadListener
{
	private static final NutLogger logger = NutLogger.getLogger(MultiThreadDownloadActivity.class);

	private EditText edtURL;
	private Spinner spThreadCount;
	private Button btnDownload;
	private ProgressBar pbDownload;
	private TextView tvLog;
	private ScrollView scrollView;

	private int mDownloadThreadCount = 5;

	private TextView tvSpeed;

	private long mStartTime;

	private TextView tvBytes;

	private long mTotalBytes;

	private float mMaxSpeed = 0, mMinSpeed = Float.MAX_VALUE;

	private TextView tvMaxSpeed;

	private TextView tvMinSpeed;

	private boolean mDownloading = false;

	private MultiThreadDownloadTask mTask;

	private String mFileName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
//		HttpFactory.configProxy("10.45.15.131", 8989);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multithread_download);
		edtURL = (EditText) findViewById(R.id.edtURL);
		spThreadCount = (Spinner) findViewById(R.id.spThreadCount);
		btnDownload = (Button) findViewById(R.id.btnDownload);
		pbDownload = (ProgressBar) findViewById(R.id.pbDownload);
		tvBytes = (TextView) findViewById(R.id.tvBytes);
		tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
		tvMinSpeed = (TextView) findViewById(R.id.tvMinSpeed);
		tvLog = (TextView) findViewById(R.id.tvLog);
		scrollView = (ScrollView) tvLog.getParent();
		btnDownload.setOnClickListener(this);
		// http://10.45.15.131:8080/Server4Android/123.png
		// "http://10.45.15.131:8080/Server4Android/speedTest/speedtest/random4000x4000.jpg"
		// String testUrl = "http://dl_dir.qq.com/qqfile/qq/QQ2012/QQ2012.exe";
		// String testUrl = "http://" +
		// URLEncoder.encode("dl_dir.qq.com/qqfile/qq/QQ2012/QQ2012.exe");
		String testUrl = "http://download.taobaocdn.com/wangwang/AliIM2012_taobao(7.20.22C).exe";
//		String testUrl = "http://www.baidu.com/img/baidu_sylogo1.gif";
		logger.debug("testUrl:{}", testUrl);
		edtURL.setText(testUrl);

		ArrayAdapter<Integer> aa = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, new Integer[] { 1, 2, 5, 10, 25, 50 });
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spThreadCount.setAdapter(aa);
		spThreadCount.setOnItemSelectedListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (v == btnDownload)
		{
			if (mDownloading)
			{
				mTask.cancel(false);
				btnDownload.setText("Download");
				tvBytes.setText("");
				tvSpeed.setText("");
				tvMaxSpeed.setText("");
				tvMinSpeed.setText("");
				pbDownload.setProgress(0);
				spThreadCount.setEnabled(true);
				log("download canceled");
				mDownloading = false;
			}
			else
			{
				logger.debug("sd card state:{}", Environment.getExternalStorageState());
				if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
				{
					Toast.makeText(this, "SD CARD IS NOT AVAILABLE O.", Toast.LENGTH_LONG).show();
					return;
				}
				File downloadCacheDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				mFileName = downloadCacheDirectory.getAbsolutePath() + File.separator + "taobao.exe";
				doDownload();
				btnDownload.setEnabled(false);
			}
		}
	}

	private void doDownload()
	{
		String url = edtURL.getText().toString();
		btnDownload.setEnabled(false);
		spThreadCount.setEnabled(false);
		mMaxSpeed = 0;
		mMinSpeed = Float.MAX_VALUE;
		mTask = new MultiThreadDownloadTask(url, mFileName, this, mDownloadThreadCount);
		mTask.execute();
		// DownloadManager manager = (DownloadManager)
		// getSystemService(Context.DOWNLOAD_SERVICE);
		// Request req = new
		// Request(Uri.parse("http://dl_dir.qq.com/qqfile/qq/QQ2012/QQ2012.exe"));
		// manager.enqueue(req);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		Integer number = (Integer) arg0.getItemAtPosition(arg2);
		logger.debug("new thread count:{}", number);
		mDownloadThreadCount = number.intValue();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
		logger.debug("nothing selected");
	}

	@Override
	public void onDownloadBegin(long totalBytes)
	{
		mTotalBytes = totalBytes;
		mStartTime = System.nanoTime();
		logger.info("download Start,time:{},bytes:{}", mStartTime, mTotalBytes);
		log("download begin width " + mDownloadThreadCount + " threads");
		btnDownload.setEnabled(true);
		btnDownload.setText("Cancel");
		mDownloading = true;
	}

	@Override
	public void onProgressChanged(long finishedBytes)
	{
		tvBytes.setText(mTotalBytes + "/" + finishedBytes);
		long currentTime = System.nanoTime();
		float durationInSec = (currentTime - mStartTime) / 1000f / 1000 / 1000;
		float finishedKBs = finishedBytes / 1024f;
		float speed = finishedKBs / durationInSec;
		if (speed > mMaxSpeed)
		{
			// logger.debug("new max:{}", speed);
			mMaxSpeed = speed;
			tvMaxSpeed.setText(speed + "KB/s");
		}
		if (speed < mMinSpeed && finishedBytes != 0)
		{
			// logger.debug("new min:{}", speed);
			mMinSpeed = speed;
			tvMinSpeed.setText(speed + "KB/s");
		}
		tvSpeed.setText(speed + "KB/s");
		float progress = FloatMath.ceil((finishedBytes * 100f / mTotalBytes));
		pbDownload.setProgress((int) progress);
	}

	@Override
	public void onDownloadFinished()
	{
		long currentTime = System.nanoTime();
		float durationInSec = (currentTime - mStartTime) / 1000f / 1000 / 1000;
		tvBytes.append("(" + durationInSec + "s)");
		btnDownload.setText("Download");
		btnDownload.setEnabled(true);
		mDownloading = false;
		spThreadCount.setEnabled(true);
		logger.info("download finished in {} seconds", durationInSec);
		log(MessageFormat.format("download {4} bytes within {3,number} seconds\r\nMax:{0}\r\nMin:{1}\r\nAverage:{2}\r\n", tvMaxSpeed.getText(), tvMinSpeed.getText(), tvSpeed.getText(), durationInSec,
				mTotalBytes));
	}

	@Override
	public void onFailed(Throwable error)
	{
		log("download error");
		log(error.toString());
		btnDownload.setEnabled(true);
		btnDownload.setText("Download");
		spThreadCount.setEnabled(true);

		mDownloading = false;
	}

	private void log(String msg)
	{
		tvLog.append(msg);
		tvLog.append("\r\n");
		tvLog.post(new Runnable()
		{

			@Override
			public void run()
			{
				scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mDownloading)
		{
			mTask.cancel(false);
		}
	}

}
