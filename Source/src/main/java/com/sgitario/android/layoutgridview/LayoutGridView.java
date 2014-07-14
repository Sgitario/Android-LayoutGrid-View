package com.sgitario.android.layoutgridview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.Checkable;

import com.sgitario.android.layoutgridview.layouts.LayoutResolutor;
import com.sgitario.android.layoutgridview.model.LayoutItem;
import com.sgitario.android.layoutgridview.utils.RecycleBin;
import com.sgitario.android.layoutgridview.utils.Utils;

public class LayoutGridView extends AbsLayoutContainer {

	// ViewPool class
	protected RecycleBin viewpool;

	// Not used yet, but we'll probably need to
	// prevent layout in <code>layout()</code> method
	private boolean preventLayout = false;

	protected BaseAdapter mAdapter;
	protected LayoutResolutor mLayout;
	
	protected List<LayoutItem> mData;

	/**
	 * Holds the checked items when the Container is in CHOICE_MODE_MULTIPLE
	 */
	protected SparseBooleanArray mCheckStates = null;

	ActionMode mChoiceActionMode;

	/**
	 * Normal list that does not indicate choices
	 */
	public static final int CHOICE_MODE_NONE = 0;

	/**
	 * The list allows up to one choice
	 */
	public static final int CHOICE_MODE_SINGLE = 1;

	/**
	 * The value of the current ChoiceMode
	 * 
	 * @see <a href=
	 *      "http://developer.android.com/reference/android/widget/AbsListView.html#attr_android:choiceMode"
	 *      >List View's Choice Mode</a>
	 */
	int mChoiceMode = CHOICE_MODE_NONE;

	private LayoutParams params = new LayoutParams(0, 0);

	private boolean dirtyData = false;
	private List<LayoutItem> mDataDisplayed = new ArrayList<LayoutItem>();

	public LayoutGridView(Context context) {
		super(context);
	}

	public LayoutGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LayoutGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context) {
		super.init(context);
		
		viewpool = new RecycleBin();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {		
		if (mLayout != null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			
			if (this.mLayout != null) {
				int afterWidth = MeasureSpec.getSize(widthMeasureSpec);
				int afterHeight = MeasureSpec.getSize(heightMeasureSpec);				
				
				if (mLayout.updateDimensions(afterWidth, afterHeight) || dirtyData) {
					setViewPortX(0);
					setViewPortY(0);
					dirtyData = false;
					clearFrames();
					mData = mLayout.prepareLayout(getResources().getConfiguration(), Utils.getDataInAdapter(getAdapter()));
				}
				
				persistViewChanges();
			}		
			
		} else {
			logLifecycleEvent("Nothing to do: returning");
		}
	}
	
	public void dataInvalidated() {
		logLifecycleEvent("Data Invalidated");
		if (mLayout == null) {
			return;
		}
		
		dirtyData = true;
		requestLayout();
	}
	
	public LayoutItem getItemAt(float x, float y) {
		return (LayoutItem) getItemAt((int) x, (int) y);
	}

	/**
	 * Adds a view based on the current viewport. If we can get a view from the
	 * ViewPool, we dont need to construct a new instance, else we will based on
	 * the View class returned by the <code>Adapter</code>
	 * 
	 * @param layoutItem
	 *            <code>LayoutItem</code> instance that determines the View
	 *            being positioned
	 */
	protected void addAndMeasureViewIfNeeded(LayoutItem layoutItem) {
		View view;
		if (layoutItem.view == null) {

			view = getAdapter().getView(layoutItem.itemIndex, viewpool.get(), this);

			if (view instanceof LayoutGridView)
				throw new IllegalStateException(
						"A container cannot be a direct child view to a container");

			layoutItem.view = view;
			prepareViewForAddition(view, layoutItem);
			addView(view, getChildCount(), params);
		}

		view = layoutItem.view;

		int widthSpec = MeasureSpec.makeMeasureSpec(layoutItem.frame.width(),
				MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(
				layoutItem.frame.height(), MeasureSpec.EXACTLY);
		view.measure(widthSpec, heightSpec);
	}

	/**
	 * Does all the necessary work right before a view is about to be laid out.
	 * 
	 * @param view
	 *            The View that will be added to the Container
	 * @param layoutItem
	 *            The <code>LayoutItem</code> instance that represents the
	 *            view that will be positioned
	 */
	protected void prepareViewForAddition(View view, LayoutItem layoutItem) {
		if (view instanceof Checkable) {
			((Checkable) view).setChecked(isChecked(layoutItem.itemIndex));
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		

	}

	protected void doLayout(LayoutItem layoutItem) {
		View view = layoutItem.view;
		Rect frame = layoutItem.frame;
		int l = frame.left - viewPortX;
		int t = frame.top - viewPortY;
		int r = frame.right - viewPortX;
		int b = frame.bottom - viewPortY;
		
		view.layout(l, t, r, b);
	}

	/**
	 * Sets the layout on the Container. If a previous layout was already
	 * applied, this causes the views to animate to the new layout positions.
	 * Scroll positions will also be reset.
	 * 
	 * @param newLayout
	 */
	public void setLayout(LayoutResolutor newLayout) {
		if (newLayout == mLayout || newLayout == null) {
			return;
		}
		
		stopScrolling();
		mLayout = newLayout;

		setViewPortX(0);
		setViewPortY(0);

		logLifecycleEvent("Setting layout");
		requestLayout();

	}

	/**
	 * @return The layout currently applied to the Container
	 */
	@Override
	public LayoutResolutor getLayout() {
		return mLayout;
	}
	
	@Override
	protected List<LayoutItem> getLayoutItems() {
		return mData;
	}
	
	@Override
	public BaseAdapter getAdapter() {
		return this.mAdapter;
	}
	
	public void setAdapter(BaseAdapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(dataObserver);
		}
		
		this.mAdapter = adapter;
		this.mAdapter.registerDataSetObserver(dataObserver);
	}

	/**
	 * Returns the actual frame for a view as its on stage. The LayoutItem's
	 * frame object always represents the position it wants to be in but actual
	 * frame may be different based on animation etc.
	 * 
	 * @param freeflowItem
	 *            The freeflowItem to get the <code>Frame</code> for
	 * @return The Frame for the freeflowItem or null if that view doesn't exist
	 */
	public Rect getActualFrame(final LayoutItem freeflowItem) {
		View v = freeflowItem.view;
		if (v == null) {
			return null;
		}

		Rect of = new Rect();
		of.left = (int) (v.getLeft() + v.getTranslationX());
		of.top = (int) (v.getTop() + v.getTranslationY());
		of.right = (int) (v.getRight() + v.getTranslationX());
		of.bottom = (int) (v.getBottom() + v.getTranslationY());

		return of;
	}
	
	public Collection<LayoutItem> getDisplayedFreeFlowItems() {		
		return this.mDataDisplayed;
	}

	/**
	 * TODO: This should be renamed to layoutInvalidated, since the layout isn't
	 * changed
	 */
	public void layoutChanged() {
		logLifecycleEvent("layoutChanged");
		requestLayout();
	}

	public void persistViewChanges() {		
		// Viewport of the container
		Rect viewport = new Rect(viewPortX, 
				viewPortY, 
				viewPortX + this.getMeasuredWidth(), 
				viewPortY + this.getMeasuredHeight());

		// Loop to the data to check if draw or not
		if (mData != null) {
			mDataDisplayed.clear();
			for (LayoutItem item : mData) {
				
				if (Rect.intersects(item.frame, viewport)) {
					mDataDisplayed.add(item);
					drawFreeFlowItem(item, true);
				} else {
					hideFreeFlowItem(item);
				}
			}
		}
	}

	@Override
	public void requestLayout() {
		if (!preventLayout) {
			/**
			 * Ends up with a call to <code>onMeasure</code> where all the logic
			 * lives
			 */
			super.requestLayout();
		}

	}
	
	private void drawFreeFlowItem(LayoutItem item, boolean withVisibility) {
		addAndMeasureViewIfNeeded(item);
		doLayout(item);
		
		if (withVisibility) {
			item.view.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideFreeFlowItem(LayoutItem item) {		
		if (item.view != null) {
			item.view.setVisibility(View.GONE);
			removeView(item.view);
			returnItemToPoolIfNeeded(item);
		}
	}

	protected void returnItemToPoolIfNeeded(LayoutItem freeflowItem) {
		viewpool.recycle(freeflowItem.view);
		freeflowItem.view = null;
	}

	public void clearFrames() {
		removeAllViews();
		
		if (mData != null) {
			mData.clear();
		}
	}

	@Override
	public boolean shouldDelayChildPressedState() {
		return true;
	}

	public int getCheckedItemCount() {
		return mCheckStates.size();
	}

	public ArrayList<Integer> getCheckedItemPositions() {
		ArrayList<Integer> checked = new ArrayList<Integer>();
		for (int i = 0; i < mCheckStates.size(); i++) {
			checked.add(mCheckStates.keyAt(i));
		}

		return checked;
	}

	public void clearChoices() {
		mCheckStates.clear();
	}

	/**
	 * Defines the choice behavior for the Container allowing multi-select etc.
	 * 
	 * @see <a href=
	 *      "http://developer.android.com/reference/android/widget/AbsListView.html#attr_android:choiceMode"
	 *      >List View's Choice Mode</a>
	 */
	public void setChoiceMode(int choiceMode) {
		mChoiceMode = choiceMode;
		if (mChoiceActionMode != null) {
			mChoiceActionMode.finish();
			mChoiceActionMode = null;
		}
		if (mChoiceMode != CHOICE_MODE_NONE) {
			if (mCheckStates == null) {
				mCheckStates = new SparseBooleanArray();
			}
		}
	}

	boolean isLongClickable = false;

	@Override
	public void setLongClickable(boolean b) {
		isLongClickable = b;
	}

	@Override
	public boolean isLongClickable() {
		return isLongClickable;
	}

	public void setItemChecked(int position, boolean value) {
		if (mChoiceMode == CHOICE_MODE_NONE) {
			return;
		}

		setCheckedValue(position, value);
		requestLayout();
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (this.isEnabled()) {
			ViewTreeObserver observer = this.getViewTreeObserver();
		    observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

		        @SuppressWarnings("deprecation")
				@Override
		        public void onGlobalLayout() {	        	
		        	dataInvalidated();
		        	
		        	if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
		        		getViewTreeObserver().removeGlobalOnLayoutListener(this);
		        	} else {
		        		getViewTreeObserver().removeOnGlobalLayoutListener(this);
		        	}
		        }
		    });
		}		
		
	}

	@Override
	public boolean performItemClick(View view, int position,
			long id) {
		boolean handled = false;
		boolean dispatchItemClick = true;
		if (mChoiceMode != CHOICE_MODE_NONE) {
			handled = true;
			boolean checkedStateChanged = false;

			if (mChoiceMode == CHOICE_MODE_SINGLE) {
				boolean checked = !isChecked(position);
				if (checked) {
					setCheckedValue(position, checked);
				}
				checkedStateChanged = true;
			}

			if (checkedStateChanged) {
				updateOnScreenCheckedViews();
			}
		}

		if (dispatchItemClick) {

			handled |= super.performItemClick(view, position, id);
		}

		return handled;
	}

	/**
	 * Perform a quick, in-place update of the checked or activated state on all
	 * visible item views. This should only be called when a valid choice mode
	 * is active.
	 */
	private void updateOnScreenCheckedViews() {
		Iterator<LayoutItem> it = mData.iterator();
		View child = null;
		while (it.hasNext()) {
			LayoutItem proxy = it.next();
			child = proxy.view;
			boolean isChecked = isChecked(proxy.itemIndex);
			if (child instanceof Checkable) {
				((Checkable) child).setChecked(isChecked);
			} else {
				child.setActivated(isChecked);
			}
		}
	}

	public boolean isChecked(int positionInSection) {
		for (int i = 0; i < mCheckStates.size(); i++) {
			Integer p = mCheckStates.keyAt(i);
			if (p == positionInSection) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates the internal ArrayMap keeping track of checked states. Will not
	 * update the check UI.
	 */
	protected void setCheckedValue(int position,
			boolean val) {
		
		if (val == false) {
			mCheckStates.delete(position);
		} else {
			mCheckStates.put(position, val);
		}
	}

	/**
	 * Returns the percentage of width scrolled. The values range from 0 to 1
	 * 
	 * @return
	 */
	public float getScrollPercentX() {
		if (mLayout == null)
			return 0;
		float w = mLayout.getContentWidth();
		float scrollableWidth = w - getWidth();
		if (scrollableWidth == 0)
			return 0;
		return viewPortX / scrollableWidth;
	}

	/**
	 * Returns the percentage of height scrolled. The values range from 0 to 1
	 * 
	 * @return
	 */
	public float getScrollPercentY() {
		if (mLayout == null)
			return 0;
		float ht = mLayout.getContentHeight();
		float scrollableHeight = ht - getHeight();
		if (scrollableHeight == 0)
			return 0;
		return viewPortY / scrollableHeight;
	}
	
	private DataSetObserver dataObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			dataInvalidated();
		}
		
		public void onInvalidated() {
			dataInvalidated();
		};
	};
}