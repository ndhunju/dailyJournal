package com.ndhunju.dailyjournal.model;

import static org.hamcrest.CoreMatchers.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by dhunju
 */
public class UtilsTest {

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
        String testStrId = Utils.getStringId(testId, noOfDigits);
        
        //Assert
        assertThat(testStrId, equalTo("0000000008"));
    }

    @Test
    public void passingIncorrectFormatForDateThrowsNullPointerException(){
        //Arrange
        String dateFormat = "illegalFormat";
        thrown.expect(NullPointerException.class);

        //Act
        Utils.formatDate(new Date(), dateFormat);
    }

    @Test(expected = NumberFormatException.class)
    public void incorrectCurrencyFormatThrowsNumberFormatEx(){
        Utils.parseCurrency("$%l");
    }

}