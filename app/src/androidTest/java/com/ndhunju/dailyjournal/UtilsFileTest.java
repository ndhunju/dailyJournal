package com.ndhunju.dailyjournal;

import android.test.mock.MockContext;

import com.ndhunju.dailyjournal.util.UtilsFormat;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class UtilsFileTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initializeSomethingReallyExpensive(){}

    @AfterClass
    public static void cleanUpSomethingReallyExpensive(){}

    @Before
    public void setUp(){ }

    @Test
    public void getStringIdReturnsCorrectFormat() throws Exception {
        //Arrange
        int testId = 8;
        int noOfDigits = 10;
        
        //Act
        String testStrId = UtilsFormat.getStringId(testId, noOfDigits);
        
        //Assert
        assertThat(testStrId, equalTo("0000000008"));
    }

    public void passingIncorrectFormatForDateThrowsNullPointerException(){
        //Arrange
        String dateFormat = "illegalFormat";
        thrown.expect(IllegalArgumentException.class);

        //Act
        UtilsFormat.formatDate(new Date(), dateFormat);
    }

    @Test(expected = NumberFormatException.class)
    public void incorrectCurrencyFormatThrowsNumberFormatEx(){
        UtilsFormat.parseCurrency("$%l", new MockContext());
    }

}
