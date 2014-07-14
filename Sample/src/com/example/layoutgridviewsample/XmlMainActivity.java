package com.example.layoutgridviewsample;

import com.sgitario.android.layoutgridview.layouts.builders.XmlLayoutBuilder;
import com.sgitario.android.layoutgridview.layouts.impl.StrategyLayoutResolutor;

public class XmlMainActivity extends SimpleMainActivity {
	
	@Override
	protected void setUp(StrategyLayoutResolutor layoutResolutor) {
		super.setUp(layoutResolutor);
		
		layoutResolutor.setDefaultLayoutBuilder(new XmlLayoutBuilder(this, R.xml.xml_big_layout));
	}
}
