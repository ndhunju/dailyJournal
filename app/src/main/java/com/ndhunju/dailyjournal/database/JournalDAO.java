package com.ndhunju.dailyjournal.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.database.DailyJournalContract.JournalColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 9/18/2015.
 * Data Access Object for {@link Journal}
 */
public class JournalDAO implements GenericDAO<Journal, Long> {

    private SQLiteOpenHelper mSqLiteOpenHelper;

    public JournalDAO(SQLiteOpenHelper sqLiteOpenHelper){
        mSqLiteOpenHelper = sqLiteOpenHelper;
    }

    @Override
    public long create(Journal journal) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.insert(JournalColumns.TABLE_JOURNAL, null, toContentValues(journal));
    }

    @Override
    public Journal find(Long aLong) {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(true, JournalColumns.TABLE_JOURNAL, null,
                JournalColumns.JOURNAL_ID + "=" + aLong, null, null, null, null, null);

        if(c == null || !c.moveToFirst()) return null;
        Journal journal = fromCursor(c);
        c.close();
        return journal;
    }

    @Override
    public List<Journal> findAll() {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(JournalColumns.TABLE_JOURNAL, null, null, null, null, null, null, null);
        List<Journal> temp = new ArrayList<>();
        if(!c.moveToFirst()) return temp;
        do{temp.add(fromCursor(c));
        }while(c.moveToNext());
        c.close();
        return temp;
    }

    public List<Journal> findAll(long partyId){
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();

        //order by date
        Cursor c = db.query(JournalColumns.TABLE_JOURNAL, null,
                            JournalColumns.COL_PARTY_ID + "=" + partyId, null, null, null,
                            JournalColumns.COL_JOURNAL_DATE, null);

        List<Journal> temp = new ArrayList<>();
        if(!c.moveToFirst()) return temp;

        do{temp.add(fromCursor(c));
        }while(c.moveToNext());

        c.close();
        return temp;
    }

    @Override
    public long update(Journal journal) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.update(JournalColumns.TABLE_JOURNAL, toContentValues(journal),
                         JournalColumns.JOURNAL_ID +"="+journal.getId(),null);
    }

    @Override
    public void delete(Long aLong) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.delete(JournalColumns.TABLE_JOURNAL, JournalColumns.JOURNAL_ID +"="+aLong,null );
    }

    @Override
    public void delete(Journal journal) {
        delete(journal.getId());
    }

    public void deleteAll(long partyId){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.delete(JournalColumns.TABLE_JOURNAL,
                DailyJournalContract.PartyColumns.PARTY_ID + "=" + partyId, null);
    }

    public int truncateTable(){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.delete(JournalColumns.TABLE_JOURNAL, null,null);
    }

    public static long create(Journal journal, SQLiteDatabase db){
        return db.insert(JournalColumns.TABLE_JOURNAL, null, toContentValues(journal));
    }

    private static ContentValues toContentValues(Journal journal){
        ContentValues values = new ContentValues();
        //values.put(JournalColumns.JOURNAL_ID, journal.getId());
        values.put(JournalColumns.COL_JOURNAL_AMOUNT, journal.getAmount());
        values.put(JournalColumns.COL_JOURNAL_DATE, journal.getDate());
        values.put(JournalColumns.COL_JOURNAL_NOTE, journal.getNote());
        values.put(JournalColumns.COL_JOURNAL_TYPE, journal.getType().toString());
        values.put(JournalColumns.COL_JOURNAL_ADDED_DATE, journal.getCreatedDate());
        values.put(JournalColumns.COL_PARTY_ID, journal.getPartyId());
        return values;
    }

    private static Journal fromCursor(Cursor c){
        long id = c.getLong(c.getColumnIndexOrThrow(JournalColumns.JOURNAL_ID));
        long date = c.getLong(c.getColumnIndexOrThrow(JournalColumns.COL_JOURNAL_DATE));
        long addedDate = c.getLong(c.getColumnIndexOrThrow(JournalColumns.COL_JOURNAL_ADDED_DATE));
        Journal.Type type = Journal.Type.valueOf(c.getString(c.getColumnIndexOrThrow(JournalColumns.COL_JOURNAL_TYPE)));
        double amount = c.getDouble(c.getColumnIndexOrThrow(JournalColumns.COL_JOURNAL_AMOUNT));
        String note = c.getString(c.getColumnIndexOrThrow(JournalColumns.COL_JOURNAL_NOTE));
        long partyId = c.getLong(c.getColumnIndex(JournalColumns.COL_PARTY_ID));

        Journal journal = new Journal(partyId, date, id);
        journal.setCreatedDate(addedDate);
        journal.setAmount(amount);
        journal.setType(type);
        journal.setNote(note);

        return journal;
    }
}
