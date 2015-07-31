package com.ndhunju.dailyjournal.model;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dhunju on 7/31/2015.
 */
public class UtilsTest {

    @Test
    public void getStringIdReturnsCorrectFormat() throws Exception {
        //Arrange
        int testId = 8;
        int noOfDigits = 10;
        
        //Act
        String testStrId = Utils.getStringId(testId, noOfDigits);
        
        //Assert
        assertThat(testStrId, CoreMatchers.equalTo("0000000008"));
    }

}