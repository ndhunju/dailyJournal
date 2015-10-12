package com.ndhunju.dailyjournal.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ndhunju.dailyjournal.model.Attachment;

import com.ndhunju.dailyjournal.database.DailyJournalContract.AttachmentColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 9/18/2015.
 * This is a Data Access Object for {@link Attachment}
 */
public class AttachmentDAO implements GenericDAO<Attachment, Long> {

    private SQLiteOpenHelper mSqLiteOpenHelper;

    //Constructor
    public AttachmentDAO(SQLiteOpenHelper sqLiteOpenHelper){
        mSqLiteOpenHelper = sqLiteOpenHelper;
    }

    @Override
    public long create(Attachment attch) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.insert(AttachmentColumns.TABLE_NAME_ATTACHMENTS, null, toContentValues(attch));
    }


    @Override
    public Attachment find(Long id) {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        String selection = AttachmentColumns.ATTACHMENT_ID + "=" + id;
        Cursor c = db.query(true, AttachmentColumns.TABLE_NAME_ATTACHMENTS, null,
                            selection, null, null, null, null, null);

        if(!c.moveToFirst()) return null;

        return fromCursor(c);
    }

    @Override
    public List<Attachment> findAll() {
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        Cursor c = db.query(true, AttachmentColumns.TABLE_NAME_ATTACHMENTS, null,
                            null, null, null, null, null, null);

        List<Attachment> temp = new ArrayList<>();
        if(!c.moveToFirst())  return temp;

        do{
            temp.add(fromCursor(c));
        }while(c.moveToNext());

        return temp;
    }

    public List<Attachment> findAll(long journalId){
        SQLiteDatabase db = mSqLiteOpenHelper.getReadableDatabase();
        String selection = AttachmentColumns.COL_FK_JOURNAL_ID + "=" + journalId;
        Cursor c = db.query(AttachmentColumns.TABLE_NAME_ATTACHMENTS, null, selection,
                            null, null, null, null);
        List<Attachment> attchs = new ArrayList<>();

        if(!c.moveToFirst()) return attchs;

        do{ attchs.add(fromCursor(c));}
        while(c.moveToNext());
        return attchs;
    }

    @Override
    public long update(Attachment attch) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        String selection = AttachmentColumns.ATTACHMENT_ID + "=" + attch.getId();
        return db.update(AttachmentColumns.TABLE_NAME_ATTACHMENTS, toContentValues(attch), selection, null);
    }

    @Override
    public void delete(Long aLong) {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        String selection = AttachmentColumns.ATTACHMENT_ID +"="+aLong;
        db.delete(AttachmentColumns.TABLE_NAME_ATTACHMENTS, selection, null);
    }

    @Override
    public void delete(Attachment attch) {
       delete(attch.getId());
    }

    public int deleteAll(long journalId){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.delete(AttachmentColumns.TABLE_NAME_ATTACHMENTS,
                AttachmentColumns.COL_FK_JOURNAL_ID + "=" + journalId, null);
    }

    public int truncateTable(){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        return db.delete(AttachmentColumns.TABLE_NAME_ATTACHMENTS, null,null);
    }

    //Static methods
    public static long create(Attachment attachment, SQLiteDatabase db){
        return db.insert(AttachmentColumns.TABLE_NAME_ATTACHMENTS, null, toContentValues(attachment));
    }

    /**
     * Returns Attachment object from passed cursor
     * @param c
     * @return
     */
    private static Attachment fromCursor(Cursor c){
        long id = c.getLong((c.getColumnIndexOrThrow(AttachmentColumns.ATTACHMENT_ID)));
        String name = c.getString(c.getColumnIndexOrThrow(AttachmentColumns.COL_ATTACHMENT_NAME));
        long journalId = c.getLong(c.getColumnIndexOrThrow(AttachmentColumns.COL_FK_JOURNAL_ID));
        Attachment a = new Attachment(journalId);
        a.setId(id);
        a.setPath(name);
        return a;
    }

    /**
     * Returns ContentValues object with values put form the passed
     * attachment. <Note>Ignores the Id of the attachment</Note>
     * @param attch
     * @return
     */
    private static ContentValues toContentValues(Attachment attch){
        ContentValues values = new ContentValues();
        //values.put(AttachmentColumns.ATTACHMENT_ID, attch.getId());
        values.put(AttachmentColumns.COL_ATTACHMENT_NAME, attch.getPath());
        values.put(AttachmentColumns.COL_FK_JOURNAL_ID, attch.getJournalId());
        return values;
    }

}
