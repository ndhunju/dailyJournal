package com.ndhunju.dailyjournal.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by dhunju on 7/30/2015.
 */
public class JournalTest {

    String jsonJournal;

    @BeforeClass
    public static void initializeSomethingReallyExpensive(){}

    @AfterClass
    public static void cleanUpSomethingReallyExpensive(){}

    @Before
    public void setUp(){
        //Arrange
        jsonJournal = "{" +
                "id:2," +
                "date:1424312194476," +
                "added_date:1424225794476," +
                "type:Debit," +
                "amount:2500.25," +
                "mNote:checks no 56778," +
                "attachments:[\"\\/data\\/data\\/com.ndhunju.dailyjournal\\/app_DailyJournal\\/.attachments\\/.Bill Gates\\/2-2-0.png\"]" +
                "}";
    }

    @After
    public void cleanUp(){}

    @Test
    public void correctlyParsesJSONStr() throws Exception {
        //Arrange
        boolean newId = false;

        //Act
        Journal testJournal = Journal.fromJSON(new JSONObject(jsonJournal), newId);

        //Assert
        assertThat("Journal Id didn't match", testJournal.getId(), equalTo(2));
        assertThat("Journal date didn't match", testJournal.getDate(), equalTo(1424312194476l));
        assertThat("Journal type didn't match", testJournal.getType(), equalTo(Journal.Type.Debit));
        assertThat("Journal amount didn't match", testJournal.getAmount(), equalTo(2500.25));
        assertThat("Journal note didn't match", testJournal.getNote(), equalTo("checks no 56778"));
        assertThat("Journal attachment path didn't match", testJournal.getAttachmentPaths().get(0),
                equalTo("/data/data/com.ndhunju.dailyjournal/app_DailyJournal/.attachments/.Bill Gates/2-2-0.png"));


    }

    @Test
    public void passingTrueToNewIdIgnoresIdInJSONStr() throws JSONException {
        //Arrange
        boolean newId = true;

        //Act
        Journal testJournal = Journal.fromJSON(new JSONObject(jsonJournal), newId);

        //Assert
        assertNotEquals(testJournal.getId(), equalTo(2));
    }

    @Test
    public void deletingAttchShouldDeleteFile() throws IOException {
        //Arrange
        File testFile = new File("testFile.jpg");
        testFile.createNewFile();
        Journal testJournal = new Journal(0);
        testJournal.addAttachmentPaths(testFile.getAbsolutePath());

        //Act
        testJournal.deleteAttachment(testFile.getAbsolutePath());

        //Assert
        assertFalse("Attachment file not deleted.", testFile.exists());
    }

}