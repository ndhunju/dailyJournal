package com.ndhunju.dailyjournal.database;

import android.provider.BaseColumns;

/**
 * This class defines the structure of the tables in the database
 * It also defines relevant data and common SQL statements
 */
public final class DailyJournalContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it a private empty constructor.
    private DailyJournalContract() {
    }

    //Database Data types
    private static final String TEXT_TYPE = " TEXT ";
    private static final String BOOLEAN_TYPE = " BOOLEAN ";
    private static final String INTEGER = " INTEGER ";
    private static final String ALLOW_NULL = " NULL ";
    private static final String LONG = " LONG ";
    private static final String DOUBLE = " DOUBLE ";

    private static final String COMMA_SEP = " , ";

    /* Inner class that defines the table contents */
    public static abstract class PartyColumns implements BaseColumns {

        //Values for Merchant Table
        public static final String TABLE_PARTY = "party";
        public static final String PARTY_ID = "id";
        public static final String COL_PARTY_NAME = "name";
        public static final String COL_PARTY_PHONE = "phone";
        public static final String COL_PARTY_TYPE = "type";
        public static final String COL_PARTY_DR_AMT= "drAmt";
        public static final String COL_PARTY_CR_AMT= "crAmt";

        public static final String SQL_CREATE_ENTRIES_PARTY =
                "CREATE TABLE " + TABLE_PARTY +
                        " (" +
                        PARTY_ID + INTEGER + " PRIMARY KEY AUTOINCREMENT," +
                        COL_PARTY_NAME + TEXT_TYPE + "UNIQUE " + COMMA_SEP +
                        COL_PARTY_PHONE + TEXT_TYPE + COMMA_SEP +
                        COL_PARTY_TYPE + TEXT_TYPE + COMMA_SEP +
                        COL_PARTY_DR_AMT + DOUBLE + COMMA_SEP +
                        COL_PARTY_CR_AMT +  DOUBLE +
                        " )";

        public static final String SQL_DROP_ENTRIES_PARTY =
                "DROP TABLE " + TABLE_PARTY;
    }

    public static abstract class JournalColumns implements BaseColumns {

        //Values for Journal Table
        public static final String TABLE_JOURNAL = "journal";
        public static final String JOURNAL_ID = "id";
        public static final String COL_JOURNAL_DATE = "date";
        public static final String COL_JOURNAL_ADDED_DATE = "addedDate";
        public static final String COL_JOURNAL_TYPE = "type";
        public static final String COL_JOURNAL_AMOUNT = "amount";
        public static final String COL_JOURNAL_NOTE = "note";
        public static final String COL_PARTY_ID = "merchantId";

        public static final String SQL_CREATE_ENTRIES_JOURNALS =
                "CREATE TABLE " + TABLE_JOURNAL +
                        " ( " + JOURNAL_ID + INTEGER + " PRIMARY KEY AUTOINCREMENT, " +
                        COL_JOURNAL_NOTE + TEXT_TYPE + COMMA_SEP +
                        COL_JOURNAL_DATE + LONG + COMMA_SEP +
                        COL_JOURNAL_ADDED_DATE + LONG + COMMA_SEP +
                        COL_JOURNAL_TYPE + TEXT_TYPE + COMMA_SEP +
                        COL_JOURNAL_AMOUNT + DOUBLE + COMMA_SEP +
                        COL_PARTY_ID + LONG +
                        " )";

        public static final String SQL_DROP_ENTRIES_JOURNALS =
                "DROP TABLE " + TABLE_JOURNAL;

    }

    public static abstract class AttachmentColumns implements BaseColumns {

        //Values for Attachment Table
        public static final String TABLE_NAME_ATTACHMENTS = "attachment";
        public static final String ATTACHMENT_ID = "id";
        public static final String COL_ATTACHMENT_NAME = "fileName";
        public static final String COL_FK_JOURNAL_ID = "journalId";

        public static final String SQL_CREATE_ENTRIES_ATTACHMENTS =
                "CREATE TABLE " + TABLE_NAME_ATTACHMENTS +
                        " (" +
                        ATTACHMENT_ID + INTEGER + " PRIMARY KEY AUTOINCREMENT," +
                        COL_ATTACHMENT_NAME + TEXT_TYPE + COMMA_SEP +
                        COL_FK_JOURNAL_ID + LONG +
                        " )";

        public static final String SQL_DROP_ENTRIES_ATTACHMENTS =
                "DROP TABLE " + TABLE_NAME_ATTACHMENTS;

    }
}
