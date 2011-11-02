package com.tuwien.ac.at.main.service;

import java.util.regex.Pattern;

import com.tuwien.ac.at.main.R;
import com.tuwien.ac.at.main.parser.ServiceParser;
import com.tuwien.ac.at.main.twitter.OAuthLogin;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * @author Raunig Stefan
 * 
 * This android service class, runs in the background and collects tweets, it is designed
 * to wake up the device if it is in sleep mode. It also preparses the every tweet 
 * of Tweetflow syntax.
 *
 */
public class TweetFlowService extends Service{
	private static final String TAG = "SERVICE";
	
	private final int TWEETNUMBER = 80;
	private long lastTweedId = 0;
	private boolean cleanList = false;
	
	private final IBinder binder = new TweetFlowBinder();
	private OAuthLogin session = OAuthLogin.getInstance();
	private Twitter twitter = session.getTwitter();
	private ResponseList<Status> status = null;

	private Editor editor;
	private SharedPreferences settings;
	
	private final String matchPattern = 
			"\\[?" +						//opt. beg. closedseq
			"[SRFPLGTDJC]{2}" +				//quanitfier
			".*" +							//any character
			"(\\s?\\])?";					//opt. end closedseq

	//This binder class provides the callback function
	public class TweetFlowBinder extends Binder{
		public TweetFlowService retrieve(){
			return TweetFlowService.this;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//load tweets
		settings = getSharedPreferences(getString(R.string.preference), Context.MODE_PRIVATE);
		
  	  synchronized(this) {
			  try {
				  //load persistent Token
				  lastTweedId = settings.getLong("TweetId", 0);
				    	
				  Paging page;
				    	
				  if(lastTweedId == 0){
					  page = new Paging(1, TWEETNUMBER);
				  } else {
					  page = new Paging(lastTweedId);
				  }

				  ResponseList<Status> newStatus = twitter.getHomeTimeline(page);

				  //pre check for tweetflow syntax
				  for(Status tweet: newStatus){
					  while(tweet.isRetweet()){
						  //go back to original tweet
						  tweet = tweet.getRetweetedStatus();					  
					  }
					  
					  if(Pattern.matches(matchPattern, tweet.getText())){
						  Log.d(TAG, "Pattern found: "+tweet.getText());
						  new ServiceParser(this).validate(tweet);
					  }
				  }
				  
				  //fill the array, if status is colleced delete old entries
				  if(status == null || cleanList){
					  cleanList = false;
					  status = newStatus;
				  } else {
					  status.addAll(0, newStatus);
				  }
				 
				  if(newStatus.size() > 0){
					Log.d(TAG, "Service tweets: " + status.get(0).getText());

					//persist LastTweedId for next Tweet downloads
				  	lastTweedId = status.get(0).getId();
				  }
				 
				  editor = settings.edit();
				  editor.putLong("TweetId", lastTweedId);
				  editor.commit();
			  } catch (TwitterException e) {
				  Log.d(TAG, "TwitterException in service at loading tweets", e);
			  } catch (NullPointerException ex){
				  Log.d(TAG, "NullPointerException through", ex);
			  }
  	  	}
  	  	return START_STICKY;
	}
		
	@Override
    public IBinder onBind(Intent intent) {
		//Bind to Twitternaut
		return binder;
    }	
	
	public ResponseList<Status> getStatus(){
		//returns the TweetStatus Objects and set flag to clean up
		cleanList = true;
		return status;
	}
}