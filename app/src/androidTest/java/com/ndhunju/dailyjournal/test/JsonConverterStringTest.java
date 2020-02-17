package com.ndhunju.dailyjournal.test;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverter;
import com.ndhunju.dailyjournal.service.json.JsonConverterStream;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsZip;

import org.junit.After;
import org.junit.Before;

import java.util.List;

/**
 * This class uses the JUnit3 syntax for tests.
 * <p/>
 * With the new AndroidJUnit runner you can run both JUnit3 and JUnit4 tests in a single test
 * suite. The {@link androidx.test.internal.runner.AndroidRunnerBuilder} which extends JUnit's
 * {@link org.junit.internal.builders.AllDefaultPossibilitiesBuilder} will create a single {@link
 * junit.framework.TestSuite} from all tests and run them.
 * Test class for {@link JsonConverterString}
 */
public class JsonConverterStringTest extends InstrumentationTestCase{

    public Context targetCtx;
    public Context testCtx;
    JsonConverter jc;

    @Before
    public void setUp(){
        targetCtx = getInstrumentation().getTargetContext();
        jc = JsonConverterString.getInstance(targetCtx);
        //testCtx has limited privilege; it can't open DB nor create file
        testCtx = getInstrumentation().getContext();

    }

    @After
    public void breakDown(){

    }

    /**
     * Must have test prefix to run with JUnit3
     * @throws Exception
     */
    @LargeTest
    public void testReadFromJSON() throws Exception {
        //Arrange
        String jsonFileName = "dailyJournal-9-25-2015-13-16-53.json";
        UtilsZip.copy(testCtx.getAssets().open(jsonFileName), targetCtx.openFileOutput(jsonFileName, Context.MODE_PRIVATE));
        Services services = Services.getInstance(targetCtx);
        services.eraseAll();

        //Act
        boolean success = jc.readFromJSON(targetCtx.getFileStreamPath(jsonFileName).getAbsolutePath());

        //Assert that the operation was successful
        assertTrue("Reading from JSON file failed", success);

        //Assert that all the information is parsed correctly
        List<Party> partyList = services.getParties();
        List<Journal> journals = services.getJournals();
        assertEquals("Number of parties inserted in the DB doesn't match", 2, partyList.size());
        assertEquals("Number of journals inserted in the DB doesn't match", 4, journals.size());

        //Assert that a Party's information is parsed correctly
        Party testParty = partyList.get(0);
        assertEquals("Party's name is not in alphabetical order", "Aaron Sorkin", testParty.getName());
        assertEquals("Party's phone number doesn't match", "743-849-4893", testParty.getPhone());
        assertEquals("Party's debit balance doesn't match", 555.0, testParty.getDebitTotal());
        assertEquals("Party's credit balance doesn't match", 552.0, testParty.getCreditTotal());
        assertEquals("Party's type doesn't match" , Party.Type.Debtors, testParty.getType());
        assertEquals("Party's total journal number doesn't match", 2, services.getJournals(testParty.getId()).size());

        //Assert that a Journal's information is parsed correctly
        Journal testJournal = services.getJournals(testParty.getId()).get(0);
        assertEquals("Journal is not ordered by date", 1442866589969l, testJournal.getDate());
        assertEquals("Journal note doesn't match", "test notes", testJournal.getNote());
        assertEquals("Journal amount doesn't match", 552.0, testJournal.getAmount());
        assertEquals("Journal type doesn't match", Journal.Type.Credit, testJournal.getType());
        assertEquals("Journal created/added date doesn't match", 1443212184956l, testJournal.getCreatedDate());
        assertEquals("Journal's total attachment doesn't match", 0, services.getAttachmentPaths(testJournal.getId()).size());

    }

    @LargeTest
    public void testWriteToJSON() throws Exception {
        //Arrange
        String outputJsonFile = "testOutputFile.json";
        targetCtx.openFileOutput(outputJsonFile, Context.MODE_PRIVATE);
        String path = targetCtx.getFileStreamPath(outputJsonFile).getAbsolutePath();

        //Act
        boolean success = jc.writeToJSON(path);
        //Parse the file with JsonConverterString
        JsonConverter jc = JsonConverterStream.getInstance(targetCtx);
        boolean parseSuccess = jc.readFromJSON(path);

        //Assert that the operation was successful
        assertTrue("Writing to Json file failed", success);
        assertTrue("Reading newly created JSON file failed", parseSuccess);
    }
}
