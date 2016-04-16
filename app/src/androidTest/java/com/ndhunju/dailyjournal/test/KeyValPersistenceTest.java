package com.ndhunju.dailyjournal.test;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.ndhunju.dailyjournal.service.KeyValPersistence;

import org.junit.Before;

/**
 * Created by dhunju on 10/28/2015.
 * Test class for {@link KeyValPersistence}
 */
public class KeyValPersistenceTest extends InstrumentationTestCase{

    KeyValPersistence keyValPersistence;
    String key = "key";

    @Before
    public void setUp() {
        keyValPersistence = KeyValPersistence.from(getInstrumentation().getTargetContext());
        keyValPersistence.clear();
    }

    @SmallTest
    public void testPutStringCorrectlyInsertsAndRetrievesVal() throws Exception {
        //arrange
        String val = "value";
        String def = "default";

        //act
        keyValPersistence.putString(key, val);

        //assert
        assertEquals(val, keyValPersistence.get(key, def));
    }

    @SmallTest
    public void testPutLongCorrectlyInsertsAndRetrievesVal() throws Exception {
        //arrange
        long val = 7l;
        long def = 0l;

        //act
        keyValPersistence.putLong(key, val);

        //assert
        assertEquals(val, keyValPersistence.getLong(key, def));
    }

    @SmallTest
    public void testPutIntCorrectlyInsertsAndRetrievesVal() throws Exception {
        //arrange
        int val = 7;
        int def = 0;

        //act
        keyValPersistence.putInt(key, val);

        //assert
        assertEquals(val, keyValPersistence.getInt(key, def));
    }

    @SmallTest
    public void testPutBooleanCorrectlyInsertsAndRetrievesVal() throws Exception {
        //arrange
        boolean val = true;
        boolean def = false;

        //act
        keyValPersistence.putBoolean(key, val);

        //assert
        assertTrue("Boolean value mismatch", keyValPersistence.getBoolean(key, def));
    }

}