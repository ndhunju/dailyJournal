package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ndhunju.dailyjournal.database.AttachmentDAO;
import com.ndhunju.dailyjournal.database.DbHelper;
import com.ndhunju.dailyjournal.database.JournalDAO;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Services {

	//Declare variables
	private Context mContext;
	private DbHelper mDbHelper;

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
	}

    /**
     * Creates a backup file of existing data along with attachments.
     *
     * @return : It retuns the absolute path of the backup file.
     * @throws IOException
     */
    public String createBackUp(@NonNull String dir) throws IOException {
        //1.Create JSON with latest data
        JsonConverter converter = JsonConverter.getInstance(mServices);
        converter.createJSONFile();

        //2. Zip app folder  as all the attachments and json file are here
        //2.1 Get the app folder
        File directoryToZip = UtilsFile.getAppFolder(mContext);

        //2.2 get backup Folder. Backup file will be created here
        File backupFolder = new File(dir);
		//if the file doesn't exit choose default folder
		if(!backupFolder.exists())  backupFolder = UtilsFile.getAppFolder(false);


        //2.3 create a zip file in not hidden app folder so that user can use it
        String fileName = UtilsFile.getZipFileName();
        File zipFile = new File(backupFolder.getAbsoluteFile(), fileName);
        zipFile.createNewFile();

        //3 zip the directory file into zipFile
        UtilsZip.zip(directoryToZip, zipFile);

        //let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext,
                    new String[]{backupFolder.getAbsolutePath(), zipFile.getAbsolutePath()}, null, null);

        Log.i("BackUp", "Backup file created");

        return zipFile.getAbsolutePath();
    }

    public Context getContext(){
        return  mContext;
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
		return new ArrayList<>();

	}

	public ArrayList<String> getPartyNames() {
		//Get parties from the DAO
		return (ArrayList<String>)partyDAO.getNames();
	}

	public String[] getPartyNameAsArray(){
		return partyDAO.getNamesAsArray();
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
		boolean success = false;
		db.beginTransaction();
		try{
			deleteAllJournals(party.getId());
			UtilsFile.deleteFile(party.getPicturePath());
			partyDAO.delete(party.getId());
			db.setTransactionSuccessful();
			success = true;
		}catch (Exception e){
			e.printStackTrace();

		}finally {
			db.endTransaction();
		}
		return success;
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
		return journalDAO.find(journalId);
	}

    public Journal getNewJournal(){
        //get the last created new journal's id
        KeyValPersistence persistence = KeyValPersistence.from(mContext);
        long rowId = persistence.get(String.valueOf(Constants.ID_NEW_JOURNAL), 0);
        //get the journal from the table
        Journal newJournal = null;
        if(rowId != 0){newJournal= journalDAO.find(rowId);}
        //check if this journal has been associated with a party
        if(newJournal == null || (newJournal.getPartyId() != Constants.NO_PARTY)){
            //if the journal is null or has been associated with a party, create new
            newJournal = new Journal(Constants.NO_PARTY);
            long id = journalDAO.create(newJournal);
            newJournal.setId(id);
            //save the id of new journal
            persistence.putLong(String.valueOf(Constants.ID_NEW_JOURNAL), id);
        }
        return newJournal;
    }

    public long getNewJournalId(){
        //get the last created new journal's id
        KeyValPersistence persistence = KeyValPersistence.from(mContext);
        long rowId = persistence.get(String.valueOf(Constants.ID_NEW_JOURNAL), 0);
        return rowId;
    }


    public void deleteNewJournal(Journal newJournal){
        journalDAO.delete(newJournal);
    }

    public void addNewJournal(Journal newJournal){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            journalDAO.update(newJournal);
            updatePartyAmount(newJournal, "+");
            db.setTransactionSuccessful();
        }catch (Exception e){
            //can have custom exception such as negative balance
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

	public ArrayList<Journal> getJournals(long partyId){
		ArrayList<Journal> journals = (ArrayList<Journal>)journalDAO.findAll(partyId);
		if(journals != null) return journals;
		return new ArrayList<>();
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
	private int updatePartyAmount(Journal journal, String operation){
		if(journal.getType() == Journal.Type.Debit){
			return partyDAO.updateDr(journal.getPartyId(), journal.getAmount(), operation);
		}else{
			return partyDAO.updateCr(journal.getPartyId(), journal.getAmount(), operation);
		}
	}

	private boolean deleteAllJournals(long partyId){
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
			//1. get old journal from database and use it to update the party table
			Journal oldJournal = journalDAO.find(journal.getId());
			//subtract from the Dr/Cr column in party table
			if(!(updatePartyAmount(oldJournal, "-") > 0)) throw new Exception("No rows updated");

			//update the party table with new values of the journal
			updatePartyAmount(journal, "+");

            //update the journal
            journalDAO.update(journal);

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
		//doesn't reset the table ids
		partyDAO.truncateTable();
		journalDAO.truncateTable();
		attachmentDAO.truncateTable();
		return true;
	}

    public boolean recreateDB(){
        return mDbHelper.recreateDB();
    }



	/**
	 * Deletes all parties, journals along with the attachments.
	 * @param context
	 * @return
	 */
	public boolean eraseAll(Context context){
		boolean success = true;
		try{
			success &= recreateDB();
			success &= KeyValPersistence.from(context).nukeAll(mContext);
			success &= PreferenceService.from(context).nukeAll();
			success &= UtilsFile.deleteDirectory(UtilsFile.getAppFolder(context));
		}catch (Exception e){
			e.printStackTrace();
		}

		return success;
	}


}
