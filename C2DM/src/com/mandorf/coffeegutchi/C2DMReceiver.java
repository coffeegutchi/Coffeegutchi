/***
	Copyright (c) 2010 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.mandorf.coffeegutchi;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.mandorf.coffeegutchi.c2dm.C2DMBaseReceiver;
import com.mandorf.coffeegutchi.http.HttpService;

public class C2DMReceiver extends C2DMBaseReceiver {
	
	static ArrayList<C2DMReceiveListener> sListeners = null;
	
	public C2DMReceiver() {
		super("coffeegutchi@gmail.com");
	}
	
	public static void setListener(C2DMReceiveListener listener) {
		if (sListeners == null) {
			sListeners = new ArrayList<C2DMReceiveListener>();
		}
		sListeners.add(listener);
	}
	
	public static void removeListener(C2DMReceiveListener listener) {
		if (sListeners == null) {
			sListeners.remove(listener);
		}
	}

	@Override
	public void onRegistrered(Context context, String registrationId) {
		Log.w("C2DMReceiver-onRegistered", registrationId);
		String name = PreferencesManager.getInstance(context).getString(PreferencesManager.KEY_NAME);
		
        String url = "http://coffeegutchi.x10.mx/updateregid.php?name=" + name + "&regid=" + registrationId;
		
		Intent intent = new Intent(this, Coffeegutchi.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction("REGISTERED");
        HttpService.getInstance().httpRequest(1, url, HttpService.GET, null);
		if (sListeners != null) {
			for (C2DMReceiveListener listener : sListeners) {
				listener.onRegistrered(registrationId);
			}
		}
	}
	
	@Override
	public void onUnregistered(Context context) {
		Log.w("C2DMReceiver-onUnregistered", "got here!");
		if (sListeners != null) {
			for (C2DMReceiveListener listener : sListeners) {
				listener.onUnregistered();
			}
		}
	}
	
	@Override
	public void onError(Context context, String errorId) {
		Log.w("C2DMReceiver-onError", errorId);
		if (sListeners != null) {
			for (C2DMReceiveListener listener : sListeners) {
				listener.onError(errorId);
			}
		}
	}
	
	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.w("C2DMReceiver", intent.getStringExtra("payload"));
		
		notifyOnMessage(context, intent.getStringExtra("payload"));
		
		if (sListeners != null) {
			for (C2DMReceiveListener listener : sListeners) {
				listener.onMessage(intent.getStringExtra("payload"));
			}
		}
	}

	public void notifyOnMessage(Context context, final String message) {
		
		Handler handler = new Handler(context.getMainLooper());
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (sListeners != null) {
					for (C2DMReceiveListener listener : sListeners) {
						listener.onMessage(message);
					}
				}
			}
		});
		
	}
	
	
}
