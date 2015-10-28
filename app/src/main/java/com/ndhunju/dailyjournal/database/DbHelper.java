package com.ndhunju.dailyjournal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ndhunju.dailyjournal.database.DailyJournalContract.AttachmentColumns;
import com.ndhunju.dailyjournal.database.DailyJournalContract.JournalColumns;
import com.ndhunju.dailyjournal.database.DailyJournalContract.PartyColumns;
import com.ndhunju.dailyjournal.database.DailyJournalContractOld.AttachmentColumnsOld;
import com.ndhunju.dailyjournal.database.DailyJournalContractOld.JournalColumnsOld;
import com.ndhunju.dailyjournal.database.DailyJournalContractOld.PartyColumnsOld;
import com.ndhunju.dailyjournal.service.DBTransferService;
import com.ndhunju.dailyjournal.util.UtilsDb;

public class DbHelper extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "DailyJournal.db";

    //Constructor
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        Log.i(DATABASE_NAME, "Upgrading database from " + oldVersion + " to " + newVersion);
        if(oldVersion == 1 && newVersion == DATABASE_VERSION){
            try {
                db.beginTransaction();

                //change the name of old table to temp_+table name
                String prefix = "TEMP_";
                db.execSQL(UtilsDb.getRenameTableSt(PartyColumnsOld.TABLE_NAME_MERCHANT, prefix));
                db.execSQL(UtilsDb.getRenameTableSt(JournalColumnsOld.TABLE_NAME_JOURNAL, prefix));
                db.execSQL(UtilsDb.getRenameTableSt(AttachmentColumnsOld.TABLE_NAME_ATTACHMENTS, prefix));

                //create new tables
                db.execSQL(PartyColumns.SQL_CREATE_ENTRIES_PARTY);
                db.execSQL(JournalColumns.SQL_CREATE_ENTRIES_JOURNALS);
                db.execSQL(AttachmentColumns.SQL_CREATE_ENTRIES_ATTACHMENTS);

                //transfer data
                DBTransferService.getInstance(db).transferToNewDb(prefix);

                //drop old tables
                db.execSQL(UtilsDb.getDropTableSt(PartyColumnsOld.TABLE_NAME_MERCHANT, prefix));
                db.execSQL(UtilsDb.getDropTableSt(JournalColumnsOld.TABLE_NAME_JOURNAL, prefix));
                db.execSQL(UtilsDb.getDropTableSt(AttachmentColumnsOld.TABLE_NAME_ATTACHMENTS, prefix));

                db.setTransactionSuccessful();
                Log.i(DATABASE_NAME, "Finished transferring data from version " + oldVersion + " to " + newVersion);
            }catch (RuntimeException ex){
                ex.printStackTrace();
            }finally {
                db.endTransaction();
            }

        }
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


}
