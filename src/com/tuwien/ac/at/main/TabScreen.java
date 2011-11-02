package com.tuwien.ac.at.main;

import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;

import com.tuwien.ac.at.main.service.TweetFlowService;
import com.tuwien.ac.at.main.twitter.OAuthLogin;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

/**
 * TabScreen
 * 
 * @author Raunig Stefan
 *
 * Container for users hometimeline and parsed tweetflows
 * 
 * use of free icon set from Akhtar Sheikha, http://www.2expertsdesign.com/
 */
public class TabScreen extends TabActivity{
	private final String TAG = "TAB";

	private final OAuthLogin session = OAuthLogin.getInstance();
	private ProgressBar mProgressBar;
	
	private boolean isRunning = false;
	private boolean hideText = false;
	private AlarmManager alarm = null;
	private PendingIntent mAlarmSender = null;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		final int currTab = intent.getIntExtra("currTab", 0);
		
	    setContentView(R.layout.tab);
	    
	    mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

	    //load persistent Token
	    final SharedPreferences setting = getSharedPreferences(getString(R.string.preference), Context.MODE_PRIVATE);
		String accessTokenKey = setting.getString("AccessTokenKey", "");
		String accessTokenSecret = setting.getString("AccessTokenSecret", "");
	    
		//automaticaly login with Token
		session.loginAuto(accessTokenKey, accessTokenSecret);
	    
	    //start service if its not started
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    
	    List<RunningServiceInfo> services = am.getRunningServices(Integer.MAX_VALUE);
	    
	    for(RunningServiceInfo s: services){
	    	if(s.service.getClassName().equals("com.tuwien.ac.at.main.servie.TweetFlowService")){
	    		isRunning = true;
	    		break;
	    	}
	    }
	    
	    if(!isRunning){
	    	//begin of time counter
	    	long startTime = SystemClock.elapsedRealtime();
	    	final long SLEEPTIME = 60*1000;
		
	    	//IntentSender that will launch the service
	    	mAlarmSender = PendingIntent.getService(this, 0, new Intent(this, TweetFlowService.class), 0);
  	  	
	    	//set AlarmManager it makes possible to run Service while phone is in sleep mode
	    	alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
  	  		alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime, SLEEPTIME, mAlarmSender);
  	  	}
	    
	    //Resource object to get Drawables
	    final Resources res = getResources();
	    final TabHost tabHost = getTabHost();
	    
	    //compute in background
	    new AsyncTask<Void, Integer, TabSpec[]>() {
	    	private TabSpec[] tabs = new TabSpec[3];
    		private Intent intent;
	    	
	    	@Override
			protected void onPreExecute (){
				//show progressbar
			}
	    	
	    	@Override
			protected TabSpec[] doInBackground(Void... params) {
	    		//Reusable Intent for each tab
				intent = new Intent().setClass(TabScreen.this, TweetList.class);
			    tabs[0] = tabHost.newTabSpec("Tweets").setIndicator("Home", res.getDrawable(R.drawable.tab_home)).setContent(intent);
			    intent = new Intent().setClass(TabScreen.this, MentionsList.class);
			    tabs[1] = tabHost.newTabSpec("Mentions").setIndicator("Mention", res.getDrawable(R.drawable.tab_mention)).setContent(intent);
			    intent = new Intent().setClass(TabScreen.this, TweetFlowList.class);
			    tabs[2] = tabHost.newTabSpec("Tweetflow").setIndicator("TweetFlow", res.getDrawable(R.drawable.tab_tweetflow)).setContent(intent);
			    return tabs;
			}
	    	
	    	@Override
	    	protected void onPostExecute(TabSpec[] result){    		
	    		try{
	    			for(TabSpec spec: result){
	    				tabHost.addTab(spec);
	    			}
	    		} catch (NullPointerException e){
	    			Log.d("BUFFER", "NullPointer at refresh.", e);
	    		}
	    		//stop progressbar and proceed with result
	    		mProgressBar.setVisibility(ProgressBar.GONE);
	    		tabHost.setCurrentTab(currTab);
	    	}
		}.execute();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainactivitymenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		final OAuthLogin twitter = OAuthLogin.getInstance();
		
	    switch (item.getItemId()) {
	    case R.id.refresh:
	    	TabHost tabHost = getTabHost();
	    	
	    	switch(tabHost.getCurrentTab()){
	    	case 0:
	    		tabHost.setCurrentTab(2);
	    		tabHost.setCurrentTab(0);
	    		return true;
	    	case 1:
	    		tabHost.setCurrentTab(2);
	    		tabHost.setCurrentTab(1);
	    		return true;
	    	case 2:
	    		tabHost.setCurrentTab(1);
	    		tabHost.setCurrentTab(2);
	    		return true;
	    	default:
	    		return true;
	    	} 	
		case R.id.logout:
			//logout the user and terminate connections
			if(isRunning){
				//get alarm service to stop
				mAlarmSender = PendingIntent.getService(this, 0, new Intent(this, TweetFlowService.class), 0);
	        	alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			}
			
			alarm.cancel(mAlarmSender);
			twitter.logout(this);
			startActivity(new Intent(this, LoginScreen.class));
	    	this.finish();
	        return true;
	    case R.id.send:
	    	toggleTextArea("");
	    	return true;
	    case R.id.answer:
	    	Status answerStatus = (Status) item.getIntent().getSerializableExtra("status");
	    	toggleTextArea("@"+answerStatus.getUser().getScreenName()+ " ");
	    	return true;
	    case R.id.retweet:
	    	Status retweetStatus = (Status) item.getIntent().getSerializableExtra("status");
	    	try {
				twitter.retweet(retweetStatus.getId());
			} catch (TwitterException e) {
				Toast.makeText(this, "Retweeting failed please retry later!", Toast.LENGTH_LONG).show();
				Log.d(TAG, "Twitter Exception at retweeting", e);
			}
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }  
	}
	
	private void toggleTextArea(String msg){
		final OAuthLogin twitter = OAuthLogin.getInstance();
		
		final EditText text = (EditText) findViewById(R.id.sendText);
		final Button btnSend = (Button) findViewById(R.id.sendButton);
		final Button btnToggle = (Button) findViewById(R.id.toggleButton);

		if(hideText){
			text.setText("");
			text.setVisibility(EditText.GONE);
			btnSend.setVisibility(Button.GONE);
			btnToggle.setVisibility(Button.GONE);
			hideText = false;
    	} else {
    		text.setText(msg);
    		text.setVisibility(TextView.VISIBLE);
    		btnSend.setVisibility(Button.VISIBLE);
			btnToggle.setVisibility(Button.VISIBLE);
    		hideText = true;
    	}
		
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String msg = text.getText().toString();
					
					//length must be under 140 charakters
					if(msg.length() < 140){
						Toast.makeText(TabScreen.this, "Sending Tweet", Toast.LENGTH_LONG).show();
						twitter.sendTweet(msg);
						text.setText("");
						text.setVisibility(EditText.GONE);
						btnSend.setVisibility(Button.GONE);
						btnToggle.setVisibility(Button.GONE);
					} else {
						Toast.makeText(TabScreen.this, "Message to long, length: "+msg.length(), Toast.LENGTH_LONG).show();
					}
				} catch (TwitterException e) {
					Log.d(TAG, "sendTweet failed ", e);
				}
			}
		});
		
		btnToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				text.setVisibility(EditText.GONE);
				btnSend.setVisibility(Button.GONE);
				btnToggle.setVisibility(Button.GONE);
				hideText = false;
			}
		});
	}
}