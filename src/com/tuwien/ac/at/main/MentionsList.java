package com.tuwien.ac.at.main;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tuwien.ac.at.main.utils.ResponseListBuffer;

import twitter4j.ResponseList;
import twitter4j.Status;
import android.os.Bundle;

public class MentionsList extends ContextMenuListAcitvity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //set a custom listview for the listactivity
        this.setContentView(R.layout.customlistview);
        ResponseListBuffer rsBuffer = new ResponseListBuffer(MentionsList.this, null);
        
    	ResponseList<Status> mentionsStatus = rsBuffer.loadMentionsTweets();
    	List<HashMap<String, Object>> mentions = new ArrayList<HashMap<String, Object>>();
    	HashMap<String, Object> map;
    	   	
    	for(Status status: mentionsStatus){
    		map = new HashMap<String, Object>();
    		
    		URL url = status.getUser().getProfileImageURL();

    		map.put("image", url);
    		map.put("name", status.getUser().getName());
    		map.put("tweet", status.getText());
    		map.put("status", status);

    		mentions.add(map);
    	}
    	
    	super.timeline = mentions;
    	
    	ListAdapter lA = new ListAdapter(this.getBaseContext(), mentions, R.layout.listviewrow,
    			new String[] {"image", "name", "tweet"}, new int[] {R.id.image, R.id.name, R.id.tweet});

    	this.setListAdapter(lA);
    	
    	//register items for contextmenu
    	this.registerForContextMenu(getListView());
	}
}
