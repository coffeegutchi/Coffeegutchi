package com.mandorf.coffeegutchi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HttpService {

    private static final String TAG = "HttpService";
    public static final int GET = 0;
    public static final int POST = 1;
    public static final String HTTP_RESPONSE = "HTTP_RESPONSE";
    public static final String HTTP_STATUS = "HTTP_STATUS";
	private static ArrayList<HttpResponseListener> mListeners;
	private static HttpService sInstance = null;

    /*
     *  for example:
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("aaa", "bbb"));
        nameValuePairs.add(new BasicNameValuePair("ccc", "ddd"));
     */
    private HttpService(){
        mListeners = new ArrayList<HttpResponseListener>();
    }
    
    public static HttpService getInstance() {
    	if (sInstance == null) {
    		sInstance  = new HttpService();
    	}
		return sInstance;
    }
    
    public void setListener(HttpResponseListener listener) {
    	mListeners.add(listener);
    }
    
    public void removeListener(HttpResponseListener listener) {
    	mListeners.remove(listener);
    }
    
    public void httpRequest(int requestCode, String url, int getOrPost, List<NameValuePair> nameValuePairs) {
    	HttpRequest req = new HttpRequest(requestCode, url, getOrPost, nameValuePairs);
    	Thread thread = new Thread(req);
        try{
            thread.start();
        }catch(Exception e){
            Log.e(TAG, "Could not start httpRequest", e);
        }
    }
    
    synchronized private static void notifyListeners (int requestId, HttpRequestResponse response) {
    	
    	//notify listeners
        for (HttpResponseListener listener : mListeners) {
        	listener.onResponseReceived(requestId, response);
		}
    }

    
    private class HttpRequest implements Runnable {
        public int mRequestCode;
        public String mUrl;
        public int mGetOrPost = 0;
        public List<NameValuePair> mNameValuePairs;

    	public HttpRequest(int requestCode, String url, int getOrPost, List<NameValuePair> nameValuePairs) {
            mRequestCode = requestCode;
            mUrl = url;
            mGetOrPost = getOrPost;
            mNameValuePairs = nameValuePairs;
        }

		@Override
		public void run() {
			doRequest(this);
		}
    }
    
    private void doRequest(HttpRequest req){
        HttpClient httpclient = new DefaultHttpClient();
        HttpRequestBase httpRequest = null;
        HttpResponse httpResponse = null;
        InputStream inputStream = null;
        StringBuffer buffer = new StringBuffer();

        if(POST == req.mGetOrPost){
            httpRequest = new HttpPost(req.mUrl);
            try {
                ((HttpPost)httpRequest).setEntity(new UrlEncodedFormEntity(req.mNameValuePairs));
            } catch (UnsupportedEncodingException usee) {
                Log.e(TAG, "Could not encode the nameVaulePairs.", usee);
            }
        }else{
            try {
                URI uri = new URI(req.mUrl);
                httpRequest = new HttpGet(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
        if(httpRequest != null){
            try{
                httpResponse = httpclient.execute(httpRequest);

                inputStream = httpResponse.getEntity().getContent();
                int contentLength = (int) httpResponse.getEntity().getContentLength();
                if (contentLength < 0){
                    Log.e(TAG, "The HTTP response is too long.");
                }
                byte[] data = new byte[256];
                int len = 0;
                while (-1 != (len = inputStream.read(data)) )
                {
                    buffer.append(new String(data, 0, len));
                }
                inputStream.close();
            }catch (ClientProtocolException cpe) {
                Log.e(TAG, "Http protocol error occured.", cpe);
            }catch (IllegalStateException ise) {
                Log.e(TAG, "Could not get a HTTP response from the server.", ise);
            }catch (IOException ioe) {
                Log.e(TAG, "Could not establish a HTTP connection to the server or could not get a response properly from the server.", ioe);
            }
        }
        
        HttpRequestResponse resp = new HttpRequestResponse(httpResponse.getStatusLine().getStatusCode(), buffer.toString());
        
        notifyListeners(req.mRequestCode, resp);
    }

}
