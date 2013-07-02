package com.crakac.fallintoofuton;

import java.util.ArrayList;

import com.crakac.fallintoofuton.timeline.BaseTimelineFragment;
import com.crakac.fallintoofuton.timeline.FavoriteTimelineFragment;
import com.crakac.fallintoofuton.timeline.HomeTimelineFragment;
import com.crakac.fallintoofuton.timeline.ListTimelineFragment;
import com.crakac.fallintoofuton.timeline.MentionsTimelineFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TimelineFragmentPagerAdapter extends FragmentPagerAdapter {

	ArrayList<BaseTimelineFragment> fragments;

	public TimelineFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		fragments = new ArrayList<BaseTimelineFragment>();
	}

	@Override
	public Fragment getItem(int i) {
		return fragments.get(i);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return fragments.get(position).getTimelineName();
	}
	
	/**
	 * add user's List.
	 * @param id List ID
	 * @param title List title. It is shown in Tab.
	 */
	public void addList(int id, String title){
		ListTimelineFragment lf = new ListTimelineFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("listId", id);
		bundle.putString("listTitle", title);
		lf.setArguments(bundle);
		fragments.add(lf);
		notifyDataSetChanged();
	}
	
	/**
	 * add HomeTimeline
	 */
	public void addHomeTimeline(){
		fragments.add(new HomeTimelineFragment());
		notifyDataSetChanged();
	}
	
	/**
	 * add mentions timeline
	 */
	public void addMentionsTimeline(){
		fragments.add(new MentionsTimelineFragment());
		notifyDataSetChanged();
	}
	
	/**
	 * add favorite timeline
	 */
	public void addFavoriteTimeline(){
		fragments.add(new FavoriteTimelineFragment());
		notifyDataSetChanged();
	}
}
