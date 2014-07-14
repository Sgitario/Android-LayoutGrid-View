package com.example.layoutgridviewsample;

import com.example.layoutgridviewsample.layout.MyXmlLayoutBuilder;
import com.sgitario.android.layoutgridview.layouts.impl.StrategyLayoutResolutor;

public class ManyXmlMainActivity extends SimpleMainActivity {
	
	@Override
	protected void setUp(StrategyLayoutResolutor layoutResolutor) {
		super.setUp(layoutResolutor);
		
		layoutResolutor.addLayoutBuilder(new MyXmlLayoutBuilder(this, R.xml.xml_type1type2_layout));
		layoutResolutor.addLayoutBuilder(new MyXmlLayoutBuilder(this, R.xml.xml_type2type1_layout));
		layoutResolutor.addLayoutBuilder(new MyXmlLayoutBuilder(this, R.xml.xml_beautiful_layout));
	}
}
