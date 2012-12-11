package com.example.nutandroid.http;

import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRouteParamBean;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;

public class HttpFactory
{
	private static final HttpClient httpClient;
	private static final HttpContextEx httpContext = new HttpContextEx();
	private static final CookieStore cookieStore = new BasicCookieStore();
	private static final Header[] commonHeaders;

	static
	{
		httpContext.setCookieStore(cookieStore);
		httpClient = AndroidHttpClient.newInstance();
		
		Locale curLocale = Locale.getDefault();
		commonHeaders = new Header[3];
		commonHeaders[0] = new BasicHeader("zsmart-user-agent", "ZSMART_MOBILE_FRAMEWORK");
		commonHeaders[1] = new BasicHeader("Accept-Language", curLocale.getLanguage() + "-" + curLocale.getCountry());
		commonHeaders[2] = new BasicHeader("Accept-Charset", "utf-8");
		// 需要一个广播接收器，获取locale改变。
	}

	/**
	 * 设置网络连接代理，暂不考虑HTTPS代理
	 * 
	 * @param host
	 * @param port
	 */
	public static synchronized void configProxy(String host,int port)
	{
		HttpParams params = httpClient.getParams();
		ConnRouteParamBean routeBean = new ConnRouteParamBean(params);
		HttpHost httpHost = new HttpHost(host, port);
		routeBean.setDefaultProxy(httpHost);
	}

	public static synchronized HttpClient getHttpClient()
	{
		return httpClient;
	}

	public static synchronized HttpContextEx getSharedHttpContext()
	{
		return httpContext;
	}

	public static synchronized Header[] getCommonHeaders()
	{
		return commonHeaders;
	}
}
