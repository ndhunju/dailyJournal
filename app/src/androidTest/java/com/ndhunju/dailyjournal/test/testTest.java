package com.ndhunju.dailyjournal.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by dhunju on 10/27/2015.
 */
public class testTest {

    @BeforeClass
    public static void testinitializeSomethingReallyExpensive() {
    }

    @After
    public void cleanUp() {
    }

    @AfterClass
    public static void cleanUpSomethingReallyExpensive() {
    }

    @Test
    public void test(){
        assert true;
    }
}
