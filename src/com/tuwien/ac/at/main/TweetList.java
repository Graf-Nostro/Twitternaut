package com.tuwien.ac.at.main;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tuwien.ac.at.main.service.TweetFlowService;
import com.tuwien.ac.at.main.service.TweetFlowService.TweetFlowBinder;
import com.tuwien.ac.at.main.utils.ResponseListBuffer;

import twitter4j.ResponseList;
import twitter4j.Status;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * TweetList
 * 
 * @author Raunig Stefan
 *
 * Represents the home timeline of the user in a List
 */
public class TweetList extends ContextMenuListAcitvity {
	private final String TAG = "TIMELINE";
	
	private TweetFlowService mService = null;
	private List<HashMap<String, Object>> timeline = null;
	
	private boolean mBound = false;
	private boolean flag = true;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		flag = false;
		bindToService();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(flag){
			fillList();
		}
	}
	
	private void bindToService(){
		if(!mBound){
			Intent intent = new Intent(this, TweetFlowService.class);
			mBound = this.getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	boolean flagg = true;
	Drawable drawable = null;
	
	private void fillList(){
		//set a custom listview for the listactivity
        this.setContentView(R.layout.customlistview);

        ResponseListBuffer rsBuffer = new ResponseListBuffer(this, mService);
    	ResponseList<Status> timelineStatus = rsBuffer.loadTimelineTweets();
        
    	timeline = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> map;

    	if(timelineStatus != null){
    		
    	for(final Status status: timelineStatus){
    		map = new HashMap<String, Object>();

			URL url = status.getUser().getProfileImageURL();
			
			map.put("image", url);
			map.put("name", status.getUser().getName());
			map.put("tweet", status.getText());
			map.put("status", status);

			timeline.add(map);
    	}
    	
    	super.timeline = timeline;
    	
    	ListAdapter lA = new ListAdapter(this.getBaseContext(), timeline, R.layout.listviewrow, new String[] {"image", "name", "tweet"}, new int[] {R.id.image, R.id.name, R.id.tweet});
    	
    	this.setListAdapter(lA);
    	
    	//register items for contextmenu
    	this.registerForContextMenu(getListView());
    	
    	} else {
    		Log.d(TAG, "No tweets returned");
    	}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		// Called when the connection with the service is established
		public void onServiceConnected(ComponentName className, IBinder service) {
			//IBinder callback
			TweetFlowBinder binder = (TweetFlowBinder) service;
			mService = binder.retrieve();
			fillList();
			flag = true;
			mBound = true;
		}

		// Called when the connection with the service disconnects unexpectedly
		public void onServiceDisconnected(ComponentName className) {
		    mBound = false;
		    unbindService(mConnection);
		}
	};
}
