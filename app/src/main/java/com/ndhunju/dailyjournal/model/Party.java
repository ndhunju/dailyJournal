package com.ndhunju.dailyjournal.model;

import java.io.Serializable;

public class Party implements Serializable{

	//Custom Type of Party
	public enum Type { Debtors, Creditors }

	//Declare variables
	private long mId;
    private Type mType;
	private String mName;
	private String mNote;
	private String mPhone;
	private double mDebitTotal;
	private String mPicturePath;
	private double mCreditTotal;

    /** it is mainly used to relay instance's position across activities.
     * there is no need to persist its value outside of app's lifecycle */
	private transient Object mTag;

	//private ArrayList<Journal> mJournals;

	//Constructor
	public Party(String name){
		mPhone = "";
		mName = name;
		mDebitTotal = 0;
		mCreditTotal = 0;
        mPicturePath = "";
        mType = Type.Debtors;
	}

	public Party(String name, long id){
		this(name);
		mId = id;
	}
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
		this.mId = id;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String mName) {
		this.mName = mName;
	}

	public void setNote(String note) {
		this.mNote = note;
	}

	public String getNote() {
		return this.mNote;
	}

	public String getPhone() {
		return mPhone;
	}
	
	public void setPhone(String phone) {
		this.mPhone = phone;
	}
	
	public Type getType() {
		return mType;
	}
	
	public void setType(Type mType) {
		this.mType = mType;
	}

	public String getPicturePath(){
		return this.mPicturePath;
	}

	public void setPicturePath(String path){
		this.mPicturePath = path;
	}

	public double calculateBalances() {
		return mDebitTotal - mCreditTotal;
	}

    public void setDebitTotal(double amount){
        if( amount >= 0 ) mDebitTotal = amount;
    }

    public void setCreditTotal(double amount){
        if( amount >= 0 ) mCreditTotal = amount;
    }

	public double getDebitTotal(){
		return mDebitTotal;
	}

	public double getCreditTotal(){
		return mCreditTotal;
	}

	public Object getTag() {
		return mTag;
	}

	public void setTag(Object tag) {
		this.mTag = tag;
	}

	@Override
	public String toString() {
		return mName;
	}

}
