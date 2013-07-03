package com.crakac.ofuton.timeline;

import java.util.List;
import java.util.ListIterator;

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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseTimelineFragment extends Fragment implements ActionSelectListener {

	private TweetStatusAdapter mAdapter;// status��ێ�����listview�ɕ\������z
	private Twitter mTwitter;
	private PullToRefreshListView ptrListView;// ���������čX�V�ł�����
	private ListView listView;// ���������čX�V�ł����̒��g
	private View footerView, emptyView;// ��ԉ��̂��,�ŏ��̂��
	private TextView emptyText;
	private ProgressBar emptyProgress;
	// private GestureDetector gestureDetector;
	private long sinceId = -1l, maxId = -1l;// �c�C�[�g���擾����Ƃ��Ɏg���D
	AsyncTask<Void, Void, List<twitter4j.Status>> initTask, loadNewTask,
			loadPreviousTask;
	AsyncTask<Void, Void, twitter4j.Status> favTask, rtTask;
	private final BaseTimelineFragment selfFragment;
	private static final String TAG = BaseTimelineFragment.class.getSimpleName();

	public BaseTimelineFragment(){
		selfFragment = this;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);// �C���X�^���X��ێ��@��ʂ���]���Ă��擾����Tweet�������Ȃ��Ȃ�
		setHasOptionsMenu(true);//�I�v�V�������j���[���t���O�����g����ǉ�
	}

	@Override
	// onCreateView��pager��2�ׂ�fragment����ׂ�fragment�Ɉڂ��Ă����Ƃ��ɌĂ΂��D
	// �\�����邽�߂�View���쐬����C���[�W�ŁD
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// gestureDetector = new GestureDetector(new
		// MyGestureListener(getActivity()));
		View view = inflater.inflate(R.layout.tweet_lists, null);

		if (mAdapter == null) {
			mAdapter = new TweetStatusAdapter(getActivity());
		}
		
		if (mTwitter == null){
			mTwitter = TwitterUtils.getTwitterInstance(getActivity());
		}

		//���������čX�V�ł�����
		ptrListView = (PullToRefreshListView) view.findViewById(R.id.listView1);
		ptrListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				loadNewTweets();
			}
		});
		ptrListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
					@Override
					public void onLastItemVisible() {
						loadPreviousTweets();
					}
				});
		//���g��ListView
		listView = ptrListView.getRefreshableView();
		listView.setFastScrollEnabled(true);
		listView.setScrollingCacheEnabled(false);//�X�N���[�����̂�����h�~
		listView.setDivider(getResources().getDrawable(R.color.dark_gray));
		listView.setDividerHeight(1);
		if (footerView == null) {
			footerView = (View) inflater.inflate(R.layout.list_item_footer,
					null);
		}
		listView.addFooterView(footerView);
		footerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadPreviousTweets();
			}
		});
		if (emptyView == null) {
			emptyView = (View) inflater.inflate(R.layout.list_item_empty, null);
		}
		emptyText = (TextView) emptyView.findViewById(R.id.emptyText);
		emptyProgress = (ProgressBar) emptyView
				.findViewById(R.id.emptyProgress);
		emptyView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				initTimeline();
			}
		});
		listView.setEmptyView(emptyView);
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
				StatusDialogFragment dialog = new StatusDialogFragment(selfFragment);
				dialog.show(getFragmentManager(), "dialog");
			}
		});
		initTimeline();
		return view;
	}

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo) menuInfo;
//		menu.setHeaderTitle("Menu");
//		twitter4j.Status tweet = mAdapter.getItem(adapterInfo.position - 1);
//		menu.add("@" + tweet.getUser().getScreenName());
//		menu.add("reply");
//		menu.add("retweet");
//		menu.add("favorite");
//	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.timeline, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_refresh :
			mAdapter.clear();
			mAdapter.notifyDataSetChanged();
			initialStatuses();
		}
		return super.onOptionsItemSelected(item);
	}

	private void initTimeline() {
		if (initTask != null
				&& initTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		if (mAdapter.getCount() > 0) {
			ptrListView.setMode(Mode.PULL_FROM_START);
			Log.d(TAG, "mAdapter has items." );
			return;
		}
		initTask = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if (emptyView.isEnabled()) {
					setEmptyViewLoading();
				}
			}

			@Override
			protected List<twitter4j.Status> doInBackground(Void... params) {
				return initialStatuses();
			}

			@Override
			protected void onPostExecute(List<twitter4j.Status> result) {
				if (result != null) {
					for (twitter4j.Status status : result) {
						mAdapter.add(status);
					}
					if (result.size() > 0) {
						maxId = result.listIterator(result.size()).previous()
								.getId();
						sinceId = result.iterator().next().getId();
						mAdapter.notifyDataSetChanged();
					}
					ptrListView.setMode(Mode.PULL_FROM_START);
				} else {
					AppUtil.showToast(getActivity(), "fail to get Tilmeline");
					setEmptyViewStandby();
				}
			}
		};
		initTask.execute();
	}

	private void loadNewTweets() {
		if (loadNewTask != null
				&& loadNewTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		loadNewTask = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
			
			@Override
			protected List<twitter4j.Status> doInBackground(Void... params) {
				return newStatuses(sinceId, 200);
			}

			@Override
			protected void onPostExecute(List<twitter4j.Status> result) {
				if (result != null) {
					int lastPos = listView.getFirstVisiblePosition();//�V����status�ǉ��O�̈�ԏ�̃|�W�V������ێ�
					for (ListIterator<twitter4j.Status> ite = result
							.listIterator(result.size()); ite.hasPrevious();) {
						mAdapter.insert(ite.previous(), 0);
					}
					if (result.size() > 0) {
						sinceId = result.iterator().next().getId();
						mAdapter.notifyDataSetChanged();
						
						//����܂肤�܂����Ƃ����ĂȂ����ۂ��D
						listView.setSelection(lastPos + result.size());//�ǉ����������炷
					}
				} else {
					AppUtil.showToast(getActivity(), "fail to get Tilmeline");
				}
				ptrListView.onRefreshComplete();
			}
		};
		loadNewTask.execute();
	}

	private void loadPreviousTweets() {
		if (loadPreviousTask != null
				&& loadPreviousTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		loadPreviousTask = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
			TextView tv;
			ProgressBar pb;
			String readMore;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				tv = (TextView) footerView.findViewById(R.id.listFooterText);
				tv.setText(getResources().getString(R.string.now_loading));
				pb = (ProgressBar) footerView
						.findViewById(R.id.listFooterProgress);
				pb.setVisibility(View.VISIBLE);
				readMore = getResources().getString(R.string.read_more);
			}

			@Override
			protected List<twitter4j.Status> doInBackground(Void... params) {
				return previousStatuses(maxId, 50);
			}

			@Override
			protected void onPostExecute(List<twitter4j.Status> result) {
				if (result != null) {
					for (twitter4j.Status status : result) {
						mAdapter.add(status);
					}
					if (result.size() > 0) {
						maxId = result.listIterator(result.size()).previous()
								.getId();
						mAdapter.notifyDataSetChanged();
					}
				} else {
					failToGetStatuses();
				}
				tv.setText(readMore);
				pb.setVisibility(View.GONE);
			}
		};
		loadPreviousTask.execute();
	}

	private void setEmptyViewLoading() {
		emptyText.setText(getResources().getString(R.string.now_loading));
		emptyProgress.setVisibility(View.VISIBLE);
		ptrListView.setMode(Mode.DISABLED);
	}

	private void setEmptyViewStandby() {
		if (emptyText != null && emptyProgress != null)
			emptyText.setText(getResources().getString(R.string.tap_to_reload));
		emptyProgress.setVisibility(View.GONE);
	}

	/**
	 * ��ԍŏ��ɌĂԓz�D
	 * 
	 * @return
	 */
	abstract List<twitter4j.Status> initialStatuses();

	/**
	 * �X�V����Ƃ��ɌĂԂ��
	 * 
	 * @param sinceId
	 * @param count
	 * @return
	 */
	abstract List<twitter4j.Status> newStatuses(long sinceId, int count);

	/**
	 * �Â��c�C�[�g���擾����Ƃ��ɌĂԓz
	 * 
	 * @param maxId
	 * @param count
	 * @return
	 */
	abstract List<twitter4j.Status> previousStatuses(long maxId, int count);

	/**
	 * �擾�Ɏ��s�������ɂ�Ԃ��
	 * 
	 */
	abstract void failToGetStatuses();

	/**
	 * FragmentPagerAdapter�ɓn���ă^�C�g����\�����邽�߂̂��
	 * 
	 * @return
	 */
	public abstract String getTimelineName();

	@Override
	public void onReply() {
		// TODO Auto-generated method stub
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
					AppUtil.showToast(getActivity(), "�����ł���");
				} else {
					if(doUnfav){
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
					AppUtil.showToast(getActivity(), "�����ł���");
				} else {
					AppUtil.showToast(getActivity(), "���c�C�[�g���܂���");
				}
			}
		};
		rtTask.execute();
	}
	@Override
	public void onUser() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
	}
}