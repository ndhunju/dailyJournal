package com.ndhunju.dailyjournal.database;

import com.ndhunju.dailyjournal.model.Journal;

import java.util.List;

public interface IJournalDAO extends IGenericDAO<Journal, Long> {

    List<Journal> findAll(long partyId);

    int deleteAll(long partyId);

    int truncateTable();

    List<Journal> findByDate(long start, long end, long... excludeJournalsWithId);

    List<Journal> findByPartyAndDate(long partyId, long date, long... excludeJournalsWithId);

    List<Journal> findByNotes(String keywords);

    void registerObserver(JournalDAO.Observer observer);

    void unregisterObserver(JournalDAO.Observer observer);


    interface Observer {
        void onJournalAdded(Journal journal);
        void onJournalChanged(Journal journal);
        void onJournalDeleted(Journal journal);
        void onJournalDataSetChanged(long party);
    }
}
