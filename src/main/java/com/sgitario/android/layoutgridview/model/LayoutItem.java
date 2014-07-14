package com.sgitario.android.layoutgridview.model;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

public class LayoutItem {
	public int itemIndex;
	public Object data;
	public Rect frame;
	public View view;
	public Bundle extras;

	public static LayoutItem clone(LayoutItem desc) {
		if (desc == null)
			return null;

		LayoutItem fd = new LayoutItem();
		fd.itemIndex = desc.itemIndex;
		fd.data = desc.data;
		fd.frame = new Rect(desc.frame);
		fd.view = desc.view;
		fd.extras = desc.extras;
		return fd;
	}
}
