package com.crakac.ofuton.acounts;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crakac.fallintoofuton.R;
import com.crakac.ofuton.MainActivity;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.User;
import com.crakac.ofuton.util.UserDBAdapter;
import com.loopj.android.image.SmartImageView;

public class AcountListFragment extends Fragment{
	
	private static final String TAG = AcountListFragment.class.getSimpleName();
	ClickFooterListner listener;
	AcountAdapter mAdapter;

	public interface ClickFooterListner{
		public void onClickFooter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.acount_listfragment, container, false);
		mAdapter = new AcountAdapter(getActivity());
		ListView lv = (ListView)view.findViewById(R.id.acountList);
		listener = (ClickFooterListner)getActivity();
		View footerView = inflater.inflate(R.layout.acount_footer, null);
		footerView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				listener.onClickFooter();
			}
		});
		lv.addFooterView(footerView);
		
		/****************テスト用****************************/
//		View headerView = inflater.inflate(R.layout.acount_footer, null);
//		headerView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});
//		lv.addHeaderView(headerView);
		/***********************************************************************/		

		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				ListView lv = (ListView)parent;
				User user = (User)lv.getItemAtPosition(pos);
				TwitterUtils.setCurrentUser(getActivity(), user);
				getActivity().finish();
				startActivity(new Intent(getActivity(), MainActivity.class));
				
			}
		});
		reloadAcounts();
		return view;
	}
	
	private class AcountAdapter extends ArrayAdapter<User>{
		LayoutInflater mInflater;
		Context mContext;
		public AcountAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mContext = context;
			mInflater = (LayoutInflater) context
			.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.acount_listitem, parent, false);
			}
			SmartImageView icon = (SmartImageView) convertView.findViewById(R.id.acountIcon);
			ImageView check = (ImageView) convertView.findViewById(R.id.checkMark);
			ImageView remove = (ImageView) convertView.findViewById(R.id.remove);
			TextView screenName = (TextView) convertView.findViewById(R.id.acountName);

			final User item = getItem(position);
			icon.setImageUrl(item.getIconUrl());
			screenName.setText(item.getScreenName());
			if(item.IsCurrent()){
				check.setVisibility(View.VISIBLE);
				remove.setVisibility(View.GONE);
			} else {
				check.setVisibility(View.GONE);
				remove.setVisibility(View.VISIBLE);
			}

			remove.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					TwitterUtils.removeUser(item);
				}
			});
			return convertView;
		}
	}

	public void reloadAcounts() {
		Log.d(TAG, "called reloadAcounts");
		mAdapter.clear();
		List<User> users = TwitterUtils.getUsers(getActivity());
		for(User user : users){
			mAdapter.add(user);
		}
		mAdapter.notifyDataSetChanged();
	}
}