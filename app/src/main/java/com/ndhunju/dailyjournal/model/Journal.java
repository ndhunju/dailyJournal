package com.ndhunju.dailyjournal.model;

import java.io.Serializable;
import java.util.Calendar;

public class Journal implements Serializable{

    //Custom Type of a Journal
	public enum Type { Debit, Credit}

    //Declare variables
	private long mId;
	private long mDate;
	private Type  mType;
	private String mNote;
	private long  mPartyId;
	private double mAmount;
	private long mAddedDate; //Date when the journal was recorded

    //Class constructor
    public Journal(long partyId){
        mNote = "";
        mAmount = 0;
        mType = Type.Debit;
        mPartyId = partyId;
        long time = Calendar.getInstance().getTimeInMillis();
        mDate = mAddedDate = time;
    }


    public Journal(long partyId, long date, long id){
		this(partyId);
		mDate = date;
		mId = id;
	}

    //Methods
	public long getId(){
		return mId;
	}

	public void setId(long id){
		mId = id;
	}
	
	public long getPartyId(){
		return mPartyId;
	}
	
	public void setPartyId(long partyId){
		mPartyId = partyId;
	}
	
	public long getDate() {
		return mDate;
	}

	public void setDate(long mDate) {
		this.mDate = mDate;
	}

    /**
     * Returns the date that the Journal was created in the
     * app rather than the date the actual Journal was made
     * @return
     */
	public long getAddedDate() {
		return mAddedDate;
	}

    /**
     * Sets the date that the Journal was created in the
     * app. Should be used when this information was
     * retrieved from the database or other form of backup data
     * @param date
     */
	public void setAddedDate(long date){
		mAddedDate = date;
	}

	public Type getType() {
		return mType;
	}

	public void setType(Type mType) {
		this.mType = mType;
	}

	public double getAmount() {
		return mAmount;
	}

	public void setAmount(double mAmount) {
		this.mAmount = mAmount;
	}

	public String getNote() {
		return mNote;
	}

	public void setNote(String mNote) {
		this.mNote = mNote;
	}

	/**
	 * Returns a deep copy of this Journal object
	 * @return
	 */
	@Override
	public Journal clone(){
		Journal tempJournal = new Journal(mPartyId, mDate, mId);
		tempJournal.setPartyId(mPartyId);
		tempJournal.setAddedDate(mAddedDate);
		tempJournal.setAmount(mAmount);
		tempJournal.setType(mType);
		tempJournal.setNote(mNote);
		return tempJournal;
	}

	/**
	 * Perform deep copy of passed Journal object into this
	 * Journal
	 * @param journal
	 * @return
	 */
	public boolean deepCopyFrom(Journal journal){
		mDate = journal.getDate();
		mId = journal.getId();
		mPartyId = journal.getPartyId();
		mAddedDate = journal.getAddedDate();
		mAmount = journal.getAmount();
		mType = journal.getType();
		mNote = journal.getNote();
		return true;
	}
}
