package com.example.layoutgridviewsample.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;

import com.example.layoutgridviewsample.model.Feed;
import com.sgitario.android.layoutgridview.layouts.Layout;
import com.sgitario.android.layoutgridview.layouts.builders.XmlLayoutBuilder;

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
public class MyXmlLayoutBuilder extends XmlLayoutBuilder {

	private static final String TYPE_ATTR = "type";
	private static final String CONDITION_ELEMENT = "condition";
	private static final String KEY_ATTR = "key";
	private static final String VALUE_ATTR = "value";
	
	public MyXmlLayoutBuilder(Context context, int xmlResId)
			throws NotFoundException {
		super(context, xmlResId);
	}
	
	public Layout buildLayout(XmlResourceParser parser) {
		MyLayout layout = new MyLayout();
		
		layout.setType(parser.getAttributeValue(null, TYPE_ATTR));
		
		return layout;
	}
	
	public void appendElement(XmlResourceParser parser, Layout layout) {
		if (layout instanceof MyLayout 
				&& parser.getName().equals(CONDITION_ELEMENT)) {
			MyLayout myLayout = (MyLayout) layout;
			
			// It's a condition element of the previous holder.
        	String key = parser.getAttributeValue(null, KEY_ATTR);
        	String value = parser.getAttributeValue(null, VALUE_ATTR);
        	
        	if (layout != null) {
        		myLayout.addCondition(key, value);
        	}
		}
	}
	
	private class MyLayout extends Layout {
		private String type;
		
		/**
		 * The list of conditions to apply the layout. Only needs to apply one.
		 */
		private final List<ConditionAttr> conditions = new ArrayList<ConditionAttr>();
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		@Override
		public boolean isFor(Object item) {
			boolean isFor = false;

			if (item instanceof Feed) {
				Feed feed = (Feed) item;
				isFor = feed.getType().equals(getType()) 
						&& applyConditions(feed.getAttrs());
			}
			
			return isFor;
		}
		
		/**
		 * Add an attr condition to the layout.
		 * @param key
		 * @param value
		 */
		public void addCondition(String key, String value) {
			conditions.add(new ConditionAttr(key, value));
		}
		
		/**
		 * Check whether the feed attrs apply the conditions attrs in the layout.
		 * @param attrs
		 * @return
		 */
		private boolean applyConditions(Map<String, String> attrs) {
			boolean applies = true;
			if (!conditions.isEmpty()) {
				applies = false;
				int index = 0;
				while (index < conditions.size() && !applies) {
					ConditionAttr condition = conditions.get(index);
					applies = attrs.containsKey(condition.getKey()) && attrs.get(condition.getKey()).equals(condition.getValue());
					
					index++;
				}
			}
			
			return applies;
		}
		
		/**
		 * The list of conditions for this layout. 
		 * It's using a custom class insteads of a map collection for including more options in the condition attr for future (OR, NESTING, ...)
		 * 
		 * @author jhilario
		 *
		 */
		private class ConditionAttr {
			private final String key;
			private final String value;
			
			/**
			 * Initializes a new instance of the ConditionAttr class.
			 * @param key
			 * @param value
			 */
			public ConditionAttr(String key, String value) {
				this.key = key;
				this.value = value;
			}

			public String getKey() {
				return key;
			}

			public String getValue() {
				return value;
			}
		}
	}
}
