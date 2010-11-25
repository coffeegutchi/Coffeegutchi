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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.facebook.android.Util;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;
import com.mandorf.coffeegutchi.c2dm.C2DMessaging;
import com.mandorf.coffeegutchi.http.HttpService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class Coffeegutchi extends Activity implements C2DMReceiveListener{


    private static final int SETUP_USER_ID = 1;
	private static final String APP_ID = "168243349873179";
	
	
	 private ArrayList<HashMap<String, String>> mArrayListMap;
	
    private ListView mListView;
	private SimpleAdapter mListAdapter;
	private View mMessageIcon;
	private Handler mHandler;
	private TextView mLoginName;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//Setup facebook
       	mFacebook = new Facebook(APP_ID);
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);
        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());

		mHandler = new Handler();
		
    	mListView = (ListView)findViewById(R.id.list);
    	mMessageIcon = findViewById(R.id.message_icon);
     	mMessageIcon.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mMessageIcon.setVisibility(View.GONE);
				return true;
			}
		});
     	mLoginName = (TextView) findViewById(R.id.login_name);
     	
      mArrayListMap = new ArrayList<HashMap<String, String>>();
      final String sKeys[] = new String[] { "name", "online" };
      final int sResourceIds[] = new int[] { R.id.list_item_name, R.id.list_item_online };

		mListAdapter = new SimpleAdapter(this, mArrayListMap,
				R.layout.user_list_item, sKeys, sResourceIds);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View item,
					int position, long id) {
				HashMap<String, String> map = (HashMap<String, String>) mListAdapter
						.getItem(position);

				String name = map.get("name");
				String url = "http://coffeegutchi.x10.mx/sendmessage.php?user="
						+ name + "&name=" + name;
				System.out.println("TOMA Sending url[" + url + "]");
				HttpService.getInstance().httpRequest(1, url, HttpService.GET,
						null);

			}

		});


	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	C2DMReceiver.removeListener(this);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if(!mFacebook.isSessionValid()) {
    		showDialog(R.id.login_dialog);
    	}
//    	//Make sure the user has been authenticated
//    	if (userName == null) {
//		    Intent intent = new Intent(Coffeegutchi.this, LoginActivity.class);
//            startActivityForResult(intent, SETUP_USER_ID);
//            return;
//		}
//
//    	mLoginName.setText(userName);
//
//		mMessageIcon.setVisibility(View.GONE);
// 
//    	//Update Regid
//    	C2DMReceiver.setListener(this);
//    	C2DMessaging.register(this, "coffeegutchi@gmail.com");
//
//    	updateUsers();
    }


    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	// TODO Auto-generated method stub
    	super.onPrepareDialog(id, dialog);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mFacebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	
    	if (id == R.id.login_dialog) {
    		dialog = new AlertDialog.Builder(this)
					.setMessage(
							"Welcome\n\n This service uses Facebook to autorize usage. Please log in with your Facebook credentials")
					.setTitle("Login")
					.setPositiveButton("Login", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//TODO TOMA Change to add correct permissions if needed
					String[] mPermissions = new String[] {};
					mFacebook.authorize(Coffeegutchi.this, mPermissions,
                            new LoginDialogListener());
					
				}
			}).create();
    	} else {
    		dialog = super.onCreateDialog(id);
    	}
    	return dialog;
    }
    
    
//    private void updateUsers() {
//        try {
//
//            /** Handling XML */
//            SAXParserFactory spf = SAXParserFactory.newInstance();
//            SAXParser sp = spf.newSAXParser();
//            XMLReader xr = sp.getXMLReader();
//
//            /** Send URL to parse XML Tags */
//            URL sourceUrl = new URL("http://coffeegutchi.x10.mx/getusers.php");
//
//            /** Create handler to handle XML Tags ( extends DefaultHandler ) */
//            MyXMLHandler myXMLHandler = new MyXMLHandler();
//            xr.setContentHandler(myXMLHandler);
//            xr.parse(new InputSource(sourceUrl.openStream()));
//
////            mListView.setAdapter(new ArrayAdapter<UserListItem>(this, android.R.layout.simple_list_item_1, myXMLHandler.getResult()));
//            final String sKeys[] = new String[] { "name", "online" };
//            final int sResourceIds[] = new int[] { R.id.list_item_name, R.id.list_item_online };
//
//            mListAdapter = new SimpleAdapter(this, myXMLHandler.mArrayListMap, R.layout.user_list_item, sKeys, sResourceIds);
//            mListView.setAdapter(mListAdapter);
//            mListView.setOnItemClickListener(new OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> arg0, View item, int position, long id) {
//					HashMap<String, String> map =  (HashMap<String, String>) mListAdapter.getItem(position);
//					
//					String name = map.get("name");
//					 String url = "http://coffeegutchi.x10.mx/sendmessage.php?user=" + name + "&name=" + name;
//					 System.out.println("TOMA Sending url[" + url + "]");
//				     HttpService.getInstance().httpRequest(1, url, HttpService.GET, null);
//				     
//				}
//             
//			});
//
//        } catch (Exception e) {
//            System.out.println("XML Pasing Excpetion = " + e);
//        }
//
//    }

    private class UserListItem {
        public String username;
        public int online;
        public int caffeinLevel;
    }

    private class MyXMLHandler extends DefaultHandler {

        private ArrayList<UserListItem> mArrayList;
        public ArrayList<HashMap<String, String>> mArrayListMap;


        public MyXMLHandler() {
            mArrayList = new ArrayList<UserListItem>();
            mArrayListMap = new ArrayList<HashMap<String, String>>();
        }

        public ArrayList<UserListItem> getResult() {
            return mArrayList;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {


            if (localName.equals("user")) {
                /** Start */
                UserListItem sitesList = new UserListItem();

                /** Get attribute value */
                sitesList.username = attributes.getValue("name");
                sitesList.online = Integer.valueOf(attributes.getValue("online"));
                sitesList.caffeinLevel = Integer.valueOf(attributes.getValue("caffeinLevel"));
                mArrayList.add(sitesList);
                mArrayListMap = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("name", attributes.getValue("name"));
                map.put("online", ((Integer.valueOf(attributes.getValue("online"))==0)?"Offline":"Online"));
                map.put("caffeinLevel", attributes.getValue("caffeinLevel"));
                mArrayListMap.add(map);
            }

        }
    }

	@Override
	public void onRegistrered(String registrationId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnregistered() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String errorId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(final String message) {
		
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(Coffeegutchi.this, message, Toast.LENGTH_LONG).show();

				mMessageIcon.setVisibility(View.VISIBLE);
				
				MediaPlayer mp = MediaPlayer.create(getBaseContext(),
                        R.raw.knock);
				
				AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
				int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
				
                mp.start();
                mp.setOnCompletionListener(new OnCompletionListener() {
 
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
			}
		});
	}
	
	/**
	 * Facebook stuff
	 * 
	 */
    public class SampleAuthListener implements AuthListener {

        public void onAuthSucceed() {
        	mLoginName.setText("You have logged in!");
        	mAsyncRunner.request("me/friends", new SampleRequestListener());
//            mText.setText("You have logged in! ");
//            mRequestButton.setVisibility(View.VISIBLE);
//            mUploadButton.setVisibility(View.VISIBLE);
//            mPostButton.setVisibility(View.VISIBLE);
        }

        public void onAuthFail(String error) {
            mLoginName.setText("Login Failed: " + error);
        }
    }

    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
        	mLoginName.setText("Logging out...");
        }

        public void onLogoutFinish() {
        	mLoginName.setText("You have logged out! ");
//            mRequestButton.setVisibility(View.INVISIBLE);
//            mUploadButton.setVisibility(View.INVISIBLE);
//            mPostButton.setVisibility(View.INVISIBLE);
        }
    }
    
    private final class LoginDialogListener implements DialogListener {
        public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        public void onFacebookError(FacebookError error) {
            SessionEvents.onLoginError(error.getMessage());
        }
        
        public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }
    
    public class SampleRequestListener extends BaseRequestListener {
		public void onComplete(final String response) {
            try {
                // process the response here: executed in background thread
                Log.d("Facebook-Example", "Response: " + response.toString());
                JSONObject json = Util.parseJson(response);
                
                
                JSONArray kalle = json.getJSONArray("data");
                
                int items = kalle.length();
                for (int i = 0; i < items; i++) {
                	JSONObject friendItem = kalle.getJSONObject(i);
                	System.out.println("hej");
                  
                  HashMap<String, String> map = new HashMap<String, String>();
                  map.put("name", friendItem.getString("name"));
                  map.put("online", "Offline");
                  map.put("caffeinLevel", friendItem.getString("id"));
                  mArrayListMap.add(map);

				}
                mHandler.post(new Runnable() {
					@Override
					public void run() {
						 mListAdapter.notifyDataSetChanged();
					}
				});
               
            } catch (JSONException e) {
                Log.w("Facebook-Example", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
            }
        }
    }
	
}
