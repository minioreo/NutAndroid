package com.example.nutandroid.content;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.example.nutandroid.R;
import com.example.nutandroid.activity.CrashActivity;
import com.example.nutandroid.activity.CustomTab;
import com.example.nutandroid.activity.CustomviewActivity;
import com.example.nutandroid.activity.DownloadActivity;
import com.example.nutandroid.activity.GestureSurfaceActivity;
import com.example.nutandroid.activity.IOSpeedActivity;
import com.example.nutandroid.activity.MTWrite;
import com.example.nutandroid.activity.MultiThreadDownloadActivity;
import com.example.nutandroid.activity.PieBarActivity;
import com.example.nutandroid.activity.RelativePositionActivity;
import com.example.nutandroid.activity.RingViewActivity;
import com.example.nutandroid.activity.SimpleTestActivity;
import com.example.nutandroid.activity.SpeedTestViewActivity;
import com.example.nutandroid.activity.SurfaceActivity;
import com.example.nutandroid.activity.TabShowActivity;
import com.example.nutandroid.activity.TestFlickerAcivity;
import com.example.nutandroid.activity.WindowBackGroundActivity;
import com.example.nutandroid.util.MenuMgr;
import com.example.nutandroid.util.NutLogger;

@ReportsCrashes(
		formKey = "",
		mailTo = "minioreo@foxmail.com",
		mode = ReportingInteractionMode.DIALOG,
		resToastText = R.string.acra_toast,
		resDialogText = R.string.acra_dlg_txt,
		resDialogCommentPrompt = R.string.acra_dlg_comment_prpmpt,
		customReportContent = { ReportField.USER_COMMENT, ReportField.USER_CRASH_DATE, ReportField.USER_APP_START_DATE, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.BRAND,
				ReportField.STACK_TRACE, ReportField.LOGCAT, ReportField.THREAD_DETAILS })
public class NutApplication extends Application
{

	private static final NutLogger logger = NutLogger.getLogger(NutApplication.class);

	private static NutApplication _instanse;

	private MenuMgr menuMgr = new MenuMgr();

	public NutApplication()
	{
		_instanse = this;
	}

	public static NutApplication getInstance()
	{
		return _instanse;
	}

	@Override
	public void onCreate()
	{
		ACRA.init(this);
		CustomEmailReportSender sender = new CustomEmailReportSender(this);
		ACRA.getErrorReporter().setReportSender(sender);
		super.onCreate();
		// String clzName =
		// "org.apache.http.impl.client.DefaultRequestDirector";
		// String newClzName = NutLogger.loggerNameToTag(clzName);
		// logger.debug("classname:{},new class name:{}", clzName, newClzName);
		// Logger jdkLogger = Logger.getLogger(clzName);
		// jdkLogger.setLevel(Level.ALL);
		// if (jdkLogger.isLoggable(Level.FINE))
		// {
		// logger.debug("jdklog can log");
		// jdkLogger.log(Level.FINE, "fine");
		// jdkLogger.log(Level.INFO, "custmsg");
		// }
		// else
		// {
		// logger.debug("jdklogger cannot log");
		// }

		logger.debug("NutApplication created");

		loadMenu();

	}

	public MenuMgr getMenuMgr()
	{
		return menuMgr;
	}

	private void loadMenu()
	{
		logger.info("load menus");
		menuMgr.addItem("View/Custom View", CustomviewActivity.class);
		menuMgr.addItem("View/Ring View", RingViewActivity.class);
		menuMgr.addItem("Layout/Relative Layout", RelativePositionActivity.class);
		menuMgr.addItem("Layout/Tab", TabShowActivity.class);
		menuMgr.addItem("Layout/Tab2", CustomTab.class);
		menuMgr.addItem("View/Surface", SurfaceActivity.class);
		menuMgr.addItem("View/Surface Gesture", GestureSurfaceActivity.class);
		menuMgr.addItem("View/Window BackGround", WindowBackGroundActivity.class);
		menuMgr.addItem("View/Test Flicker", TestFlickerAcivity.class);
		menuMgr.addItem("View/Pie & Bar", PieBarActivity.class);
		menuMgr.addItem("HTTP/Download", DownloadActivity.class);
		menuMgr.addItem("HTTP/MultiThreadDownload", MultiThreadDownloadActivity.class);
		menuMgr.addItem("IO/Write Test", IOSpeedActivity.class);
		menuMgr.addItem("IO/MultiThread Write", MTWrite.class);
		menuMgr.addItem("ACRA/Crash", CrashActivity.class);
		menuMgr.addItem("View/simple", SimpleTestActivity.class);
		menuMgr.addItem("View/Gauge", SpeedTestViewActivity.class);
		logger.info("load menus finished");
	}

}
