package com.tuwien.ac.at.main.db.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tuwien.ac.at.main.utils.TweetFlowException;

import twitter4j.Status;

/**
 * TweetFlowDao
 * 
 * @author Raunig Stefan
 *
 * @singelton
 * 
 * A parsed tweet will be stored and persisted in a DB, this Data Access Object performs
 * that task.
 */
public class TweetFlowDao extends SQLiteOpenHelper{
	
	private final String TAG = "DAO";
	private static TweetFlowDao instance = null;
	
	//sqlite db identifier
	public static final String ID = "id";
	public static final String IDENTIFIER = "identifier";
	public static final String OWNER = "owner";
	public static final String DATE = "date";
	public static final String OPERATION = "operation";
	public static final String CLOSED_SEQUENCE = "closedsequence";
	public static final String VARIABLES = "variables";
	public static final String HASHTAGS = "hashtags";
	public static final String MENTIONS = "mentions";
	public static final String URL = "url";
	public static final String STATUS = "status";
	
	private static final int DATABASE_VERSION = 2;
    private static final String TABLE = "tweetflow";
    private static final String CREATE_DB =
                "CREATE TABLE " + TABLE + " (" +
                ID + " INTEGER PRIMARY KEY, " +
                IDENTIFIER + " TEXT, " +
                OWNER + " TEXT, " +
                DATE + " TEXT, " +
                OPERATION + " TEXT, " +
                URL + " BLOB, " +
                MENTIONS + " BLOB, " +
                HASHTAGS + " BLOB, " +
                VARIABLES + " BLOB, " +
                STATUS + " BLOB, " +
                CLOSED_SEQUENCE + " INTEGER);";

	public static TweetFlowDao getInstance(Context context){
		if(instance == null){
			instance = new TweetFlowDao(context);
		}
		return instance;
	}
	
	private TweetFlowDao(Context context){
		super(context, TABLE, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_DB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//if DB structure changes
		if(DATABASE_VERSION != newVersion){
			//save entries if structure changes
		}
	}
	
	public void insert(TweetFlowBean bean) throws SQLiteException{
		// Opens the database object in "write" mode.
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        
        //content map for query
        if (values.containsKey(IDENTIFIER) == false) {
            values.put(IDENTIFIER, bean.getIdentifier());
        }

        if (values.containsKey(OWNER) == false) {
            values.put(OWNER, bean.getOwner());
        }
        
        //sql boolean value: true 1, false 0
        if (values.containsKey(CLOSED_SEQUENCE) == false) {
            values.put(CLOSED_SEQUENCE, (bean.isClosedSequence() ? 1 : 0));
        }
        
        if (values.containsKey(DATE) == false) {
            values.put(DATE, bean.getParseDate());
        }
        
        if (values.containsKey(OPERATION) == false) {
            values.put(OPERATION, bean.getServiceInformation());
        }
        
        if (values.containsKey(STATUS) == false) {
            values.put(STATUS, serialize(bean.getStatus()));
        }
        
        if (values.containsKey(VARIABLES) == false) {
            values.put(VARIABLES, serialize(bean.getVariables()));
        }
       
        if (values.containsKey(HASHTAGS) == false) {
            values.put(HASHTAGS, serialize(bean.getHashTags()));
        }
        
        if (values.containsKey(MENTIONS) == false) {
            values.put(MENTIONS, serialize(bean.getMentions()));
        }
        
        if (values.containsKey(URL) == false) {
            values.put(URL, serialize(bean.getUrl()));
        }
        db.insertOrThrow(TABLE, null, values);
        db.close();
        
        Log.d(TAG, "Data: "+ bean.getIdentifier()+ " " +bean.getOwner()+ " " +bean.isClosedSequence()+ " " +bean.getParseDate()+" "+bean.getServiceInformation());
        try {
			loadAllStatus();
		} catch (TweetFlowException e) {
			Log.d(TAG, "Fail", e);
		}
	}
		
	private byte[] serialize(Object object) {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream(); 
		 
	    try { 
	      ObjectOutput out = new ObjectOutputStream(byteArray); 
	      out.writeObject(object);
	      out.close(); 

	      return byteArray.toByteArray();
	      
	    } catch(IOException e) { 
	      Log.e(TAG, "IO Exception at serialization", e); 
	      return null;
	    }
	}
	
	private Status deserialize(byte[] data) {
		try {
			ByteArrayInputStream byteArray = new ByteArrayInputStream(data);
		    ObjectInputStream in = new ObjectInputStream(byteArray);
		    
		    Status object = (Status) in.readObject(); 
		    in.close(); 
		 
		    return object; 
		} catch(ClassNotFoundException e) { 
		      Log.d(TAG, "Class not found ", e);
		      return null; 
		} catch(IOException e) { 
		      Log.d(TAG, "Io Exception at deserialization", e);
		      return null; 
		}
	}

	public void clearDB(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
	}
	
	public ArrayList<Status> loadAllStatus() throws TweetFlowException{
		ArrayList<Status> status = new ArrayList<Status>();
	
		SQLiteDatabase db = this.getReadableDatabase();
        
        String[] colums = {STATUS};
        
        Cursor c = db.query(TABLE, colums, null, null, null, null, null);
               
        while(c.moveToNext()){
        	status.add(deserialize(c.getBlob(0)));
        }
        c.close();
        db.close();
		
		return status;
	}

	public ArrayList<Status> loadFromOwner(String name){
		ArrayList<Status> status = new ArrayList<Status>();
		
		SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = OWNER + "=" + name + ";";
        
        Cursor c = db.query(TABLE, null, selection, null, null, null, null);
               
        while(c.moveToNext()){
        	status.add(deserialize(c.getBlob(0)));
        }
        c.close();
        db.close();
		
		return status;
	}
	
	public ArrayList<Status> loadFromMentioned(String mention){
		ArrayList<Status> status = new ArrayList<Status>();
		
		SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = MENTIONS + "=" + mention + ";";
        
        Cursor c = db.query(TABLE, null, selection, null, null, null, null);
               
        while(c.moveToNext()){
        	status.add(deserialize(c.getBlob(0)));
        }
        c.close();
        db.close();
		
		return status;
	}
	
	public ArrayList<Status> loadBySQL(String sql){
		ArrayList<Status> status = new ArrayList<Status>();
		
		SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TABLE, null, sql, null, null, null, null);
               
        while(c.moveToNext()){
        	status.add(deserialize(c.getBlob(0)));
        }
        c.close();
        db.close();
		
		return status;
	}
}
