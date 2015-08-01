package com.ndhunju.dailyjournal.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockContext;

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
public class StorageTest {

    Storage testStorage;

    @BeforeClass
    public static void initializeSomethingReallyExpensive(){}

    @AfterClass
    public static void cleanUpSomethingReallyExpensive(){}

    @Before
    public void setUp(){
        //Create Mock Objects
        Storage.setSharedPreference(mock(SharedPreferences.class));
        testStorage = Storage.getInstance(new MockContext());
    }

    @Test
    public void firstCallToIsOldDataReturnAlwaysReturnsFalse(){
        //Arrange
        SharedPreferences pm = mock(SharedPreferences.class);
        when(pm.getBoolean(Utils.KEY_IMPORT_OLD_DATA, false)).thenReturn(false);

        //Act
        boolean testValue = Storage.isOldDataImported(pm);

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
        testStorage.addParty(firstParty);
        testStorage.addParty(secondParty);
        testStorage.addParty(thirdParty);


        //Assert
        assertThat(testStorage.getParties().get(0), equalTo(secondParty));
        assertThat(testStorage.getParties().get(1), equalTo(firstParty));
        assertThat(testStorage.getParties().get(2), equalTo(thirdParty));

    }

    @Test
    public void deletingPartyShouldDeleteAllJournals(){
        //Arrange
        Journal journal1 = new Journal(0);
        Journal journal2 = new Journal(1);
        Party testParty = new Party("testParty", 0);
        testParty.addJournal(journal1);
        testParty.addJournal(journal2);
        testStorage.addParty(testParty);

        //Act
        testStorage.deleteParty(testParty.getId());

        //Assert
        assertThat(testParty.getJournals().size(), equalTo(0));
    }

    @Test
    public void eraseAllShouldEmptyAppDirAndParties() throws IOException {
        //Arrange
        Journal testJournal = new Journal(0);
        Party testParty = new Party("testParty", 0);
        testParty.addJournal(testJournal);
        testStorage.addParty(testParty);

        //Create Mock Object for Context
        Context context = mock(Context.class);
        File appFolder = new File(Utils.APP_FOLDER_NAME);
        when(context.getDir(Utils.APP_FOLDER_NAME, Context.MODE_PRIVATE)).thenReturn(appFolder);

        File testAttchmnt = Utils.createImageFile(context, testJournal, testParty);
        testJournal.addAttachmentPaths(testAttchmnt.getAbsolutePath());


        //Act
        testStorage.eraseAll(context);

        //Assert
        assertThat("Erase all didn't clear the directory", Utils.getAppFolder(context).listFiles().length , equalTo(0));
        assertThat("eraseAll() didn't clear the party", testStorage.getParties().size(), equalTo(0));


    }






}