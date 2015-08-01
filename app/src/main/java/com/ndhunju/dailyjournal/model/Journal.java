package com.ndhunju.dailyjournal.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Journal{

    //Static variable
    private static int mCurrentJournalId;

    //Custom Type of a Journal
	public enum Type { Debit, Credit}

    //Keys used for respective properties
	private static String KEY_ID = "id";
	private static String KEY_TYPE="type";
	private static String KEY_DATE = "date";
	private static String KEY_NOTE = "mNote";
	private static String KEY_AMOUNT = "amount";
	private static String KEY_ATTCH = "attachments";
	private static String KEY_ADDED_DATE = "added_date";

    //Declare variables
	private int mId;
	private long mDate;
	private Type  mType;
	private String mNote;
	private int  mPartyId;
	private double mAmount;
	private long mAddedDate; 							//Date when the journal was recorded
	private ArrayList<String> mAttachmentPaths;

    //Class constructor
    public Journal(int id){
        mId = id;
        mNote = "";
        mAmount = 0;
        mType = Type.Debit;
        mPartyId = Utils.NO_PARTY;
        mAttachmentPaths = new ArrayList<String>();
        long time = Calendar.getInstance().getTimeInMillis();
        mDate = mAddedDate = time;
    }

    public Journal(long date, int id){
        this(id);
        mDate = date;
	}

    //Methods
	public int getId(){
		return mId;
	}

	public void setId(int id){
		mId = id;
	}
	
	public int getPartyId(){
		return mPartyId;
	}
	
	public void setPartyId(int partyId){
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

    /**
     * Returns type of the Journal: Debit or Credit
     * @return
     */
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
	
	public boolean deleteAllAttachment(){
		for(int i = 0 ; i < mAttachmentPaths.size(); i++){
            if(!Utils.deleteFile(mAttachmentPaths.get(i)))
				return false;
			mAttachmentPaths.remove(i); //this decrements the size of array;
            i--;
        }
		return true;
	}

    /**
     * Deletes the attachment with specified path
     * Returns false if path not found
     * @param path
     * @return
     */
    public boolean deleteAttachment(String path){
        for(int i = 0 ; i < mAttachmentPaths.size(); i++)
            if(mAttachmentPaths.get(i).equals(path)){
				if(!Utils.deleteFile(path)) //delete path only if file is deleted
					return false;
                mAttachmentPaths.remove(i);
                return true;
            }
        return false;
    }

    public boolean deleteAttachment(int pos){
		if(!Utils.deleteFile(mAttachmentPaths.get(pos)))
			return false;
        mAttachmentPaths.remove(pos);
		return true;
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

	/**
	 * Returns a deep copy of this Journal object
	 * @return
	 */
	@Override
	public Journal clone(){
		Journal tempJournal = new Journal(mDate, mId);
		tempJournal.setPartyId(mPartyId);
		tempJournal.setAddedDate(mAddedDate);
		tempJournal.setAmount(mAmount);
		tempJournal.setType(mType);
		tempJournal.setNote(mNote);
		for(String path: mAttachmentPaths)
			tempJournal.addAttachmentPaths(path.toString());
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
		mAttachmentPaths.clear();
		mNote = journal.getNote();
		for(String path: journal.getAttachmentPaths())
			mAttachmentPaths.add(path);
		return true;
	}

    //Static methods
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

    /**
     * Creates a Journal Object from passed json parameter
     * @param json
     * @param newId If true, new Id is given to the journal object. Useful
     *              when data is imported from a backup file as it can avoid
     *              having duplicate IDs
     * @return
     */
    public static Journal fromJSON(JSONObject json, boolean newId){
        try {
            int id = json.getInt("id");

			if(newId){
				//usu when merging data
				id = ++mCurrentJournalId;
			}else if(mCurrentJournalId < id) {
				//when restoring which deletes old objects
				//we need to track latest/highest id
				mCurrentJournalId = id + 1;
			}else if(mCurrentJournalId == id){
				mCurrentJournalId++;
			}

            long date = json.getLong("date");
            long added_date = json.getLong("added_date");
            Type type = Type.valueOf(json.getString("type"));
            double amount = json.getDouble("amount");
            String note = json.getString("mNote");
            JSONArray attachmentJSONS = json.getJSONArray("attachments");
            ArrayList<String> attachments = new ArrayList<String>();
            for(int i = 0; i < attachmentJSONS.length(); i++){
				//Since app's data such as attachments are now stored in internal storage
				//we need to check if the path for attachments are still referring to external(old)
				//storage. If it is, then change it to the new one
				String path = attachmentJSONS.getString(i);
				path = Utils.replaceOldDir(path);
                attachments.add(path);
            }

            Journal newJournal = new Journal(date, id);
            newJournal.setAddedDate(added_date);
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
}
