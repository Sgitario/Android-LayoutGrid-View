package com.example.layoutgridviewsample.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.example.layoutgridviewsample.R;
import com.example.layoutgridviewsample.model.Feed;

public class MockFeedFetcher implements FeedFetcher {
	
	private static final int NUM_FEEDS = 20;
	private static final List<Integer> IMAGES = new ArrayList<Integer>();
	private static final List<String> TYPES = new ArrayList<String>();
	private static final Random RND = new Random(new Date().getTime());
	
	static {
		IMAGES.add(R.drawable.image1);
		IMAGES.add(R.drawable.image2);
		IMAGES.add(R.drawable.image3);
		IMAGES.add(R.drawable.image4);
		IMAGES.add(R.drawable.image5);
		
		TYPES.add("TYPE1");
		TYPES.add("TYPE2");
		TYPES.add("TYPE3");
		TYPES.add("TYPE4");
		TYPES.add("TYPE5");
	}

	@Override
	public List<Feed> getFeeds() {
		List<Feed> feeds = new ArrayList<Feed>();
		
		for (int index = 0; index < NUM_FEEDS; index++) {
			Feed feed = new Feed();
			feed.setImageResId(getImage());
			feed.setType(getType());
			
			feeds.add(feed);
		}
		
		return feeds;
	}
	
	private Integer getImage() {
		return IMAGES.get(RND.nextInt(IMAGES.size()));
	}
	
	private String getType() {
		return TYPES.get(RND.nextInt(TYPES.size()));
	}

}
