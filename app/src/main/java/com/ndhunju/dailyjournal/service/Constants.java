package com.ndhunju.dailyjournal.service;

public interface Constants {

    //Constants
    public static final int NO_PARTY = -1;
    int ID_NEW_JOURNAL=-2;
    long ID_NEW_PARTY = -3;

    //Constant Keys usu used to get and put data in an Intent or Bundle
    String APP_PREFIX = "com.ndhunju.dailyJournal"; //DON'T CHANGE THIS VAL
    String KEY_PARTY_NAME = APP_PREFIX + "partyName";
    String KEY_REQUEST_CODE = APP_PREFIX + "RequestCode";
    String KEY_JOURNAL_ID = APP_PREFIX + "journalId";
    String KEY_PARTY_ID = APP_PREFIX + "merchantId";
    String KEY_JOURNAL_CHGD = APP_PREFIX + "nameJournalChanged";
    String KEY_ATTACHMENTS = APP_PREFIX + "keyAttachments";
    String KEY_PARTY_INFO_CHGD = APP_PREFIX + "partyInfoChanged";
    String KEY_IMPORT_OLD_DATA = APP_PREFIX + "KeyImportedOLdData";
    String KEY_ATTACHMENTS_IS_CHGD = APP_PREFIX + "isAttachmentChanged";


}
