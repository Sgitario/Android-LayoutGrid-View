package com.sgitario.android.layoutgridview;

import java.util.List;

import com.sgitario.android.layoutgridview.events.OnItemClickListener;
import com.sgitario.android.layoutgridview.events.OnItemSelectedListener;
import com.sgitario.android.layoutgridview.layouts.LayoutResolutor;
import com.sgitario.android.layoutgridview.model.LayoutItem;
import com.sgitario.android.layoutgridview.utils.Utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.BaseAdapter;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

/**
 * Responsabilities: 
 * 
 * - Touch gestures.
 * 
 * @author jhilario
 *
 */
public abstract class AbsLayoutContainer extends ViewGroup {
	
	private static final String TAG = AbsLayoutContainer.class.toString();
	
	/**
	 * Indicates that we are not in the middle of a touch gesture
	 */
	public static final int TOUCH_MODE_REST = -1;

	/**
	 * Indicates we just received the touch event and we are waiting to see if
	 * the it is a tap or a scroll gesture.
	 */
	public static final int TOUCH_MODE_DOWN = 0;

	/**
	 * Indicates the touch has been recognized as a tap and we are now waiting
	 * to see if the touch is a longpress
	 */
	public static final int TOUCH_MODE_TAP = 1;

	/**
	 * Indicates we have waited for everything we can wait for, but the user's
	 * finger is still down
	 */
	public static final int TOUCH_MODE_DONE_WAITING = 2;

	/**
	 * Indicates the touch gesture is a scroll
	 */
	public static final int TOUCH_MODE_SCROLL = 3;

	/**
	 * Indicates the view is in the process of being flung
	 */
	public static final int TOUCH_MODE_FLING = 4;

	/**
	 * Indicates the touch gesture is an overscroll - a scroll beyond the
	 * beginning or end.
	 */
	public static final int TOUCH_MODE_OVERSCROLL = 5;

	/**
	 * Indicates the view is being flung outside of normal content bounds and
	 * will spring back.
	 */
	public static final int TOUCH_MODE_OVERFLING = 6;

	/**
	 * The duration for which the scroller will wait before deciding whether the
	 * user was actually trying to stop the scroll or swuipe again to increase
	 * the velocity
	 */
	protected final int FLYWHEEL_TIMEOUT = 40;
	
	private OverScroller scroller;
	private EdgeEffect mLeftEdge, mRightEdge, mTopEdge, mBottomEdge;
	
	/**
	 * The X position of the active ViewPort
	 */
	protected int viewPortX = 0;

	/**
	 * The Y position of the active ViewPort
	 */
	protected int viewPortY = 0;

	/**
	 * The scrollable width in pixels. This is usually computed as the
	 * difference between the width of the container and the contentWidth as
	 * computed by the layout.
	 */
	protected int mScrollableWidth;

	/**
	 * The scrollable height in pixels. This is usually computed as the
	 * difference between the height of the container and the contentHeight as
	 * computed by the layout.
	 */
	protected int mScrollableHeight;
	
	/**
	 * One of TOUCH_MODE_REST, TOUCH_MODE_DOWN, TOUCH_MODE_TAP,
	 * TOUCH_MODE_SCROLL, or TOUCH_MODE_DONE_WAITING
	 */
	private int mTouchMode = TOUCH_MODE_REST;

	private VelocityTracker mVelocityTracker = null;
	private float deltaX = -1f;
	private float deltaY = -1f;

	private int maxFlingVelocity;
	private int minFlingVelocity;
	private int overflingDistance;
	private int touchSlop;
	private LayoutItem beginTouchAt;
	protected LayoutItem selectedItem;
	
	// Runnables
	private Runnable mTouchModeReset;
	private Runnable mPerformClick;
	
	// Events
	protected OnItemSelectedListener mOnItemSelectedListener;
	protected OnItemClickListener mOnItemClickListener;

	public AbsLayoutContainer(Context context) {
		super(context);
		init(context);
	}

	public AbsLayoutContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AbsLayoutContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public abstract LayoutResolutor getLayout();
	public abstract BaseAdapter getAdapter();
	protected abstract List<LayoutItem> getLayoutItems();
	protected abstract void persistViewChanges();
	
	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been selected.
	 * 
	 * @param listener
	 *            The callback that will run
	 */
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		mOnItemSelectedListener = listener;
	}

	public final OnItemSelectedListener getOnItemSelectedListener() {
		return mOnItemSelectedListener;
	}
	
	/**
	 * Register a callback to be invoked when an item in this AdapterView has
	 * been clicked.
	 * 
	 * @param listener
	 *            The callback that will be invoked.
	 */
	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	/**
	 * @return The callback to be invoked with an item in this AdapterView has
	 *         been clicked, or null id no callback has been set.
	 */
	public final OnItemClickListener getOnItemClickListener() {
		return mOnItemClickListener;
	}
	
	public LayoutItem getSelectedLayoutItem() {
		return selectedItem;
	}
	
	protected void setViewPortX(int x) {		
		viewPortX = x;
	}
	
	protected void setViewPortY(int y) {
		viewPortY = y;
	}	
	
	/**
	 * Scroll to the specified item.
	 * @param itemIndex
	 */
	public void scrollToItem(int itemIndex) {

		if (getAdapter().getCount() == 0) {
			return;
		}

		if (itemIndex < 0 || itemIndex > getAdapter().getCount()) {
			return;
		}

		LayoutItem freeflowItem = Utils.getLayoutItemForItem(getLayoutItems(), getAdapter().getItem(itemIndex));
		freeflowItem = LayoutItem.clone(freeflowItem);

		int newVPX = freeflowItem.frame.left;
		int newVPY = freeflowItem.frame.top;

		if (newVPX > getLayout().getContentWidth() - getMeasuredWidth())
			newVPX = getLayout().getContentWidth() - getMeasuredWidth();

		if (newVPY > getLayout().getContentHeight() - getMeasuredHeight())
			newVPY = getLayout().getContentHeight() - getMeasuredHeight();

		scroller.startScroll(viewPortX, viewPortY, (newVPX - viewPortX),
				(newVPY - viewPortY), 1500);
		post(flingRunnable);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		super.onTouchEvent(event);
		if (getLayout() == null || !this.isEnabled()) {
			return false;
		}

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		if (mVelocityTracker != null) {
			mVelocityTracker.addMovement(event);
		}

		switch (event.getAction()) {
		case (MotionEvent.ACTION_DOWN):
			touchDown(event);
			break;
		case (MotionEvent.ACTION_MOVE):
			touchMove(event);
			break;
		case (MotionEvent.ACTION_UP):
			touchUp(event);
			break;
		case (MotionEvent.ACTION_CANCEL):
			touchCancel(event);
			break;
		}

		return true;

	}

	protected void touchDown(MotionEvent event) {

		/*
		 * Recompute this just to be safe. TODO: We should optimize this to be
		 * only calculated when a data or layout change happens
		 */
		mScrollableHeight = getLayout().getContentHeight() - getHeight() + 200;
		mScrollableWidth = getLayout().getContentWidth() - getWidth();

		if (mTouchMode == TOUCH_MODE_FLING) {
			// Wait for some time to see if the user is just trying
			// to speed up the scroll
			postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mTouchMode == TOUCH_MODE_DOWN) {
						if (mTouchMode == TOUCH_MODE_DOWN) {
							scroller.forceFinished(true);
						}
					}
				}
			}, FLYWHEEL_TIMEOUT);
		}

		beginTouchAt = Utils.getItemAt(
				getLayoutItems(),
				(int) (viewPortX + event.getX()),
				(int) (viewPortY + event.getY()));

		deltaX = event.getX();
		deltaY = event.getY();

		mTouchMode = TOUCH_MODE_DOWN;
	}

	protected void touchMove(MotionEvent event) {
		float xDiff = event.getX() - deltaX;
		float yDiff = event.getY() - deltaY;

		double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		boolean isPortrait = isPortrait();

		if (!isPortrait && xDiff > 0 && viewPortX == 0) {
			float str = (float) distance / getWidth();
			mLeftEdge.onPull(str);
			// invalidate();
			
			return;
		}

		if (!isPortrait && xDiff < 0 && viewPortX == mScrollableWidth) {
			float str = (float) distance / getWidth();
			mRightEdge.onPull(str);
			// invalidate();
			
			return;
		}
		
		if (isPortrait && yDiff > 0 && viewPortY == 0) {
			float str = (float) distance / getHeight();
			mTopEdge.onPull(str);
			// invalidate();
			
			return;
		}

		if (isPortrait && yDiff < 0 && viewPortY == mScrollableHeight) {
			float str = (float) distance / getHeight();
			mBottomEdge.onPull(str);
			// invalidate();
			
			return;
		}

		if ((mTouchMode == TOUCH_MODE_DOWN || mTouchMode == TOUCH_MODE_REST)
				&& distance > touchSlop) {
			mTouchMode = TOUCH_MODE_SCROLL;
		}

		if (mTouchMode == TOUCH_MODE_SCROLL) {
			if (isPortrait) {
				int newViewPortY = (int) (viewPortY - yDiff);
				if (newViewPortY > mScrollableHeight) {
					newViewPortY = mScrollableHeight;
				} 
				
				setViewPortY(newViewPortY);
				deltaY = event.getY();
			} else {
				int newViewPortX = (int) (viewPortX - xDiff);
				if (newViewPortX > mScrollableWidth) {
					newViewPortX = mScrollableWidth;
				} 
				
				setViewPortX(newViewPortX);
				deltaX = event.getX();
			}
			
			moveViewport(false);
		}
	}

	protected void touchCancel(MotionEvent event) {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	protected void touchUp(MotionEvent event) {		
		if (mTouchMode == TOUCH_MODE_SCROLL
				|| mTouchMode == TOUCH_MODE_OVERFLING) {

			mVelocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);

			if (Math.abs(mVelocityTracker.getXVelocity()) > minFlingVelocity
					|| Math.abs(mVelocityTracker.getYVelocity()) > minFlingVelocity) {

				int maxX = getLayout().getContentWidth() - getWidth();
				int maxY = getLayout().getContentHeight() - getHeight();

				int allowedScrollOffset;
				if (mTouchMode == TOUCH_MODE_SCROLL) {
					allowedScrollOffset = 0;
				} else {
					allowedScrollOffset = overflingDistance;
				}

				scroller.fling(viewPortX, viewPortY,
						-(int) mVelocityTracker.getXVelocity(),
						-(int) mVelocityTracker.getYVelocity(), 0, maxX, 0,
						maxY, allowedScrollOffset, allowedScrollOffset);

				mTouchMode = TOUCH_MODE_FLING;

				post(flingRunnable);

			} else {
				mTouchMode = TOUCH_MODE_REST;
			}

		} else if (mTouchMode == TOUCH_MODE_DOWN
				|| mTouchMode == TOUCH_MODE_DONE_WAITING) {
			if (mTouchModeReset != null) {
				removeCallbacks(mTouchModeReset);
			}
			if (beginTouchAt != null && beginTouchAt.view != null) {
				beginTouchAt.view.setPressed(true);

				mTouchModeReset = new Runnable() {
					@Override
					public void run() {
						mTouchModeReset = null;
						mTouchMode = TOUCH_MODE_REST;

						if (beginTouchAt != null && beginTouchAt.view != null) {
							beginTouchAt.view.setPressed(false);
						}
						if (mOnItemSelectedListener != null) {
							mOnItemSelectedListener.onItemSelected(
									AbsLayoutContainer.this,
									selectedItem);
						}
					}
				};
				selectedItem = beginTouchAt;
				postDelayed(mTouchModeReset,
						ViewConfiguration.getPressedStateDuration());

				mTouchMode = TOUCH_MODE_TAP;
				boolean isPortrait = isPortrait();
				
				if ((Math.abs(event.getX() - deltaX) <= 20.0 && !isPortrait)
						|| (Math.abs(event.getY() - deltaY) <= 20.0 && isPortrait)) {
					mPerformClick = new PerformClick();
					mPerformClick.run();
				}
			} else {
				mTouchMode = TOUCH_MODE_REST;
			}

		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		boolean needsInvalidate = false;

		final int height = getMeasuredHeight() - getPaddingTop()
				- getPaddingBottom();
		final int width = getMeasuredWidth();

		if (!mLeftEdge.isFinished()) {
			final int restoreCount = canvas.save();

			canvas.rotate(270);
			canvas.translate(-height + getPaddingTop(), 0);// width);
			mLeftEdge.setSize(height, width);

			needsInvalidate = mLeftEdge.draw(canvas);
			canvas.restoreToCount(restoreCount);
		}

		if (!mTopEdge.isFinished()) {
			final int restoreCount = canvas.save();

			mTopEdge.setSize(width, height);

			needsInvalidate = mTopEdge.draw(canvas);
			canvas.restoreToCount(restoreCount);
		}

		if (!mRightEdge.isFinished()) {
			final int restoreCount = canvas.save();

			canvas.rotate(90);
			canvas.translate(0, -width);// width);
			mRightEdge.setSize(height, width);

			needsInvalidate = mRightEdge.draw(canvas);
			canvas.restoreToCount(restoreCount);
		}

		if (!mBottomEdge.isFinished()) {
			final int restoreCount = canvas.save();

			canvas.rotate(180);
			canvas.translate(-width + getPaddingTop(), -height);

			mBottomEdge.setSize(width, height);

			needsInvalidate = mBottomEdge.draw(canvas);
			canvas.restoreToCount(restoreCount);
		}

		if (needsInvalidate) {
			this.postInvalidateOnAnimation();
		}
	}
	
	/**
	 * Initialize scrollers.
	 * @param c
	 */
	protected void init(Context context) {
		ViewConfiguration configuration = ViewConfiguration.get(context);
		maxFlingVelocity = configuration.getScaledMaximumFlingVelocity();
		minFlingVelocity = configuration.getScaledMinimumFlingVelocity();
		overflingDistance = configuration.getScaledOverflingDistance();

		touchSlop = configuration.getScaledTouchSlop();

		scroller = new OverScroller(context);

		setWillNotDraw(false);
		mLeftEdge = new EdgeEffect(context);
		mRightEdge = new EdgeEffect(context);
		mTopEdge = new EdgeEffect(context);
		mBottomEdge = new EdgeEffect(context);
	}
	
	/**
	 * @return true if the view is in portrait.
	 */
	protected boolean isPortrait() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}
	
	/**
	 * Stops the scrolling immediately
	 */
	protected void stopScrolling() {
		if (!scroller.isFinished()) {
			scroller.forceFinished(true);
		}
		removeCallbacks(flingRunnable);
		resetAllCallbacks();
		mTouchMode = TOUCH_MODE_REST;
	}
	
	/**
	 * Resets all Runnables that are checking on various statuses
	 */
	protected void resetAllCallbacks() {
		if (mTouchModeReset != null) {
			removeCallbacks(mTouchModeReset);
			mTouchModeReset = null;
		}
		if (mPerformClick != null) {
			removeCallbacks(mPerformClick);
			mPerformClick = null;
		}
	}
	
	/**
	 * Will move viewport to viewPortX and viewPortY values
	 * 
	 * @param isInFlingMode
	 *            Setting this
	 */
	private void moveViewport(boolean isInFlingMode) {

		mScrollableWidth = getLayout().getContentWidth() - getWidth();
		if (mScrollableWidth < 0) {
			mScrollableWidth = 0;
		}
		mScrollableHeight = getLayout().getContentHeight() - getHeight();
		if (mScrollableHeight < 0) {
			mScrollableHeight = 0;
		}

		if (isInFlingMode) {
			if (viewPortX < 0 || viewPortX > mScrollableWidth || viewPortY < 0
					|| viewPortY > mScrollableHeight) {
				mTouchMode = TOUCH_MODE_OVERFLING;
			}
		} else {

			if (viewPortX < -overflingDistance) {
				setViewPortX(-overflingDistance);
			} else if (viewPortX > mScrollableWidth + overflingDistance) {
				setViewPortX(mScrollableWidth + overflingDistance);
			}

			if (viewPortY < (int) (-overflingDistance)) {
				setViewPortY((int) -overflingDistance);
			} else if (viewPortY > mScrollableHeight + overflingDistance) {
				setViewPortY((int) (mScrollableHeight + overflingDistance));
			}

			if (viewPortX < 0) {
				mLeftEdge.onPull(viewPortX / (-overflingDistance));
			} else if (viewPortX > mScrollableWidth) {
				mRightEdge.onPull((viewPortX - mScrollableWidth)
						/ (-overflingDistance));
			}

			if (viewPortY < 0) {
				mTopEdge.onPull(viewPortY / (-overflingDistance));
			} else if (viewPortY > mScrollableHeight) {
				mBottomEdge.onPull((viewPortY - mScrollableHeight)
						/ (-overflingDistance));
			}
		}

		persistViewChanges();

	}
	
	private class PerformClick implements Runnable {
		@Override
		public void run() {
			// if (mDataChanged) return;
			View view = beginTouchAt.view;
			if (view != null) {
				performItemClick(view, beginTouchAt.itemIndex, getAdapter().getItemId(
								beginTouchAt.itemIndex));
			}
		}
	}
	
	private Runnable flingRunnable = new Runnable() {

		@Override
		public void run() {
			if (scroller.isFinished()) {
				mTouchMode = TOUCH_MODE_REST;

				return;
			}
			boolean more = scroller.computeScrollOffset();
			checkEdgeEffectDuringScroll();
			
			if (isPortrait()) {
				setViewPortY(scroller.getCurrY());
			} else {
				setViewPortX(scroller.getCurrX());
			}			

			moveViewport(true);
			if (more) {
				post(flingRunnable);
			}
		}
	};

	protected void checkEdgeEffectDuringScroll() {
		if (mLeftEdge.isFinished() && viewPortX < 0) {
			mLeftEdge.onAbsorb((int) scroller.getCurrVelocity());
		}

		if (mRightEdge.isFinished()
				&& viewPortX > getLayout().getContentWidth() - getMeasuredWidth()) {
			mRightEdge.onAbsorb((int) scroller.getCurrVelocity());
		}

	}

	/**
	 * Call the OnItemClickListener, if it is defined. Performs all normal
	 * actions associated with clicking: reporting accessibility event, playing
	 * a sound, etc.
	 * 
	 * @param view
	 *            The view within the AdapterView that was clicked.
	 * @param position
	 *            The position of the view in the adapter.
	 * @param id
	 *            The row id of the item that was clicked.
	 * @return True if there was an assigned OnItemClickListener that was
	 *         called, false otherwise is returned.
	 */
	public boolean performItemClick(View view, int position, long id) {
		if (mOnItemClickListener != null) {
			// playSoundEffect(SoundEffectConstants.CLICK);
			if (view != null) {
				view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
			}
			mOnItemClickListener.onItemClick(this, Utils.getLayoutItemAt(getLayoutItems(), position));
			return true;
		}

		return false;
	}
	
	/**
	 * A utility method for debugging lifecycle events and putting them in the
	 * log messages
	 * 
	 * @param msg
	 */
	protected void logLifecycleEvent(String msg) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, msg);
		}
	}
}
