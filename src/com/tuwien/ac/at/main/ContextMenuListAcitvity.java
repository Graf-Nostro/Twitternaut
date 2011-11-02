package com.tuwien.ac.at.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import twitter4j.Status;
import twitter4j.URLEntity;

import com.tuwien.ac.at.main.utils.UrlPreview;
import com.tuwien.ac.at.main.utils.UrlShortener;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * ActivityList
 * 
 * @author Raunig Stefan
 *
 *
 * An abstract class to inherent the optionmenu for it's subclasses
 */
public abstract class ContextMenuListAcitvity extends ListActivity{
	
	protected List<HashMap<String, Object>> timeline = null;
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	  
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	  
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	  
		//get standard menu from resource
		menu.setHeaderTitle("Options");
		menu.setHeaderIcon(R.drawable.icon);
	   
		final Status status = (Status) timeline.get(info.position).get("status");
		final URLEntity[] urls = status.getURLEntities();
		  
		if((urls != null) && (urls.length >= 1)){
			//enable url if in the tweet present
			MenuItem urlItem = (MenuItem) menu.findItem(R.id.url);
			urlItem.setVisible(true);
			urlItem.setTitle(urls[0].getExpandedURL().toExternalForm());

			//check for preview
			try {
				final UrlShortener shortener = new UrlShortener();
				final UrlPreview pv = new UrlPreview(shortener.getLongUrl(urls[0].getExpandedURL()));
				final URL result = pv.isPreview();

				if(result != null){		  
					MenuItem previewItem = (MenuItem) menu.findItem(R.id.preview);
					Intent intent = new Intent(this, TweetList.class);
					intent.putExtra("url", result);
					previewItem.setIntent(intent);
					previewItem.setVisible(true);
				}
			} catch (MalformedURLException e) {
				MenuItem previewItem = (MenuItem) menu.findItem(R.id.preview);
				previewItem.setVisible(false);
			}
		}
	}
		
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		//retriev context about selected item and the value associated with the item
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final Status status = (Status) timeline.get(info.position).get("status");
		final URLEntity[] urls = status.getURLEntities();
		final UrlShortener shortener = new UrlShortener();
		URL shUrl = null;
	  
		if(urls.length > 0 ){
			try {
				shUrl = shortener.getLongUrl(urls[0].getExpandedURL());
			} catch (MalformedURLException e1) {
				shUrl = urls[0].getExpandedURL();
			}
		}
		final Intent intent = new Intent(this, TabScreen.class);
	  
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog alert;
	  
		switch (item.getItemId()) {
		case R.id.answer:
			intent.putExtra("status", status);
			item.setIntent(intent);
		  
			getParent().onOptionsItemSelected(item);		  
			return true;
		case R.id.retweet:
		  	builder.setMessage("Are you sure you want to retweet this message?")
		  			.setCancelable(false)
		  			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		  				public void onClick(DialogInterface dialog, int id) {
		  					intent.putExtra("status", status);
		  					item.setIntent(intent);
		    		  
		  					getParent().onOptionsItemSelected(item);	
		  				}
		  			})
		  			.setNegativeButton("No", new DialogInterface.OnClickListener() {
		  				public void onClick(DialogInterface dialog, int id) {
		  					dialog.cancel();
		  				}
		  			});
		  	alert = builder.create();
		  	alert.show();
		  	return true;
		case R.id.url:
		  	if(urls != null){
		  		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(shUrl.toExternalForm())));
		  	}
		  	return true;
	  case R.id.preview:
		  	//get loaded data
		  	Intent urlIntent = item.getIntent();
		  	URL url = (URL) urlIntent.getSerializableExtra("url");
		  	if(url != null){
		  		//display url
		  		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		  		View layout = inflater.inflate(R.layout.imagedialog, (ViewGroup) findViewById(R.id.customdialog));

		  		ImageView image = (ImageView) layout.findViewById(R.id.previewimage);
			  
		  		//dl and set bitmap
		  		try {
		  			Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream((InputStream) url.getContent()));
		  			image.setImageBitmap(bitmap);
		  		} catch(IOException e){
		  			image.setImageResource(R.drawable.icon);
		  		}

		  		builder.setView(layout).setTitle("Your preview from: "+url.toExternalForm())
		  				.setNeutralButton("Close", new DialogInterface.OnClickListener() {
		  					public void onClick(DialogInterface dialog, int id) {
		  						dialog.cancel();
		  					}
		  				});
		  		alert = builder.create();
		  		alert.show();
		  	}
		  	return true;
	  default:
		  return super.onContextItemSelected(item);
		}
	}
}