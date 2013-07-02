package com.crakac.fallintoofuton.status;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crakac.fallintoofuton.R;

/**
 * ツイートをタップした時に出てくるダイアログのメニュー部分
 * @author Kosuke
 *
 */
public class StatusActionAdapter extends ArrayAdapter<Pair<String, Integer>> {
	private Context mContext;
	private LayoutInflater mInflater;

	private static class ViewHolder {
		TextView actionName;
		ImageView icon;
	}

	public StatusActionAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_1);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.status_action_item, null);
			holder = new ViewHolder();
			holder.actionName = (TextView) convertView
					.findViewById(R.id.action_name);
			holder.icon = (ImageView) convertView
					.findViewById(R.id.action_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Pair<String, Integer> item = getItem(position);
		int type = item.second;
		if (type == StatusConstant.REPLY) {
			holder.actionName.setText(mContext.getResources().getString(
					R.string.reply));
			holder.icon.setImageResource(R.drawable.ic_menu_reply);
		} else if (type == StatusConstant.USER) {
			holder.actionName.setText(item.first);
			holder.icon.setImageResource(R.drawable.ic_menu_user);
		} else if (type == StatusConstant.CONVERSATION) {
			holder.actionName.setText(mContext.getResources().getString(R.string.show_conversation));
			holder.icon.setImageResource(R.drawable.ic_menu_conversation);
		} else if (type == StatusConstant.RETWEET) {
			if(StatusHolder.getStatus().isRetweetedByMe()){
				holder.actionName.setText(mContext.getResources().getString(R.string.unretweet));
			} else {
				holder.actionName.setText(mContext.getResources().getString(R.string.retweet));
			}
			holder.icon.setImageResource(R.drawable.ic_menu_retweet);
		} else if (type == StatusConstant.FAV) {
			if( StatusHolder.getStatus().isFavorited() ){
				holder.actionName.setText(mContext.getResources().getString(
						R.string.unfavorite));
				holder.icon.setImageResource(R.drawable.ic_menu_unfav);
			} else {
				holder.actionName.setText(mContext.getResources().getString(
						R.string.favorite));
				holder.icon.setImageResource(R.drawable.ic_menu_favorite);
			}
		} else if (type == StatusConstant.LINK) {
			holder.actionName.setText(item.first);
			holder.icon.setImageResource(R.drawable.ic_menu_link);
		} else if (type == StatusConstant.MEDIA) {
			holder.actionName.setText(item.first);
			holder.icon.setImageResource(R.drawable.ic_menu_media);
		} else if (type == StatusConstant.HASHTAG) {
			holder.actionName.setText(item.first);
			holder.icon.setImageResource(R.drawable.ic_menu_hashtag);
		}
		return convertView;
	}
}