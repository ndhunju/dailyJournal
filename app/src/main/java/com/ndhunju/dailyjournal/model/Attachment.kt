package com.ndhunju.dailyjournal.model;

import java.io.Serializable;

/**
 * Created by dhunju on 9/18/2015.
 * POJO for representing Attachments of a Journal
 * Attachments are generally picture so far
 */
public class Attachment implements Serializable{

    private long mId;
    private String mPath;
    private long mJournalId;

    public Attachment(long journalId){
        mJournalId = journalId;
    }

    public long getJournalId() {
        return mJournalId;
    }

    public void setJournalId(long mJournalId) {
        this.mJournalId = mJournalId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }


}
