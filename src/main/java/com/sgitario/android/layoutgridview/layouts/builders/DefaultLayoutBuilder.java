package com.sgitario.android.layoutgridview.layouts.builders;

import java.util.List;

import com.sgitario.android.layoutgridview.layouts.Layout;
import com.sgitario.android.layoutgridview.layouts.LayoutBuilder;

public class DefaultLayoutBuilder implements LayoutBuilder {
	
	private final float width;
	private final float height;
	
	public DefaultLayoutBuilder(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String getName() {
		return "Default";
	}
	
	@Override
	public boolean isFor(List<Object> feeds) {
		return true;
	}

	@Override
	public Layout getLayoutFor(int position) {
		Layout layout = new Layout();
		layout.setWidthPercentage(this.width);
		layout.setHeightPercentage(this.height);
		return layout;
	}

	@Override
	public int getNumItems() {
		return 1;
	}
}
