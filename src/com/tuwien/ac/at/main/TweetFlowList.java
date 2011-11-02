package com.tuwien.ac.at.main;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tuwien.ac.at.main.parser.ServiceParser;
import com.tuwien.ac.at.main.utils.ResponseListBuffer;

import twitter4j.Status;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

public class TweetFlowList extends ContextMenuListAcitvity {

//	private final String TAG = "TWEETFLOW";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //set a custom listview for the listactivity
        this.setContentView(R.layout.customlistview);
        ResponseListBuffer rsBuffer = new ResponseListBuffer(TweetFlowList.this, null);
        
    	ArrayList<Status> tweetStatus = rsBuffer.loadParsedTweets();
    	List<HashMap<String, Object>> tweets = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> map;
    	   	
    	for(Status status: tweetStatus){
    		map = new HashMap<String, Object>();
    		
    		URL url = status.getUser().getProfileImageURL();

    		map.put("image", url);
    		map.put("name", status.getUser().getName());
    		map.put("tweet", status.getText());
    		map.put("status", status);
    		
    		tweets.add(map);
    	}
    	
    	super.timeline = tweets;
    	
    	ListAdapter lA = new ListAdapter(this.getBaseContext(), tweets, R.layout.listviewrow,
                new String[] {"image", "name", "tweet"}, new int[] {R.id.image, R.id.name, R.id.tweet});
    	
    	this.setListAdapter(lA);
    	
    	//register items for contextmenu
    	this.registerForContextMenu(getListView());
    	
    	//cancel notification after visiting
    	NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	nm.cancel(ServiceParser.NOTIFICATION_ID);
	  	}
}

