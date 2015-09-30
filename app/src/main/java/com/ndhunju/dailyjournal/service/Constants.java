package com.ndhunju.dailyjournal.service;

/**
 * Created by dhunju on 9/24/2015.
 */
public class Constants {
    
    public static final int NO_PARTY = -1;
    public static final int ID_NEW_JOURNAL=-2;
    public static final long ID_NEW_PARTY = -3;

    //Constant Keys usu used to get and put data in an Intent or Bundle
    public static final String APP_PREFIX = "com.ndhunju.dailyJournal";
    public static final String KEY_PARTY_NAME = APP_PREFIX + "partyName";
    public static final String KEY_REQUEST_CODE = APP_PREFIX + "RequestCode";
    public static final String KEY_JOURNAL_ID = APP_PREFIX + "journalId";
    public static final String KEY_PARTY_ID = APP_PREFIX + "merchantId";
    public static final String KEY_JOURNAL_CHGD = APP_PREFIX + "nameJournalChanged";
    public static final String KEY_ATTACHMENTS = APP_PREFIX + "keyAttachments";
    public static final String KEY_PARTY_INFO_CHGD = APP_PREFIX + "partyInfoChanged";
    public static final String KEY_IMPORT_OLD_DATA = APP_PREFIX + "KeyImportedOLdData";
    public static final String KEY_ATTACHMENTS_IS_CHGD = APP_PREFIX + "isAttachmentChanged";
    public static final String KEY_CHANGE_TYPE = "changeType";

    //Hex Value of colors
    public static final String RED = "#f63752";
    public static final String BLACK = "#000000";
    public static final String GREEN = "#5CB85C";

    public static enum ChangeType{EDITED, DELETED};

}
