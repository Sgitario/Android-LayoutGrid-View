package com.sgitario.android.layoutgridview.layouts.impl;

import java.util.ArrayList;
import java.util.List;

import com.sgitario.android.layoutgridview.layouts.LayoutBuilder;
import com.sgitario.android.layoutgridview.layouts.LayoutResolutorBase;
import com.sgitario.android.layoutgridview.layouts.builders.DefaultLayoutBuilder;

public class StrategyLayoutResolutor extends LayoutResolutorBase {
	private List<LayoutBuilder> layoutBuilders = new ArrayList<LayoutBuilder>();
	private LayoutBuilder defaultLayoutBuilder = new DefaultLayoutBuilder(33.3f, 33.3f);
	
	public void addLayoutBuilder(LayoutBuilder builder) {
		if (builder != null) {
			layoutBuilders.add(builder);
		}
	}
	
	public void setDefaultLayoutBuilder(LayoutBuilder builder) {
		if (builder != null) {
			defaultLayoutBuilder = builder;
		}
	}

	@Override
	protected LayoutBuilder getLayoutBuilder(List<Object> nextItems) {
		LayoutBuilder result = defaultLayoutBuilder;
		
		for (LayoutBuilder layoutBuilder : layoutBuilders) {
			if (layoutBuilder.isFor(nextItems) == true) {
				result = layoutBuilder;
				break;
			}
		}
		
		return result;
	}	
}
