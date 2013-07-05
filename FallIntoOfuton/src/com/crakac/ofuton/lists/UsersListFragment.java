package com.crakac.ofuton.lists;

import java.util.List;
import java.util.TreeSet;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ProgressDialogFragment;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;

public class UsersListFragment extends Fragment{
	
	private static final String TAG = UsersListFragment.class.getSimpleName();
	private ListAdapter mAdapter;
	private FragmentManager manager;
	private TreeSet<Integer> currentListIds, initialListIds;
	private AsyncTask<Void, Void, List<twitter4j.UserList>> loadListTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		//リストに変更があったか確かめるために，最初の時点でのリストのID一覧を持ってくる
		initialListIds = new TreeSet<Integer>();
		for(TwitterList list : TwitterUtils.getCurrentUserLists(getActivity())){
			initialListIds.add(list.getListId());
		}
		currentListIds = new TreeSet<Integer>(initialListIds);
		ListObserver.init();
		
		manager = getActivity().getSupportFragmentManager();
		View view = inflater.inflate(R.layout.base_listfragment, container, false);
		mAdapter = new ListAdapter(getActivity());
		ListView lv = (ListView)view.findViewById(R.id.listView);
		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			ImageView checkMark;
			TwitterList list;
			AsyncTask<Void, Void, Boolean>addTask, removeTask;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				ListView lv = (ListView)parent;
				list = (TwitterList)lv.getItemAtPosition(pos);
				checkMark = (ImageView)view.findViewById(R.id.checkMark);
				if(currentListIds.contains(list.getListId())){
					removeList();
				} else {
					addList();
				}
			}

			private void checkListSelectionDiffs() {
				if(currentListIds.size() != initialListIds.size() || !currentListIds.containsAll(initialListIds)){
					ListObserver.changed();
				} else {
					ListObserver.notChanged();
				}
			}

			private void addList() {
				if(addTask != null && addTask.getStatus() == AsyncTask.Status.RUNNING){
					return;
				}
				addTask = new AsyncTask<Void, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Void... params) {
						return TwitterUtils.addList(getActivity(), list);
					}
					@Override
					protected void onPostExecute(Boolean result) {
						if(result){
							checkMark.setVisibility(View.VISIBLE);
							currentListIds.add(list.getListId());
							checkListSelectionDiffs();
						} else {
							AppUtil.showToast(getActivity(), getActivity().getString(R.string.something_wrong));
						}
					}
				};
				addTask.execute();
			}

			private void removeList() {
				if(removeTask != null && removeTask.getStatus() == AsyncTask.Status.RUNNING){
					return;
				}
				removeTask = new AsyncTask<Void, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Void... params) {
						return TwitterUtils.removeList(getActivity(), list);
					}
					@Override
					protected void onPostExecute(Boolean result) {
						if(result){
							checkMark.setVisibility(View.INVISIBLE);
							currentListIds.remove(Integer.valueOf(list.getListId()));
							checkListSelectionDiffs();
						} else {
							AppUtil.showToast(getActivity(), getActivity().getString(R.string.something_wrong));
						}
					}
				};
				removeTask.execute();
			}
		});
		loadList();
		return view;
	}
	
	
	
	@Override
	public void onPause() {
		super.onPause();
		if(loadListTask != null && loadListTask.getStatus() == AsyncTask.Status.RUNNING){
			loadListTask.cancel(true);
			loadListTask = null;
		}
	}



	private class ListAdapter extends ArrayAdapter<TwitterList>{
		private LayoutInflater mInflater;
		
		public ListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mInflater = (LayoutInflater) context
			.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.user_list_listitem, parent, false);
			}
			ImageView check = (ImageView) convertView.findViewById(R.id.checkMark);
			TextView listName = (TextView) convertView.findViewById(R.id.listName);
			
			TwitterList item = getItem(position);

			//FragmentのonCreateViewでcurrentListIdsは取得済
			if(currentListIds.contains(item.getListId())){
				check.setVisibility(View.VISIBLE);
			} else {
				check.setVisibility(View.INVISIBLE);
			}
			listName.setText(item.getName());
			return convertView;
		}
	}

	void loadList(){
		loadListTask = new AsyncTask<Void, Void, List<twitter4j.UserList>>(){
			ProgressDialogFragment progressDialog;
			Twitter twitter;
			@Override
			protected void onPreExecute() {
				//ダイアログ表示，リストのデータ取得用Twitterインスタンスの生成
				progressDialog = ProgressDialogFragment.newInstance(getString(R.string.loading_list));
				progressDialog.show(manager, "progress");
				twitter = TwitterUtils.getTwitterInstance(getActivity());
			}

			@Override
			protected List<twitter4j.UserList> doInBackground(Void... params) {
				try {
					return twitter.getUserLists(twitter.getId());
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(List<twitter4j.UserList> lists) {
				progressDialog.dismiss();
				if(lists != null){
					long userId = TwitterUtils.getCurrentUserId(getActivity());//リスト選ぶんだから現在のユーザでおｋ
					for(UserList list : lists){
						mAdapter.add(new TwitterList(userId, list.getId(), list.getName(), list.getFullName()));
					}
				}
			}
		};
		loadListTask.execute();
	}
}