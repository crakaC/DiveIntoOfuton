package com.crakac.ofuton.timeline;

import java.util.List;

import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class MentionsTimelineFragment extends BaseTimelineFragment {
	private Twitter mTwitter;
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
			return mTwitter.getMentionsTimeline();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	List<Status> newStatuses(long id, int count) {
		try {
			return mTwitter.getMentionsTimeline(new Paging().sinceId(
						id).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	List<Status> previousStatuses(long id, int count) {
		try {
			return mTwitter.getMentionsTimeline(new Paging().maxId(
					id - 1l).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	void failToGetStatuses() {
		AppUtil.showToast(getActivity(), "Mention‚ÌŽæ“¾‚ÉŽ¸”s‚µ‚Ü‚µ‚½");
	}

	final String title ="Mentions";
	@Override
	public String getTimelineName() {
		return title;
	}

}