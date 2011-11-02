package com.tuwien.ac.at.main.utils;

import java.net.MalformedURLException;
import java.net.URL;

import com.rosaloves.bitlyj.Bitly;
import com.rosaloves.bitlyj.Bitly.Provider;
import com.rosaloves.bitlyj.Url;

public class UrlShortener {

	private static final String USER = "twitternaut";
	private static final String API_KEY = "R_fc0583bcf5e2acba3b5aeec5dc387adb";
	
	private Provider bitly = null;

	public UrlShortener(){

		//ger bitly provider with user and api key
		bitly = Bitly.as(USER, API_KEY);
	}
	
	/**
	 * shortens a url
	 * 
	 * @param java.net.URL url
	 * @return Url shortUrl
	 */
	public Url getShortUrl(URL url){
		Url shortUrl = bitly.call(Bitly.shorten(url.toExternalForm()));
		return shortUrl;
	}
	
	/**
	 * 
	 * @param java.net.URL url
	 * @return Url longUrl
	 * @throws MalformedURLException 
	 */
	public URL getLongUrl(URL url) throws MalformedURLException{
		if(url.toExternalForm().contains("bit.ly")){
			Url longUrl = bitly.call(Bitly.expand(url.toExternalForm()));
			return new URL(longUrl.getLongUrl().toString());
		} else {
			return url;
		}
	}
}
