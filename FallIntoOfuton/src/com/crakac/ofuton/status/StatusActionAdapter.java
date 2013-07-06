package com.crakac.ofuton.status;

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
 * 
 * @author Kosuke
 * 
 */
public class StatusActionAdapter extends ArrayAdapter<Pair<String, ActionType>> {
	private static final String TAG = StatusActionAdapter.class.getSimpleName();
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
		Pair<String, ActionType> item = getItem(position);
		switch (item.second) {
		case REPLY:
			holder.actionName.setText(mContext.getResources().getString(
					R.string.reply));
			holder.icon.setImageResource(R.drawable.ic_menu_reply);
			break;
		case USER:
			holder.actionName.setText("@" + item.first);// スクリーンネームの最初に@をつけて表示
			holder.icon.setImageResource(R.drawable.ic_menu_user);
			break;

		case CONVERSATION:
			holder.actionName.setText(mContext.getResources().getString(
					R.string.show_conversation));
			holder.icon.setImageResource(R.drawable.ic_menu_conversation);
			break;

		case RETWEET:
			if (StatusHolder.getStatus().isRetweetedByMe()) {
				holder.actionName.setText(mContext.getResources().getString(
						R.string.unretweet));
			} else {
				holder.actionName.setText(mContext.getResources().getString(
						R.string.retweet));
			}
			holder.icon.setImageResource(R.drawable.ic_menu_retweet);
			break;

		case FAV:
			if (StatusHolder.getStatus().isFavorited()) {
				holder.actionName.setText(mContext.getResources().getString(
						R.string.unfavorite));
				holder.icon.setImageResource(R.drawable.ic_menu_unfav);
			} else {
				holder.actionName.setText(mContext.getResources().getString(
						R.string.favorite));
				holder.icon.setImageResource(R.drawable.ic_menu_favorite);
			}
			break;

		case LINK:
			holder.actionName.setText(item.first);
			holder.icon.setImageResource(R.drawable.ic_menu_link);
			break;
		case MEDIA:
			holder.actionName.setText(item.first);
			holder.icon.setImageResource(R.drawable.ic_menu_media);
			break;

		case HASHTAG:
			holder.actionName.setText(item.first);
			holder.icon.setImageResource(R.drawable.ic_menu_hashtag);
			break;

		default:
			Log.d(TAG, "unknown case " + item.second);
			break;
		}
		return convertView;
	}
}