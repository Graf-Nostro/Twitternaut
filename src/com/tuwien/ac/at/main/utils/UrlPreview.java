package com.tuwien.ac.at.main.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlPreview {
	
	private URL url = null;
		
	public UrlPreview(URL url){
		this.url = url;
	}

	public URL isPreview() throws MalformedURLException{
		URL val;

		if((val = isYoutubeVideo()) != null){
			return val;
		} else if((val = isPicture()) != null){
			return val;
		}
		return null;
	}
	
	/**
	 * 
	 * @return the url if it is link to a pic, or null
	 * @throws MalformedURLException
	 */
	private URL isPicture() throws MalformedURLException{
		String extention = url.getFile();		
		for(Type t: Type.values()){
			if(extention.contains(t.extention())){
				return url;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return an url to the youtube preview service with, or null
	 * @throws MalformedURLException
	 */
	private URL isYoutubeVideo() throws MalformedURLException{
		if(url.toExternalForm().contains("youtu")){
			String strFile = url.getFile().replace("/watch?v=", "").replaceAll("&.*", "").replaceAll("\\/", "").replaceAll("\\?.*", "");
			return new URL("http://img.youtube.com/vi/"+strFile+"/2.jpg");
		}
		return null;
	}

	protected enum Type{
		JPG		(".jpg"),
		JPG_U 	(".JPG"),
		JPEG 	(".jpeg"),
		JPEG_U 	(".JPEG"),
		GIF 	(".gif"),
		GIF_U 	(".GIF"),
		PNG 	(".png"),
		PNG_U 	(".PNG"),
		TIFF	(".tiff"),
		TIFF_U	(".TIFF"),
		RAW		(".raw"),
		RAW_U	(".RAW"),
		BMP		(".bmp"),
		BMP_U	(".BMP");
			
		private final CharSequence extention;
		private Type(String extention){
			this.extention = extention;
		}
		protected CharSequence extention(){return extention;}
	}
}
