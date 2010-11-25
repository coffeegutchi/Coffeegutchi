package com.mandorf.coffeegutchi.http;

public interface HttpResponseListener {

	public void onResponseReceived(int requestId, HttpRequestResponse response);
}
