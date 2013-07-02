package com.crakac.fallintoofuton.timeline;

import java.util.List;

import com.crakac.fallintoofuton.util.AppUtil;
import com.crakac.fallintoofuton.util.TwitterUtils;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeTimelineFragment extends BaseTimelineFragment {
	private Twitter mTwitter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("HomeTimeline","onCreateView");
		if (mTwitter == null) {
			mTwitter = TwitterUtils.getTwitterInstance(getActivity());
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	List<Status> initialStatuses() {
		try {
			return mTwitter.getHomeTimeline();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	List<Status> newStatuses(long id, int count) {
		try {
			return mTwitter.getHomeTimeline(new Paging().sinceId(
						id).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	List<Status> previousStatuses(long id, int count) {
		try {
			return mTwitter.getHomeTimeline(new Paging().maxId(
					id - 1l).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	void failToGetStatuses() {
		AppUtil.showToast(getActivity(), "ƒ^ƒCƒ€ƒ‰ƒCƒ“‚ÌŽæ“¾‚ÉŽ¸”s‚µ‚Ü‚µ‚½");
	}

	final String title="Home";
	@Override
	public String getTimelineName() {
		return title;
	}
}