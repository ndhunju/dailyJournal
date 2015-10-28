package com.ndhunju.dailyjournal.test.suite;

import com.ndhunju.dailyjournal.test.UtilsFileTest;
import com.ndhunju.dailyjournal.test.testTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({UtilsFileTest.class, testTest.class})
public class UnitTestSuite {}
