package com.ndhunju.dailyjournal.database;

import android.provider.BaseColumns;

/**
 * This class defines the structure of the tables in the old database
 */
public final class DailyJournalContractOld {

    // To prevent someone from accidentally instantiating the contract class,
    // give it a private empty constructor.
    private DailyJournalContractOld() {}

    /* Inner class that defines the table contents */
    public static abstract class PartyColumnsOld implements BaseColumns {

        //Values for Merchant Table
        public static final String TABLE_NAME_MERCHANT = "merchant";
        public static final String MERCHANT_ID = "id";
        public static final String COL_MERCHANT_NAME = "name";
        public static final String COL_MERCHANT_PHONE = "phone";
        public static final String COL_MERCHANT_TYPE = "type";
        public static final String COL_MERCHANT_JOURNALS = "journals";

    }

    public static abstract class JournalColumnsOld implements BaseColumns {

        //Values for Journal Table
        public static final String TABLE_NAME_JOURNAL = "journal";
        public static final String JOURNAL_ID = "journal_id";
        public static final String COL_JOURNAL_DATE = "date";
        public static final String COL_ADDED_DATE = "added_date";
        public static final String COL_JOURNAL_TYPE = "type";
        public static final String COL_JOURNAL_AMOUNT = "amount";
        public static final String COL_JOURNAL_NOTE = "note";
        public static final String COL_JOURNAL_ATTACHMENTS = "attachements";
        public static final String COL_JOURNAL_MERCHANT_ID = "merchantId";
        public static final String COL_JOURNAL_PARENT_ARRAY = PartyColumnsOld.COL_MERCHANT_JOURNALS;

    }

    public static abstract class AttachmentColumnsOld implements BaseColumns {

        //Values for Attachment Table
        public static final String TABLE_NAME_ATTACHMENTS = "attachment";
        public static final String COL_ATTACHMENT_NAME = "file_name";
        public static final String  COL_ATTACHMENT_PARENT = JournalColumnsOld.COL_JOURNAL_ATTACHMENTS;

    }
}
