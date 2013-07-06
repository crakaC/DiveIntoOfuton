package com.crakac.ofuton.user;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.TimelineFragmentPagerAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ProgressDialogFragment;
import com.crakac.ofuton.util.TwitterUtils;
import com.loopj.android.image.SmartImageView;

public class UserDetailFragment extends Fragment{
	private static final String TAG = UserDetailFragment.class.getSimpleName();
	private TextView name, screenName, bio, location, url, relation;
	private Button followBtn;
	private SmartImageView icon;
	private AsyncTask<String, Void, twitter4j.User> loadContentTask;
	private AsyncTask<Long, Void, twitter4j.Relationship> loadRelationTask;
	private ProgressDialogFragment dialog;
	private FragmentManager manager;
	private twitter4j.User userInfo;
	private Twitter twitter;
	
	private PagerSlidingTabStrip tab;
	private ViewPager pager;
	private TimelineFragmentPagerAdapter adapter;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		adapter = new TimelineFragmentPagerAdapter(getFragmentManager());
		adapter.addHomeTimeline();
		adapter.addFavoriteTimeline();
		adapter.addMentionsTimeline();
		pager.setAdapter(adapter);
		tab.setViewPager(pager);
		
		twitter = TwitterUtils.getTwitterInstance(getActivity());
		manager = getActivity().getSupportFragmentManager();

		initTexts();
		loadContent();

		return null;
	}
	
	private void initTexts(){
		//xml�̕��ɂ̓_�~�[�̃e�L�X�g�����Ă���̂ŁD
		name.setText("");
		screenName.setText("");
		bio.setText("");
		location.setText("");
		url.setText("");
		relation.setText("");
		followBtn.setText(getString(R.string.now_loading));
	}

	private void loadContent(){
		if(loadContentTask != null && loadContentTask.getStatus() == AsyncTask.Status.RUNNING){
			return;
		}
		loadContentTask = new AsyncTask<String, Void, twitter4j.User>() {
			@Override
			protected void onPreExecute() {
				dialog = ProgressDialogFragment.newInstance("���[�U�[����ǂݍ���ł��܂�");
				dialog.show( manager , "user detail");
			}

			@Override
			protected twitter4j.User doInBackground(String... params) {
				try {
					return twitter.showUser(params[0]);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(twitter4j.User result) {
				dialog.dismiss();
				if(result != null){
					setContent(result);
					getRelationShip(result);
				} else {
					AppUtil.showToast(getActivity(), "���[�U�[�����擾�o���܂���ł���");
					getActivity().finish();
				}
			}
		};
		loadContentTask.execute(getArguments().getString("screenName"));
	}
	
	private void setContent(twitter4j.User user){
		name.setText(user.getName());
		screenName.setText(user.getScreenName());
		bio.setText(user.getDescription());
		location.setText(user.getLocation());
		url.setText(user.getURL());
		icon.setImageUrl(user.getBiggerProfileImageURL());
	}
	
	private void getRelationShip(twitter4j.User user){
		if(loadRelationTask != null && loadRelationTask.getStatus() == AsyncTask.Status.RUNNING){
			return;
		}
		loadRelationTask = new AsyncTask<Long, Void, twitter4j.Relationship>() {
			@Override
			protected twitter4j.Relationship doInBackground(Long... params) {
				try {
					return twitter.showFriendship(params[0], params[1]);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(twitter4j.Relationship result) {
				if(result != null){
					if(result.isSourceBlockingTarget()){
						relation.setText("�u���b�N��");
					} else if(result.isSourceFollowedByTarget() && result.isSourceFollowingTarget()){
						relation.setText("���݃t�H���[");
					} else if(result.isSourceFollowingTarget()){
						relation.setText("�Ўv��");
					} else if(result.isSourceFollowedByTarget()){
						relation.setText("�t�@��");
					} else {
						relation.setText("���֐S");
					}
				} else {
					AppUtil.showToast(getActivity(), "����������������");
					Log.d(TAG, "getRelaitionship(), something wrong");
				}
			}
		};
		long currentUserId = TwitterUtils.getCurrentUserId(getActivity());
		if(user.getId() == currentUserId){
			relation.setText("����͂��Ȃ��ł��I");
			return;
		}
		loadRelationTask.execute(currentUserId, user.getId());
	}
}