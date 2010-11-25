package com.mandorf.coffeegutchi.http;

public class HttpRequestResponse {

	private int mStatus;
	private String mResponse;

	public HttpRequestResponse(int status, String responce) {
		mStatus = status;
		mResponse = responce;
	}

	public String getResponse() {
		return mResponse;
	}

	public int getStatus() {
		return mStatus;
	}
}
