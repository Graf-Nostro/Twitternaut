package com.tuwien.ac.at.main;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 
 * @author Raunig Stefan
 *
 * Custom adapter for tweet representation
 */
public class ListAdapter extends SimpleAdapter {
	
	private final String TAG = "ADAPTER";
	
	private Context context;
	private final Map<String, Bitmap> cache;  

    public ListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		this.cache = new HashMap<String, Bitmap>();
		this.context = context;
	}
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

    	//re use resources to speed up loading
        if (convertView == null) {
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listviewrow, null);
        }

        @SuppressWarnings("unchecked")
		final HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

        ((TextView) convertView.findViewById(R.id.name)).setText((String) data.get("name"));
        ((TextView) convertView.findViewById(R.id.tweet)).setText((String) data.get("tweet"));
        
        final View mView = convertView;
        
        //move to background for dl and compuation
        new AsyncTask<URL, Void, Bitmap>() {
        	@Override
        	protected void onPreExecute() {
        		((ImageView) mView.findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon));
        	}
        	
			@Override
			protected Bitmap doInBackground(URL... urls) {
				try {
					//cache downloaded pic to save resources
					if(cache.containsKey(urls[0].toExternalForm())){
						return cache.get(urls[0].toExternalForm());
					} else {
						Bitmap bitmap = null;									
						Bitmap mBitmapStream = BitmapFactory.decodeStream(new FlushedInputStream((InputStream) urls[0].getContent()));
						if(mBitmapStream == null){
							bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
						} else {
							//rescale bitmap from 48*48 to 36*36
							bitmap = Bitmap.createScaledBitmap(mBitmapStream, 36, 36, true);
						}
						cache.put(urls[0].toExternalForm(), bitmap);
						return bitmap;
					}
				} catch (IOException e) {
					Log.d(TAG, "IOException at creating Drawable", e);
				}
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
			}
			
			@Override
	    	protected void onPostExecute(Bitmap bitmap){ 
				//set Images after computation
				((ImageView) mView.findViewById(R.id.image)).setImageBitmap(bitmap);
			}
		}.execute((URL) data.get("image"));
        return convertView;
    }
}

/** 
 * @author debu...@google.com 
 *
 * This is a bug fix provided by the @author where some pic could not
 * decoded properly by using the InputStream decode methode.
 * Discussion could be found at: http://code.google.com/p/android/issues/detail?id=6066
 *
 */
    class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                      int byteData = read();
                      if (byteData < 0) {
                          break;// we reached EOF
                      } else {
                          bytesSkipped = 1; // we read one byte
                      }
               }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }