package com.example.layoutgridviewsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button simple = (Button) findViewById(R.id.simpleOption);
		simple.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				navigateTo(SimpleMainActivity.class);
			}			
		});
		
		Button xml = (Button) findViewById(R.id.xmlOption);
		xml.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				navigateTo(XmlMainActivity.class);
			}			
		});
		
		Button manyXml = (Button) findViewById(R.id.manyXmlOption);
		manyXml.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				navigateTo(ManyXmlMainActivity.class);
			}			
		});
	}
	
	private void navigateTo(Class<? extends Activity> activityTo) {
		Intent intent = new Intent(this, activityTo);
		startActivity(intent);
	}
}
