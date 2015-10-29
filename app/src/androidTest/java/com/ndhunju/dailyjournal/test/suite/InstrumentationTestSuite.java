package com.ndhunju.dailyjournal.test.suite;

import com.ndhunju.dailyjournal.test.AnalyticsTest;
import com.ndhunju.dailyjournal.test.JsonConverterStreamTest;
import com.ndhunju.dailyjournal.test.JsonConverterStringTest;
import com.ndhunju.dailyjournal.test.KeyValPersistenceTest;
import com.ndhunju.dailyjournal.test.PartyDetailActivityTest;
import com.ndhunju.dailyjournal.test.ServicesTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all tests, unit + instrumentation tests.
 * To organize the execution of your instrumented unit tests, you can group a collection of test
 * classes in a test suite class and run these tests together.
 */

@RunWith(Suite.class)
//list the individual test classes or test suites as arguments.
@Suite.SuiteClasses({ServicesTest.class, JsonConverterStreamTest.class, JsonConverterStringTest.class, AnalyticsTest.class, PartyDetailActivityTest.class, KeyValPersistenceTest.class})
public class InstrumentationTestSuite {}
