package com.crakac.ofuton.status;

/**
 * ツイートをタップした時に出てくるダイアログに使う定数
 * @author Kosuke
 *
 */
//public class StatusConstant {
//	public static final int REPLY = 0;
//	public static final int USER = 1;
//	public static final int RETWEET = 2;
//	public static final int FAV = 3;
//	public static final int FAV_AND_RT = 4;
//	public static final int LINK = 5;
//	public static final int SHARE = 6;
//	public static final int BUSTER = 7;
//	public static final int MEDIA = 8;
//	public static final int HASHTAG = 9;
//	public static final int CONVERSATION = 10;
//}

public enum ActionType{
	REPLY,
	USER,
	RETWEET,
	FAV,
	FAV_AND_RT,
	LINK,
	SHARE,
	BUSTER,
	MEDIA,
	HASHTAG,
	CONVERSATION,
}