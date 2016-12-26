package com.ndhunju.dailyjournal.database;

import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

/**
 * Created by dhunju on 10/21/2015.
 */
public interface IPartyDAO extends IGenericDAO<Party, Long> {

    int updateDr(Journal journal, String operation);

    int updateCr(Journal journal, String operation);

    Party[] findTopDrCrAmt(Journal.Type type, int limit);

    public int resetDrCrBalance();

    String[] getNamesAsArray();

    int truncateTable();

    void registerObserver(PartyDAO.Observer observer);

    void unregisterObserver(PartyDAO.Observer observer);

    interface Observer {
        void onPartyAdded(Party party);
        void onPartyChanged(Party party);
        void onPartyDeleted(Party party);
    }
}
