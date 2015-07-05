package com.ndhunju.dailyjournal.database;

import android.provider.BaseColumns;

public final class FeedReaderContract {

	// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FeedReaderContract() {}

    	/* Inner class that defines the table contents */
    	public static abstract class FeedEntry implements BaseColumns {
    		//Data types
    		private static final String TEXT_TYPE = " TEXT ";
    		//private static final String BOOLEAN_TYPE = " BOOLEAN ";
    		private static final String COMMA_SEP = " , ";
    		private static final String INTEGER = " INTEGER ";
    		//private static final String ALLOW_NULL = " NULL ";
    		private static final String LONG = " LONG ";
    		private static final String DOUBLE = " DOUBLE ";
    		
    		public static final String TABLE_NAME_MERCHANT = "merchant";
    		public static final String MERCHANT_ID = "id";
    		public static final String COL_MERCHANT_NAME = "name";
    		public static final String COL_MERCHANT_PHONE = "phone";
    		public static final String COL_MERCHANT_TYPE = "type";
    		public static final String COL_MERCHANT_JOURNALS = "journals";
    		
    		
    		public static final String TABLE_NAME_JOURNAL = "journal";
    		public static final String JOURNAL_ID = "journal_id";
    		public static final String COL_JOURNAL_DATE = "date";
    		public static final String COL_ADDED_DATE = "added_date";
    		public static final String COL_JOURNAL_TYPE = "type";
    		public static final String COL_JOURNAL_AMOUNT = "amount";
    		public static final String COL_JOURNAL_NOTE = "note";
    		public static final String COL_JOURNAL_ATTACHMENTS = "attachements";
    		public static final String COL_JOURNAL_MERCHANT_ID = "merchantId";
    		public static final String COL_JOURNAL_PARENT_ARRAY = COL_MERCHANT_JOURNALS;
    		
    		public static final String TABLE_NAME_ATTACHMENTS = "attachment";
    		public static final String COL_ATTACHMENT_NAME = "file_name";
    		public static final String  COL_ATTACHMENT_PARENT = COL_JOURNAL_ATTACHMENTS;
    		
    		
    		public static final String SQL_CREATE_ENTRIES_MERCHANTS =
    		    "CREATE TABLE " + TABLE_NAME_MERCHANT + 
    		    " (" +
    		    MERCHANT_ID + " " + TEXT_TYPE + " PRIMARY KEY," +
    		    COL_MERCHANT_NAME + TEXT_TYPE + COMMA_SEP +
    		    COL_MERCHANT_PHONE + TEXT_TYPE + COMMA_SEP +
    		    COL_MERCHANT_TYPE + TEXT_TYPE + COMMA_SEP +
    		    COL_MERCHANT_JOURNALS + TEXT_TYPE + 
    		    " )";

    		public static final String SQL_DELETE_ENTRIES_MERCHANTS =
    		    "DROP TABLE " + TABLE_NAME_MERCHANT;
    		    		
    		public static final String SQL_CREATE_ENTRIES_JOURNALS= 
    				"CREATE TABLE " + TABLE_NAME_JOURNAL+
    				" ( " + JOURNAL_ID + " TEXT PRIMARY KEY, " +
    				COL_JOURNAL_NOTE + TEXT_TYPE + COMMA_SEP +
        		    COL_JOURNAL_DATE + LONG + COMMA_SEP +
        		    COL_ADDED_DATE + LONG + COMMA_SEP +
        		    COL_JOURNAL_TYPE + TEXT_TYPE + COMMA_SEP +
        		    COL_JOURNAL_AMOUNT + DOUBLE + COMMA_SEP +
        		    COL_JOURNAL_ATTACHMENTS + TEXT_TYPE + COMMA_SEP +
        		    COL_JOURNAL_MERCHANT_ID + INTEGER + COMMA_SEP +
        		    COL_JOURNAL_PARENT_ARRAY + TEXT_TYPE +
        		    " )";
    		
    		public static final String SQL_CREATE_ENTRIES_ATTACHMENTS =
        		    "CREATE TABLE " + TABLE_NAME_ATTACHMENTS + 
        		    " (" +
        		    COL_ATTACHMENT_NAME + " TEXT PRIMARY KEY," + TEXT_TYPE + COMMA_SEP +
        		    COL_ATTACHMENT_PARENT + TEXT_TYPE + 
        		    " )";
    		
    		
    		public static final String SQL_DELETE_ENTRIES_JOURNALS = 
    				"DROP TABLE " + TABLE_NAME_JOURNAL;
    		
    		public static final String SQL_DELETE_ENTRIES_ATTACHMENTS =
        		    "DROP TABLE " + TABLE_NAME_ATTACHMENTS;
    		
    }
    	
    	
    	

}
