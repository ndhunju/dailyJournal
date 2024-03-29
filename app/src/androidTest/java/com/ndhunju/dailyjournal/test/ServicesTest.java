package com.ndhunju.dailyjournal.test;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.SmallTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.service.json.OldJsonConverter;
import com.ndhunju.dailyjournal.util.UtilsFile;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * JUnit4 Ui Tests for {@link ServicesTest} using the {@link androidx.test.runner.AndroidJUnitRunner}.
 * This class uses the JUnit4 syntax for tests.
 */
@RunWith(AndroidJUnit4.class)
public class ServicesTest {

    public Services services;

    @Before
    public void createService() {
        services = Services.getInstance(ApplicationProvider.getApplicationContext());
        services.eraseAll();
    }

    @After
    public void cleanUp() {
    }

    @AfterClass
    public static void cleanUpSomethingReallyExpensive() {
    }

    @Test
    @SmallTest
    public void addingOnlyCreditJournalsGivesNegativeBalance() throws Exception {
        //Arrange
        Party testParty = new Party("Party", 0);
        long newId = services.addParty(testParty);
        for (int i = 0; i < 10; i++) {
            Journal j = new Journal(newId);
            j.setAmount(i);
            j.setType(Journal.Type.Credit);
            services.addJournal(j);
        }

        //Act
        //getInt fresh copy of party
        double testBalance = services.getParty(newId).calculateBalances();

        //Assert
        assertTrue(testBalance < 0);
    }

    @Test
    @SmallTest
    public void testAddingEqualNumOfDrCrJournalWithSameAmtGivesZeroBalance() {
        //Arrange
        Party testParty = new Party("Party", 0);
        long newId = services.addParty(testParty);
        for (int i = 0; i < 10; i++) {
            Journal j = new Journal(newId);
            j.setAmount(7);
            j.setType(i % 2 == 0 ? Journal.Type.Credit : Journal.Type.Debit);
            services.addJournal(j);
        }

        //Act
        double testBalance = services.getParty(newId).calculateBalances();

        //Assert
        assertTrue(testBalance == 0);
    }

    @Test
    @SmallTest
    public void journalsAreAddedInDateOrder() {
        //Arrange
        //Add a test party
        long newId = services.addParty(new Party("testParty"));
        //add journals with different dates
        for (int i = 0; i < 10; i++) {
            long date = 1424312194476l;
            date = i % 2 == 0 ? date + i : date - i;
            Journal j = new Journal(newId, date, i);
            services.addJournal(j);
        }

        //Act
        List<Journal> journals = services.getJournals(newId);
        long previousJournalDate = journals.get(0).getDate();
        for (Journal J : journals) {
            if (J.getDate() < previousJournalDate)
                fail("Journals in the list are not in the order of date : "
                        + previousJournalDate + " is not smaller than " + J.getDate());
            previousJournalDate = J.getDate();
        }
    }

    @Test
    @SmallTest
    public void correctlyParsesOldPartyJSONStr() throws Exception {
        //Arrange
        String jsonParty = "{" +
                "id:2," +
                "name:\"Bill Gates\"," +
                "phone:\"12345678\"," +
                "type:\"Debtors\"," +
                "journals:[" +
                "{id:2," +
                "date:1424312194476," +
                "added_date:1424225794476," +
                "type:\"Debit\"," +
                "amount:2500," +
                "mNote:\"checks no 56778\"," +
                "attachments:[\"address\"]}" +
                "]}";
        //Act
        Party testParty = OldJsonConverter.getParty(new JSONObject(jsonParty));

        //Assert
        assertTrue(testParty.getId() == 2);

    }

    @Test
    @SmallTest
    public void deletingJournalShouldDeleteAllAttachments() {
        //Arrange
        Journal j1 = new Journal(0);
        long journalId = services.addJournal(j1);
        Attachment attachment1 = new Attachment(journalId);
        attachment1.setPath("");
        Attachment attachment2 = new Attachment(journalId);
        attachment2.setPath("");
        services.addAttachment(attachment1);
        services.addAttachment(attachment2);


        //Act
        services.deleteJournal(j1);

        //Assert
        //assertFalse(new File("image").exists());
        assertThat(services.getAttachments(j1.getId()).size(), equalTo(0));

    }

    @Test
    @SmallTest
    public void changingTypeOfJournalIsReflectedInPartyTotal() {
        //Arrange
        long newPartyId = services.addParty(new Party("testParty"));
        for (int i = 0; i < 10; i++) {
            Journal j = new Journal(newPartyId);
            j.setAmount(7);
            //Odds are Debit, Evens are Credit
            j.setType(i % 2 == 0 ? Journal.Type.Credit : Journal.Type.Debit);
            services.addJournal(j);
        }

        Party testParty = services.getParty(newPartyId);
        double testDebitTotal = testParty.getDebitTotal();
        double testCreditTotal = testParty.getCreditTotal();

        Journal testJournal = services.getJournals(testParty.getId()).get(0);

        //Act
        testJournal.setType(Journal.Type.Debit);
        services.updateJournal(testJournal);

        testParty = services.getParty(newPartyId);
        testParty.calculateBalances();

        //Assert
        assertThat(testParty.getDebitTotal(), is(equalTo(testDebitTotal + testJournal.getAmount())));
        assertThat(testParty.getCreditTotal(), equalTo(testCreditTotal - testJournal.getAmount()));
    }


    @Test
    public void correctlyParsesJournalJSONStr() throws Exception {
        //Arrange
        String jsonJournal = "{" +
                "id:2," +
                "date:1424312194476," +
                "added_date:1424225794476," +
                "type:\"Debit\"," +
                "amount:2500.25," +
                "mNote:\"checks no 56778\"," +
                "attachments:[\"attachment\"]" +
                "}";


        //Act
        Journal testJournal;
        testJournal = JsonConverterString.getJournal(new JSONObject(jsonJournal));
        if (testJournal == null) {
            //if an error occurs, try old json converter
            testJournal = OldJsonConverter.getJournal(new JSONObject(jsonJournal), 0);
        }

        //Assert
        assertThat("Journal Id didn't match", testJournal.getId(), equalTo(2l));
        assertThat("Journal date didn't match", testJournal.getDate(), equalTo(1424312194476l));
        assertThat("Journal type didn't match", testJournal.getType(), equalTo(Journal.Type.Debit));
        assertThat("Journal amount didn't match", testJournal.getAmount(), equalTo(2500.25));
        assertThat("Journal note didn't match", testJournal.getNote(), equalTo("checks no 56778"));
    }

    @Test
    @SmallTest
    public void deletingAttchShouldDeleteFile() throws IOException {
        //Arrange
        File testFile = UtilsFile.createImageFile(services.getContext());
        //testFile.createNewFile();
        Attachment attachment = new Attachment(services.addJournal(new Journal(0)));
        attachment.setPath(testFile.getAbsolutePath());
        services.addAttachment(attachment);


        //Act
        services.deleteAttachment(attachment);

        //Assert
        assertFalse("Attachment file not deleted.", testFile.exists());
    }
}