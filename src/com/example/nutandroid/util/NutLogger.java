package com.example.nutandroid.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NutLogger
{
	private static final boolean IS_DEVELOP_MODE = true;
	private Logger _logger;

	private NutLogger(String className)
	{
		_logger = LoggerFactory.getLogger(className);
	}

	@SuppressWarnings("rawtypes")
	public static NutLogger getLogger(Class cls)
	{
		return new NutLogger(cls.getSimpleName());
	}

	public static NutLogger getLogger(String className)
	{
		return new NutLogger(className);
	}

	public void debug(String msg, Object... params)
	{
		if (_logger.isDebugEnabled() || IS_DEVELOP_MODE)
		{
			_logger.debug(msg, params);
		}
	}

	public void info(String msg, Object... params)
	{
		if (_logger.isInfoEnabled())
		{
			_logger.info(msg, params);
		}
	}

	public void warn(String msg, Object... params)
	{
		if (_logger.isWarnEnabled())
		{
			_logger.warn(msg, params);
		}
	}

	public void warn(String msg, Throwable t)
	{
		if (_logger.isWarnEnabled())
		{
			_logger.warn(msg, t);
		}
	}

	public void error(String msg, Object... params)
	{
		if (_logger.isErrorEnabled())
		{
			_logger.error(msg, params);
		}
	}

	public void error(String msg, Throwable t)
	{
		if (_logger.isErrorEnabled())
		{
			_logger.error(msg, t);
		}
	}
	
	public static String loggerNameToTag(String loggerName)
	  {
	    if (loggerName == null) {
	      return "null";
	    }

	    int length = loggerName.length();
	    if (length <= 23) {
	      return loggerName;
	    }

	    int lastPeriod = loggerName.lastIndexOf(".");
	    return length - (lastPeriod + 1) <= 23 ? loggerName.substring(lastPeriod + 1) : loggerName.substring(loggerName.length() - 23);
	  }
}
