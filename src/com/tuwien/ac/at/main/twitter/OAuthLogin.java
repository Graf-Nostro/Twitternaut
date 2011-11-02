package com.tuwien.ac.at.main.twitter;

import com.tuwien.ac.at.main.service.TweetFlowService;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

/**
 * OAuthLogin
 * 
 * @author Raunig Stefan
 *
 * @singelton instance
 * 
 * Basic class for login over oauth callback methode, and twitter functionality 
 */
public class OAuthLogin {
	
	public static OAuthLogin instance;
	
	private Twitter twitter;
	private RequestToken requestToken;
	private AccessToken accessToken;

	private final String CALL_BACK_URL = "oauth://login";
	private final String CONSUMER_KEY = "Xi3CFd0c9ByhaJXlTv0ew";
	private final String CONSUMER_PASSWORD = "aHd5A3B9oC0o9011NU9iZvJBUbHYDCI30DTKAjUwDI";
	
	private String accessTokenKey;
	private String accessTokenSecret;

	private OAuthLogin() {};
	
	public static OAuthLogin getInstance(){
		if(instance == null){
			instance = new OAuthLogin();
		}
			return instance;
	}

	public String buildRequestToken() throws TwitterException {
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_PASSWORD);
		requestToken = twitter.getOAuthRequestToken(CALL_BACK_URL);
		String authUrl = requestToken.getAuthorizationURL();
		return authUrl;
	}

	public void loginIntent(Uri uri) throws TwitterException {
		String verifier = uri.getQueryParameter("oauth_verifier");
		
		if(verifier == null || getTwitter() == null) throw new TwitterException("Requesting permition form twitter failed!");
		
		accessToken = getTwitter().getOAuthAccessToken(getRequestToken(), verifier);
		accessTokenKey = accessToken.getToken();
		accessTokenSecret = accessToken.getTokenSecret();
		twitter.setOAuthAccessToken(accessToken);
	}

	public void loginAuto(String accessTokenKey, String accessTokenSecret) {
		twitter = new TwitterFactory().getInstance();
		accessToken = new AccessToken(accessTokenKey, accessTokenSecret);
		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_PASSWORD);
		twitter.setOAuthAccessToken(accessToken);
	}

	public void sendTweet(String message) throws TwitterException {
		if(twitter!=null && !message.equals("")) twitter.updateStatus(message);
	}
	
	public void retweet(long statusId) throws TwitterException {
		if(twitter!=null) twitter.retweetStatus(statusId);
	}

	public Twitter getTwitter() {
		return twitter;
	}

	public void setTwitter(Twitter twitter) {
		this.twitter = twitter;
	}

	public RequestToken getRequestToken() {
		return requestToken;
	}

	public AccessToken getAccessToken() {
		return accessToken;
	}

	public String getAccessTokenKey() {
		return accessTokenKey;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void logout(Context context) {
		//quit service
		Intent intent = new Intent(context, TweetFlowService.class);
		intent.setClassName(context, "com.tuwien.ac.at.main.servie.TweetFlowService");
		context.stopService(intent);
		
		requestToken = null;
		accessToken = null;

		accessTokenKey = null;
		accessTokenSecret = null;

		twitter = null;

		//delete buffered tweets
		context.deleteFile("tweetbuffer");
		
		//delete entries in the preference
		final SharedPreferences setting = context.getSharedPreferences("preference", Context.MODE_PRIVATE);
		final Editor editor = setting.edit();
	
		editor.remove("AccessTokenKey");
		editor.remove("AccessTokenSecret");
		editor.remove("RequestTokenKey");
		editor.remove("RequestTokenSecret");
		editor.remove("TweetId");
		editor.putBoolean("AutoLogin", false);
		editor.commit();
	}
}
