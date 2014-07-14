package com.sgitario.android.layoutgridview.layouts;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.graphics.Rect;

import com.sgitario.android.layoutgridview.layouts.LayoutResolutor;
import com.sgitario.android.layoutgridview.model.LayoutItem;

public abstract class LayoutResolutorBase implements LayoutResolutor {

	private int maxRight = 0;
	private int maxBottom = 0;
	private int width = 0;
	private int height = 0;
	
	/**
	 * Retrieve layout builder strategy.
	 * @param nextItems
	 * @return
	 */
	protected abstract LayoutBuilder getLayoutBuilder(List<Object> nextItems);
	
	@Override
	public List<LayoutItem> prepareLayout(Configuration conf, List<Object> data){
		maxRight = 0;
		maxBottom = 0;
		
		List<LayoutItem> placeHolders = new ArrayList<LayoutItem>();
		
		// Start at the top left corner.
		// ColumnAvailability columnsAvailability = new ColumnAvailability(width, height); // Column X: remains Y top space available.
		List<Rect> availablePlaceHolders = new ArrayList<Rect>();
		
		// Dependending on the number of data, the columns will be calculated.
		LayoutBuilder builder = null;
		int dataIndex = 0;
		for (int index = 0; index < data.size(); index++) {
			// Request for layout
			if (builder == null 
					|| dataIndex >= builder.getNumItems()) {
				
				builder = getLayoutBuilder(data.subList(index, data.size()));
				dataIndex = 0;
			}
			
			// Layout needs
			Layout layout = builder.getLayoutFor(dataIndex);
			int itemWidth = (int) (width * layout.getWidthPercentage() / 100);
			int itemHeight = (int) (height * layout.getHeightPercentage() / 100);
			
			// De we have available placeholders?
			Rect placeholder = null;
			for (Rect availablePlaceHolder : availablePlaceHolders) {
				if (((availablePlaceHolder.right - availablePlaceHolder.left) >= itemWidth) 
						&& ((availablePlaceHolder.bottom - availablePlaceHolder.top) >= itemHeight)) {
							// can be used
					placeholder = availablePlaceHolder;
					break;
				}
			}
			
			if (placeholder != null) {
				availablePlaceHolders.remove(placeholder);
				
				int placeHolderWidth = placeholder.right - placeholder.left;
				int placeHolderHeight = placeholder.bottom - placeholder.top;
				
				// Split to down
				if (placeHolderHeight > itemHeight) {
					int newHeight = placeHolderHeight - itemHeight;
					
					availablePlaceHolders.add(new Rect(placeholder.left, placeholder.top + itemHeight, 
							placeholder.right, placeholder.top + itemHeight + newHeight));
				}
				
				// Split to right
				if (placeHolderWidth > itemWidth) {
					int newWidth = placeHolderWidth - itemWidth;
					
					availablePlaceHolders.add(new Rect(placeholder.left + itemWidth, placeholder.top, 
							placeholder.left + itemWidth + newWidth, placeholder.bottom));
				}
			} else {
				placeholder = new Rect();
				
				if (conf.orientation == Configuration.ORIENTATION_PORTRAIT) {
					// Generate a new row
					placeholder.left = 0;
					placeholder.top = maxBottom;
					placeholder.right = itemWidth;
					placeholder.bottom = maxBottom + itemHeight;
					
					// Create new place holders.
					if (placeholder.right < width) {
						availablePlaceHolders.add(new Rect(placeholder.right, placeholder.top, width, placeholder.bottom));
					}
					
				} else if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					// Generate a new column
					placeholder.left = maxRight;
					placeholder.top = 0;
					placeholder.right = maxRight + itemWidth;
					placeholder.bottom = itemHeight;
					
					// Create new place holders.
					if (placeholder.bottom < height) {
						availablePlaceHolders.add(new Rect(placeholder.left, placeholder.bottom, placeholder.right, height));
					}
				}
			}
			
			// Generate a new column
			LayoutItem p = new LayoutItem();
			p.itemIndex = index;
			p.data = data.get(index);
			
			Rect r = new Rect();
			r.left = placeholder.left;
			r.top = placeholder.top;
			r.right = placeholder.left + itemWidth;
			r.bottom = placeholder.top + itemHeight;
			
			p.frame = r;
			placeHolders.add(p);
			
			dataIndex++;
			
			// Update accumulative width
			maxRight = Math.max(maxRight, r.right);
			maxBottom = Math.max(maxBottom, r.bottom);
		}
		
		return placeHolders;
	}

	@Override
	public int getContentWidth() {
		return Math.max(maxRight, width);
	}

	@Override
	public int getContentHeight() {
		return Math.max(maxBottom, height);
	}

	@Override
	public boolean updateDimensions(int width, int height) {
		boolean hasChanged = this.width != width || this.height != height; 
		
		this.width = width;
		this.height = height;
		
		return hasChanged;
	}
}
