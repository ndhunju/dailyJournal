package com.ndhunju.dailyjournal.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all tests, unit + instrumentation tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({InstrumentationTestSuite.class, UnitTestSuite.class})
public class AndroidTestSuite {}