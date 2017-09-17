package com.ndhunju.dailyjournal.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.UiThread;

import com.ndhunju.dailyjournal.database.DailyJournalContract.JournalColumns;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 9/18/2015.
 * Data Access Object for {@link Journal}
 */
public class JournalDAO implements IJournalDAO {

    private SQLiteOpenHelper mSqLiteOpenHelper;
    private List<Observer> mObservers;
    private long rowsAffected;          // use same identifier/variable

    public JournalDAO(SQLiteOpenHelper sqLiteOpenHelper){
        mSqLiteOpenHelper = sqLiteOpenHelper;
        mObservers = new ArrayList<>();
    }

    @Override
    public long create(Journal journal) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        long rowId = db.insert(JournalColumns.TABLE_JOURNAL, null, toContentValues(journal));
        if (rowId != -1) notifyJournalAdded(journal);
        return rowId;
        // Possible logic for caching and using cached value in the memory
//        if (id != -1) {
//            journal.setId(id);
//            List<Journal> journals;
//            if (mJournals.containsKey(journal.getPartyId())) {
//                // was previously cached
//                journals = mJournals.get(journal.getPartyId());
//                if (journal.getDate() >= journals.get(journals.size() - 1).getDate()) {
//                    // journal with later date added. new item will go at the end of the list
//                    journals.add(journal);
//                    notifyJournalAdded(journal);
//                } else {
//                    // order is disrupted . update the cached list
//                    mJournals.put(journal.getPartyId(), findAll(journal.getPartyId()));
//                    notifyJournalDataSetChanged(journal.getPartyId());
//                }
//            } else {
//                // first time being cached
//                journals = new ArrayList<>();
//                journals.add(journal);
//                mJournals.put(journal.getPartyId(), journals);
//                notifyJournalAdded(journal);
//            }
//        }
//        return id;
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

    @Override
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
// Possible logic for caching and using cached value in the memory
//    public List<Journal> findAllInCacheFirst(long partyId){
//        // check cache
//        if (mJournals.containsKey(partyId)) return mJournals.get(partyId);
//
//        // database
//        List<Journal> temp = findAll(partyId);
//
//        // cache it
//        mJournals.put(partyId, temp);
//
//        return temp;
//    }

    public List<Journal> findByNotes(String keywords) {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();

        //order by date
        Cursor c = db.query(JournalColumns.TABLE_JOURNAL, null,
                JournalColumns.COL_JOURNAL_NOTE + " like '%" + keywords + "%' ", null, null, null,
                JournalColumns.COL_JOURNAL_DATE, null);

        List<Journal> temp = new ArrayList<>();
        if(!c.moveToFirst()) return temp;

        Journal journal;
        do{
            journal = fromCursor(c);
            temp.add(journal);
        }while(c.moveToNext());

        c.close();
        return temp;
    }

    public List<Journal> findByDate(long startDate, long endDate, long... excludeJournalsWithId) {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();

        //order by date
        Cursor c = db.query(JournalColumns.TABLE_JOURNAL, null,
                JournalColumns.COL_JOURNAL_DATE + " >= " + startDate + " AND " + JournalColumns.COL_JOURNAL_DATE + " < " + (endDate + Constants.ONE_DAY_IN_MILLI) , null, null, null,
                JournalColumns.COL_JOURNAL_DATE, null);

        List<Journal> temp = new ArrayList<>();
        if(!c.moveToFirst()) return temp;

        Journal journal;
        do{
            journal = fromCursor(c);
            if (!Utils.contains(excludeJournalsWithId, journal.getId())) {
                temp.add(journal);
            }
        }while(c.moveToNext());

        c.close();
        return temp;
    }

    @Override
    public List<Journal> findByPartyAndDate(long partyId, long date) {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();

        Cursor c = db.query(JournalColumns.TABLE_JOURNAL, null, (partyId > 0
                ? JournalColumns.COL_PARTY_ID + " == " + partyId + " AND "
                : "")
                        + JournalColumns.COL_JOURNAL_DATE + " >= " + date + " AND "
                        + JournalColumns.COL_JOURNAL_DATE + " < " + (date + Constants.ONE_DAY_IN_MILLI), null, null, null,
                JournalColumns.COL_JOURNAL_DATE, null);

        List<Journal> temp = new ArrayList<>();
        if(!c.moveToFirst()) return temp;

        Journal journal;
        do{
            journal = fromCursor(c);
            temp.add(journal);
        }while(c.moveToNext());

        c.close();
        return temp;
    }

    @Override
    public long update(Journal journal) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        rowsAffected = db.update(JournalColumns.TABLE_JOURNAL, toContentValues(journal),
                         JournalColumns.JOURNAL_ID +"="+journal.getId(),null);
        if (rowsAffected > 0) notifyJournalChanged(journal);
        return rowsAffected;
    }

    // Possible logic for caching and using cached value in the memory
//    public long updateInCache(Journal journal) {
//        Journal oldJournal = find(journal.getId());
//
//        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
//        rowsAffected = db.update(JournalColumns.TABLE_JOURNAL, toContentValues(journal),
//                JournalColumns.JOURNAL_ID +"="+journal.getId(),null);
//
//        if (rowsAffected > 0) {
//            List<Journal> journals = mJournals.get(journal.getPartyId());
//            if (journal.getTag() instanceof Integer) {
//                int pos = (Integer) journal.getTag();
//                if (journals.get(pos).getId() == journal.getId()) {
//                    if (journal.getDate() != oldJournal.getDate()) {
//                        // journal date changed. could be in any order. update the cache
//                        mJournals.put(journal.getPartyId(), findAll(journal.getPartyId()));
//                        notifyJournalDataSetChanged(journal.getPartyId());
//                    } else {
//                        // order didn't change. just update local cache.
//                        journals.set(pos, journal);
//                        notifyJournalChanged(journal);
//                    }
//
//                    return rowsAffected;
//                }
//            }
//
//            // position not found. loop through journals
//            for (int i = 0; i < journals.size() ; i++) {
//                if (journals.get(i).getId() == journal.getId()) {
//                    if (journal.getDate() != oldJournal.getDate()) {
//                        // journal date changed. could be in any order. update the cache
//                        mJournals.put(journal.getPartyId(), findAll(journal.getPartyId()));
//                        notifyJournalDataSetChanged(journal.getPartyId());
//                    } else {
//                        // order didn't change. just update local cache.
//                        journals.set(i, journal);
//                        notifyJournalChanged(journal);
//                    }
//
//                    return rowsAffected;
//                }
//            }
//
//            notifyJournalDataSetChanged(journal.getPartyId());
//        }
//        return rowsAffected;
//    }


    @Override
    public void delete(Journal journal) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        rowsAffected = db.delete(JournalColumns.TABLE_JOURNAL, JournalColumns.JOURNAL_ID +"="+journal.getId(),null );
        if (rowsAffected > 0) notifyJournalDeleted(journal);
    }

// Possible logic for caching and using cached value in the memory
//    public void deleteInCache(Journal journal) {
//        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
//        rowsAffected = db.delete(JournalColumns.TABLE_JOURNAL, JournalColumns.JOURNAL_ID +"="+journal.getId(),null );
//        if (rowsAffected > 0) {
//            // remove from local cache
//            mJournals.get(journal.getPartyId()).remove(journal);
//            notifyJournalDeleted(journal);
//        }
//    }

    @Override
    public void deleteAll(long partyId){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.delete(JournalColumns.TABLE_JOURNAL,
                DailyJournalContract.PartyColumns.PARTY_ID + "=" + partyId, null);
    }

    @Override
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

    public void registerObserver(Observer observer) {
        mObservers.add(observer);
    }

    public void unregisterObserver(Observer observer) {
        mObservers.remove(observer);
    }

    @UiThread
    private void notifyJournalChanged(Journal journal) {

        for(Observer observer: mObservers)
            observer.onJournalChanged(journal);

    }

    @UiThread
    private void notifyJournalDeleted(Journal journal) {
        for (Observer observer: mObservers)
            observer.onJournalDeleted(journal);
    }

    @UiThread
    private void notifyJournalAdded(Journal journal) {
        for (Observer observer: mObservers)
            observer.onJournalAdded(journal);
    }

    @UiThread
    private void notifyJournalDataSetChanged(long partyId) {
        for (Observer observer: mObservers)
            observer.onJournalDataSetChanged(partyId);
    }

}
