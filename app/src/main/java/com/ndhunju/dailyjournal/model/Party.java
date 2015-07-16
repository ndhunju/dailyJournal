package com.ndhunju.dailyjournal.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Party {

	//Static variables
	private static int mCurrentPartyId;

	//Custom Type of Party
	public static enum Type { Debtors, Creditors }

	//Keys used for respective properties
	private static String KEY_ID = "id";
	private static String KEY_NAME = "name";
	private static String KEY_PHONE = "phone";
	private static String KEY_TYPE = "type";
	private static String KEY_JOURNALS = "journals";
	
	//Declare variables
	private int mId;
	private String mName;
	private String mPhone;
	private Type mType;
	private ArrayList<Journal> mJournals;

	//Constructor
	public Party(String name, int id){
		mId = id;
		mPhone = "";
		mName = name;
		mType = Type.Debtors;
		mJournals = new ArrayList<Journal>();
	}
	
	public int getId() {
		return mId;
	}
	
	public void setId(int id) {
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
	
	public double getBalance() {
		double balance = 0;
		for(Journal j : mJournals)
			if(j.getType().equals(Journal.Type.Debit))
				balance += j.getAmount();
			else
				balance -= j.getAmount();
		return balance;
	}
	
	public ArrayList<Journal> getJournals() {
		return mJournals;
	}
	
	public void addJournal(Journal journal){
		
		//add journal in the order of date
		mJournals.add(journal);
		int index = mJournals.size()-1;
		while(index >= 1 && mJournals.get(index).getDate() < mJournals.get(index-1).getDate()){
			Journal tempJournal = mJournals.get(index);
			mJournals.set(index, mJournals.get(index-1));
			mJournals.set(index-1, tempJournal);
			index--;
		}
	}

	/**
	 * Deletes a jouranl along with the attachments associated with it
	 * @param journalId
	 */
	public void deleteJournal(int journalId){
		for(int i = 0; i < mJournals.size() ; i++){
			Journal mJournal = mJournals.get(i);
			if(mJournal.getId() == journalId){
				mJournal.deleteAllAttachment();
				mJournals.remove(i);
			}
		}
	}
	
	public boolean deleteAllJournals(){
		for(int i = 0; i < mJournals.size(); i++){
			if(!mJournals.get(i).deleteAllAttachment())
				return false;
			mJournals.remove(i);
		}
		return true;
	}
	
	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try{
			json.put(KEY_ID, mId);
			json.put(KEY_NAME, mName);
			json.put(KEY_PHONE, mPhone);
			json.put(KEY_TYPE, mType.toString());
			JSONArray journalJSONs = new JSONArray();
			for(Journal j: mJournals)
				journalJSONs.put(j.toJSON());
			json.put(KEY_JOURNALS, journalJSONs);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return json;
	}
	
	@Override
	public String toString() {
		return mName;
	}

	//Static methods
	public static int getCurrentId(){
		return mCurrentPartyId;
	}

	public static void setCurrentId(int id){
		mCurrentPartyId = id;
	}

	public static int incrementCurrentId(){
		mCurrentPartyId++;
		return mCurrentPartyId;
	}

	/**
	 * Creates a Party Object from passed json parameter
	 * @param json
	 * @param newId If true, new Id is given to the party object. Useful
	 *              when data is imported/merged from a backup file as it can avoid
	 *              having duplicate IDs.
	 * @return
	 */
	public static Party fromJSON(JSONObject json, boolean newId){

		try {
			int id = json.getInt(KEY_ID);

			if(newId){
				//usu when merging data
				id = ++mCurrentPartyId;
			}else if(mCurrentPartyId < id) {
				//when restoring which deletes old objects
				//we need to track latest/highest id
				mCurrentPartyId = id + 1;
			}else if(mCurrentPartyId == id){
					mCurrentPartyId++;
			}

			String name = json.getString(KEY_NAME);
			Party newParty = new Party(name, id);
			String phone = json.getString(KEY_PHONE);

			//Since Debitors was corrected to Debtors, Type.valueOf("Debitors") throws error
			String type = json.getString(KEY_TYPE);
			Type t = type.equals("Debitors") ? Type.Debtors : Type.valueOf(type);

			newParty.setPhone(phone);
			newParty.setType(t);

			JSONArray journalJSONS = json.getJSONArray(KEY_JOURNALS);
			for(int i = 0 ; i < journalJSONS.length(); i++){
				Journal newJournal = Journal.fromJSON(journalJSONS.getJSONObject(i), newId);
				newJournal.setPartyId(id);
				newParty.addJournal(newJournal);
			}

			return newParty;
		} catch (JSONException e) {	e.printStackTrace();}

		return null;
	}
}
