package com.tuwien.ac.at.main.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.tuwien.ac.at.main.R;
import com.tuwien.ac.at.main.TabScreen;
import com.tuwien.ac.at.main.db.utils.TweetFlowBean;
import com.tuwien.ac.at.main.db.utils.TweetFlowDao;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import twitter4j.Status;

/**
 * 
 * @author Raunig Stefan
 *
 * Parse and save a tweet which was preparsed and could be a TweetFlow message. Also notifies 
 * the user if parsing was successfull.
 */
public class ServiceParser{
	
	private final String TAG = "SERVICEPARSER";
	
	private final int REQUEST_CODE = 0;
	public static final int NOTIFICATION_ID = 0;
	
	private TweetFlowBean bean;
	private TweetFlowDao instance = null;
	private final Context context;
	
	private ArrayList<String[]> tokensArray = new ArrayList<String[]>();
	
	/*
	 * \w = [a-zA-Z_0-9]
	 * \S = no whitespace character
	 * 
	 */
	private final String matchPattern = 
			"(\\[(\\s?))?" +					// closed sequence
			"[SRFPLGTDJC]{2}" +					// qualifier
			"( @\\w+(\\.\\w+(\\.\\w+)?\\?)?)?"+ // mention, and implicit variable
			" \\w+\\.\\w+" +					// operation.service
			"( >> \\w+\\.\\w+)?" +				// optional service mapping
			"(\\?\\w+=\\S+(&\\w+=\\S+)*)?" +	// optional payload
			"( http://(www\\.)?\\S+)*" +		// optional url (beware of url shorting)
			"( \\w+)?" +						// optinal expressions
			"( #\\w+)*" + 						// optional hashtags
			"(\\s?\\|)?\\s?.*" + 				// optional pipe
			"(\\s?\\])?";						// optional end of closed sequence
											

	public ServiceParser(Context context){
		this.context = context;
		this.instance = TweetFlowDao.getInstance(context);
	}
	
	public void validate(Status tweets){
		try{
			boolean cseq = false;
				
			if(tweets.getText().startsWith("[")){
				//closed sequence found
				String[] split = tweets.getText().replaceAll("\\[", "").replaceAll("\\]", "").split("\\|");

				for(String msg: split){
					tokensArray.add(msg.split(" "));
				}
				cseq = true;
			} else {
				tokensArray.add(tweets.getText().split(" "));
			}

			if(Pattern.matches(matchPattern, tweets.getText())){
				for(String[] tokens: tokensArray){
					//fill bean
					bean = new TweetFlowBean(tweets, tokens[0], tweets.getUser().getName(), tweets.getCreatedAt().toLocaleString());
					bean.setUrl(tweets.getURLEntities());
				
					bean.setClosedSequence(cseq);
					
					final ArrayList<String> mMention = new ArrayList<String>();
					final ArrayList<String> mTag = new ArrayList<String>();
				
					for(String t: tokens){
						Log.d(TAG, "Parsed currently: "+ t);
						if(t.contains("@")){
							//get the mentions in the tweet
							if(!t.equals(tweets.getUser().getName())){
								mMention.add(t);
							}
						} else if(t.contains("#")){
							//get the hashtags in the tweet
							mTag.add(t);
						} else if(t.contains(".")){
							//get variables and operation
							if(t.contains("?")){
								HashMap<String, String> varMap = new HashMap<String, String>();
								String[] sequence = t.split("\\?");
								bean.setServiceInformation(sequence[0]);

								if(sequence[1].contains("&")){
									String[] variable = sequence[1].split("\\&"); 
									for(int i = 0; i < variable.length; i++){
										String[] var = variable[i].split("\\=");
										varMap.put(var[0], var[1]);
									}
									bean.setVariables(varMap);
								} else if(sequence[1].contains("=")){
									String[] var = sequence[1].split("\\=");
									varMap.put(var[0], var[1]);
								}
							} else {
								if (t.contains("http")){
									//skip urls we have saved them already
									continue;
								}
							bean.setServiceInformation(t);
							}
						}
					}
				
					bean.setMentions(mMention.toArray(new String[mMention.size()]));
					bean.setHashTags(mTag.toArray(new String[mTag.size()]));
				
					notification("You have received a new: "+ bean.getIdentifier() +" request!");
				
					Log.d(TAG, bean.toString());
				
					instance.insert(bean);
				}
			} else {
				Log.d(TAG, "Failure at parsing TweetFlow: " + tweets.getText());
				//Toast.makeText(context, "Syntax error in a TweetFlow , please check timeline!", Toast.LENGTH_LONG).show();
			}
		} catch (NullPointerException ex){
			Log.d(TAG, "Nullpointer tried to parse ?", ex);
		} catch (ArrayIndexOutOfBoundsException ex){
			Log.d(TAG, "No Variables left to parse");
		} catch (SQLiteException e) {
			Log.d(TAG, "SQL Exception at insert detected", e);
		}	
	}
		
	private void notification(String msg){
		String tickerText = context.getString(R.string.ticker);
		
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon, tickerText, System.currentTimeMillis());
		
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		//set to default vibration
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		//set the tab number
		Intent intent = new Intent(context, TabScreen.class);
		intent.putExtra("currTab", 2);
		
		// The PendingIntent will launch activity if the user selects this notification
		PendingIntent pIntent = PendingIntent.getActivity(context, REQUEST_CODE, intent, 0);

		notification.setLatestEventInfo(context, "TweetFlow", msg, pIntent);
		manager.notify(NOTIFICATION_ID, notification);
	}
	
	public boolean testInterface(String msg){	
		return msg.matches(matchPattern);
	}
}
