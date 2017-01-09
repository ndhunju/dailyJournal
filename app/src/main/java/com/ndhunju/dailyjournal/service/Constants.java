package com.ndhunju.dailyjournal.service;

public interface Constants {

    //Constants
    public static final int NO_PARTY = -1;
    int ID_NEW_JOURNAL=-2;
    long ID_NEW_PARTY = -3;

    //Constant Keys usu used to get and put data in an Intent or Bundle
    String APP_PREFIX = "com.ndhunju.dailyJournal"; //DON'T CHANGE THIS VAL
    String KEY_REQUEST_CODE = APP_PREFIX + "RequestCode";
    String KEY_JOURNAL_ID = APP_PREFIX + "journalId";
    String KEY_PARTY_ID = APP_PREFIX + "merchantId";
    String KEY_IMPORT_OLD_DATA = APP_PREFIX + "KeyImportedOLdData";
    String KEY_JOURNAL_POS = APP_PREFIX + "journalPosition";


    String KEY_POS = APP_PREFIX + "position";
}
