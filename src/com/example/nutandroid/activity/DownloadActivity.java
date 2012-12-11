package com.example.nutandroid.activity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.nutandroid.R;
import com.example.nutandroid.http.HttpFactory;

public class DownloadActivity extends Activity implements OnClickListener
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadActivity.class);

	private Button btnDownload;
	private Thread mThread;

	private static final int MSG_PROGRESS = 0;
	private static final int MSG_FINISH = 1;
	private static final int MSG_SHOW_MSG = 2;

	private NutHandler mHandler = new NutHandler();
	private ProgressBar pbDownload;
	private EditText edtURL;

	private TextView tvLog;

	private ScrollView scrollView;

	private static class NutHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			DownloadActivity activity = (DownloadActivity) msg.obj;
			switch (msg.what)
			{
			case MSG_PROGRESS:
				activity.onDownloadProgress(msg.arg1);
				break;
			case MSG_FINISH:
				activity.onDownloadFinish();
				break;
			case MSG_SHOW_MSG:
				activity.showMsg(msg.getData().getString("MSG"));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		edtURL = (EditText) findViewById(R.id.edtURL);
		btnDownload = (Button) findViewById(R.id.btnDownload);
		pbDownload = (ProgressBar) findViewById(R.id.pbDownload);
		tvLog = (TextView) findViewById(R.id.tvLog);
		scrollView = (ScrollView) tvLog.getParent();

		// edtURL.setText("http://10.45.15.131:8080/Server4Android/DownloadServlet");
		edtURL.setText("http://10.45.15.131:8080/Server4Android/123.png");
		btnDownload.setOnClickListener(this);
	}

	private void onDownloadFinish()
	{
		pbDownload.setProgress(0);
		btnDownload.setEnabled(true);
	}

	private void onDownloadProgress(int progress)
	{
		pbDownload.setProgress(progress);
	}

	@Override
	public void onClick(View v)
	{
		if (v == btnDownload)
		{
			btnDownload.setEnabled(false);
			doDownload();
		}
	}

	private void doDownload()
	{
		logger.debug("do download");
		mThread = new Thread()
		{
			@Override
			public void run()
			{
				// mHandler.obtainMessage(MSG_PROGRESS, i + 1, 0,
				// DownloadActivity.this).sendToTarget();
				long startTime = System.nanoTime();
				HttpClient httpClient = HttpFactory.getHttpClient();
				String url = edtURL.getText().toString();
				logger.debug("url is {}", url);
				HttpGet get = new HttpGet(url);
				// HttpHead get = new HttpHead(url);
				try
				{
					HttpResponse response = httpClient.execute(get, HttpFactory.getSharedHttpContext());
					int statusCode = response.getStatusLine().getStatusCode();
					logger.debug("status Code:{}", statusCode);
					showMsg("status code:" + statusCode);
					printHeaders(response);
					HttpEntity entity = response.getEntity();
					if (entity != null)
					{
						logger.debug("isChuncked:{}", entity.isChunked());
						long contentLength = entity.getContentLength();
						showMsg("content-length:" + contentLength);
						InputStream inputStream = entity.getContent();
						byte[] buffer = new byte[2048];
						int i = 0;
						long totalBytes = 0;
						while ((i = inputStream.read(buffer)) != -1)
						{
							totalBytes += i;
							int progress = (int) (((float) totalBytes / contentLength) * 100);
							mHandler.obtainMessage(MSG_PROGRESS, progress, 0, DownloadActivity.this).sendToTarget();
						}
						showMsg("total read bytes:" + totalBytes);
					}
					else
					{
						showMsg("no entity,content-length:" + response.getHeaders(HTTP.CONTENT_LEN)[0].getValue());
					}
					float duration = System.nanoTime() - startTime;
					logger.debug("duration:{} = {} seconds", duration, duration / 1000 / 1000 / 1000);
					showMsg("time cost: " + duration / 1000 / 1000 / 1000 + " seconds");
				}
				catch (IOException e)
				{
					logger.error("network exception", e);
					showMsg(e.toString());
				}
				mHandler.obtainMessage(MSG_FINISH, 0, 0, DownloadActivity.this).sendToTarget();

			}
		};
		mThread.setDaemon(true);
		mThread.start();
	}

	private void showMsg(String msg)
	{
		if (getMainLooper().getThread() == Thread.currentThread())
		{
			tvLog.append(msg + "\r\n");
			scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		}
		else
		{
			Message message = mHandler.obtainMessage(MSG_SHOW_MSG, this);
			Bundle bundle = new Bundle();
			bundle.putString("MSG", msg);
			message.setData(bundle);
			message.sendToTarget();
		}
	}

	private void printHeaders(HttpResponse response)
	{
		Header[] headers = response.getAllHeaders();
		logger.debug("print response headers begin");
		for (int i = 0; i < headers.length; i++)
		{
			Header header = headers[i];
			logger.debug("name:{},value:{}", header.getName(), header.getValue());
			// HeaderElement[] elements = header.getElements();
			// logger.debug("\tprint elements begin.");
			// for (int j = 0; j < elements.length; j++)
			// {
			// HeaderElement element = elements[j];
			// logger.debug("\telement name:{},element value:{}",
			// element.getName(), element.getValue());
			// NameValuePair[] parameters = element.getParameters();
			// logger.debug("\t\tprint parameter begin.");
			// for (int k = 0; k < parameters.length; k++)
			// {
			// NameValuePair parameter = parameters[k];
			// logger.debug("\t\tparameter name:{},parameter value:{}",
			// parameter.getName(), parameter.getValue());
			// }
			// logger.debug("\t\tprint parameter finish.");
			// }
			// logger.debug("\tprint elements finish.");
		}
		logger.debug("print response headers finish");
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mThread != null && mThread.isAlive())
		{
			mThread.interrupt();
			mThread = null;
		}
	}
}
