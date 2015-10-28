package com.ndhunju.dailyjournal.test.suite;

import com.ndhunju.dailyjournal.test.JsonConverterStreamTest;
import com.ndhunju.dailyjournal.test.JsonConverterStringTest;
import com.ndhunju.dailyjournal.test.ServicesTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by dhunju on 10/24/2015.
 */

@RunWith(Suite.class)
//list the individual test classes or test suites as arguments.
@Suite.SuiteClasses({ServicesTest.class, JsonConverterStreamTest.class, JsonConverterStringTest.class})
public class InstrumentationTestSuite {
}
