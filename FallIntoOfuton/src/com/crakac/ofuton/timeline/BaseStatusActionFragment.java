package com.crakac.ofuton.timeline;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.crakac.ofuton.TweetActivity;
import com.crakac.ofuton.conversation.ShowConversationActivity;
import com.crakac.ofuton.status.StatusDialogFragment.ActionSelectListener;
import com.crakac.ofuton.status.StatusHolder;
import com.crakac.ofuton.status.TweetStatusAdapter;
import com.crakac.ofuton.user.UserDetailActivity;
import com.crakac.ofuton.util.AppUtil;

public abstract class BaseStatusActionFragment extends Fragment implements ActionSelectListener {

	private TweetStatusAdapter mAdapter;// statusを保持してlistviewに表示する奴
	private Twitter mTwitter;
	AsyncTask<Void, Void, twitter4j.Status> favTask, rtTask;

	@Override
	public void onReply() {
		Intent intent = new Intent(getActivity(),
				TweetActivity.class);
		intent.putExtra("replyId", StatusHolder.getStatus().getId());
		intent.putExtra("replyName", StatusHolder.getStatus()
				.getUser().getScreenName());
		startActivity(intent);
	}
	@Override
	public void onFav() {
		if(favTask != null && favTask.getStatus() == AsyncTask.Status.RUNNING ){
			return;
		}
		favTask = new AsyncTask<Void, Void, twitter4j.Status>() {
			twitter4j.Status status;
			boolean doUnfav = false;			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				status = StatusHolder.getStatus();
				if(status.isFavorited())
					doUnfav = true;
			}
			@Override
			protected twitter4j.Status doInBackground(Void... params) {
				try {
					if(doUnfav){
						return mTwitter.destroyFavorite(status.getId());
					} else {
						return mTwitter.createFavorite(status.getId());
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if(result == null){
					AppUtil.showToast(getActivity(), "無理でした");
				} else {
					if(doUnfav){
						AppUtil.showToast(getActivity(), "あんふぁぼしました");
					} else {
						AppUtil.showToast(getActivity(), "お気に入りに追加しました");
					}
					int pos = mAdapter.getPosition(status);
					mAdapter.remove(status);
					mAdapter.insert(result, pos);
					mAdapter.notifyDataSetChanged();
				}
			}
		};
		favTask.execute();
	}

	@Override
	public void onRT() {
		if(rtTask != null && rtTask.getStatus() == AsyncTask.Status.RUNNING ){
			return;
		}
		rtTask = new AsyncTask<Void, Void, twitter4j.Status>() {
			@Override
			protected twitter4j.Status doInBackground(Void... params) {
				try {
					return mTwitter.retweetStatus(StatusHolder.getStatus().getId());
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if(result == null){
					AppUtil.showToast(getActivity(), "無理でした");
				} else {
					AppUtil.showToast(getActivity(), "リツイートしました");
				}
			}
		};
		rtTask.execute();
	}
	@Override
	public void onUser(String screenName) {
		Intent intent = new Intent(getActivity(), UserDetailActivity.class);
		intent.putExtra("screenName", screenName);
		startActivity(intent);
	}
	@Override
	public void onLink(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri
				.parse(url));
		startActivity(intent);
	}
	@Override
	public void onMedia(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri
				.parse(url));
		startActivity(intent);
	}
	@Override
	public void onHashTag(String tag) {
		Intent intent = new Intent(getActivity(),
				TweetActivity.class);
		intent.putExtra("hashTag", tag);
		startActivity(intent);
	}
	@Override
	public void onConvesation() {
		startActivity(new Intent(getActivity(), ShowConversationActivity.class));
	}
}