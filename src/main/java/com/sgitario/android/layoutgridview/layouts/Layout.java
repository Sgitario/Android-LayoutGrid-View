package com.sgitario.android.layoutgridview.layouts;

public class Layout {
	public static final float MAX = -1;
	
	private float widthPercentage;
	private float heightPercentage;
	
	
	
	public float getWidthPercentage() {
		return widthPercentage;
	}
	
	public void setWidthPercentage(float widthPercentage) {
		this.widthPercentage = widthPercentage;
	}

	public float getHeightPercentage() {
		return heightPercentage;
	}

	public void setHeightPercentage(float heightPercentage) {
		this.heightPercentage = heightPercentage;
	}

	/**
	 * Check whether the current feed applies the conditional layout.
	 * @param feed
	 * @return
	 */
	public boolean isFor(Object feed) {
		return true;
	}	
	
}
