package com.ndhunju.dailyjournal.database;

import com.ndhunju.dailyjournal.model.Attachment;

import java.util.List;

/**
 * Created by dhunju on 10/27/2015.
 */
public interface IAttachmentDAO extends IGenericDAO<Attachment, Long> {
    /**
     * Bulk insertion can increase the performance of insertion.
     * @param attachments
     */
    void bulkInsert(List<Attachment> attachments);

    List<Attachment> findAll(long journalId);

    int deleteAll(long journalId);

    int truncateTable();
}
