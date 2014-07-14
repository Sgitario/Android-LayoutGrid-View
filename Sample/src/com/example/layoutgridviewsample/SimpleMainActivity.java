package com.example.layoutgridviewsample;

import java.util.List;

import com.example.layoutgridviewsample.adapters.MyDataAdapter;
import com.example.layoutgridviewsample.data.FeedFetcher;
import com.example.layoutgridviewsample.data.MockFeedFetcher;
import com.example.layoutgridviewsample.model.Feed;
import com.sgitario.android.layoutgridview.LayoutGridView;
import com.sgitario.android.layoutgridview.layouts.impl.StrategyLayoutResolutor;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class SimpleMainActivity extends Activity {
	
	private FeedFetcher fetcher;
	private LayoutGridView container;
	private MyDataAdapter adapter;
	private StrategyLayoutResolutor layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grid);
		
		// Components
		adapter = new MyDataAdapter(this);
		layout = new StrategyLayoutResolutor();
		fetcher = new MockFeedFetcher();
		
		// View
		container = (LayoutGridView) findViewById(R.id.container);
		container.setAdapter(adapter);
		container.setLayout(layout);
		
		// Setup
		setUp(layout);		
		fetchData();
	}
	
	protected void setUp(StrategyLayoutResolutor layoutResolutor) {
		
	}

	private void fetchData() {
		new AsyncTask<Void, Void, List<Feed>>() {

			@Override
			protected List<Feed> doInBackground(Void... params) {
				return fetcher.getFeeds();
			}
			
			@Override
			protected void onPostExecute(List<Feed> result) {
				adapter.addAll(result);
			}
		}.execute();
	}
}
