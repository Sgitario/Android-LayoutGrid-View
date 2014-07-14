LayoutGridView
==============

Android control enables to visualize data in a grid layouts such as the GridView widget in Android native. 

The LayoutGridView widget enables:

- Dynamic visualization data into layouts. 
- Horizontal and vertical scrolling depending on screen orientation. 
- Selected item click events.

Dynamic visualization data into layouts
============

The widget is going to resolve the size of each layout depending on the data to be displayed and the next items in the list. 

By default, the StrategyLayoutResolutor is based on Strategy pattern and the user is able to add any suitable strategy (LayoutBuilder classes) to place the layouts. The LayoutBuilder instances is going to receive a list of the next object classes that the Adapter contains (when an object classes is resolved, the process will continue with the next ones). If the user requires to specify any other kind of implementation for LayoutResolutor, can do it by specifying other instance of this interface. 

There is an special implementation of LayoutBuilder, the XmlLayoutBuilder is to read XMLs configuration files such as:

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout name="Default">
    <holder width="40.0" height="100.0"></holder>
</layout>
```

These files must be located into the res/xml.. folder in the project. The advantage is that the design of a layout may vary if the screen is on portrait or landscape. 

The user can adapt the xml files by adding any other attribute by extending the XmlLayoutBuilder to its purposes:

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout name="MyLayout">
    <holder width="40.0" height="100.0" type="TYPEA"></holder>
	<holder width="40.0" height="100.0" type="TYPEB"></holder>
</layout>
```

In the above example, the MyLayout is going to be used if the next two items in the data model are the TYPEA and TYPEB. The only methods to be modified should be "buildLayout" and "appendElement":
 
```
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
}
```

This type attribute is not in the LayoutItem class supplied in this framework, it has be extended:

```
private class MyLayout extends Layout {
	private String type;
	
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
			isFor = feed.getType().equals(getType());
		}
		
		return isFor;
	}
}
```

All this info can be found in the sample project.