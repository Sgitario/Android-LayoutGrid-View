package com.sgitario.android.layoutgridview.events;

import com.sgitario.android.layoutgridview.AbsLayoutContainer;
import com.sgitario.android.layoutgridview.model.LayoutItem;

/**
 * Interface definition for a callback to be invoked when an item in this
 * view has been selected.
 */
public interface OnItemSelectedListener {
	/**
	 * <p>
	 * Callback method to be invoked when an item in this view has been
	 * selected. This callback is invoked only when the newly selected
	 * position is different from the previously selected position or if
	 * there was no selected item.
	 * </p>
	 * 
	 * Impelmenters can call getItemAtPosition(position) if they need to
	 * access the data associated with the selected item.
	 * 
	 * @param parent
	 *            The AdapterView where the selection happened
	 * @param proxy
	 *            The FreeFlowItem instance representing the item selected
	 * @param id
	 *            The row id of the item that is selected
	 */
	void onItemSelected(AbsLayoutContainer parent, LayoutItem proxy);

	/**
	 * Callback method to be invoked when the selection disappears from this
	 * view. The selection can disappear for instance when touch is
	 * activated or when the adapter becomes empty.
	 * 
	 * @param parent
	 *            The AdapterView that now contains no selected item.
	 */
	void onNothingSelected(AbsLayoutContainer parent);
}
