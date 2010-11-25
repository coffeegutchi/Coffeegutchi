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
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
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

import com.mandorf.coffeegutchi.c2dm.C2DMessaging;
import com.mandorf.coffeegutchi.http.HttpService;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class Coffeegutchi extends Activity implements C2DMReceiveListener{


    private static final int SETUP_USER_ID = 1;
    private ListView mListView;
	private SimpleAdapter mListAdapter;
	private View mMessageIcon;
	private Handler mHandler;
	private TextView mLoginName;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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

	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	C2DMReceiver.removeListener(this);
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	String userName = PreferencesManager.getInstance(this).getString(PreferencesManager.KEY_NAME);
    	//Make sure the user has been authenticated
    	if (userName == null) {
		    Intent intent = new Intent(Coffeegutchi.this, LoginActivity.class);
            startActivityForResult(intent, SETUP_USER_ID);
            return;
		}

    	mLoginName.setText(userName);

		mMessageIcon.setVisibility(View.GONE);
 
    	//Update Regid
    	C2DMReceiver.setListener(this);
    	C2DMessaging.register(this, "coffeegutchi@gmail.com");

    	updateUsers();
    }


    private void updateUsers() {
        try {

            /** Handling XML */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            /** Send URL to parse XML Tags */
            URL sourceUrl = new URL("http://coffeegutchi.x10.mx/getusers.php");

            /** Create handler to handle XML Tags ( extends DefaultHandler ) */
            MyXMLHandler myXMLHandler = new MyXMLHandler();
            xr.setContentHandler(myXMLHandler);
            xr.parse(new InputSource(sourceUrl.openStream()));

//            mListView.setAdapter(new ArrayAdapter<UserListItem>(this, android.R.layout.simple_list_item_1, myXMLHandler.getResult()));
            final String sKeys[] = new String[] { "name", "online" };
            final int sResourceIds[] = new int[] { R.id.list_item_name, R.id.list_item_online };

            mListAdapter = new SimpleAdapter(this, myXMLHandler.mArrayListMap, R.layout.user_list_item, sKeys, sResourceIds);
            mListView.setAdapter(mListAdapter);
            mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View item, int position, long id) {
					HashMap<String, String> map =  (HashMap<String, String>) mListAdapter.getItem(position);
					
					String name = map.get("name");
					 String url = "http://coffeegutchi.x10.mx/sendmessage.php?user=" + name + "&name=" + name;
					 System.out.println("TOMA Sending url[" + url + "]");
				     HttpService.getInstance().httpRequest(1, url, HttpService.GET, null);
				     
				}
             
			});

        } catch (Exception e) {
            System.out.println("XML Pasing Excpetion = " + e);
        }

    }

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
}
