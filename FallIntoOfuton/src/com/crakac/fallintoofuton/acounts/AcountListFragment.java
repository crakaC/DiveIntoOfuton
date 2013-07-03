package com.crakac.fallintoofuton.acounts;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crakac.fallintoofuton.R;
import com.crakac.fallintoofuton.util.AppUtil;
import com.crakac.fallintoofuton.util.TwitterUtils;

public class AcountListFragment extends Fragment{
	
	ClickFooterListner listener;

	public interface ClickFooterListner{
		public void onClickFooter();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.acount_listfragment, container, false);
		AcountAdapter adapter = new AcountAdapter(getActivity());
		ListView lv = (ListView)view.findViewById(R.id.acountList);
		View footerView = inflater.inflate(R.layout.acount_footer, null);

		listener = (ClickFooterListner)getActivity();
		footerView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				listener.onClickFooter();
			}
		});
		lv.addFooterView(footerView);
		lv.setEmptyView(footerView);
		adapter.add(new Acount(TwitterUtils.getUserId(getActivity()), TwitterUtils.getUserScreenName(getActivity())));
		lv.setAdapter(adapter);
		return view;
	}
	
	private class Acount{
		String screenName;
		String ImageUrl;
		long userId;
		Acount(long id, String name){
			userId = id;
			screenName = name;
		}
	}

	private class AcountAdapter extends ArrayAdapter<Acount>{
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
			//SmartImageView icon = (SmartImageView) convertView.findViewById(R.id.acountIcon);
			ImageView check = (ImageView) convertView.findViewById(R.id.checkMark);
			ImageView remove = (ImageView) convertView.findViewById(R.id.remove);
			TextView screenName = (TextView) convertView.findViewById(R.id.acountName);
			//icon.setImageUri(uri);
			final Acount item = getItem(position);
			screenName.setText(item.screenName);
			if(item.userId == TwitterUtils.getUserId(mContext)){
				check.setVisibility(View.VISIBLE);
			} else {
				check.setVisibility(View.GONE);
			}

			remove.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					TwitterUtils.removeUser(item.userId);//TODO é¿ëïÇµÇÊÇ§
					AppUtil.showToast(mContext, "Ç‹Çæè¡ÇπÇ»Ç¢");
				}
			});

			return convertView;
		}
	}
}