package com.tuwien.ac.at.main;

import twitter4j.TwitterException;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.tuwien.ac.at.main.service.TweetFlowService;
import com.tuwien.ac.at.main.twitter.OAuthLogin;

/**
 * LoginScreen
 * 
 * @author Raunig Stefan
 *
 * This activity is used to login a twitter user over the twitter authentification page
 * go to www.twitter.com for further information.
 */
public class LoginScreen extends Activity {
	
	private static String TAG = "SERVICE";
	private final long SLEEPTIME = 60*1000;
	private OAuthLogin session;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.login);

	    final Button btnlogin = (Button) findViewById(R.id.btnlogin);
	    btnlogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    						
				//authentificat and login user
				try {
					session = OAuthLogin.getInstance();
					String urlRequestToken = session.buildRequestToken();
			
					//starts the Browser for the user authentification
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlRequestToken)));
					finish();
				} catch (TwitterException  e) {
					Log.d(TAG, "TwitterException befor opening Browser", e);
				}
			}
	    });
    }
	
	@Override
	protected void onResume(){
		super.onResume();

		session = OAuthLogin.getInstance();
		Intent intent = getIntent();
		
		if(intent != null && intent.getData() != null){
		
			Log.d(TAG, "AccessToken is " + intent.getDataString());
		
			//parse uri string 
			Uri uri = intent.getData();
		
			if(uri != null){
				try {
					//login to twitter with accessToken
					session.loginIntent(uri);
					session.loginAuto(session.getAccessTokenKey(), session.getAccessTokenSecret());
				
					Log.d(TAG, "AccessToken: "+uri.toString());
					
					CheckBox rememberBox = (CheckBox) findViewById(R.id.checkRemember);
					if(rememberBox.isChecked()){
						final SharedPreferences setting = getSharedPreferences(getString(R.string.preference), Context.MODE_PRIVATE);
						final Editor editor = setting.edit();
						
						//save token and make toggle automated login
						editor.putString("AccessTokenKey", session.getAccessTokenKey());
						editor.putString("AccessTokenSecret", session.getAccessTokenSecret());
						editor.putBoolean("AutoLogin", true);
						editor.commit();
					}
					
					//begin of time counter
					long startTime = SystemClock.elapsedRealtime();
			  	  	
					//IntentSender that will launch the service
			        PendingIntent mAlarmSender = PendingIntent.getService(this, 0, new Intent(this, TweetFlowService.class), 0);
			  	  	
			        //set AlarmManager it triggers the Service while phone is in sleep mode
			        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					
			  	  	alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, SLEEPTIME, mAlarmSender);
					
					//redirect to TabScreen
					startActivity(new Intent(LoginScreen.this, TabScreen.class));
					finish();
				} catch (TwitterException e) {
					Log.e(TAG, "Exception at login Uri response: "+ intent.getDataString(), e);
					Toast.makeText(this, "Twitter authentification request failed please try again.", Toast.LENGTH_LONG).show();
				}
			}
		}
	}
}
