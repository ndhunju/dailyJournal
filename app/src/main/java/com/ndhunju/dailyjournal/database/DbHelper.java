package com.ndhunju.dailyjournal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.ndhunju.dailyjournal.database.DailyJournalContract.*;

public class DbHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1; 
    public static final String DATABASE_NAME = "DailyJournal.db";
    
    Context mContext;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext =  context;
        //onCreate(getWritableDatabase());
    }
    
    //this method is create only if no database has been created before
    //if you add new tables to be created then they are never created
    //unless to clear the app data which will erase database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PartyColumns.SQL_CREATE_ENTRIES_PARTY);
        db.execSQL(JournalColumns.SQL_CREATE_ENTRIES_JOURNALS);
        db.execSQL(AttachmentColumns.SQL_CREATE_ENTRIES_ATTACHMENTS);
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO Transfer data from old table to new
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public void recreateGoalsDB(){
    	SQLiteDatabase db = getWritableDatabase();
    	try{db.execSQL(PartyColumns.SQL_DROP_ENTRIES_PARTY);
		}catch(Exception e){e.printStackTrace();}
		try{db.execSQL(JournalColumns.SQL_DROP_ENTRIES_JOURNALS);
		}catch (Exception e) {		}
		try{db.execSQL(AttachmentColumns.SQL_DROP_ENTRIES_ATTACHMENTS);
		}catch (Exception e) {}
		
		db.execSQL(PartyColumns.SQL_CREATE_ENTRIES_PARTY);
        db.execSQL(JournalColumns.SQL_CREATE_ENTRIES_JOURNALS);
        db.execSQL(AttachmentColumns.SQL_CREATE_ENTRIES_ATTACHMENTS);
    }

    public boolean dropAllTables(){
        SQLiteDatabase db = getWritableDatabase();
        try{
            db.execSQL(AttachmentColumns.SQL_DROP_ENTRIES_ATTACHMENTS);
            db.execSQL(JournalColumns.SQL_DROP_ENTRIES_JOURNALS);
            db.execSQL(PartyColumns.SQL_DROP_ENTRIES_PARTY);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }



}
