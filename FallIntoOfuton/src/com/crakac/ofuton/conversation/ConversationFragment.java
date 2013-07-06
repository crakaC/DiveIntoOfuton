package com.crakac.ofuton.conversation;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.TweetActivity;
import com.crakac.ofuton.status.StatusDialogFragment;
import com.crakac.ofuton.status.StatusHolder;
import com.crakac.ofuton.status.TweetStatusAdapter;
import com.crakac.ofuton.status.StatusDialogFragment.ActionSelectListener;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConversationFragment extends Fragment implements
		ActionSelectListener {

	private TweetStatusAdapter mAdapter;// status��ێ�����listview�ɕ\������z
	private Twitter mTwitter;
	private PullToRefreshListView ptrListView;
	private ListView listView;// ���������čX�V�ł����̒��g
	private View footerView;// ��ԉ��̂��
	private TextView footerText;
	private ProgressBar footerProgress;
	// private GestureDetector gestureDetector;
	private long nextId = -1l;// �c�C�[�g���擾����Ƃ��Ɏg���D
	private AsyncTask<Void, Void, twitter4j.Status> initTask;
	private AsyncTask<Void, Void, twitter4j.Status> favTask, rtTask;
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
		setRetainInstance(true);// �C���X�^���X��ێ��@��ʂ���]���Ă��擾����Tweet�������Ȃ��Ȃ�
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
		 * �_�u���^�b�v�Ƃ����g�����߂�GestureDetector���g��
		 * �ł����ꂾ�ƃA�C�e������Ȃ���ListView�S�̂ɑ΂��ă��X�i�[���쐬�����̂� ���܂����ƍs���Ȃ��D
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
					AppUtil.showToast(getActivity(), "����������������");
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
					Log.d(TAG, "����I��");
				}
			} else {
				AppUtil.showToast(getActivity(), "����������������");
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

	/************** �ȉ���BaseTimelineFragment�Ɠ���(onConversation()������) *****/

	@Override
	public void onReply() {
		Intent intent = new Intent(getActivity(), TweetActivity.class);
		intent.putExtra("replyId", StatusHolder.getStatus().getId());
		intent.putExtra("replyName", StatusHolder.getStatus().getUser()
				.getScreenName());
		startActivity(intent);
	}

	@Override
	public void onFav() {
		if (favTask != null && favTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		favTask = new AsyncTask<Void, Void, twitter4j.Status>() {
			twitter4j.Status status;
			boolean doUnfav = false;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				status = StatusHolder.getStatus();
				if (status.isFavorited())
					doUnfav = true;
			}

			@Override
			protected twitter4j.Status doInBackground(Void... params) {
				try {
					if (doUnfav) {
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
				if (result == null) {
					AppUtil.showToast(getActivity(), "�����ł���");
				} else {
					if (doUnfav) {
						AppUtil.showToast(getActivity(), "����ӂ��ڂ��܂���");
					} else {
						AppUtil.showToast(getActivity(), "���C�ɓ���ɒǉ����܂���");
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
		if (rtTask != null && rtTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		rtTask = new AsyncTask<Void, Void, twitter4j.Status>() {
			@Override
			protected twitter4j.Status doInBackground(Void... params) {
				try {
					return mTwitter.retweetStatus(StatusHolder.getStatus()
							.getId());
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if (result == null) {
					AppUtil.showToast(getActivity(), "�����ł���");
				} else {
					AppUtil.showToast(getActivity(), "���c�C�[�g���܂���");
				}
			}
		};
		rtTask.execute();
	}

	@Override
	public void onUser(String screenName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLink(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	@Override
	public void onMedia(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	@Override
	public void onHashTag(String tag) {
		Intent intent = new Intent(getActivity(), TweetActivity.class);
		intent.putExtra("hashTag", tag);
		startActivity(intent);
	}

	@Override
	public void onConvesation() {
		// TODO Auto-generated method stub
	}
}