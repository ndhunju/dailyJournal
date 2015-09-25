package com.ndhunju.dailyjournal.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class Party {

	//Custom Type of Party
	public static enum Type { Debtors, Creditors }

	//Declare variables
	private long mId;
    private Type mType;
	private String mName;
	private String mPhone;
	private double mDebitTotal;
	private double mCreditTotal;

	//private ArrayList<Journal> mJournals;

	//Constructor
	public Party(String name){
		mPhone = "";
		mName = name;
		mDebitTotal = 0;
		mCreditTotal = 0;
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

	/**
	 * Returns the balance of this party. <b>NOTE: </b>This method should be used as less as
	 * possible as it loops through all the journals to calculate the total debit, credit and balance.
	 * @return
	 */
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
	

	@Override
	public String toString() {
		return mName;
	}

}
