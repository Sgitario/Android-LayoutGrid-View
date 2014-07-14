package com.sgitario.android.layoutgridview.layouts;

import java.util.List;

public interface LayoutBuilder {
	public String getName();
	public boolean isFor(List<Object> feeds);
	public int getNumItems();
	public Layout getLayoutFor(int position);
}
