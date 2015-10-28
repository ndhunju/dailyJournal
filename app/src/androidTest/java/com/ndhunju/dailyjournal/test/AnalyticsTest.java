package com.ndhunju.dailyjournal.test;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.ndhunju.dailyjournal.controller.HomeActivity;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Analytics;
import com.ndhunju.dailyjournal.service.Services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by dhunju on 10/27/2015.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class AnalyticsTest extends AndroidTestCase{

    public Analytics analytics;

    @Rule
    public ActivityTestRule<HomeActivity> mActivity = new ActivityTestRule<HomeActivity>(HomeActivity.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Services services = Services.getInstance(mActivity.getActivity());
        analytics = Analytics.from(services.getContext());

        services.eraseAll();
        fillDatabase(services);

    }




    @Test
    public void testGetTopDrCrOnly() throws Exception {
        //Arrange
        int limit = 5;
        double[] expectedTopCrAmtOfParties = { 455.0, 355.0,255.0,155.0,55.0};
        double[] expectedTopDrAmtOfParties = { 640.0, 620.0, 600.0, 580.0,560.0};

        //Act
        Party[] crParties = analytics.getTopDrCrOnly(Party.Type.Creditors, limit);
        Party[] drParties = analytics.getTopDrCrOnly(Party.Type.Debtors, limit);

        //Assert
        for(int pos= 0 ; pos < limit ; pos++){
            assertEquals("", expectedTopCrAmtOfParties[pos], crParties[pos].getCreditTotal());
            assertEquals("", expectedTopDrAmtOfParties[pos], drParties[pos].getDebitTotal());
        }
    }

    @Test
    public void testGetTopPartiesByBalance() throws Exception {
        //Arrange
        int limit = 5;
        double[] expectedTopNegativeBalOfParties = { -455.0, -355.0, -255.0,-155.0,-55.0};
        double[] expectedTopPositiveBalOfParties = { 640.0, 620.0 , 600.0, 580.0,560.0};

        //Act
        Analytics.PartyData crPartyData = analytics.getTopPartiesByBalance(Journal.Type.Credit, limit);
        Analytics.PartyData drPartyData = analytics.getTopPartiesByBalance(Journal.Type.Debit, limit);

        //Assert
        for(int pos= 0 ; pos < limit ; pos++){
            assertEquals("", expectedTopNegativeBalOfParties[pos], crPartyData.parties[pos].calculateBalances());
            assertEquals("", expectedTopPositiveBalOfParties[pos], drPartyData.parties[pos].calculateBalances());
        }
    }


    /**
     * Helper method to fill database with arbitrary data
     */
    public void fillDatabase(Services services){

        //Initialize instances here to reuse the objects
        Attachment newAttachment = new Attachment(0);
        Party newParty = new Party("test Party ");
        Journal newJournal = new Journal(0);

        long newPartyId;
        long newJournalId;


        for(int i = 0; i < 10; i++){
            newParty.setName("test Party " + i);
            newPartyId = services.addParty(newParty);
            for(int j=0; j < 10; j++){
                newJournal.setPartyId(newPartyId);
                newJournal.setAmount((i&1) == 1 ? 10 + i + (j*10) : 10 + (i*5) - j);
                newJournal.setDate(Calendar.getInstance().getTimeInMillis() + i + j);
                newJournal.setType(((i & 1) == 1 ? (  Journal.Type.Debit): Journal.Type.Credit));
                newJournalId = services.addJournal(newJournal);
                for(int z = 0; z < 2; z++){
                    newAttachment.setJournalId(newJournalId);
                    newAttachment.setPath(i + j + z + "");
                    services.addAttachment(newAttachment);
                }
            }
        }
    }
}