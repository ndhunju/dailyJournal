package com.ndhunju.dailyjournal.model;

import android.test.ActivityTestCase;

import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverter;
import com.ndhunju.dailyjournal.service.json.JsonConverterStream;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;

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
public class JournalTest extends ActivityTestCase{

    private String jsonJournal;
    private Services services;

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

        services = Services.getInstance(getActivity());
    }

    @After
    public void cleanUp(){}

    @Test
    public void correctlyParsesJSONStr() throws Exception {
        //Arrange
        boolean newId = false;


        //Act
        Journal testJournal = JsonConverterString.getInstance(getActivity()).getJournal(new JSONObject(jsonJournal));

        //Assert
        assertThat("Journal Id didn't match", testJournal.getId(), equalTo(2l));
        assertThat("Journal date didn't match", testJournal.getDate(), equalTo(1424312194476l));
        assertThat("Journal type didn't match", testJournal.getType(), equalTo(Journal.Type.Debit));
        assertThat("Journal amount didn't match", testJournal.getAmount(), equalTo(2500.25));
        assertThat("Journal note didn't match", testJournal.getNote(), equalTo("checks no 56778"));
    }

    @Test
    public void passingTrueToNewIdIgnoresIdInJSONStr() throws JSONException {
        //Arrange
        boolean newId = true;

        //Act
        Journal testJournal = JsonConverterString.getInstance(getActivity()).getJournal(new JSONObject(jsonJournal));

        //Assert
        assertNotEquals(testJournal.getId(), equalTo(2));
    }

    @Test
    public void deletingAttchShouldDeleteFile() throws IOException {
        //Arrange
        File testFile = new File("testFile.jpg");
        testFile.createNewFile();
        Attachment attachment = new Attachment(0);
        attachment.setPath(testFile.getAbsolutePath());
        services.addAttachment(attachment);


        //Act
        services.deleteAttachment(attachment);

        //Assert
        assertFalse("Attachment file not deleted.", testFile.exists());
    }

}