package com.crakac.ofuton.util;

import twitter4j.auth.AccessToken;

public class User {
	private long userId;
	private String screenName;
	private String iconUrl;
	private String token;
	private String tokenSecret;
	private boolean isCurrent;
	
	public User(long id, String name, String url, String token, String secret, boolean current){
		userId = id;
		screenName = name;
		iconUrl = url;
		this.token = token;
		tokenSecret = secret;
		isCurrent = current;
	}
	public long getUserId() {
		return userId;
	}
	public String getScreenName() {
		return screenName;
	}
	public String getIconUrl() {
		return iconUrl;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public boolean IsCurrent() {
		return isCurrent;
	}
}
