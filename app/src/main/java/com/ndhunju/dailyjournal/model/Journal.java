package com.ndhunju.dailyjournal.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Journal implements Comparator<Journal>{
	
	public static enum Type { Debit, Credit};
	
	private static String KEY_ID = "id";
	private static String KEY_DATE = "date";
	private static String KEY_ADDED_DATE = "added_date";
	private static String KEY_AMOUNT = "amount";
	private static String KEY_TYPE = "type";
	private static String KEY_NOTE = "mNote";
	private static String KEY_ATTCH = "attachments";
	
	private static int mCurrentJournalId;
	
	private int mId;
	private int mPartyId;
	private long mDate;
	private long mAddedDate; //Date when the journal was recorded
	private Type mType;
	private double mAmount;
	private String mNote;
	private ArrayList<String> mAttachmentPaths;
	
	public Journal(long date, int id){
		mId = id;
		mPartyId = Utils.NO_PARTY;
		mAddedDate = Calendar.getInstance().getTimeInMillis(); //set it to current time
		mDate = date;
		mType = Type.Debit;
		mAmount = 0;
		mNote = "";
		mAttachmentPaths = new ArrayList<String>();
	}
	
	public static int getCurrentId(){
		return mCurrentJournalId;
	}
	
	public static void setCurrentId(int id){
		mCurrentJournalId = id;
	}
	
	public static int incrementCurrentId(){
		mCurrentJournalId++;
		return mCurrentJournalId;
	}
	
	public int getId(){
		return mId;
	}
	
	public void setIdFromDB(int id){
		mId = id;
	}
	
	public int getPartyId(){
		return mPartyId;
	}
	
	public void setPartyId(int merchantId){
		mPartyId = merchantId;
	}
	
	public long getDate() {
		return mDate;
	}

	public void setDate(long mDate) {
		this.mDate = mDate;
	}

	public long getAddedDate() {
		return mAddedDate;
	}
	
	public void setAddedDateFromDB(long date){
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

	public ArrayList<String> getAttachmentPaths() {
		return mAttachmentPaths;
	}

	
	public void setAttachmentPaths(ArrayList<String> mAttachmentPaths) {
		this.mAttachmentPaths = mAttachmentPaths;
	}

	public void addAttachmentPaths(String path){
		mAttachmentPaths.add(path);
	}
	
	public void deleteAttachments(){
		for(String path: mAttachmentPaths)
			Utils.deleteFile(path);
		mAttachmentPaths.clear();
	}
	
	@Override
	public int compare(Journal lhs, Journal rhs) {
		//if the difference between two dates are huge then it might
		//throw error
		return (int)(lhs.getDate()- rhs.getDate());
	}
	
	public JSONObject toJSON(){
		JSONObject j = new JSONObject();
		try{
			j.put(KEY_ID,  mId);
			j.put(KEY_DATE, mDate);
			j.put(KEY_ADDED_DATE , mAddedDate );
			j.put(KEY_TYPE , mType.toString() );
			j.put(KEY_AMOUNT, mAmount);
			j.put(KEY_NOTE , mNote);
			JSONArray attachmentJSONs = new JSONArray(mAttachmentPaths);
			j.put(KEY_ATTCH , attachmentJSONs );
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return j;
	}
	
	public static Journal fromJSON(JSONObject json, boolean newId){
		try {
			int id = json.getInt("id");
			if(newId) id = ++mCurrentJournalId;
			long date = json.getLong("date");
			long added_date = json.getLong("added_date");
			Type type = Type.valueOf(json.getString("type"));
			double amount = json.getDouble("amount");
			String note = json.getString("mNote");
			JSONArray attachmentJSONS = json.getJSONArray("attachments");
			ArrayList<String> attachments = new ArrayList<String>();
			for(int i = 0; i < attachmentJSONS.length(); i++){
				attachments.add(attachmentJSONS.getString(i));
			}
			Journal newJournal = new Journal(date, id);
			newJournal.setAddedDateFromDB(added_date);
			newJournal.setAmount(amount);
			newJournal.setType(type);
			newJournal.setNote(note);
			newJournal.setAttachmentPaths(attachments);
			
			//while exporting from JSON put the largest id as current
			/*if(id > mCurrentJournalId)
				mCurrentJournalId = id;*/ //we will instead give a new id
			
			return newJournal;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public Journal getDeepCopy(){
		Journal tempJournal = new Journal(mDate, mId);
		tempJournal.setPartyId(mPartyId);
		tempJournal.setAddedDateFromDB(mAddedDate);
		tempJournal.setAmount(mAmount);
		tempJournal.setType(mType);
		tempJournal.setNote(mNote);
		for(String path: mAttachmentPaths)
			tempJournal.addAttachmentPaths(path.toString());
		return tempJournal;
	}
	
	public boolean deepCopyFrom(Journal journal){
		mDate = journal.getDate();
		mId = journal.getId();
		mPartyId = journal.getPartyId();
		mAddedDate = journal.getAddedDate();
		mAmount = journal.getAmount();
		mType = journal.getType();
		mAttachmentPaths.clear();
		mNote = journal.getNote();
		for(String path: journal.getAttachmentPaths())
			mAttachmentPaths.add(path);
		return true;
	}
}
