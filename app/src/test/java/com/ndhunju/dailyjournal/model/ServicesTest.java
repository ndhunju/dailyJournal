package com.ndhunju.dailyjournal.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockContext;

import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.KeyValPersistence;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dhunju
 */
public class ServicesTest {

    private Services testServices;

    @BeforeClass
    public static void initializeSomethingReallyExpensive(){}

    @AfterClass
    public static void cleanUpSomethingReallyExpensive(){}

    @Before
    public void setUp(){
        //Create Mock Objects
        KeyValPersistence.setSharedPreference(mock(SharedPreferences.class));
        testServices = Services.getInstance(new MockContext());
    }

    @Test
    public void firstCallToIsOldDataReturnAlwaysReturnsFalse(){
        //Arrange
        SharedPreferences pm = mock(SharedPreferences.class);
        when(pm.getBoolean(Constants.KEY_IMPORT_OLD_DATA, false)).thenReturn(false);

        //Act
        KeyValPersistence.setSharedPreference(pm);
        boolean testValue = KeyValPersistence.isOldDataImported();

        //Assert
        assertFalse(testValue);
    }

    @Test
    public void partiesAreAddedInAlphabeticalOrder(){
        //Arrange
        Party firstParty = new Party("elon", 0);
        Party secondParty = new Party("Bill", 1);
        Party thirdParty = new Party("steve",2);

        //Act
        testServices.addParty(firstParty);
        testServices.addParty(secondParty);
        testServices.addParty(thirdParty);


        //Assert
        assertThat(testServices.getParties().get(0), equalTo(secondParty));
        assertThat(testServices.getParties().get(1), equalTo(firstParty));
        assertThat(testServices.getParties().get(2), equalTo(thirdParty));

    }

    @Test
    public void deletingPartyShouldDeleteAllJournals(){
        //Arrange
        Journal journal1 = new Journal(0);
        Journal journal2 = new Journal(1);
        Party testParty = new Party("testParty", 0);
        testServices.addJournal(journal1);
        testServices.addJournal(journal2);
        testServices.addParty(testParty);

        //Act
        testServices.deleteParty(testParty.getId());

        //Assert
        assertThat(testServices.getJournals(testParty.getId()).size(), equalTo(0));
    }

    @Test
    public void eraseAllShouldEmptyAppDirAndParties() throws IOException {
        //Arrange
        Journal testJournal = new Journal(0);
        Party testParty = new Party("testParty", 0);
        testServices.addJournal(testJournal);
        testServices.addParty(testParty);

        //Create Mock Object for Context
        Context context = mock(Context.class);
        File appFolder = new File(UtilsFile.APP_FOLDER_NAME);
        when(context.getDir(UtilsFile.APP_FOLDER_NAME, Context.MODE_PRIVATE)).thenReturn(appFolder);

        File testAttchmnt = UtilsFile.createImageFile(context, testJournal, testParty);
        Attachment testAttch = new Attachment(testJournal.getId());
        testAttch.setPath(testAttchmnt.getAbsolutePath());
        testServices.addAttachment(testAttch);


        //Act
        testServices.eraseAll(context);

        //Assert
        assertThat("Erase all didn't clear the directory", UtilsFile.getAppFolder(context).listFiles().length , equalTo(0));
        assertThat("eraseAll() didn't clear the party", testServices.getParties().size(), equalTo(0));


    }






}