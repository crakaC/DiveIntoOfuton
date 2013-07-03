package com.crakac.fallintoofuton.util;

public class TwitterList {
	private long userId;
	private int listId;
	private String name;
	private String fullName;
	
	public TwitterList(long uid, int lid, String name, String fname){
		userId = uid;
		listId = lid;
		this.name = name;
		fullName = fname;
	}

	public long getUserId() {
		return userId;
	}

	public int getListId() {
		return listId;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}
	
	
}
