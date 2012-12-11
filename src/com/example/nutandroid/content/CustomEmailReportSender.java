package com.example.nutandroid.content;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class CustomEmailReportSender implements ReportSender
{

	private final Context mContext;

	public CustomEmailReportSender(Context ctx)
	{
		mContext = ctx;
	}

	@Override
	public void send(CrashReportData errorContent) throws ReportSenderException
	{

		final String subject = "ZSmart DataMall Crash Report";
		final String body = buildBody(errorContent);

		Uri uri = Uri.parse("mailto:" + ACRA.getConfig().mailTo());
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO, uri);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		mContext.startActivity(emailIntent);
	}

	private String buildBody(CrashReportData errorContent)
	{
		ReportField[] fields = ACRA.getConfig().customReportContent();
		if (fields.length == 0)
		{
			fields = ACRA.DEFAULT_MAIL_REPORT_FIELDS;
		}

		final StringBuilder builder = new StringBuilder();
		for (ReportField field : fields)
		{
			builder.append(field.toString()).append("=");
			builder.append(errorContent.get(field));
			builder.append('\n');
		}
		return builder.toString();
	}

}
