package com.ndhunju.dailyjournal.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ndhunju.dailyjournal.controller.JournalPagerActivity;
import com.ndhunju.dailyjournal.controller.journal.JournalNewActivity;
import com.ndhunju.dailyjournal.controller.party.PartyActivity;
import com.ndhunju.dailyjournal.controller.party.PartyDetailActivity;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Functional test across multiple Activities. Tests {@link PartyDetailActivity},
 * {@link PartyActivity} and {@link JournalNewActivity}.
 */
public class PartyDetailActivityTest {

    public static final int TIME_OUT = 5000;

    private PartyDetailActivity mPartyDetailActivity;
    private Instrumentation mInstrumentation;
    Services services;
    long testPartyId;

    ActivityScenario<PartyDetailActivity> activityScenario;

    @Before
    public void setUp() {
        services = Services.getInstance(ApplicationProvider.getApplicationContext());
        //Clear old data
        services.eraseAll();
        //fill with new data
        UtilsTest.fillDatabase(services);

        testPartyId = services.getParties().get(0).getId();
        Intent sendIntent = new Intent(
                getInstrumentation().getTargetContext(),
                PartyDetailActivity.class
        );
        sendIntent.putExtra(Constants.KEY_PARTY_ID, String.valueOf(testPartyId));
        activityScenario = ActivityScenario.launch(sendIntent);
        activityScenario.onActivity(activity -> {
            mPartyDetailActivity = activity;
        });

        mInstrumentation = getInstrumentation();
    }

    @After
    public void tearDown() throws Exception { }

    /**
     * Tests the preconditions of this test fixture.
     */
    @Test
    @MediumTest
    public void testPreconditions() {
        assertNotNull("mPartyDetailActivity is null", mPartyDetailActivity);
    }

    @Test
    @MediumTest
    public void testActionBarButtonsExists() {
        Espresso.onView(ViewMatchers.withId(R.id.menu_party_activity_info))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.menu_party_activity_share))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test when Click Option Menu Information, goes to {@link PartyActivity}PartyActivity,
     * users changes the name, press backs button, activity result passed to previous activity,
     * new name is reflected in the {@link PartyDetailActivity}
     */
    @Test
    @LargeTest
    public void testChangingNameInPartyActivityIsReflected() {

        final String NEW_NAME = "NIKESH DHUNJU";

        //Mock up an ActivityResult:
        Intent returnIntent = new Intent();
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, returnIntent);

        // Create an ActivityMonitor to monitor the interaction between the PartyDetailActivity and PartyActivity
        Instrumentation.ActivityMonitor activityMonitor = mInstrumentation.addMonitor(PartyActivity.class.getName(), activityResult, false);

        //click on the option menu
        //getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        //getInstrumentation().invokeMenuActionSync(mPartyDetailActivity, R.id.menu_party_activity_info, 0);

        //Or // Click ActionBar
        Espresso.onView(ViewMatchers.withId(R.id.menu_party_activity_info))
                .perform(ViewActions.click());

        // Wait for the ActivityMonitor to be launched, Instrumentation will then return the mock ActivityResult, getInt a reference to it
        final PartyActivity partyActivity = (PartyActivity) mInstrumentation.waitForMonitorWithTimeout(activityMonitor, TIME_OUT);

        //Verify that PartyActivity was started
        assertNotNull("partyActivity is null", partyActivity);
        //doesn't work
        //assertTrue("Monitor for partyActivity has not been called", activityMonitor.getHits() > 0);
        assertEquals("Activity is of wrong type", PartyActivity.class, partyActivity.getClass());

        mInstrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ((TextView) partyActivity.findViewById(R.id.activity_party_name_et)).setText(NEW_NAME);
                ((Button) partyActivity.findViewById(R.id.activity_party_ok_btn)).performClick();
            }
        });

        //wait for changes to occur
        mInstrumentation.waitForIdleSync();

        //Check that the name is updated
        assertEquals(((TextView) mPartyDetailActivity.findViewById(R.id.fragment_party_detail_name_tv)).getText().toString(), (NEW_NAME));

        //Unregister monitor for PartyActivity
        getInstrumentation().removeMonitor(activityMonitor);


    }

    /**
     * Tests that when a journal is changed in {@Link JournalNewActivity}, it is reflected in
     * {@Link PartyDetailActivity}
     */
    @Test
    @LargeTest
    public void testChangingJournalAmtInJournalActivityIsReflected(){

        final int position = 0;
        final double NEW_AMT = 2000;
        final String NEW_NOTES = "notes";

       final long testJournalId;

        // Simulate a button click that start ChildActivity for result:
        final RecyclerView journalLV = (RecyclerView) mPartyDetailActivity.findViewById(R.id.activity_party_lv);

        //Wait until all events from the MainHandler's queue are processed
        getInstrumentation().waitForIdleSync();

        testJournalId = journalLV.getAdapter().getItemId(position);
        //Perform Click should be done on UI thread
        mInstrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                journalLV.findViewHolderForAdapterPosition(position).itemView.performClick();
            }
        });

       // Mock up an ActivityResult:
       Intent returnIntent = new Intent();
       Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, returnIntent);

       // Create an ActivityMonitor that catch ChildActivity and return mock ActivityResult:
       Instrumentation.ActivityMonitor activityMonitor = mInstrumentation.addMonitor(JournalPagerActivity.class.getName(), activityResult, false);

        // Wait for the ActivityMonitor to be hit, Instrumentation will then return the mock ActivityResult:
        final JournalPagerActivity journalPagerActivity = (JournalPagerActivity)mInstrumentation.waitForMonitorWithTimeout(activityMonitor, TIME_OUT);

        //wait for change in UI
        mInstrumentation.waitForIdleSync();

        //Verify that PartyActivity was started
        assertNotNull("journalPagerActivity is null", journalPagerActivity);
        //assertTrue("Monitor for journalPagerActivity has not been called", activityMonitor.getHits() > 0);
        assertEquals("Activity is of wrong type", JournalPagerActivity.class, journalPagerActivity.getClass());

       //save journal information to test later
        Journal testJournal= services.getJournal(testJournalId);
        Party testParty = services.getParty(testJournal.getPartyId());
        double oldAmt = testJournal.getAmount();
        double expectedNewBal = testParty.calculateBalances() - oldAmt + NEW_AMT;

        mInstrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ((EditText) journalPagerActivity.findViewById(R.id.fragment_home_amount_et)).setText(String.valueOf(NEW_AMT));
                ((EditText) journalPagerActivity.findViewById(R.id.fragment_home_note_et)).setText(NEW_NOTES);
                journalPagerActivity.findViewById(R.id.fragment_home_save_btn).performClick();
            }
        });

        mInstrumentation.waitForIdleSync();

        assertEquals(UtilsFormat.formatCurrency(expectedNewBal, mInstrumentation.getTargetContext()),
                ((TextView) mPartyDetailActivity.findViewById(R.id.fragment_party_detail_balance_tv)).getText());

        //Unregister monitor for JournalPagerActivity
        getInstrumentation().removeMonitor(activityMonitor);

    }
}