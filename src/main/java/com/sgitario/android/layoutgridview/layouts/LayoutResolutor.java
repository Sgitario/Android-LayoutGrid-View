package com.sgitario.android.layoutgridview.layouts;

import java.util.List;

import com.sgitario.android.layoutgridview.model.LayoutItem;

import android.content.res.Configuration;

/**
 * The base class for all custom layouts. The Layout is responsible for figuring
 * out all the positions for all the views created by the Container based on the
 * <code>SectionedAdapter</code> supplied to it.
 * 
 */
public interface LayoutResolutor {

	/**
	 * Called whenever Container's onMeasure is triggered Note: We don't support
	 * margin and padding yet, so the dimensions are the entire actual of the
	 * Container. Note that setDimensions can be called multiple times, so don't
	 * use it to recompute your frames, use computeLayout instead
	 * 
	 * @param measuredWidth
	 *            The width of the Container
	 * @param measuredHeight
	 *            The height of the Container
	 */
	public boolean updateDimensions(int width, int height);

	public List<LayoutItem> prepareLayout(Configuration conf, List<Object> items);

	public int getContentWidth();

	public int getContentHeight();

}
