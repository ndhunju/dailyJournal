package com.ndhunju.dailyjournal.service;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.ndhunju.dailyjournal.database.AttachmentDAO;
import com.ndhunju.dailyjournal.database.DailyJournalContract.PartyColumns;
import com.ndhunju.dailyjournal.database.DailyJournalContractOld.AttachmentColumnsOld;
import com.ndhunju.dailyjournal.database.DailyJournalContractOld.JournalColumnsOld;
import com.ndhunju.dailyjournal.database.DailyJournalContractOld.PartyColumnsOld;
import com.ndhunju.dailyjournal.database.JournalDAO;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

/**
 * Created by dhunju on 9/26/2015.
 * This class transfers data from old database to new database
 */
public class DBTransferService {

    private static DBTransferService mTransferService;
    private SQLiteDatabase mDb;

    public static DBTransferService getInstance(SQLiteDatabase db){
        if(mTransferService == null) mTransferService = new DBTransferService(db);
        return mTransferService;
    }

    private DBTransferService(SQLiteDatabase db){
        mDb = db;
    }

    public void transferToNewDb(String prefix){
        addOldPartiesToNewDB(prefix);
    }

    private void addOldPartiesToNewDB(String prefix) {

        String[] projectionOld = {PartyColumnsOld.COL_MERCHANT_NAME,
                PartyColumnsOld.COL_MERCHANT_PHONE,
                PartyColumnsOld.COL_MERCHANT_TYPE,
                PartyColumnsOld.COL_MERCHANT_JOURNALS};

        Cursor c = mDb.query(prefix + PartyColumnsOld.TABLE_NAME_MERCHANT,
                projectionOld, null, null, null, null, null);

        if(!c.moveToFirst()) return;

        do{
            String name = c.getString(c.getColumnIndexOrThrow(PartyColumnsOld.COL_MERCHANT_NAME));
            String phone = c.getString(c.getColumnIndexOrThrow(PartyColumnsOld.COL_MERCHANT_PHONE));
            String typeStr = c.getString(c.getColumnIndexOrThrow(PartyColumnsOld.COL_MERCHANT_TYPE));
            Party.Type type = typeStr.equals("Debitors") ? Party.Type.Debtors : Party.Type.valueOf(typeStr);
            String journalsHashCode = (c.getString(c.getColumnIndexOrThrow(PartyColumnsOld.COL_MERCHANT_JOURNALS)));

            Party party = new Party(name);
            party.setPhone(phone);
            party.setType(type);

            long partyId = PartyDAO.create(party, mDb);

            String selectionArg = JournalColumnsOld.COL_JOURNAL_PARENT_ARRAY + "=" + journalsHashCode;
            addOldJournalsToNewDB(prefix,selectionArg,mDb, partyId);

        }while(c.moveToNext());

        c.close();

    }

    private void addOldJournalsToNewDB(String prefix, String selection, SQLiteDatabase db, long partyId) {

        String[] projection = {
                JournalColumnsOld.COL_JOURNAL_DATE, JournalColumnsOld.COL_ADDED_DATE,
                JournalColumnsOld.COL_JOURNAL_TYPE, JournalColumnsOld.COL_JOURNAL_AMOUNT,
                JournalColumnsOld.COL_JOURNAL_NOTE, JournalColumnsOld.COL_JOURNAL_ATTACHMENTS,
                JournalColumnsOld.COL_JOURNAL_MERCHANT_ID };

        Cursor c = db.query( prefix + JournalColumnsOld.TABLE_NAME_JOURNAL,projection,selection,
                            null, null, null,null);

        if(!c.moveToFirst()) return;

        Journal journal = new Journal(partyId);

        do {
            long date = c.getLong(c.getColumnIndexOrThrow(JournalColumnsOld.COL_JOURNAL_DATE));
            long addedDate = c.getLong(c.getColumnIndexOrThrow(JournalColumnsOld.COL_ADDED_DATE));
            Journal.Type type = Journal.Type.valueOf(c.getString(c.getColumnIndexOrThrow(JournalColumnsOld.COL_JOURNAL_TYPE)));
            double amount = c.getDouble(c.getColumnIndexOrThrow(JournalColumnsOld.COL_JOURNAL_AMOUNT));
            String note = c.getString(c.getColumnIndexOrThrow(JournalColumnsOld.COL_JOURNAL_NOTE));
            String attachmentHashCode = c.getString(c.getColumnIndexOrThrow(JournalColumnsOld.COL_JOURNAL_ATTACHMENTS));

            journal.setDate(date);
            journal.setCreatedDate(addedDate);
            journal.setType(type);
            journal.setAmount(amount);
            journal.setNote(note);


            long journalId = -1;

            //When adding a new journal, add values to respective DrAmt or CrAmt column of Party table
            if (journal.getType() == Journal.Type.Debit) {
                execUpdateOnParty(PartyColumns.COL_PARTY_DR_AMT, amount, "+", partyId);
            } else {
                execUpdateOnParty(PartyColumns.COL_PARTY_CR_AMT, amount, "+", partyId);
            }

            journalId = JournalDAO.create(journal, mDb);

            String selectionArgs = AttachmentColumnsOld.COL_ATTACHMENT_PARENT + "=" + attachmentHashCode;
            addOldAttchToNewDB(prefix, selectionArgs,db, journalId);
        }while (c.moveToNext());

        c.close();
    }


    private int execUpdateOnParty(String column, double amount, String operation, long val){
        String sql = "UPDATE " + PartyColumns.TABLE_PARTY + " SET " + column + "=" + column + operation + " ? WHERE " + PartyColumns.PARTY_ID + "=?";
        SQLiteStatement sqlSt = mDb.compileStatement(sql);
        sqlSt.bindDouble(1, amount);
        sqlSt.bindLong(2, val);
        return sqlSt.executeUpdateDelete();
    }


    private  void addOldAttchToNewDB(String prefix, String selection, SQLiteDatabase db, long journalId) {

        String[] projection = { AttachmentColumnsOld.COL_ATTACHMENT_NAME, };

        Cursor c = db.query(prefix + AttachmentColumnsOld.TABLE_NAME_ATTACHMENTS,
                            projection,  selection, null,null,null, null);

        if(!c.moveToFirst()) return;

        Attachment attachment = new Attachment(journalId);

        do {
            String name = c.getString(c.getColumnIndex(AttachmentColumnsOld.COL_ATTACHMENT_NAME));
            attachment.setPath(name);
            AttachmentDAO.create(attachment, mDb);
        }while (c.moveToNext());

        c.close();
    }

}
