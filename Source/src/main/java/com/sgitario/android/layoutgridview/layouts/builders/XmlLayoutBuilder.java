package com.sgitario.android.layoutgridview.layouts.builders;

import java.util.List;

import com.sgitario.android.layoutgridview.layouts.Layout;
import com.sgitario.android.layoutgridview.layouts.LayoutBuilder;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.SparseArray;

/**
 * Parse a xml layout resource to a layout matrix. 
 * The xml layout should look like:
 * 
 * <layout name="[NAME OF THE LAYOUT FOR DEBUGGING PURPOSES]">
 * 		<holder width="[%]" height="[%]" type="[classifier]">
 * 			[OPTIONAL, if only one condition is applied, this holder will be used]
 * 				<condition key="[ATTR KEY]" value="[ATTR VALUE"]/>
 * 		</holder>
 * 		<holder>..</holder>
 * </layout> 
 * 
 * @author jhilario
 *
 */
public class XmlLayoutBuilder implements LayoutBuilder {
	private static final String LAYOUT_ELEMENT = "layout";
	private static final String NAME_ATTR = "name";
	private static final String HOLDER_ELEMENT = "holder";
	private static final String WIDTH_ATTR = "width";
	private static final String HEIGHT_ATTR = "height";
	
	private static final String TAG = XmlLayoutBuilder.class.toString();
	
	private SparseArray<Layout> matrix = new SparseArray<Layout>();
	private String name = "NONE";
	
	/**
	 * Initializes a new instance of the XmlLayoutBuilder class.
	 * @param context
	 * @param xmlResId
	 */
	public XmlLayoutBuilder(Context context, int xmlResId) throws NotFoundException {
		XmlResourceParser parser = context.getResources().getXml(xmlResId);
		try {
			int position = 0;
			
			while (parser.getEventType() != XmlResourceParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlResourceParser.START_TAG) {
	                String s = parser.getName();

	                if (s.equals(LAYOUT_ELEMENT)) {
	                	// It's the layout element.
	                	name = parser.getAttributeValue(null, NAME_ATTR);
	                } else if (s.equals(HOLDER_ELEMENT)) {
	                	// It's a holder element (a matrix holder)
	                	Layout layout = buildLayout(parser);
	                	
	                	layout.setWidthPercentage(parser.getAttributeFloatValue(null, WIDTH_ATTR, 0.0f));
	                	layout.setHeightPercentage(parser.getAttributeFloatValue(null, HEIGHT_ATTR, 0.0f));
	                    
	                    matrix.put(position, layout);
	                    position++;
	                } else {
	                	appendElement(parser, matrix.get(position - 1));
	                }
	            }
				
				parser.next();
			}
			
		} catch (Exception ex) {
			Log.e(TAG, "Error reading file " + xmlResId, ex);
		} finally {
			parser.close();
		}
	}
	
	public void appendElement(XmlResourceParser parser, Layout layout) {
		
	}
	
	public Layout buildLayout(XmlResourceParser parser) {
		return new Layout();
	}
	
	/**
	 * Get the name of the layout.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Check whether the feeds list applies for this layout.
	 */
	@Override
	public boolean isFor(List<Object> feeds) {
		
		boolean applies = false;
		
		if (!feeds.isEmpty() && getNumItems() <= feeds.size()) {
			applies = true;
			int index = 0;
			while (index < getNumItems() && applies) {
				Layout layout = matrix.get(index);
				
				applies = layout != null
						&& layout.isFor(feeds.get(index));
				
				index++;
			}
		}

		return applies;
	}
	
	@Override
	public int getNumItems() {
		return matrix.size();
	}

	@Override
	public Layout getLayoutFor(int position) {
		return matrix.get(position);
	}
}
