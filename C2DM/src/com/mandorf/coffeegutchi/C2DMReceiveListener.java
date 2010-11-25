package com.mandorf.coffeegutchi;


public interface C2DMReceiveListener {
	
	public void onRegistrered(String registrationId);
	
	public void onUnregistered();
	
	public void onError(String errorId);
	

	public void onMessage (String message);
}
