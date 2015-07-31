package com.ndhunju.dailyjournal.model;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.test.mock.*;
import static org.mockito.Mockito.*;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;

/**
 * Created by dhunju
 */
public class StorageTest {

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

        Storage.setSharedPreference(mock(SharedPreferences.class));
        Storage testStorage = Storage.getInstance(new MockContext());

        //Act
        testStorage.addParty(firstParty);
        testStorage.addParty(secondParty);
        testStorage.addParty(thirdParty);


        //Assert
        assertThat(testStorage.getParties().get(0), CoreMatchers.equalTo(secondParty));
        assertThat(testStorage.getParties().get(1), CoreMatchers.equalTo(firstParty));
        assertThat(testStorage.getParties().get(2), CoreMatchers.equalTo(thirdParty));

    }


}