package com.ndhunju.dailyjournal.suite;

import com.ndhunju.dailyjournal.ServicesTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by dhunju on 10/24/2015.
 */

@RunWith(Suite.class)
//list the individual test classes or test suites as arguments.
@Suite.SuiteClasses(ServicesTest.class)
public class InstrumentationTestSuite {
}
