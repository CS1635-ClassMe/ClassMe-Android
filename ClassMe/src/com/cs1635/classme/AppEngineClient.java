package com.cs1635.classme;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.net.URI;
import java.util.List;

public class AppEngineClient
{
	static final String BASE_URL = "https://studentclassnet.appspot.com";

	public static HttpResponse makeRequest(String urlPath, List<NameValuePair> params) throws Exception 
	{
		// Make POST request
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpParams httpParameters = client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10 * 1000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10 * 1000);
		
	    URI uri = new URI(BASE_URL + urlPath);
	    HttpPost post = new HttpPost(uri);
	    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
	    post.setEntity(entity);
	    HttpResponse res = client.execute(post);
	    return res;
	}
}
