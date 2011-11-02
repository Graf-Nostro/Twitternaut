package com.tuwien.ac.at.main.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.tuwien.ac.at.main.db.utils.TweetFlowDao;
import com.tuwien.ac.at.main.service.TweetFlowService;
import com.tuwien.ac.at.main.twitter.OAuthLogin;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.util.Log;

/**
 * ResponseListBuffer
 * 
 * @author Raunig Stefan
 *
 * Load Tweets from Twitter and stores the ResponseList (Tweets) in a Buffer, it also saves it to internal storage
 * and loads Resposes if necessary
 */
public class ResponseListBuffer implements Serializable{
	
	private static final long serialVersionUID = -1324782708139904631L;
	
	private final String TAG = "BUFFER";
	private final String FILENAME = "tweetbuffer";
	
	private final OAuthLogin session = OAuthLogin.getInstance();
	private final Twitter twitter = session.getTwitter();
	private TweetFlowDao mtweetFlowDao = null;
    private TweetFlowService mService = null;

    //treshold for maximum size of the list
    private final int MAX = 400;
    private final int MENTIONS_VALUE = 100;
	
	private ResponseList<Status> timelineStatus = null;
	private Context context;
	
	public ResponseListBuffer(Context context, TweetFlowService mService){
		//get timeline and place it into ring buffer
	    this.context = context;
	    this.mService = mService;
	    this.mtweetFlowDao = TweetFlowDao.getInstance(context);
	}
	
	public ResponseList<Status> loadMentionsTweets(){
		Paging page = new Paging(1, MENTIONS_VALUE);
		ResponseList<Status> mentions = null;
		
		try {
			mentions = twitter.getMentions(page);
		} catch (TwitterException e) {
			Log.d(TAG, "Exception at loading Mentionings", e);
		}
		return mentions; 
	}
	
	public ArrayList<Status> loadParsedTweets(){
		ArrayList<Status> status = null;
		try{
			status = mtweetFlowDao.loadAllStatus();
		}catch(TweetFlowException ex){
			Log.d(TAG, "Exception at loading from DB", ex);
		}
		return status;
	}
	
	public ResponseList<Status> loadTimelineTweets(){
		try {	    	
	    	//load persistent list
	    	File file = new File(context.getFilesDir()+"/"+FILENAME);
	    	
	    	if(file.exists()){
	    		Log.d(TAG, "File found at " + context.getFilesDir() + "/" + FILENAME);
	    		timelineStatus = (ResponseList<Status>) restoreList();
	    	}
	    	
	    	//load tweets provided from service
	    	ResponseList<Status> newTweets = mService.getStatus();
	    	
	    	if(timelineStatus != null){
	    		
	    		Log.d(TAG, "Tweets restored: " + timelineStatus.size());
	    		
	    		//clean up if the list gets to large and remove 100 entries
	    		if(timelineStatus.size() >= MAX) {
	    			int t = 100 + timelineStatus.size() - MAX;
	    			for(int i=0 ; i < t ; i++){
	    				timelineStatus.remove(0);
	    			}
	    		}	    	
	    		Log.d(TAG, "Tweets in the BufferList: "+ newTweets.size());
	    		
	    		//merge the lists
	    		timelineStatus.addAll(0, newTweets);
	    	} else {
	    		timelineStatus = newTweets;
	    	}
	    	
	    	//persist list to device
	    	saveList(timelineStatus);
	    		
		} catch (Exception e){
			Log.d(TAG, "Exception at Buffer", e);
		}
		return timelineStatus;
	}
	
	private void saveList(ResponseList<Status> timelineStatus){
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream(); 
		 
	    try { 
	      ObjectOutput out = new ObjectOutputStream(byteArray); 
	      out.writeObject(timelineStatus);
	      out.close(); 
	      
	      FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
	      fos.write(byteArray.toByteArray());
	      fos.close();
	      
	    } catch(IOException e) { 
	      Log.e(TAG, "IO Exception at serialization", e); 
	    }
	}
	
	private ResponseList<Status> restoreList(){
		try {
			File file = new File(context.getFilesDir() + "/" + FILENAME);
			FileInputStream fin = context.openFileInput(FILENAME);
			
			long length = file.length();
		    if (length > Integer.MAX_VALUE) {
		      Log.d(TAG, "File is to big");
		      throw new IOException();
		    }
		    byte[] buf = new byte[(int) length];
		    
			fin.read(buf);
			ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);
			fin.close();
			
		    ObjectInputStream in = new ObjectInputStream(byteArray);
		    	
		    @SuppressWarnings("unchecked")
			ResponseList<Status> object = (ResponseList<Status>) in.readObject(); 
		    
		    in.close(); 
		 
		    return object; 
		} catch(ClassNotFoundException e) { 
		      Log.d(TAG, "Class not found ", e);
		      return null; 
		} catch(IOException e) { 
		      Log.d(TAG, "Io Exception at deserialization of the tweetlist", e);
		      return null; 
		}
	}
}
