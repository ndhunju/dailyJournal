package com.ndhunju.dailyjournal.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ndhunju.dailyjournal.controller.MyPreferenceFragment;
import com.ndhunju.dailyjournal.database.AttachmentDAO;
import com.ndhunju.dailyjournal.database.DbHelper;
import com.ndhunju.dailyjournal.database.JournalDAO;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Services {

	//Constant variables
	private static final String KEY_CURRENT_PARTY_ID = Constants.APP_PREFIX + "currentMerchantId";
	private static final String KEY_CURRENT_JOURNAL_ID = Constants.APP_PREFIX + "currentJournalId";
	private static final String KEY_OLD_DATA_IMPORTED = Constants.APP_PREFIX + "oldDataImported";

    //Declare variables
	private Context mContext;
	private DbHelper mDbHelper;
	private static SharedPreferences sharedPref;

	//DAOs
	private PartyDAO partyDAO;
	private JournalDAO journalDAO;
	private AttachmentDAO attachmentDAO;

	private static Services mServices;

	public static Services getInstance(Context con) {
		if (mServices == null)	mServices = new Services(con);
		return mServices;
	}

	//Constructor
	private Services(Context con) {
		mContext = con;
		mDbHelper = new DbHelper(mContext);

		partyDAO = new PartyDAO(mDbHelper);
		journalDAO = new JournalDAO(mDbHelper);
		attachmentDAO = new AttachmentDAO(mDbHelper);

		//Get current id for party and journal
		if(sharedPref == null ) sharedPref = PreferenceManager.getDefaultSharedPreferences(con);
	}

    /**
     * Creates a backup file of existing data along with attachments.
     *
     * @param inExtDir : true to create backup file in external storage. Backup file created
     *                 in ext. storage can be accessed by computers through USB connection
     * @return : It retuns the absolute path of the backup file.
     * @throws IOException
     */
    public String createBackUp(boolean inExtDir, String extDir) throws IOException {
        //1.Create JSON with latest data
        JsonConverter converter = new JsonConverter(mServices);
        converter.createJSONFile();

        //2. Zip app folder  as all the attachments and json file are here
        //2.1 Get the app folder
        File directoryToZip = UtilsFile.getAppFolder(mContext);

        //2.2 get App Folder that is not hidden. Backup file will be created here
        File appFolder;
        //if backup is to be created in sdcard
        if (inExtDir) {
        //if folder where backup should be created is provided
            if (extDir != null)
                appFolder = new File(extDir);
            else //if folder is not provided, get default app folder
                appFolder = UtilsFile.getAppFolder(false);
        } else {
        //if backup is to be created in internal storage
        //usu if the backup file is for uploading to Google Drive
            appFolder = UtilsFile.getCacheDir(mContext);
        }


        //2.3 create a zip file in not hidden app folder so that user can use it
        String fileName = UtilsFile.getZipFileName();
        File zipFile = new File(appFolder.getAbsoluteFile(), fileName);
        zipFile.createNewFile();

        //3 zip the directory file into zipFile
        UtilsZip.zip(directoryToZip, zipFile);

        if (inExtDir) //let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext,
                    new String[]{appFolder.getAbsolutePath(), zipFile.getAbsolutePath()}, null, null);

        Log.i("BackUp", "Backup file created");

        return zipFile.getAbsolutePath();
    }

    public Context getContext(){
        return  mContext;
    }


	/********************SHARED PREFERENCES SERVICES ************************/

	/**
	 * Created to use it while JUnit testing
	 * @param sp
	 * @return
	 */
	public static boolean setSharedPreference(SharedPreferences sp){
		if(sp == null) return false;
		sharedPref = sp;
		return true;
	}

	public static boolean isOldDataImported(){
		return sharedPref.getBoolean(KEY_OLD_DATA_IMPORTED, false);
	}

	public void oldDataImportAttempted(boolean imported){
		sharedPref.edit().putBoolean(KEY_OLD_DATA_IMPORTED, imported).commit();
	}

	/********************PARTY SERVICES ************************/

	public Party getParty(long partyId) {
		return partyDAO.find(partyId);
	}

	public Party getParty(String partyName) {
		return partyDAO.find(partyName);
	}

	public ArrayList<Party> getParties() {
		ArrayList<Party> parties = (ArrayList<Party>)partyDAO.findAll();
		if(parties != null) return parties;
		return new ArrayList<Party>();

	}

	public ArrayList<String> getPartyNames() {
		//Get parties from the DAO
		ArrayList<String> names = (ArrayList<String>)partyDAO.getNames();
		return names;
	}

	/**
	 * Adds passed party to the party list in its right alphabetical order
	 * @param party
	 */
	public long addParty(Party party) {
		return partyDAO.create(party);
	}

	public Party addParty(String name){
		Party party = new Party(name);
		long partyId = addParty(party);
		party.setId(partyId);
		return party;
	}


	public boolean deleteParty(Party party){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();;
		try{
			deleteAllJournals(party.getId());
			UtilsFile.deleteFile(party.getPicturePath());
			partyDAO.delete(party.getId());
			db.setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			db.endTransaction();
		}
		return true;
	}


	public boolean updateParty(Party party){
		return (partyDAO.update(party) > 0);
	}

	/********************JOURNAL SERVICES ************************/

	/**
	 * Returns a Journal with passed id.
	 * @param journalId
	 * @return
	 */
	public Journal getJournal(long journalId) {
		Journal journal = journalDAO.find(journalId);
		if(journal != null) return journal;
		else journal = new Journal(Constants.NO_PARTY);
		journal.setId(Constants.ID_NEW_JOURNAL);
		return journal;
	}

	public ArrayList<Journal> getJournals(long partyId){
		ArrayList<Journal> journals = (ArrayList<Journal>)journalDAO.findAll(partyId);
		if(journals != null) return journals;
		return new ArrayList<Journal>();
	}

	public long addJournal(Journal journal){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
        long id = Constants.ID_NEW_JOURNAL;
		try{
			updatePartyAmount(journal, "+");
			id = journalDAO.create(journal);
			db.setTransactionSuccessful();
		}catch (Exception e){
			//can have custom exception such as negative balance
			e.printStackTrace();
		}finally {
			db.endTransaction();
		}

        return id;
	}

	public boolean deleteJournal(Journal journal){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		boolean success =false;
		try{
			//delete the relevant attachments
			for(Attachment attch : attachmentDAO.findAll(journal.getId()))
				deleteAttachment(attch);

			//delete the journal
			journalDAO.delete(journal);

			//subtract from the Dr/Cr column in party table
			if(!(updatePartyAmount(journal, "-") > 0)) throw new Exception("No rows updated");

			db.setTransactionSuccessful();
			success = true;
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			db.endTransaction();
		}

		return  success;

	}

	public boolean deleteJournal(long journalId){
		Journal journal = getJournal(journalId);
		return deleteJournal(journal);
	}

	/**
	 * Helper method
	 * @param journal
	 * @param operation
	 */
	public int updatePartyAmount(Journal journal, String operation){
		if(journal.getType() == Journal.Type.Debit){
			return partyDAO.updateDr(journal.getPartyId(), journal.getAmount(), operation);
		}else{
			return partyDAO.updateCr(journal.getPartyId(), journal.getAmount(), operation);
		}
	}

	public boolean deleteAllJournals(long partyId){
		for(Journal j : journalDAO.findAll(partyId))
			for(Attachment attch : attachmentDAO.findAll(j.getId()))
				if(!deleteAttachment(attch))
					return false;

		return true;
	}

	public boolean updateJournal(Journal journal){
		//possible changes in journal
		/* 1. Amount Change => if(type == dr) drAmt -= amount : crAmt -= amount
		 * 2. Date change => just change the date in journal row
		 * 3. Type (Dr/Cr) change =>  if(type == dr) drAmt -= amount & crAmt += amount
		 * 4. Combination of above things
		 * For most effecient way you can handle cases separately
		 * but for now we will just delete old journal and add changed one
		 * as a new journal
		 */
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		boolean success =false;
		try{
			//make changes to party
			//when the journal type is changed, the logic fails. So,
			//1. get old journal from database and use it to delete
			Journal oldJournal = journalDAO.find(journal.getId());
			deleteJournal(oldJournal);
			//2. add the new one
			addJournal(journal);
			db.setTransactionSuccessful();
			success = true;
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			db.endTransaction();
		}

		return  success;
	}

	/********************ATTACHMENT SERVICES ************************/

	public Attachment getAttachment(long attchID){
		return attachmentDAO.find(attchID);
	}

	public List<Attachment> getAttachments(long journalId){
		return attachmentDAO.findAll(journalId);
	}

    public ArrayList<String> getAttachmentPaths(long journalId){
        ArrayList<String> paths = new ArrayList<>();
        for(Attachment a : getAttachments(journalId))
            paths.add(a.getPath());
        return paths;
    }


	public void addAttachment(Attachment attachment){
		attachmentDAO.create(attachment);
	}

	/**
	 * Deletes the attachment from physical storage as
	 * well as from the table. Use {@link #deleteAttachment(Attachment)}
	 * if you have the attachment object.
	 * @param id
	 * @return
	 */
	public boolean deleteAttachment(long id){
		//1. Delete the physical file from storage
		if(!UtilsFile.deleteFile(attachmentDAO.find(id).getPath()))
			return false;

		//2. Delete from the database
		attachmentDAO.delete(id);
		return true;
	}

	public boolean deleteAttachment(Attachment attch){
		if(!UtilsFile.deleteFile(attch.getPath()))
			return false;
		attachmentDAO.delete(attch);
		return true;
	}

	public boolean deleteAllAttachments(long journalId){
		for(Attachment attach : attachmentDAO.findAll(journalId))
			if(!deleteAttachment(attach))
				return false;
		return true;
	}

    public boolean updateAttachment(Attachment attachment){
        return (attachmentDAO.update(attachment) > 0);
    }


	/********************DELETION FUNCTIONS ************************/
	/**
	 * Clears all parties. <b>Note: </b> It doesn't delete corresponding files/attachments
	 * from app folder. For that, {@link #eraseAll(Context)} should be called
	 * @return
	 */
	public boolean dropAllTables(){
		return mDbHelper.dropAllTables();
	}

	public boolean truncateAllTables(){
		partyDAO.truncateTable();
		journalDAO.truncateTable();
		attachmentDAO.truncateTable();
		return true;
	}

	/**
	 * Deletes all parties, journals along with the attachments.
	 * @param context
	 * @return
	 */
	public boolean eraseAll(Context context){
		boolean success = true;
		try{
			success &= truncateAllTables();
			success &= eraseSharedPreferences();
			success &= UtilsFile.deleteDirectory(UtilsFile.getAppFolder(context));
		}catch (Exception e){
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * Erases all stored SharedPreferences
	 * @return
	 */
	public boolean eraseSharedPreferences(){
		//sharedPref.edit().clear().clear(); // doesn't work for default shared preference
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(KEY_CURRENT_JOURNAL_ID, 1);
		editor.putInt(KEY_CURRENT_PARTY_ID, 1);
		editor.putBoolean(KEY_OLD_DATA_IMPORTED, false);
		boolean success = editor.commit();

		//Get Shared preference
		SharedPreferences sp = mContext.getSharedPreferences(
				MyPreferenceFragment.DEF_NAME_SHARED_PREFERENCE, Activity.MODE_PRIVATE);
		success &= sp.edit().clear().commit();
		return success;
	}


}
