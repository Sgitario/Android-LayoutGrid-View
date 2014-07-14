package com.sgitario.android.layoutgridview.utils;

import java.util.ArrayList;
import java.util.List;

import android.widget.BaseAdapter;

import com.sgitario.android.layoutgridview.model.LayoutItem;

public class Utils {
	public static LayoutItem getItemAt(List<LayoutItem> mData, int x, int y) {
        LayoutItem returnValue = null;

		for(LayoutItem item : mData) {
			if(item.frame.contains((int)x, (int)y)) {
                returnValue =  item;
                break;
            }
	      
	    }
		return returnValue;
	}
	
	/**
     * Returns the LayoutItem instance of a view at position if that
     * view is visible or null if thats not currently visible
     * @param	position	The position of the item in the particular section
     * @return	The <code>LayoutItem</code> instance representing that section and index. The proxy is guaranteed to have a view associated with it 
     */
	public static LayoutItem getLayoutItemAt(List<LayoutItem> mData, int position) {		
		LayoutItem found = null;
		int index = 0;
		while (index < mData.size() && found == null) {
			LayoutItem aux = mData.get(index);
			if (aux.itemIndex == position) {
				found = aux;
			}
			
			index++;
		}
		
		return found;
	}
	
	public static LayoutItem getLayoutItemForItem(List<LayoutItem> mData, Object item) {
		LayoutItem found = null;
		int index = 0;
		while (index < mData.size() && found == null) {
			LayoutItem aux = mData.get(index);
			if (aux.data.equals(item)) {
				found = aux;
			}
			
			index++;
		}
		
		return found;
	}

	public static List<Object> getDataInAdapter(BaseAdapter adapter) {
		List<Object> items = new ArrayList<Object>();
		if (adapter != null) {
			for (int index = 0; index < adapter.getCount(); index++) {
				items.add(adapter.getItem(index));
			}
		}
		
		return items;
	}
}
