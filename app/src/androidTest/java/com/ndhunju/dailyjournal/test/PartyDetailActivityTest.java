package com.ndhunju.dailyjournal.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ndhunju.dailyjournal.controller.journal.JournalActivity;
import com.ndhunju.dailyjournal.controller.party.PartyActivity;
import com.ndhunju.dailyjournal.controller.party.PartyDetailActivity;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;

import org.junit.Test;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.action.ViewActions.click;

import com.ndhunju.dailyjournal.R;

/**
 * Created by dhunju on 10/28/2015.
 * test
 */
public class PartyDetailActivityTest extends ActivityInstrumentationTestCase2<PartyDetailActivity> {

    Services services;
    long testPartyId;
    private PartyDetailActivity mPartyDetailActivity;
    private Instrumentation mInstrumentation;

    public PartyDetailActivityTest() {
        super(PartyDetailActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        services = Services.getInstance(mPartyDetailActivity);

        //Clear old data
        services.eraseAll();
        //fill with new data
        UtilsTest.fillDatabase(services);

        //get existing party id
        testPartyId = services.getParties().get(0).getId();

        //pass mock {@link Intent} to the activity before calling getActivity()
        Intent sendIntent = new Intent(getInstrumentation().getTargetContext(), PartyDetailActivity.class);
        sendIntent.putExtra(Constants.KEY_PARTY_ID, String.valueOf(testPartyId));
        setActivityIntent(sendIntent);

        mPartyDetailActivity = getActivity();
        mInstrumentation = getInstrumentation();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testActionBarButtonsExists() {
        View mainActivityDecorView = mPartyDetailActivity.getWindow().getDecorView();
        ViewAsserts.assertOnScreen(mainActivityDecorView, mPartyDetailActivity.findViewById(R.id.menu_party_activity_info));
        ViewAsserts.assertOnScreen(mainActivityDecorView, mPartyDetailActivity.findViewById(R.id.menu_party_activity_share));
    }

    /**
     * Test when Click Option Menu Information, goes to {@Link PartyActivity}PartyActivity, users changes the name,
     * press backs button, activity result passed to previous activity, new name is reflected in the {@link PartyDetailActivity}
     */
    public void testChangingNameInPartyActivityIsReflected() {

        String NEW_NAME = "NIKESH DHUNJU";

        //click on the option menu
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        getInstrumentation().invokeMenuActionSync(mPartyDetailActivity, R.id.menu_party_activity_info, 0);

        //Or // Click ActionBar Search Icon
        //TouchUtils.clickView(this, optionMenuView);

        //Mock up an ActivityResult:
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constants.KEY_PARTY_INFO_CHGD, true);
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, returnIntent);

        // Create an ActivityMonitor that catch PartyActivity and returns mock ActivityResult:
        Instrumentation.ActivityMonitor activityMonitor = mInstrumentation.addMonitor(PartyActivity.class.getName(), activityResult, false);

        // Wait for the ActivityMonitor to be hit, Instrumentation will then return the mock ActivityResult:
        PartyActivity partyActivity = (PartyActivity) mInstrumentation.waitForMonitorWithTimeout(activityMonitor, 1000);

        //Change party credential
        Party testParty = services.getParty(testPartyId);
        testParty.setName(NEW_NAME);
        services.updateParty(testParty);

        partyActivity.finish();

        //Check that the name is updated
        ((TextView) mPartyDetailActivity.findViewById(R.id.activity_party_name_tv)).getText().equals(NEW_NAME);


    }

    /**
     * Tests that when a journal is changed in {@Link JournalActivity}, it is reflected in
     * {@Link PartyDetailActivity}
     */
    public void testChangingJournalAmtInJournalActivityIsReflected(){

        final int position = 0;
        double NEW_AMT = 2000;
        String NEW_NOTES = "notes";

       final long testJournalId;

        // Simulate a button click that start ChildActivity for result:
        final ListView journalLV = (ListView) mPartyDetailActivity.findViewById(R.id.activity_party_lv);

        //wait for UI to load
        try {Thread.sleep(2000);}
        catch (InterruptedException e)
        {e.printStackTrace();}

        testJournalId = journalLV.getItemIdAtPosition(position);
        mInstrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                journalLV.performItemClick(journalLV.getAdapter().getView(0, null, null), position, testJournalId );
            }
        });

       // Mock up an ActivityResult:
       Intent returnIntent = new Intent();
       returnIntent.putExtra(Constants.KEY_JOURNAL_CHGD, true);
       Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, returnIntent);

       // Create an ActivityMonitor that catch ChildActivity and return mock ActivityResult:
       Instrumentation.ActivityMonitor activityMonitor = mInstrumentation.addMonitor(JournalActivity.class.getName(), activityResult, false);


        // Wait for the ActivityMonitor to be hit, Instrumentation will then return the mock ActivityResult:
        JournalActivity journalActivity = (JournalActivity)mInstrumentation.waitForMonitorWithTimeout(activityMonitor, 1000);

       //change journal information
        Journal testJournal= services.getJournal(testJournalId);
        Party testParty = services.getParty(testJournal.getPartyId());
        double oldAmt = testJournal.getAmount();
        testJournal.setAmount(NEW_AMT);
        testJournal.setNote(NEW_NOTES);
        services.updateJournal(testJournal);


        journalActivity.finish();

        //Check that the balance is updated
        double newBal = testParty.calculateBalances() - oldAmt + NEW_AMT;
        ((TextView) mPartyDetailActivity.findViewById(R.id.activity_party_balance_tv)).getText().equals(String.valueOf(newBal));

    }
}