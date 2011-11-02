package com.tuwien.ac.at.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

/**
 * SplashScreen
 * 
 * @author Raunig Stefan
 *
 * Starts the app and displays the app logo, if autologin was set and user is authentificated
 * it redirects to the TabScreen, else it redirects to a Login mask.
 */
public class SplashScreen extends Activity {
	
	private final int SPLASH_DISPLAY_LENGTH = 2500;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                final Intent i = new Intent(SplashScreen.this, getDefaultLogin(SplashScreen.this) ? TabScreen.class : LoginScreen.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
    
    private static boolean getDefaultLogin(final Activity activity){
    	final SharedPreferences settings = activity.getSharedPreferences(activity.getString(R.string.preference), Context.MODE_PRIVATE);
        return settings.getBoolean("AutoLogin", false);
    }
}