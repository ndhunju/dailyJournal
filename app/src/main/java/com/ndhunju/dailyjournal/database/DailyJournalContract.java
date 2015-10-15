package com.ndhunju.dailyjournal.database;

import android.provider.BaseColumns;

/**
 * This class defines the structure of the tables in the database
 * It also defines relevant data and common SQL statements
 */
public abstract class DailyJournalContract {

    //Database Data types
    private static final String TEXT_TYPE = " TEXT ";
    private static final String BOOLEAN_TYPE = " BOOLEAN ";
    private static final String INTEGER = " INTEGER ";
    private static final String ALLOW_NULL = " NULL ";
    private static final String LONG = " LONG ";
    private static final String DOUBLE = " DOUBLE ";

    private static final String COMMA_SEP = " , ";


    /* Inner class that defines the table contents */
    public interface PartyColumns extends BaseColumns {

        //Values for Merchant Table
        String TABLE_PARTY = "party";
        String PARTY_ID = "id";
        String COL_PARTY_NAME = "name";
        String COL_PARTY_PHONE = "phone";
        String COL_PARTY_TYPE = "type";
        String COL_PARTY_DR_AMT= "drAmt";
        String COL_PARTY_CR_AMT= "crAmt";
        String COL_PARTY_PICTURE = "picturePath";

        String SQL_CREATE_ENTRIES_PARTY =
                "CREATE TABLE " + TABLE_PARTY +
                        " (" +
                        PARTY_ID + INTEGER + " PRIMARY KEY AUTOINCREMENT," +
                        COL_PARTY_NAME + TEXT_TYPE + "UNIQUE " + COMMA_SEP +
                        COL_PARTY_PHONE + TEXT_TYPE + COMMA_SEP +
                        COL_PARTY_TYPE + TEXT_TYPE + COMMA_SEP +
                        COL_PARTY_DR_AMT + DOUBLE + COMMA_SEP +
                        COL_PARTY_CR_AMT +  DOUBLE + COMMA_SEP +
                        COL_PARTY_PICTURE + TEXT_TYPE +
                        " )";

        String SQL_DROP_ENTRIES_PARTY =
                "DROP TABLE " + TABLE_PARTY;
    }

    public interface JournalColumns extends BaseColumns {

        //Values for Journal Table
        String TABLE_JOURNAL = "journal";
        String JOURNAL_ID = "id";
        String COL_JOURNAL_DATE = "date";
        String COL_JOURNAL_ADDED_DATE = "addedDate";
        String COL_JOURNAL_TYPE = "type";
        String COL_JOURNAL_AMOUNT = "amount";
        String COL_JOURNAL_NOTE = "note";
        String COL_PARTY_ID = "merchantId";

        String SQL_CREATE_ENTRIES_JOURNALS =
                "CREATE TABLE " + TABLE_JOURNAL +
                        " ( " + JOURNAL_ID + INTEGER + " PRIMARY KEY AUTOINCREMENT, " +
                        COL_JOURNAL_NOTE + TEXT_TYPE + COMMA_SEP +
                        COL_JOURNAL_DATE + LONG + COMMA_SEP +
                        COL_JOURNAL_ADDED_DATE + LONG + COMMA_SEP +
                        COL_JOURNAL_TYPE + TEXT_TYPE + COMMA_SEP +
                        COL_JOURNAL_AMOUNT + DOUBLE + COMMA_SEP +
                        COL_PARTY_ID + LONG +
                        " )";

        String SQL_DROP_ENTRIES_JOURNALS =
                "DROP TABLE " + TABLE_JOURNAL;

    }

    public interface AttachmentColumns extends BaseColumns {

        //Values for Attachment Table
        String TABLE_NAME_ATTACHMENTS = "attachment";
        String ATTACHMENT_ID = "id";
        String COL_ATTACHMENT_NAME = "fileName";
        String COL_FK_JOURNAL_ID = "journalId";

        String SQL_CREATE_ENTRIES_ATTACHMENTS =
                "CREATE TABLE " + TABLE_NAME_ATTACHMENTS +
                        " (" +
                        ATTACHMENT_ID + INTEGER + " PRIMARY KEY AUTOINCREMENT," +
                        COL_ATTACHMENT_NAME + TEXT_TYPE + COMMA_SEP +
                        COL_FK_JOURNAL_ID + LONG +
                        " )";

        String SQL_DROP_ENTRIES_ATTACHMENTS =
                "DROP TABLE " + TABLE_NAME_ATTACHMENTS;

    }
}
