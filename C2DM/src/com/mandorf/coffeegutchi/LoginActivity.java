package com.mandorf.coffeegutchi;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mandorf.coffeegutchi.http.HttpRequestResponse;
import com.mandorf.coffeegutchi.http.HttpResponseListener;
import com.mandorf.coffeegutchi.http.HttpService;

public class LoginActivity extends Activity implements HttpResponseListener {

    private static final int LOGIN_USER_REQ_ID = 1;
	private Button mButton;
    private String mUserName;
	private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_user);
        mButton = (Button)findViewById(R.id.create_button);
        mButton.setEnabled(false);
        TextView tv = (TextView)findViewById(R.id.user_name);
        tv.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mButton.setEnabled(true);
                } else {
                    mButton.setEnabled(false);
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
    	HttpService.getInstance().removeListener(this);
    	super.onDestroy();
    }

    public void createUser(View v) {

        TextView tv = (TextView)findViewById(R.id.user_name);
        mUserName = tv.getText().toString();

        String url = "http://coffeegutchi.x10.mx/createuser.php?name=" + mUserName;
        HttpService.getInstance().setListener(this);
        HttpService.getInstance().httpRequest(LOGIN_USER_REQ_ID, url, HttpService.GET, null);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Creating user....");
        mProgressDialog.show();
    }

	@Override
	public void onResponseReceived(int requestId, HttpRequestResponse response) {
		if (requestId == LOGIN_USER_REQ_ID) {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}

			if (response.getStatus() == HttpStatus.SC_OK) {
				PreferencesManager.getInstance(this).setString(
						PreferencesManager.KEY_NAME, mUserName);
				finish();
			} else {
				TextView tv = (TextView) findViewById(R.id.test_tag);
				tv.setText(response.getResponse());
				Toast.makeText(this, "Failed to register user",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
