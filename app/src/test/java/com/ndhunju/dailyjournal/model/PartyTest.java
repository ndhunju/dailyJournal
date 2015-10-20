package com.ndhunju.dailyjournal.model;

import android.test.ActivityTestCase;

import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverter;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Created by dhunju
 */
public class PartyTest extends ActivityTestCase{

    private Party testParty;
    private String jsonParty;
    private Services services;

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

        services = Services.getInstance(getActivity());
    }

    @After
    public void cleanUp(){}

    @AfterClass
    public static void cleanUpSomethingReallyExpensive(){}

    @Test
    public void addingOnlyCreditJournalsGivesNegativeBalance() throws Exception {
        //Arrange
        for(int i = 0 ; i < 10; i++){
            Journal j = new Journal(1213234);
            j.setAmount(i);
            j.setType(Journal.Type.Credit);
            services.addJournal(j);
        }

        //Act
        double testBalance = testParty.calculateBalances();

        //Assert
        assertTrue(testBalance < 0);
    }

    @Test
    public void addingEqualNumOfDrCrJournalWithSameAmtGivesZeroBalance(){
        //Arrange
        Party testParty = new Party("", 0);
        for(int i = 0 ; i < 10; i++){
            Journal j = new Journal((1213234l+i));
            j.setAmount(7);
            j.setType(i % 2 == 0 ? Journal.Type.Credit : Journal.Type.Debit);
            services.addJournal(j);
        }

        //Act
        double testBalance = testParty.calculateBalances();

        //Assert
        assertTrue(testBalance == 0);
    }

    @Test
    public void journalsAreAddedInDateOrder() {
        //Arrange
        long partyId = 1l;
        for(int i = 0 ; i < 10; i++){
            long date = 1424312194476l;
            date = i%2 == 0 ?  date + i : date-i;
            Journal j = new Journal(partyId, date, i);
            services.addJournal(j);
        }

        //Act
        long previousJournalDate = services.getJournals(partyId).get(0).getDate() ;
        for(Journal J : services.getJournals(partyId)){
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
        testParty = JsonConverterString.getInstance(getActivity()).getParty(new JSONObject(jsonParty));

        assertTrue(testParty.getId() == 2);

    }

    @Test
    public void deletingJournalShouldDeleteAllAttachments(){
        //Arrange
        Journal j1 = new Journal(0);
        services.addAttachment(new Attachment(j1.getId()));
        services.addAttachment(new Attachment(j1.getId()));


        //Act
        services.deleteJournal(j1);

        //Assert
        assertThat(services.getAttachments(j1.getId()).size(), equalTo(0));
    }

    @Test
    public void changingTypeOfJournalIsReflectedInPartyTotal(){
        //Arrange
        Party testParty = new Party("", 0);
        for(int i = 0 ; i < 10; i++){
            Journal j = new Journal(1213234l);
            j.setAmount(7);
            //Odds are Debit, Evens are Credit
            j.setType(i%2 == 0 ? Journal.Type.Credit : Journal.Type.Debit);
            services.addJournal(j);
        }

        double testDebitTotal = testParty.getDebitTotal();
        double testCreditTotal = testParty.getCreditTotal();
        Journal testJournal = services.getJournal(testParty.getId());

        //Act
        testJournal.setType(Journal.Type.Debit);
        testParty.calculateBalances();

        //Assert
        assertThat(testParty.getDebitTotal(), is(equalTo(testDebitTotal+testJournal.getAmount())));
        assertThat(testParty.getCreditTotal(), equalTo(testCreditTotal-testJournal.getAmount()));
    }
}