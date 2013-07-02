package com.crakac.fallintoofuton.status;

import java.text.SimpleDateFormat;

import twitter4j.Status;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crakac.fallintoofuton.R;
import com.crakac.fallintoofuton.util.AppUtil;
import com.crakac.fallintoofuton.util.TwitterUtils;
import com.loopj.android.image.SmartImageView;

public class TweetStatusAdapter extends ArrayAdapter<twitter4j.Status> {
	private static Context mContext;
	private static LayoutInflater mInflater;
	private static long NOT_SET = -1l;
	private static long userId = NOT_SET;
	private static String userScreenName = null;
	private static final String TAG = TweetStatusAdapter.class.getSimpleName();

	private static class ViewHolder {
		TextView name;
		TextView text;
		TextView postedAt;
		TextView retweetedBy;
		TextView postedAtRt;
		SmartImageView icon;
		SmartImageView smallIcon;
		ImageView favicon;
	}

	public TweetStatusAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_1);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Status item = getItem(position);
		return createView(item, convertView);
	}

	public static View createView(Status item, View convertView){
		Status origItem = item;
		ViewHolder holder;
		
		//set userId and userScreenName to detect reply from status.text
		if(userId == NOT_SET || userScreenName == null){
			userId = TwitterUtils.getUserId(mContext);
			userScreenName = TwitterUtils.getUserScreenName(mContext);
			Log.d(TAG, "get user id " + userId);
			Log.d(TAG, "get user's screen name " + userScreenName);
		}

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_tweet, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.postedAt = (TextView) convertView
					.findViewById(R.id.posted_at);
			holder.postedAtRt = (TextView) convertView
					.findViewById(R.id.posted_at_rt);
			holder.icon = (SmartImageView) convertView.findViewById(R.id.icon);
			holder.smallIcon = (SmartImageView) convertView
					.findViewById(R.id.small_icon);
			holder.retweetedBy = (TextView) convertView
					.findViewById(R.id.retweeted_by);
			holder.favicon = (ImageView)convertView.findViewById(R.id.favedStar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// View leftbar = (View) convertView.findViewById(R.id.leftbar);
		// leftbar.setVisibility(View.INVISIBLE);

		if (item.isRetweet()) {
			item = item.getRetweetedStatus();
			convertView.setBackgroundResource(R.color.retweet_background);
			holder.postedAt.setVisibility(View.GONE);
			holder.name.setTextColor(mContext.getResources().getColor(
					R.color.droid_green));
			sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			holder.postedAtRt.setText(sdf.format(item.getCreatedAt())
					+ " Retweeted by");
			holder.postedAtRt.setVisibility(View.VISIBLE);

			holder.icon.setImageUrl(item.getUser().getProfileImageURL());
			holder.smallIcon.setImageUrl(origItem.getUser()
					.getProfileImageURL());
			holder.smallIcon.setVisibility(View.VISIBLE);
			holder.retweetedBy.setText(origItem.getUser().getScreenName() + "("
					+ origItem.getRetweetCount() + ")");
			holder.retweetedBy.setVisibility(View.VISIBLE);
			
			if (item.isFavorited() || origItem.isFavorited()){
				holder.favicon.setVisibility(View.VISIBLE);
				Log.d("TweetStatusAdapter",item.getText() + " is faved");
			} else {
				holder.favicon.setVisibility(View.GONE);
			}
		} else {
			String source = item.getSource();
			if (source.indexOf(">") != -1) {
				source = source.substring(source.indexOf(">") + 1,
						source.indexOf("</"));
			}
			sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			holder.postedAt.setText(sdf.format(item.getCreatedAt()) + " via "
					+ source);
			holder.postedAt.setVisibility(View.VISIBLE);
			holder.icon.setImageUrl(item.getUser().getProfileImageURL());
			holder.postedAtRt.setVisibility(View.GONE);
			holder.smallIcon.setVisibility(View.GONE);
			holder.retweetedBy.setVisibility(View.GONE);
			
			// change colors : reply and retweet
			if ( (userId != NOT_SET && userScreenName != null) && 
					(item.getInReplyToUserId() == userId || item.getText().contains("@" + userScreenName)) ) {
				// mention
				convertView.setBackgroundResource(R.color.mention_background);
				holder.name.setTextColor(mContext.getResources().getColor(
						R.color.droid_red));
			} else if ( item.getUser().getId() == userId ) {
				//user's own tweet
				convertView.setBackgroundResource(R.color.mytweet_background);
				holder.name.setTextColor(mContext.getResources().getColor(
						R.color.droid_blue));
			} else {
				// nomal tweet
				convertView.setBackgroundResource(R.color.timeline_background);
				holder.name.setTextColor(mContext.getResources().getColor(
						R.color.droid_blue));
			}
			if (item.isFavorited()){
				holder.favicon.setVisibility(View.VISIBLE);
			} else {
				holder.favicon.setVisibility(View.GONE);
			}
		}
		holder.name.setText(item.getUser().getName() + " @"
				+ item.getUser().getScreenName());
		holder.text.setText(item.getText());
		final Status status = origItem;
		holder.icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//AppUtil.showToast(mContext, userName);
				AppUtil.showToast(mContext, String.valueOf(status.getUser().getScreenName() + '\n' +status.getId() + '\n'  + String.valueOf(status.isFavorited())));
				if(status.isRetweet())
				AppUtil.showToast(mContext, String.valueOf(status.getRetweetedStatus().getUser().getScreenName() + '\n' + status.getRetweetedStatus().getId()) +'\n'+ String.valueOf(status.getRetweetedStatus().isFavorited()));
			}
		});
		return convertView;
	}
}