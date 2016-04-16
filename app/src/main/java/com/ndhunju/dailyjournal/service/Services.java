package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ndhunju.dailyjournal.database.AttachmentDAO;
import com.ndhunju.dailyjournal.database.DailyJournalContract;
import com.ndhunju.dailyjournal.database.DbHelper;
import com.ndhunju.dailyjournal.database.IAttachmentDAO;
import com.ndhunju.dailyjournal.database.IJournalDAO;
import com.ndhunju.dailyjournal.database.IPartyDAO;
import com.ndhunju.dailyjournal.database.JournalDAO;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class Services {

	//Variables
	private Context mContext;
	private SQLiteOpenHelper mSqLiteOpenHelper;

    //Cache
    private HashMap<Long, Party> mParties;

	//DAOs
	private IPartyDAO partyDAO;
	private IJournalDAO journalDAO;
	private IAttachmentDAO attachmentDAO;

	//Static variable
	private static Services mServices;

	public static Services getInstance(Context con) {
		if (mServices == null)	mServices = new Services(con);
		return mServices;
	}

	public static Services getInstance(Context con, SQLiteOpenHelper sqLiteOpenHelper) {
		if (mServices == null)	mServices = new Services(con, sqLiteOpenHelper);
		return mServices;
	}

	//Constructor
	private Services(@NonNull Context context) {
		mContext = context;
		mSqLiteOpenHelper = new DbHelper(mContext);
		partyDAO = new PartyDAO(mSqLiteOpenHelper);
		journalDAO = new JournalDAO(mSqLiteOpenHelper);
		attachmentDAO = new AttachmentDAO(mSqLiteOpenHelper);
	}

    private Services(Context context, SQLiteOpenHelper sqLiteOpenHelper){
        mContext = context;
        mSqLiteOpenHelper = sqLiteOpenHelper;
        partyDAO = new PartyDAO(mSqLiteOpenHelper);
        journalDAO = new JournalDAO(mSqLiteOpenHelper);
        attachmentDAO = new AttachmentDAO(mSqLiteOpenHelper);


    }

    /**
     * Creates a backup file of existing data along with attachments.
     * @return : absolute path of the backup file.
     * @throws IOException
     */
    public String createBackUp(@NonNull String dir) throws IOException {
        //1.Create JSON with latest data
        JsonConverterString converter = JsonConverterString.getInstance(mContext);
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

    /**
     * Return the reference for the {@link Context} this class holds
     * @return
     */
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
	 * Adds passed party to the database
	 * @param party
	 */
	public long addParty(Party party) {
		return partyDAO.create(party);
	}

    /**
     * Adds party with passed name argument and returns a party
     * object with its id in the database
     * @param name
     * @return
     */
	public Party addParty(String name){
		Party party = new Party(name);
		long partyId = addParty(party);
		party.setId(partyId);
		return party;
	}

    /**
     * Deletes the party along with its Journals
     * and attachments
     * @param party
     * @return
     */
	public boolean deleteParty(Party party){
		SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
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

    /**
     * Returns an instance of new journal which doesn't belong to any party
     * @return
     */
    public Journal getNewJournal(){
        //get the last created new journal's id
        long rowId = getNewJournalId();
        //get the journal from the table
        Journal newJournal = null;
        if(rowId != 0){newJournal= journalDAO.find(rowId);}
        //check if this journal has been associated with a party
        if(newJournal == null || (newJournal.getPartyId() != Constants.NO_PARTY)){
            //if the journal is null or has been associated with a party, create new
            newJournal = new Journal(Constants.NO_PARTY);
            long id = journalDAO.create(newJournal);
            newJournal.setId(id);
            //save the id of new journal.
            setNewJournalId(id);
        }

        //update the date to Todays
        newJournal.setDate(Calendar.getInstance().getTimeInMillis());

        return newJournal;
    }

    /**
     * Returns Id for the new Journal
     * @return
     */
    public long getNewJournalId(){
        //get the last created new journal's id
        KeyValPersistence persistence = KeyValPersistence.from(mContext);
        long rowId = persistence.getLong(String.valueOf(Constants.ID_NEW_JOURNAL), 0);
        return rowId;
    }

    /**
     * Stores the id of the new journal
     * @param id
     */
    public void setNewJournalId(long id){
        //get the last created new journal's id
        KeyValPersistence persistence = KeyValPersistence.from(mContext);
        persistence.putLong(String.valueOf(Constants.ID_NEW_JOURNAL), id);
    }

    /**
     * Deletes newly created Journal that is not associated with any party.
     * To delete a journal that belongs to a party use {@link #deleteJournal(Journal}
     * @param newJournal
     */
    public boolean deleteNewJournal(Journal newJournal){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success =false;
        try{
            //delete the relevant attachments
            for(Attachment attch : attachmentDAO.findAll(newJournal.getId()))
                deleteAttachment(attch);
            //delete the journal
            journalDAO.delete(newJournal);
            db.setTransactionSuccessful();
            success = true;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }

        return  success;
    }

    /**
     * Updates new journal and makes necessary changes to Party table like modify the
     * balance. This method should be used only with the Journal that is not associated
     * any Party in the table
     * @param newJournal
     */
    public void updateNewJournal(Journal newJournal){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
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

	public ArrayList<Journal> getJournals(){
		return (ArrayList<Journal>)journalDAO.findAll();
	}

    /**
     * Adds Journal that is associated with a Party
     * @param journal
     * @return
     */
	public long addJournal(Journal journal){
		SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
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

    /**
     * Deletes the journal from the table along with associated Attachments
     * @param journal
     * @return
     */
	public boolean deleteJournal(Journal journal){
		SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
		db.beginTransaction();
		boolean success =false;
		try{
			//delete the relevant attachments
			for(Attachment attch : attachmentDAO.findAll(journal.getId()))
				deleteAttachment(attch);
			//delete the journal
			journalDAO.delete(journal);
			//subtract from the Dr/Cr column in party table
			if(!(updatePartyAmount(journal, "-") > 0))
                throw new Exception("No rows updated");
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
	 * Helper method that updates the debit or credit column
     * of a party based on journal type
	 * @param updatedJournal : journal that has been updated
	 * @param operation : + or - ; + performs similar to x += x;
	 */
	private int updatePartyAmount(Journal updatedJournal, String operation){
		if(updatedJournal.getType() == Journal.Type.Debit){
			return partyDAO.updateDr(updatedJournal.getPartyId(), updatedJournal.getAmount(), operation);
		}else{
			return partyDAO.updateCr(updatedJournal.getPartyId(), updatedJournal.getAmount(), operation);
		}
	}

    /**
     * Deletes all the journal of passed party
     * @param partyId
     * @return
     */
	private boolean deleteAllJournals(long partyId){
		for(Journal j : journalDAO.findAll(partyId))
			for(Attachment attch : attachmentDAO.findAll(j.getId()))
				if(!deleteAttachment(attch))
					return false;

		return true;
	}

    /**
     * Updates the journal and associated party table
     */
    public boolean updateJournal(Journal journal) {
        /*
        * Possible changes in the journal,
     * 1. Amount Change => if(type == dr) drAmt -= amount : crAmt -= amount
     * 2. Date change => just change the date in journal row
     * 3. Type (Dr/Cr) change =>  if(type == dr) drAmt -= amount & crAmt += amount
     * 4. Combination of above things
     * For most effecient way you can handle cases separately
     * but for now we will just delete old journal and add changed one
     * as a new journal
         */
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            //make changes to party
            //when the journal type is changed, the logic fails. So,
            //1. get old journal from database and use it to update the party table
            Journal oldJournal = journalDAO.find(journal.getId());
            //subtract from the Dr/Cr column in party table
            if (!(updatePartyAmount(oldJournal, "-") > 0))
                throw new Exception("No rows updated");

            //update the party table with new values of the journal
            updatePartyAmount(journal, "+");

            //update the journal
            journalDAO.update(journal);

            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return success;
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

    public void addAttachments(List<Attachment> attachments){
        attachmentDAO.bulkInsert(attachments);
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

    /**
     * Deletes the attachment from physical storage as
     * well as from the table. Use {@link #deleteAttachment(Attachment)}
     * if you have the attachment object.
     * @return
     */
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
     * Deletes all the rows in the tables but not the schema. It
     * doesn't reset any autoincrement columns
     * @return
     */
	public boolean truncateAllTables(){
		//doesn't reset the table ids
		partyDAO.truncateTable();
		journalDAO.truncateTable();
		attachmentDAO.truncateTable();
		return true;
	}

    /**
     * Deletes all the table and recreates them
     * @return
     */
    public boolean recreateDB(){
        return dropAllTables() &&
        createDB();
    }


    /**
     * Creates tables in the database
     * @return
     */
    public boolean createDB(){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.execSQL(DailyJournalContract.PartyColumns.SQL_CREATE_ENTRIES_PARTY);
        db.execSQL(DailyJournalContract.JournalColumns.SQL_CREATE_ENTRIES_JOURNALS);
        db.execSQL(DailyJournalContract.AttachmentColumns.SQL_CREATE_ENTRIES_ATTACHMENTS);
        return true;
    }

    /**
     * Clears all parties. <b>Note: </b> It doesn't delete corresponding files/attachments
     * from app folder. For that, {@link #eraseAll()} should be called
     * @return
     */
    public boolean dropAllTables(){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        try{
            db.execSQL(DailyJournalContract.AttachmentColumns.SQL_DROP_ENTRIES_ATTACHMENTS);
            db.execSQL(DailyJournalContract.JournalColumns.SQL_DROP_ENTRIES_JOURNALS);
            db.execSQL(DailyJournalContract.PartyColumns.SQL_DROP_ENTRIES_PARTY);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

	/**
	 * Deletes all parties, journals along with the attachments from the storage as well as
     * from the database. However, it recreates the DB schema
	 * @return
	 */
	public boolean eraseAll(){
		boolean success = true;
		try{
			success &= recreateDB();
			success &= KeyValPersistence.from(mContext).clear();
			success &= PreferenceService.from(mContext).clear();
			success &= UtilsFile.deleteDirectory(UtilsFile.getAppFolder(mContext));
		}catch (Exception e){
			e.printStackTrace();
		}

		return success;
	}


}
