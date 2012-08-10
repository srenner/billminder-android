package com.srenner.billminder;

import org.apache.http.impl.client.DefaultHttpClient;

public class PostClient {

	private DefaultHttpClient mClient;
	private String mCSRFTokenValue;
	//private String mURL;
	
	public DefaultHttpClient getClient() {
		return mClient;
	}
	
	public String getCSRFTokenValue() {
		return mCSRFTokenValue;
	}
	
	/*public String getURL() {
		return mURL;
	}*/
	
	public PostClient(DefaultHttpClient client, String csrfTokenValue) {
		mClient = client;
		mCSRFTokenValue = csrfTokenValue;
		/*mURL = url;*/
	}
}
