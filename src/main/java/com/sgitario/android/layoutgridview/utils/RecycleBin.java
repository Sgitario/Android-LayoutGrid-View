package com.sgitario.android.layoutgridview.utils;

import java.util.ArrayList;
import java.util.List;

import android.view.View;

/**
 * TODO: Need to improve this class to use soft references.
 * @author jhilario
 *
 */
public class RecycleBin {

	private List<View> recycleViews = new ArrayList<View>();

	public RecycleBin() {
	}

	public void recycle(View view) {
		recycleViews.add(view);
	}

	public View get() {
		View view = null;
		if (recycleViews.size() > 0) {
			view = recycleViews.remove(0);
		}
		
		return view;
	}

}
