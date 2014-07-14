package com.example.layoutgridviewsample.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.layoutgridviewsample.R;
import com.example.layoutgridviewsample.model.Feed;

/**
 * Data adapter to update the feeds content and the fragment views.
 * 
 * @author jhilario
 */
public class MyDataAdapter extends ArrayAdapter<Feed> {
	
	public static final  String TAG = MyDataAdapter.class.toString();
	
	public static class ViewHolder {
		public ImageView image;
	}
	
	private final Activity context;

	/**
	 * Initializes a new instance of the DribbbleDataAdapter class.
	 * @param context
	 */
	public MyDataAdapter(Activity context) {
		super(context, R.layout.template_grid_item);
		this.context = context;
	}

	/**
	 * get item identifier.
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Resolve the view for the current item view.
	 */
	@Override
	public View getView(int position, View convertView,
			ViewGroup parent) {
		
		ViewHolder holder = null;
		
		// If null, then the holder is new.
		if (convertView == null) {
			// inflate the layout
			convertView = LayoutInflater.from(context).inflate(
					R.layout.template_grid_item, null, false);
			
			// well set up the ViewHolder
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			
			// store the holder with the view.
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Retrieve feed to show.
		final Feed s = this.getItem(position);
		
		// Initialize fragment if it hasn't been loaded.
		holder.image.setBackground(this.context.getResources().getDrawable(s.getImageResId()));

		return convertView;
	}

}
