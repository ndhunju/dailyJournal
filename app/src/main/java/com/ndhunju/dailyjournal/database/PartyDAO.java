package com.ndhunju.dailyjournal.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.ndhunju.dailyjournal.database.DailyJournalContract.PartyColumns;
import com.ndhunju.dailyjournal.model.Party;

import java.util.ArrayList;
import java.util.List;

public class PartyDAO implements GenericDAO<Party, Long> {


    private SQLiteOpenHelper mSqLiteOpenHelper;

    public PartyDAO(SQLiteOpenHelper sqLiteOpenHelper){
        mSqLiteOpenHelper = sqLiteOpenHelper;

    }

    /**
     * Inserts the passed party in the database
     * @param party
     * @return row id of newly inserted row. if you have column with integer data type and PK then
     * it returns the values of this column other tables maintains a separate rowid column
     */
    @Override
    public long create(Party party) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.insert(PartyColumns.TABLE_PARTY, null, toContentValues(party));
    }

    @Override
    public Party find(Long id) {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(PartyColumns.TABLE_PARTY, null,
                PartyColumns.PARTY_ID + "=" + id, null, null, null, null, null);

        if(!c.moveToFirst()) return null;

        return fromCursor(c);
    }

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
     * @param id
     * @param amount
     * @param operation
     * @return
     */
    public int updateDr(long id, double amount, String operation){
        return  execUpdate(PartyColumns.TABLE_PARTY, PartyColumns.COL_PARTY_DR_AMT,
                amount, operation, PartyColumns.PARTY_ID, id);
    }


    /**
     * Updates the credit column of respective party row
     * @param id
     * @param amount
     * @param operation
     * @return
     */
    public int updateCr(long id, double amount, String operation){
        return  execUpdate(PartyColumns.TABLE_PARTY, PartyColumns.COL_PARTY_CR_AMT,
                           amount, operation, PartyColumns.PARTY_ID, id);
    }

    private int execUpdate(String table, String column, double amount, String operation, String pk, long val){
        String sql = "UPDATE " + table + " SET " + column + "=" + column + operation + " ? WHERE " + pk + "=?";
        SQLiteStatement sqlSt = mSqLiteOpenHelper.getWritableDatabase().compileStatement(sql);
        sqlSt.bindDouble(1, amount);
        sqlSt.bindLong(2, val);
        return sqlSt.executeUpdateDelete();
    }



    public Party find(String partyName){
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(PartyColumns.TABLE_PARTY, null, PartyColumns.COL_PARTY_NAME + "= ?",
                new String[]{partyName}, null, null, null);

        if(!c.moveToFirst())   return null;
        return fromCursor(c);
    }

    @Override
    public List<Party> findAll() {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(true, PartyColumns.TABLE_PARTY, null, null, null, null, null,
                PartyColumns.COL_PARTY_NAME, null);

        List<Party> temp = new ArrayList<>();
        if(!c.moveToFirst()) return temp;

        do{
            temp.add(fromCursor(c));
        }while(c.moveToNext());

        return temp;
    }

    @Override
    public long update(Party party) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.update(PartyColumns.TABLE_PARTY, toContentValues(party),
                         PartyColumns.PARTY_ID +"="+party.getId(), null);
    }

    @Override
    public void delete(Long aLong) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.delete(PartyColumns.TABLE_PARTY, PartyColumns.PARTY_ID + "=" + aLong, null);
    }

    @Override
    public void delete(Party party) {
        delete(party.getId());
    }

    public List<String> getNames(){
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(PartyColumns.TABLE_PARTY, new String[]{PartyColumns.COL_PARTY_NAME},
                            null, null, null, null, PartyColumns.COL_PARTY_NAME);

        List<String> names = new ArrayList<>();
        if(!c.moveToFirst())return names;

        do{
            names.add(c.getString(c.getColumnIndex(PartyColumns.COL_PARTY_NAME)));
        }while(c.moveToNext());
        c.close();
        return names;
    }

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
     * Deletes all the entry from the table but keeps the schema
     * @return
     */
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
        String phone = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_PHONE));
        //Since Debitors was corrected to Debtors, Type.valueOf("Debitors") throws error
        String typeStr = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_TYPE));
        Party.Type type = typeStr.equals("Debitors") ? Party.Type.Debtors : Party.Type.valueOf(typeStr);
        double drAmt = c.getDouble(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_DR_AMT));
        double crAmt = c.getDouble(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_CR_AMT));
        String picture = c.getString(c.getColumnIndexOrThrow(PartyColumns.COL_PARTY_PICTURE));

        Party party = new Party(name, id);
        party.setPhone(phone);
        party.setType(type);
        party.setDebitTotal(drAmt);
        party.setCreditTotal(crAmt);
        party.setPicturePath(picture);
        //party.addJournal();

        return party;
    }


}
