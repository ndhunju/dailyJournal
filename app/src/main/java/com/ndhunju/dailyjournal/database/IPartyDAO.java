package com.ndhunju.dailyjournal.database;

import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

import java.util.List;

/**
 * Created by dhunju on 10/21/2015.
 */
public interface IPartyDAO extends GenericDAO<Party, Long>{

    int updateDr(long id, double amount, String operation);

    int updateCr(long id, double amount, String operation);

    Party find(String partyName);

    Party[] findTopDrCrAmt(Journal.Type type, int limit);

    List<String> getNames();

    String[] getNamesAsArray();

    int truncateTable();
}
