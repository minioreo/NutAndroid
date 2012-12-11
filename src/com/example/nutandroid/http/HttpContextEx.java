package com.example.nutandroid.http;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;

public class HttpContextEx extends BasicHttpContext
{
	public void setCookieStore(CookieStore cookieStore)
	{
		setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	public CookieStore getCookieStore()
	{
		return (CookieStore) getAttribute(ClientContext.COOKIE_STORE);
	}
}
