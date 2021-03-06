package com.crakac.ofuton.conversation;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.status.StatusDialogFragment;
import com.crakac.ofuton.status.StatusHolder;
import com.crakac.ofuton.status.TweetStatusAdapter;
import com.crakac.ofuton.timeline.BaseStatusActionFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ConversationFragment extends BaseStatusActionFragment {

	private TweetStatusAdapter mAdapter;// statusを保持してlistviewに表示する奴
	private Twitter mTwitter;
	private PullToRefreshListView ptrListView;
	private ListView listView;// 引っ張って更新できるやつの中身
	private View footerView;// 一番下のやつ
	private TextView footerText;
	private ProgressBar footerProgress;
	// private GestureDetector gestureDetector;
	private long nextId = -1l;// ツイートを取得するときに使う．
	private AsyncTask<Void, Void, twitter4j.Status> initTask;
	private LoadConversationTask loadTask;
	private boolean alreadyShown = false;
	private final ConversationFragment selfFragment;
	private static final String TAG = ConversationFragment.class
			.getSimpleName();

	public ConversationFragment() {
		selfFragment = this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);// インスタンスを保持　画面が回転しても取得したTweetが消えなくなる
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// gestureDetector = new GestureDetector(new
		// MyGestureListener(getActivity()));
		View view = inflater.inflate(R.layout.conversation_listfragment, null);

		if (mAdapter == null) {
			mAdapter = new TweetStatusAdapter(getActivity());
		}

		if (mTwitter == null) {
			mTwitter = TwitterUtils.getTwitterInstance(getActivity());
		}
		ptrListView = (PullToRefreshListView) view
				.findViewById(R.id.conversationList);
		listView = ptrListView.getRefreshableView();
		listView.setDivider(getResources().getDrawable(R.color.dark_gray));
		listView.setDividerHeight(1);
		if (footerView == null) {
			footerView = (View) inflater.inflate(R.layout.list_item_footer,
					null);
		}
		footerText = (TextView) footerView.findViewById(R.id.listFooterText);
		footerProgress = (ProgressBar) footerView
				.findViewById(R.id.listFooterProgress);
		if (!alreadyShown) {
			ptrListView
					.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
						@Override
						public void onLastItemVisible() {
							loadPrevious();
						}
					});
			listView.addFooterView(footerView);
			footerView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					loadPrevious();
				}
			});
		}
		listView.setAdapter(mAdapter);

		/*
		 * ダブルタップとかを使うためにGestureDetectorを使う
		 * でもこれだとアイテムじゃなくてListView全体に対してリスナーが作成されるので うまいこと行かない．
		 * listView.setOnTouchListener(new OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) { return
		 * gestureDetector.onTouchEvent(event); } });
		 */
		// registerForContextMenu(listView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView lv = (ListView) parent;
				twitter4j.Status status = (twitter4j.Status) lv
						.getItemAtPosition(position);
				StatusHolder.setStatus(status);
				StatusDialogFragment dialog = new StatusDialogFragment(
						selfFragment);
				dialog.show(getFragmentManager(), "dialog");
			}
		});
		initConversation();
		return view;
	}

	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo
	// menuInfo) {
	// super.onCreateContextMenu(menu, v, menuInfo);
	// AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) menuInfo;
	// menu.setHeaderTitle("Menu");
	// twitter4j.Status tweet = mAdapter.getItem(adapterInfo.position - 1);
	// menu.add("@" + tweet.getUser().getScreenName());
	// menu.add("reply");
	// menu.add("retweet");
	// menu.add("favorite");
	// }

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		if (initTask != null
				&& initTask.getStatus() == AsyncTask.Status.RUNNING) {
			initTask.cancel(true);
		}
		if (loadTask != null
				&& loadTask.getStatus() == AsyncTask.Status.RUNNING) {
			loadTask.cancel(true);
			loadTask = null;
		}
	}

	private void initConversation() {
		if (initTask != null) {
			return;
		}
		initTask = new AsyncTask<Void, Void, twitter4j.Status>() {
			@Override
			protected void onPreExecute() {
				setFooterViewLoading();
				twitter4j.Status status;
				if (StatusHolder.getStatus().isRetweet()) {
					status = StatusHolder.getStatus().getRetweetedStatus();
				} else {
					status = StatusHolder.getStatus();
				}
				mAdapter.add(status);
				nextId = status.getInReplyToStatusId();
			}

			@Override
			protected twitter4j.Status doInBackground(Void... params) {
				return nextTweet(nextId);
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if (result != null) {
					mAdapter.add(result);
					nextId = result.getInReplyToStatusId();
					Log.d(TAG, "nextId:" + nextId);
					if (nextId > 0) {
						setFooterViewStandby();
						loadTask = new LoadConversationTask();
						loadTask.execute();
					} else {
						removeFooterView();
					}
				} else {
					AppUtil.showToast(getActivity(), "何かがおかしいよ");
					setFooterViewStandby();
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				setFooterViewStandby();
			}

		};
		initTask.execute();
	}

	private class LoadConversationTask extends AsyncTask<Void, Void, Status> {

		@Override
		protected void onPreExecute() {
			setFooterViewLoading();
		}

		@Override
		protected twitter4j.Status doInBackground(Void... params) {
			return nextTweet(nextId);
		}

		@Override
		protected void onPostExecute(twitter4j.Status result) {
			if (result != null) {
				mAdapter.add(result);
				mAdapter.notifyDataSetChanged();
				nextId = result.getInReplyToStatusId();
				Log.d(TAG, "nextId:" + nextId);
				if (nextId > 0) {
					loadTask = new LoadConversationTask();
					loadTask.execute();
				} else {
					removeFooterView();
					Log.d(TAG, "正常終了");
				}
			} else {
				AppUtil.showToast(getActivity(), "何かがおかしいよ");
				setFooterViewStandby();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.d(TAG, "onCancelled()");
			setFooterViewStandby();
		}
	}

	void loadPrevious() {
		if (loadTask != null && loadTask.getStatus() == AsyncTask.Status.RUNNING) {
			Log.d(TAG, "loadPrevious() return : loadTask is running.");
			return;
		}
		loadTask = new LoadConversationTask();
		loadTask.execute();
	}

	private void setFooterViewLoading() {
		footerText.setText(getResources().getString(R.string.now_loading));
		footerProgress.setVisibility(View.VISIBLE);
	}

	private void setFooterViewStandby() {
		footerText.setText(getResources().getString(R.string.read_more));
		footerProgress.setVisibility(View.GONE);
	}

	private void removeFooterView() {
		listView.removeFooterView(footerView);
		footerProgress.setVisibility(View.GONE);
		ptrListView.setMode(Mode.DISABLED);
		ptrListView.setOnLastItemVisibleListener(null);
		alreadyShown = true;
	}

	private twitter4j.Status nextTweet(long id) {
		try {
			return mTwitter.showStatus(nextId);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
}