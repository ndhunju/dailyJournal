package com.ndhunju.dailyjournal.model;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by dhunju on 7/31/2015.
 */
public class PartyTest {

    Party testParty;
    String jsonParty;

    @Before
    public void initialize(){
        testParty =  new Party("Party", 0);
        jsonParty = "{" +
                "id:2," +
                "name:Bill Gates," +
                "phone:\"12345678\"," +
                "type:Debtors," +
                "journals:[" +
                "{id:2," +
                "date:1424312194476," +
                "added_date:1424225794476," +
                "type:Debit," +
                "amount:2500," +
                "mNote:checks no 56778," +
                "attachments:[address]}" +
                "]}";
    }

    @Test
    public void addingOnlyCreditJournalsGivesNegativeBalance() throws Exception {
        //Arrange
        for(int i = 0 ; i < 10; i++){
            Journal j = new Journal(1213234+i, i);
            j.setAmount(i);
            j.setType(Journal.Type.Credit);
            testParty.addJournal(j);
        }

        //Act
        double testBalance = testParty.getBalance();

        //Assert
        assertTrue(testBalance < 0);
    }

    @Test
    public void addingEqualNumOfDrCrJournalWithSameAmtGivesZeroBalance(){
        //Arrange
        Party testParty = new Party("", 0);
        for(int i = 0 ; i < 10; i++){
            Journal j = new Journal(1213234+i, i);
            j.setAmount(7);
            j.setType(i%2 == 0 ? Journal.Type.Credit : Journal.Type.Debit);
            testParty.addJournal(j);
        }

        //Act
        double testBalance = testParty.getBalance();

        //Assert
        assertTrue(testBalance == 0);
    }

    @Test
    public void journalsAreAddedInDateOrder() {
        //Arrange
        for(int i = 0 ; i < 10; i++){
            long date = 1424312194476l;
            date = i%2 == 0 ?  date + i : date-i;
            Journal j = new Journal(date, i);
            testParty.addJournal(j);
        }

        //Act
        long previousJournalDate = testParty.getJournals().get(0).getDate() ;
        for(Journal J : testParty.getJournals()){
            if(J.getDate() < previousJournalDate)
                fail("Journals in the list are not in the order of date : "
                        + previousJournalDate + " is not smaller than " + J.getDate());
            previousJournalDate = J.getDate();
        }
    }

    @Test
    public void correctlyParsesJSONStr() throws Exception {
        //Arrange

        //Act
        testParty = Party.fromJSON(new JSONObject(jsonParty), false);

        assertTrue(testParty.getId() == 2);

    }

    @Test
    public void passingTrueToNewIdIgnoresIdInJSONStr() throws Exception {
        //Act
        boolean newId = true;
        testParty = Party.fromJSON(new JSONObject(jsonParty), newId);

        //Assert
        assertFalse(testParty.getId() == 2);
    }
}