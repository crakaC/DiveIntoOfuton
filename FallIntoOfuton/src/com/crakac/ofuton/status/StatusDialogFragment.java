package com.crakac.ofuton.status;

import java.util.ArrayList;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import com.crakac.fallintoofuton.R;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * �c�C�[�g���^�b�v�������ɏo�Ă���_�C�A���O
 * 
 * @author Kosuke
 * 
 */
public class StatusDialogFragment extends DialogFragment {

	private StatusActionAdapter mActionAdapter;
	private Dialog dialog;

	public static interface ActionSelectListener {
		void onReply();

		void onFav();

		void onRT();

		void onUser();

		void onLink(String url);

		void onMedia(String url);

		void onHashTag(String tag);

		void onConvesation();
	}

	public StatusDialogFragment() {
	}

	public StatusDialogFragment(ActionSelectListener targetFragment) {
		setTargetFragment((Fragment) targetFragment, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.status_dialog, container);
		// �e��A�N�V�������A�_�v�^�ɒǉ����ĕ\��
		mActionAdapter = new StatusActionAdapter(getActivity());

		// ���X�g�r���[���쐬
		// �_�C�A���O�S�̂��ЂƂ̃��X�g�r���[���܂ށD
		// 2�i�c�C�[�g�\���p�C�A�N�V�����\���p�j���ƁC�c�C�[�g���c�ɒ����ƑS��ʕ��̗̈���g���Ă��܂��C�A�N�V������I���ł��Ȃ��Ȃ�
		ListView lvActions = (ListView) view
				.findViewById(R.id.status_action_list);

		// �X�e�[�^�X�\���������쐬�D�^�C�����C�����Ɠ������C�A�E�g�Ȃ̂�TweetStatusAdapter���̏������g���܂킷�D
		View statusView = TweetStatusAdapter.createView(
				StatusHolder.getStatus(), null);
		statusView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		// HeaderView�i�c�C�[�g�X�e�[�^�X�\���p�j��add. setAdapter����ɂ��Ȃ��Ɨ�����
		lvActions.addHeaderView(statusView);
		// �A�_�v�^���Z�b�g
		lvActions.setAdapter(mActionAdapter);
		// reply
		mActionAdapter
				.add(new Pair<String, Integer>(null, StatusConstant.REPLY));
		// retweet
		mActionAdapter.add(new Pair<String, Integer>(null,
				StatusConstant.RETWEET));
		// favorite
		mActionAdapter.add(new Pair<String, Integer>(null, StatusConstant.FAV));

		// conversation�@RT�̏ꍇ���l��
		if (StatusHolder.getStatus().isRetweet()
				&& StatusHolder.getStatus().getRetweetedStatus()
						.getInReplyToScreenName() != null) {
			mActionAdapter.add(new Pair<String, Integer>(null,
					StatusConstant.CONVERSATION));
		} else if (StatusHolder.getStatus().getInReplyToScreenName() != null) {
			mActionAdapter.add(new Pair<String, Integer>(null,
					StatusConstant.CONVERSATION));
		}

		// user, media, url, hashtag entities
		setEntities();
		mActionAdapter.notifyDataSetChanged();

		lvActions.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ActionSelectListener listener = (ActionSelectListener) getTargetFragment();

				ListView lv = (ListView) parent;
				Pair<String, Integer> item = (Pair<String, Integer>) lv
						.getItemAtPosition(position);
				dialog.dismiss();
				int type = item.second;
				if (type == StatusConstant.REPLY) {
					listener.onReply();
				} else if (type == StatusConstant.RETWEET) {
					listener.onRT();
				} else if (type == StatusConstant.FAV) {
					listener.onFav();
				} else if (type == StatusConstant.FAV_AND_RT) {
					listener.onFav();
					listener.onRT();
				} else if (type == StatusConstant.LINK) {
					listener.onLink(item.first);
				} else if (type == StatusConstant.MEDIA) {
					listener.onLink(item.first);
				} else if (type == StatusConstant.HASHTAG) {
					listener.onHashTag(item.first);
				} else if (type == StatusConstant.CONVERSATION) {
					listener.onConvesation();
				}
			}
		});
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        dialog = getDialog();  
        
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
          
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        
        //�c����wrap content�ŁC������92%�ŁD
        int dialogWidth = (int) (metrics.widthPixels * 0.92);  
        //int dialogHeight = (int) (metrics.heightPixels * 1.0);  
          
        lp.width = dialogWidth;
        //lp.height = dialogHeight;
        dialog.getWindow().setAttributes(lp);  
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dialog = new Dialog(getActivity());
		// �^�C�g�������������D�����Ȃ��ƃ_�C�A���O�̕\���ʒu�����ɂ����
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		// ���C�A�E�g��onCreateView�ō����D�̂ŁCdialog.setContentView�͂���Ȃ�
		
		//�S��ʉ�
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

		//�w�i�𓧖���
		dialog.getWindow().setBackgroundDrawable(	new ColorDrawable(Color.TRANSPARENT));
		
		return dialog;
	}

	private void setEntities() {
		twitter4j.Status status = StatusHolder.getStatus();
		twitter4j.Status rtStatus = status.getRetweetedStatus();
		// set user entities
		ArrayList<String> users = new ArrayList<String>();// status�Ɋ֌W����screenName���������ς�����˂�����(@����)
		users.add(status.getUser().getScreenName());// �c�C�[�g�܂��̓��c�C�[�g�����l
		UserMentionEntity[] userMentionEntities = status
				.getUserMentionEntities();
		for (UserMentionEntity user : userMentionEntities) {
			if (!users.contains(user.getScreenName()))
				users.add(user.getScreenName());
		}
		if (status.isRetweet()) {
			UserMentionEntity[] umEntities = rtStatus.getUserMentionEntities();
			for (UserMentionEntity user : umEntities) {
				if (!users.contains(user.getScreenName()))
					users.add(user.getScreenName());
			}
		}
		for (String user : users) {
			mActionAdapter.add(new Pair<String, Integer>("@" + user,
					StatusConstant.USER));
		}

		// set Media entities
		MediaEntity[] mediaEntities;
		ArrayList<String> medias = new ArrayList<String>();
		if (status.isRetweet()) {
			mediaEntities = rtStatus.getMediaEntities();
		} else {
			mediaEntities = status.getMediaEntities();
		}
		for (MediaEntity media : mediaEntities) {
			medias.add(media.getExpandedURL());
			mActionAdapter.add(new Pair<String, Integer>(
					media.getExpandedURL(), StatusConstant.MEDIA));
			Log.d("MediaEntity", media.getExpandedURL());
		}

		// set url entities
		URLEntity[] urlEntities;
		if (status.isRetweet()) {
			urlEntities = rtStatus.getURLEntities();
		} else {
			urlEntities = status.getURLEntities();
		}
		for (URLEntity url : urlEntities) {
			if (!medias.contains(url.getExpandedURL())) {
				mActionAdapter.add(new Pair<String, Integer>(url
						.getExpandedURL(), StatusConstant.LINK));
				Log.d("URLEntity", url.getExpandedURL());
			}
		}

		// set Hashtag entities
		HashtagEntity[] hashtags;
		if (status.isRetweet()) {
			hashtags = rtStatus.getHashtagEntities();
		} else {
			hashtags = status.getHashtagEntities();
		}
		for (HashtagEntity hashtag : hashtags) {
			mActionAdapter.add(new Pair<String, Integer>("#"
					+ hashtag.getText(), StatusConstant.HASHTAG));
		}
	}
}
