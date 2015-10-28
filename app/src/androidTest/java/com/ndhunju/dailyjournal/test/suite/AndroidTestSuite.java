package com.ndhunju.dailyjournal.test.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all tests, unit + instrumentation tests.
 * To organize the execution of your instrumented unit tests, you can group a collection of test
 * classes in a test suite class and run these tests together.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({UnitTestSuite.class})
public class AndroidTestSuite {}