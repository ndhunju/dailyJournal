package com.ndhunju.dailyjournal.database;

import com.ndhunju.dailyjournal.model.Journal;

import java.util.List;

public interface IJournalDAO extends GenericDAO<Journal, Long>{

    List<Journal> findAll(long partyId);

    void deleteAll(long partyId);

    int truncateTable();
}
