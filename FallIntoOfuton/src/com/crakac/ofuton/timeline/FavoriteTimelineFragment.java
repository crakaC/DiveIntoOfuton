package com.crakac.ofuton.timeline;

import java.util.List;

import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FavoriteTimelineFragment extends BaseTimelineFragment {
	private Twitter mTwitter;
	public FavoriteTimelineFragment() {
		super();
		Log.d("ListTimeline","Constractor");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mTwitter == null) {
			mTwitter = TwitterUtils.getTwitterInstance(getActivity());
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	List<Status> initialStatuses() {
		try {
			return mTwitter.getFavorites(new Paging());
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	List<Status> newStatuses(long id, int count) {
		try {
			return mTwitter.getFavorites(new Paging().sinceId(id).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	List<Status> previousStatuses(long id, int count) {
		try {
			return mTwitter.getFavorites(new Paging().maxId(
					id - 1l).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	void failToGetStatuses() {
		AppUtil.showToast(getActivity(), "‚¨‹C‚É“ü‚è‚ÌŽæ“¾‚ÉŽ¸”s‚µ‚Ü‚µ‚½");
	}

	@Override
	public String getTimelineName() {
		return "Favorites";
	}
}