package com.ndhunju.dailyjournal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.database.FeedReaderContract.FeedEntry;
import com.ndhunju.dailyjournal.database.FeedReaderDbHelper;

public class Storage {

	//Constant variables
	private static final String KEY_CURRENT_PARTY_ID = Utils.APP_PREFIX + "currentMerchantId";
	private static final String KEY_CURRENT_JOURNAL_ID = Utils.APP_PREFIX + "currentJournalId";
	private static final String KEY_OLD_DATA_IMPORTED = Utils.APP_PREFIX + "oldDataImported";

    //Declare variables
	private Context mContext;
	static SharedPreferences pm;
	private static Storage mStorage;
	private ArrayList<Party> mParties;
	private FeedReaderDbHelper mDbHelper;

	//Constructor
	private Storage(Context con) {
		mContext = con;
		mParties = new ArrayList<Party>();
		mDbHelper = new FeedReaderDbHelper(mContext);

		//Get current id for party and journal
		if(pm == null ) pm = PreferenceManager.getDefaultSharedPreferences(con);
		Journal.setCurrentId(pm.getInt(KEY_CURRENT_JOURNAL_ID, 1));
		Party.setCurrentId(pm.getInt(KEY_CURRENT_PARTY_ID, 1));
	}

	public static Storage getInstance(Context con) {
		if (mStorage == null)
			mStorage = new Storage(con);
		return mStorage;
	}

	/**
	 * Created to used it while JUnit testing
	 * @param sp
	 * @return
	 */
	public static boolean setSharedPreference(SharedPreferences sp){
		if(sp == null)
			return false;

		pm = sp;
		return true;
	}

	public boolean isOldDataImported(){ return pm.getBoolean(KEY_OLD_DATA_IMPORTED, false); }

	public static boolean isOldDataImported(SharedPreferences pm){
		return pm.getBoolean(KEY_OLD_DATA_IMPORTED, false);
	}

	public void oldDataImportAttempted(boolean imported){
		pm.edit().putBoolean(KEY_OLD_DATA_IMPORTED, imported).commit();
	}
	
	public ArrayList<String> getPartyNames() {
		ArrayList<String> names = new ArrayList<String>(mParties.size());
		for (Party m : mParties)
			names.add(m.getName());
		return names;
	}
	
	public ArrayList<Integer> getPartyIds(){
		ArrayList<Integer> temp  = new ArrayList<Integer>();
		for(Party m : mParties)
				temp.add(m.getId());
		return temp;
	}

	public ArrayList<Party> getParties() {
		return mParties;
	}

	public Party getParty(String merchantName) {
		for (Party merchant : getParties()) {
			if (merchant.getName().equals(merchantName))
				return merchant;
		}

		return null;
	}

	public Party getParty(int partyId) {
		for (Party party : getParties())
			if (party.getId() == partyId)
				return party;
		return null;
	}

    /**
     * Returns a Journal with passed id. It's more efficient to use
     * {@link #getJournal(Party, int)} if you have party object
     * @param journalId
     * @return
     */
	public Journal getJournal(int journalId) {
		for (Party m : getParties())
			for (Journal j : m.getJournals())
				if (j.getId() == journalId)
					return j;
		return null;
	}
	
	public Journal getJournal(int partyId, int journalId){
		for(Journal j : getParty(partyId).getJournals())
			if(j.getId() == journalId)
				return j;
		return null;
	}

	/**
	 * Similar to {@link #getJournal(int, int) getJournal(partyId, journalId)} method
	 * but relatively more efficient.
	 * @param party
	 * @param journalId
	 * @return
	 */
	public Journal getJournal(Party party, int journalId){
		for(Journal j : party.getJournals())
			if(j.getId() == journalId)
				return j;
		return null;
	}

    /**
     * Adds passed party to the party list in its right alphabetical order
     * @param party
     */
	public void addParty(Party party) {
		// add merchants in the alphabetical order
		mParties.add(party);
		int index = mParties.size() - 1;
		while (index >= 1 && 
				(mParties.get(index).getName().toLowerCase(Locale.getDefault())
						.compareTo(mParties.get(index - 1).getName().toLowerCase(Locale.getDefault()))) < 0) {
			Party tempParty = mParties.get(index);
			mParties.set(index, mParties.get(index - 1));
			mParties.set(index - 1, tempParty);
			tempParty = null; //avoid loitering 
			index--;
		}
		
	}

	public boolean deleteParty(int partyId){
		for(int i = 0; i < mParties.size() ; i++){
			if(mParties.get(i).getId() == partyId){
				if(!mParties.get(i).deleteAllJournals())
					return false;
				mParties.remove(i);
			}
		}
		return true;
	}

	public boolean deleteAllParties(){
		/*for(int i = 0;i < mParties.size(); i++){
			if(!mParties.get(i).deleteAllJournals())
				return false;
			mParties.remove(i);
		}*/

		mParties = null;
		return true;
	}

	public boolean eraseAll(Activity activity){
		try{
			deleteAllParties();
			Utils.cleanDirectory(Utils.getAppFolder(activity));
			writeToDB();
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public static boolean deleteViaContentProvider(Context context, String fullname)
	{
		Uri uri=getFileUri(context,fullname);

		if (uri==null)
		{
			return false;
		}

		try
		{
			ContentResolver resolver=context.getContentResolver();

			// change type to image, otherwise nothing will be deleted
			ContentValues contentValues = new ContentValues();
			int media_type = 1;
			contentValues.put("media_type", media_type);
			resolver.update(uri, contentValues, null, null);

			return resolver.delete(uri, null, null) > 0;
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	private static Uri getFileUri(Context context, String fullname)
	{
		// Note: check outside this class whether the OS version is >= 11
		Uri uri = null;
		Cursor cursor = null;
		ContentResolver contentResolver = null;

		try
		{
			contentResolver= context.getContentResolver();
			if (contentResolver == null)
				return null;

			uri=MediaStore.Files.getContentUri("external");
			String[] projection = new String[2];
			projection[0] = "_id";
			projection[1] = "_data";
			String selection = "_data = ? ";    // this avoids SQL injection
			String[] selectionParams = new String[1];
			selectionParams[0] = fullname;
			String sortOrder = "_id";
			cursor=contentResolver.query(uri, projection, selection, selectionParams, sortOrder);

			if (cursor!=null)
			{
				try
				{
					if (cursor.getCount() > 0) // file present!
					{
						cursor.moveToFirst();
						int dataColumn=cursor.getColumnIndex("_data");
						String s = cursor.getString(dataColumn);
						if (!s.equals(fullname))
							return null;
						int idColumn = cursor.getColumnIndex("_id");
						long id = cursor.getLong(idColumn);
						uri= MediaStore.Files.getContentUri("external",id);
					}
					else // file isn't in the media database!
					{
						ContentValues contentValues=new ContentValues();
						contentValues.put("_data",fullname);
						uri = MediaStore.Files.getContentUri("external");
						uri = contentResolver.insert(uri,contentValues);
					}
				}
				catch (Throwable e)
				{
					uri = null;
				}
				finally
				{
					cursor.close();
				}
			}
		}
		catch (Throwable e)
		{
			uri=null;
		}
		return uri;
	}
	
	/*
	 * You should not initialize your helper object using with new DatabaseHelper(context).
	   Instead, always use DatabaseHelper.getInstance(context), as it guarantees that only 
	   one database helper will exist across the entire application's life cycle.
	 * @return
	 */
	/*public FeedReaderDbHelper getDbHelper(){
		if(mDbHelper == null)
			mDbHelper = new FeedReaderDbHelper(mContext);
		return mDbHelper;
	}*/

	public void writeToDB() {
		
		// Gets the data repository in write mode
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// clear the database first to make the process easier but MAKE SURE that mMerchant is not null
		//when the app crashes it becomes null resulting in clearing the DB but not being written anything
		if(mParties != null)
			mDbHelper.recreateGoalsDB();

		for (Party m : mParties) {
			writePartyToDB(m, db);
			for (Journal j : m.getJournals()) {
				writeJournalToDB(j, db, m.getJournals().hashCode());
				for (String path : j.getAttachmentPaths())
					writeAttachemntPathsToDB(path, db, j.getAttachmentPaths()
							.hashCode());
			}

		}

		//db.close(); //TODO It seems it causes an error following error in Samsung Galaxy phone
		// (java.lang.IllegalStateException: Cannot perform this operation because the connection pool has been closed.))
		mDbHelper.close();

		//update current IDS for party and journal
		pm.edit().putInt(KEY_CURRENT_PARTY_ID, Party.getCurrentId()).commit();
		pm.edit().putInt(KEY_CURRENT_JOURNAL_ID, Journal.getCurrentId()).commit();
	}

	public long writePartyToDB(Party party, SQLiteDatabase db) {

		// deleteAllFromDB();

		// CREATE THE DATABASE-->> create when FeedReaderDbHelper is
		// instantiated

		// Create a new map of values, where column names are the keys
		ContentValues values_parties = new ContentValues();

		values_parties.put(FeedEntry.MERCHANT_ID, party.getId());
		values_parties.put(FeedEntry.COL_MERCHANT_NAME, party.getName());
		values_parties.put(FeedEntry.COL_MERCHANT_PHONE, party.getPhone());
		values_parties.put(FeedEntry.COL_MERCHANT_TYPE, party.getType()
				.toString());
		values_parties.put(FeedEntry.COL_MERCHANT_JOURNALS, party
				.getJournals().hashCode());

		// Insert the new row, returning the primary key value of the new row
		long newRowId = db.insert(FeedEntry.TABLE_NAME_MERCHANT, null,
				values_parties);

		return newRowId;

	}

	public long writeJournalToDB(Journal journal, SQLiteDatabase db,
			int arrayHashCode) {

		// deleteAllFromDB();

		// CREATE THE DATABASE-->> create when FeedReaderDbHelper is
		// instantiated

		// Create a new map of values, where column names are the keys
		ContentValues values_journal = new ContentValues();

		values_journal.put(FeedEntry.JOURNAL_ID, journal.getId());
		values_journal.put(FeedEntry.COL_JOURNAL_DATE, journal.getDate());
		values_journal.put(FeedEntry.COL_ADDED_DATE, journal.getAddedDate());
		values_journal.put(FeedEntry.COL_JOURNAL_TYPE, journal.getType()
				.toString());
		values_journal.put(FeedEntry.COL_JOURNAL_AMOUNT, journal.getAmount());
		values_journal.put(FeedEntry.COL_JOURNAL_NOTE, journal.getNote());
		values_journal.put(FeedEntry.COL_JOURNAL_ATTACHMENTS, journal
				.getAttachmentPaths().hashCode());
		values_journal.put(FeedEntry.COL_JOURNAL_MERCHANT_ID,
				journal.getPartyId());
		values_journal.put(FeedEntry.COL_JOURNAL_PARENT_ARRAY, arrayHashCode);

		// Insert the new row, returning the primary key value of the new row
		long newRowId = db.insert(FeedEntry.TABLE_NAME_JOURNAL, null,
				values_journal);

		return newRowId;

	}

	public long writeAttachemntPathsToDB(String path, SQLiteDatabase db,
			int parentHashCode) {
		ContentValues values_paths = new ContentValues();
		values_paths.put(FeedEntry.COL_ATTACHMENT_NAME, path);
		values_paths.put(FeedEntry.COL_ATTACHMENT_PARENT, parentHashCode);

		long newRowId = db.insert(FeedEntry.TABLE_NAME_ATTACHMENTS, null,
				values_paths);

		return newRowId;
	}

	public void readPartiesFromDB() {

		// clear out any saved data, because when the homeScreenActivity is
		// created
		// every time it loads data from database making more than one copies of
		// same goals
		mParties.clear();

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		// mDbHelper.onCreate(db); //uncomment only when you need to eras db

		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = { FeedEntry.MERCHANT_ID,
				FeedEntry.COL_MERCHANT_NAME, FeedEntry.COL_MERCHANT_PHONE,
				FeedEntry.COL_MERCHANT_TYPE, FeedEntry.COL_MERCHANT_JOURNALS, };

		// How you want the results sorted in the resulting Cursor
		// ****you might want to order by date later on: ndhunju
		// String sortOrder =
		// FeedEntry.COLUMN_NAME_UPDATED + " DESC";
		Cursor c = null;

		try {
			c = db.query(FeedEntry.TABLE_NAME_MERCHANT, // The table to query
					projection, // The columns to return
					null, // The columns for the WHERE clause
					null, // The values for the WHERE clause
					null, // don't group the rows
					null, // don't filter by row groups
					null // The sort order
			);
		} catch (Exception e) {
			Log.e(">>>>IMP>>>>>>>>>>", "c is null " + e);
		}

		/*
		 * To look at a row in the cursor, use one of the Cursor move methods,
		 * which you must always call before you begin reading values.
		 * Generally, you should start by calling moveToFirst(), which places
		 * the "read position" on the first entry in the results. For each row,
		 * you can read a column's value by calling one of the Cursor get
		 * methods, such as getString() or getLong(). For each of the get
		 * methods, you must pass the index position of the column you desire,
		 * which you can get by calling getColumnIndex() or
		 * getColumnIndexOrThrow(). For example:
		 */

		if (c == null)
			return;

		c.moveToFirst();
		int i = c.getCount();
		while (i > 0) {
			int id = c.getInt((c.getColumnIndexOrThrow(FeedEntry.MERCHANT_ID)));
			String name = c.getString(c
					.getColumnIndexOrThrow(FeedEntry.COL_MERCHANT_NAME));
			String phone = c.getString(c
					.getColumnIndexOrThrow(FeedEntry.COL_MERCHANT_PHONE));
			//Since Debitors was corrected to Debtors, Type.valueOf("Debitors") throws error
			String typeStr = c.getString(c.getColumnIndexOrThrow(FeedEntry.COL_MERCHANT_TYPE));
			Party.Type type = typeStr.equals("Debitors") ? Party.Type.Debtors : Party.Type.valueOf(typeStr);
			String journalsHashCode = (c.getString(c
					.getColumnIndexOrThrow(FeedEntry.COL_MERCHANT_JOURNALS)));

			Party party = new Party(name, id);
			party.setPhone(phone);
			party.setType(type);
			party.getJournals().addAll(
					readJournalsFromDB(FeedEntry.COL_MERCHANT_JOURNALS + "="
							+ journalsHashCode, db));
			mParties.add(party);

			i--;
			c.moveToNext();
		}
		
		c.close();
		db.close();
		mDbHelper.close();

	}

	public ArrayList<Journal> readJournalsFromDB(String selection, SQLiteDatabase db) {
		ArrayList<Journal> mJournals = new ArrayList<Journal>();

		String[] projection = { FeedEntry.JOURNAL_ID,
				FeedEntry.COL_JOURNAL_DATE, FeedEntry.COL_ADDED_DATE,
				FeedEntry.COL_JOURNAL_TYPE, FeedEntry.COL_JOURNAL_AMOUNT,
				FeedEntry.COL_JOURNAL_NOTE, FeedEntry.COL_JOURNAL_ATTACHMENTS,
				FeedEntry.COL_JOURNAL_MERCHANT_ID };

		Cursor c = db.query(FeedEntry.TABLE_NAME_JOURNAL, // The table to query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);

		c.moveToFirst();
		int i = c.getCount();
		while (i > 0) {
			int id = c.getInt(c.getColumnIndex(FeedEntry.JOURNAL_ID));
			long date = c.getLong(c
					.getColumnIndexOrThrow(FeedEntry.COL_JOURNAL_DATE));
			long addedDate = c.getLong(c
					.getColumnIndexOrThrow(FeedEntry.COL_ADDED_DATE));
			Journal.Type type = Journal.Type.valueOf(c.getString(c
					.getColumnIndexOrThrow(FeedEntry.COL_JOURNAL_TYPE)));
			double amount = c.getDouble(c
					.getColumnIndexOrThrow(FeedEntry.COL_JOURNAL_AMOUNT));
			String note = c.getString(c
					.getColumnIndexOrThrow(FeedEntry.COL_JOURNAL_NOTE));
			String attachmentHashCode = c.getString(c
					.getColumnIndexOrThrow(FeedEntry.COL_JOURNAL_ATTACHMENTS));
			int partyId = c.getInt(c
					.getColumnIndex(FeedEntry.COL_JOURNAL_MERCHANT_ID));

			i--;
			c.moveToNext();

			Journal journal = new Journal(date, id);
			journal.setAddedDate(addedDate);
			journal.setType(type);
			journal.setAmount(amount);
			journal.setNote(note);
			journal.setPartyId(partyId);
			journal.setAttachmentPaths(readAttachmentPathsFromDB(FeedEntry.COL_ATTACHMENT_PARENT
					+ "=" + attachmentHashCode, db));
			mJournals.add(journal);
		}

		return mJournals;
	}

	private ArrayList<String> readAttachmentPathsFromDB(String selection, SQLiteDatabase db) {
		ArrayList<String> mPaths = new ArrayList<String>();

		String[] projection = { FeedEntry.COL_ATTACHMENT_NAME, };

		Cursor c = db.query(FeedEntry.TABLE_NAME_ATTACHMENTS, // The table to
																// query
				projection, // The columns to return
				selection, // The columns for the WHERE clause
				null, // The values for the WHERE clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);

		c.moveToFirst();
		int i = c.getCount();
		while (i > 0) {
			String name = c.getString(c
					.getColumnIndex(FeedEntry.COL_ATTACHMENT_NAME));

			i--;
			c.moveToNext();
			mPaths.add(name);
		}

		return mPaths;
	}

}
