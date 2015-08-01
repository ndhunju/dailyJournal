package com.ndhunju.dailyjournal.model;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Created by dhunju
 */
public class PartyTest {

    Party testParty;
    String jsonParty;

    @BeforeClass
    public static void initializeSomethingReallyExpensive(){}

    @Before
    public void setUp(){
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

    @After
    public void cleanUp(){}

    @AfterClass
    public static void cleanUpSomethingReallyExpensive(){}

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

    @Test
    public void deletingJournalShouldDeleteAllAttachments(){
        //Arrange
        Journal j1 = new Journal(0);
        j1.addAttachmentPaths("path1");
        j1.addAttachmentPaths("path2");

        testParty.addJournal(j1);

        //Act
        testParty.deleteJournal(j1.getId());

        //Assert
        assertThat(j1.getAttachmentPaths().size(), equalTo(0));
    }
}