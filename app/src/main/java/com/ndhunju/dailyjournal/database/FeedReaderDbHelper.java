package com.ndhunju.dailyjournal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FeedReaderDbHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1; 
    public static final String DATABASE_NAME = "DailyJournal.db";
    
    Context mContext;

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext =  context;
        //onCreate(getWritableDatabase());
    }
    
    //this method is create only if no database has been created before
    //if you add new tables to be created then they are never created
    //unless to clear the app data which will erase database
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_MERCHANTS);
        db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_JOURNALS);
        db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_ATTACHMENTS);
        
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(FeedReaderContract.FeedEntry.SQL_DELETE_ENTRIES_MERCHANTS);
        db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_JOURNALS);
        db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_ATTACHMENTS);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public void recreateGoalsDB(){
    	SQLiteDatabase db = getWritableDatabase();
    	try{db.execSQL(FeedReaderContract.FeedEntry.SQL_DELETE_ENTRIES_MERCHANTS);
		}catch(Exception e){e.printStackTrace();}
		try{db.execSQL(FeedReaderContract.FeedEntry.SQL_DELETE_ENTRIES_JOURNALS);
		}catch (Exception e) {		}
		try{db.execSQL(FeedReaderContract.FeedEntry.SQL_DELETE_ENTRIES_ATTACHMENTS);
		}catch (Exception e) {}
		
		db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_MERCHANTS);
        db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_JOURNALS);
        db.execSQL(FeedReaderContract.FeedEntry.SQL_CREATE_ENTRIES_ATTACHMENTS);
    }


}
