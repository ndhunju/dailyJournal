package com.ndhunju.dailyjournal.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.ndhunju.dailyjournal.controller.DailyJournalApplication;
import com.ndhunju.dailyjournal.database.DailyJournalContract.PartyColumns;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is Data Access Object for Party
 * <b>Note: This class is not thread safe</b>
 */
public class PartyDAO implements IPartyDAO {

    //Variables
    private SQLiteOpenHelper mSqLiteOpenHelper;
    private List<Observer> mObservers;
    private int rowsAffected;                   // use same identifier/variable

    //Constructor
    public PartyDAO(SQLiteOpenHelper sqLiteOpenHelper){
        mSqLiteOpenHelper = sqLiteOpenHelper;
        mObservers = new ArrayList<>();
//        mParties = new ArrayList<>();
    }

    /**
     * Inserts the passed party in the database
     * @return row id of newly inserted row. if you have column with integer data type and PK then
     * it returns the values of this column other tables maintains a separate row id column
     */
    @Override
    public long create(Party party) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        long rowId = db.insert(PartyColumns.TABLE_PARTY, null, toContentValues(party));
        if (rowId != -1) {
            // Possible logic for caching and using cached value in the memory
//            party.setId(rowId);
//              update cache with new list
//            mParties = findAll();
            notifyPartyAdded(party);
        }
        return rowId;
    }

    @Override
    public Party find(Long id) {
        // check database
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(PartyColumns.TABLE_PARTY, null,
                PartyColumns.PARTY_ID + "=" + id, null, null, null, null, null);

        if(!c.moveToFirst()) return null;
        Party party = fromCursor(c);
        c.close();
        return party;
    }

// Possible logic for caching and using cached value in the memory
//    public Party findInCacheFirst(Long id) {
//        // check memory cache first
//        if (mParties.containsKey(id)) return mParties.get(id);
//
//        // check database
//        Party party = find(id);
//
//        // cache it
//        mParties.put(party.getId(), party);
//        return party;
//    }

    /**
     * This method updates the Debit column of respective party row
     * <h1>Eg. 1. current row</h1>
     * id | .... | drAmt | crAmt<br></br>
     * 1  | .... | 200.00| 100.00 <br></br>
     * exec -> updateDr(1, 100.00, "+")<br></br>
     * new row, <br></br>
     * id | .... | drAmt | crAmt<br></br>
     * 1  | .... | 300.00| 100.00<br></br>
     *
     * @param operation
     * @return
     */
    @Override
    public int updateDr(Journal journal, String operation){
        rowsAffected = execUpdate(PartyColumns.TABLE_PARTY, PartyColumns.COL_PARTY_DR_AMT,
                journal.getAmount(), operation, PartyColumns.PARTY_ID, journal.getPartyId());
        if (rowsAffected > 0) {
            // Possible logic for caching and using cached value in the memory
            // update cached value
//            Party updatedParty = find(journal.getPartyId());
//            mParties.put(updatedParty.getId(), updatedParty);
            notifyPartyChanged(find(journal.getPartyId()));
        }
        return rowsAffected;
    }


    /**
     * Updates the credit column of respective party row
     * @param operation
     * @return
     */
    @Override
    public int updateCr(Journal journal, String operation){
        rowsAffected =  execUpdate(PartyColumns.TABLE_PARTY, PartyColumns.COL_PARTY_CR_AMT,
                           journal.getAmount(), operation, PartyColumns.PARTY_ID, journal.getPartyId());
        if (rowsAffected > 0) {
            // Possible logic for caching and using cached value in the memory
            // update cached value
//            Party updatedParty = find(journal.getPartyId());
//            mParties.put(updatedParty.getId(), updatedParty);
            notifyPartyChanged(find(journal.getPartyId()));
        }
        return rowsAffected;
    }

    private int execUpdate(String table, String column, double amount, String operation, String primaryKey, long val){
        String sql = "UPDATE " + table + " SET " + column + "=" + column + operation + " ? WHERE " + primaryKey + "=?";
        SQLiteStatement sqlSt = mSqLiteOpenHelper.getWritableDatabase().compileStatement(sql);
        sqlSt.bindDouble(1, amount);
        sqlSt.bindLong(2, val);
        return sqlSt.executeUpdateDelete();
    }

    public int resetDrCrBalance() {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues(2);
        values.put(PartyColumns.COL_PARTY_CR_AMT, 0);
        values.put(PartyColumns.COL_PARTY_DR_AMT, 0);
        rowsAffected = db.update(PartyColumns.TABLE_PARTY, values, null, null);
        return rowsAffected;
    }

    @Override
    public List<Party> findAll() {
        // get the fresh list from database in chronological order
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(true, PartyColumns.TABLE_PARTY, null, null, null, null, null,
                PartyColumns.COL_PARTY_NAME, null);

        List<Party> temp = new ArrayList<>();
        if(!c.moveToFirst()) return temp;
        Party current;
        do{
            current = fromCursor(c);
            temp.add(current);
            // Possible logic for caching and using cached value in the memory
            // cache it
//            mParties.put(current.getId(), current);
        }while(c.moveToNext());
        c.close();
        return temp;
    }

    /**
     *  Returns the array[limit] with largest Credit or Debit balance among {@link Party}
     * @param type : type of jounal
     * @param limit : limit size
     */
    @Override
    public Party[] findTopDrCrAmt(Journal.Type type, int limit){
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();

        String orderBy = type == Journal.Type.Credit ? PartyColumns.COL_PARTY_CR_AMT
                : PartyColumns.COL_PARTY_DR_AMT;
        Cursor c = db.query(true, PartyColumns.TABLE_PARTY, null, null, null, null, null, orderBy
                + " DESC", String.valueOf(limit));

        Party[] temp = new Party[limit];
        if(!c.moveToFirst()) return temp;

        int index = 0;
        do{
            temp[index++] = fromCursor(c);
        }while(c.moveToNext());

        c.close();

        return temp;


    }

    @Override
    public long update(Party party) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        rowsAffected = db.update(PartyColumns.TABLE_PARTY, toContentValues(party),
                PartyColumns.PARTY_ID + "=" + party.getId(), null);
        if (rowsAffected > 0 ) {
            // Possible logic for caching and using cached value in the memory
//            mParties.put(party.getId(), party);
            notifyPartyChanged(party);
        }
        return rowsAffected;
    }

    @Override
    public void delete(Party party) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        rowsAffected = db.delete(PartyColumns.TABLE_PARTY, PartyColumns.PARTY_ID + "=" + party.getId(), null);
        if (rowsAffected > 0) {
            notifyPartyDeleted(party);
        }
    }

    @Override
    public String[] getNamesAsArray(){
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(PartyColumns.TABLE_PARTY, new String[]{PartyColumns.COL_PARTY_NAME},
                            null, null, null, null, PartyColumns.COL_PARTY_NAME);

        String[] names = new String[c.getCount()];
        if(!c.moveToFirst())return names;
        int i = 0;
        do{
            names[i++]= (c.getString(c.getColumnIndex(PartyColumns.COL_PARTY_NAME)));
        }while(c.moveToNext());
        c.close();
        return names;
    }

    /**
     * Returns total credit amount for the user
     * that is using this app
     */
    public double getUserCreditTotal() {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(PartyColumns.TABLE_PARTY,
                new String[]{PartyColumns.COL_PARTY_DR_AMT},
                null,
                null,
                null,
                null,
                PartyColumns.COL_PARTY_DR_AMT);

        if(!c.moveToFirst())return 0.0;
        double amount = 0.0;
        do{
            amount += (c.getLong(c.getColumnIndex(PartyColumns.COL_PARTY_DR_AMT)));
        }while(c.moveToNext());
        c.close();
        return amount;
    }

    /**
     * Returns total debit amout for the user
     * using this app
     */
    public double getUserDebitTotal() {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(PartyColumns.TABLE_PARTY,
                new String[]{PartyColumns.COL_PARTY_CR_AMT},
                null,
                null,
                null,
                null,
                PartyColumns.COL_PARTY_CR_AMT);

        if(!c.moveToFirst())return 0.0;

        double amount = 0.0;
        do{
            amount += (c.getLong(c.getColumnIndex(PartyColumns.COL_PARTY_CR_AMT)));
        }while(c.moveToNext());
        c.close();
        return amount;
    }

    /**
     * Deletes all the entry from the table but keeps the schema
     * @return
     */
    @Override
    public int truncateTable(){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.delete(PartyColumns.TABLE_PARTY, null,null);
    }


    //Static methods
    public static long create(Party party, SQLiteDatabase db){
        return db.insert(PartyColumns.TABLE_PARTY, null, toContentValues(party));
    }

    private static ContentValues toContentValues(Party party){
        ContentValues values = new ContentValues();
        //values.put(PartyColumns.PARTY_ID, party.getId()); //will never change
        values.put(PartyColumns.COL_PARTY_NAME, party.getName());
        values.put(PartyColumns.COL_PARTY_NOTE, party.getNote());
        values.put(PartyColumns.COL_PARTY_PHONE, party.getPhone());
        values.put(PartyColumns.COL_PARTY_TYPE, party.getType().toString());
        values.put(PartyColumns.COL_PARTY_DR_AMT, party.getDebitTotal());
        values.put(PartyColumns.COL_PARTY_CR_AMT, party.getCreditTotal());
        values.put(PartyColumns.COL_PARTY_PICTURE, party.getPicturePath());
        return values;
    }

    private static Party fromCursor(Cursor c){
        long id = c.getLong(c.getColumnIndexOrThrow(PartyColumns.PARTY_ID));
        String name = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_NAME));
        String note = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_NOTE));
        String phone = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_PHONE));
        //Since Debitors was corrected to Debtors, Type.valueOf("Debitors") throws error
        String typeStr = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_TYPE));
        Party.Type type = typeStr.equals("Debitors") ? Party.Type.Debtors : Party.Type.valueOf(typeStr);
        double drAmt = c.getDouble(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_DR_AMT));
        double crAmt = c.getDouble(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_CR_AMT));
        String picture = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_PICTURE));

        Party party = new Party(name, id);
        party.setNote(note);
        party.setPhone(phone);
        party.setType(type);
        party.setDebitTotal(drAmt);
        party.setCreditTotal(crAmt);
        party.setPicturePath(picture);
        //party.addJournal();

        return party;
    }

    public void registerObserver(Observer observer) {
        mObservers.add(observer);
    }

    public void unregisterObserver(Observer observer) {
        mObservers.remove(observer);
    }

    private void notifyPartyChanged(final Party party) {
        // most observers are fragments that mostly modifies UI
        DailyJournalApplication.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(Observer observer: mObservers)
                    observer.onPartyChanged(party);
            }
        });
    }

    private void notifyPartyDeleted(final Party party) {
        DailyJournalApplication.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Observer observer: mObservers)
                    observer.onPartyDeleted(party);
            }
        });
    }

    private void notifyPartyAdded(final Party party) {
        DailyJournalApplication.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Observer observer: mObservers)
                    observer.onPartyAdded(party);
            }
        });
    }

}
