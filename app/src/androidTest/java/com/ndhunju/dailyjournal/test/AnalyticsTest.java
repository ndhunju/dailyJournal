package com.ndhunju.dailyjournal.test;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.ndhunju.dailyjournal.controller.HomeActivity;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Analytics;
import com.ndhunju.dailyjournal.service.Services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by dhunju on 10/27/2015.
 */
@RunWith(AndroidJUnit4.class)
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
        UtilsTest.fillDatabase(services);

    }



    @Test
    @MediumTest
    public void testGetTopDrCrOnly() throws Exception {
        //Arrange
        int limit = 5;
        double[] expectedTopCrAmtOfParties = {317.0,303.0,285.0,213.0,212.0};
        double[] expectedTopDrAmtOfParties = {238.0,226.0,214.0,202.0,186.0};

        //Act
        Party[] crParties = analytics.getTopDrCrOnly(Analytics.TOP_CR_BAL, limit);
        Party[] drParties = analytics.getTopDrCrOnly(Analytics.TOP_DR_BAL, limit);

        //Assert
        for(int pos= 0 ; pos < limit ; pos++){
            assertEquals("", expectedTopCrAmtOfParties[pos], crParties[pos].getCreditTotal());
            assertEquals("", expectedTopDrAmtOfParties[pos], drParties[pos].getDebitTotal());
        }
    }

    @Test
    @MediumTest
    public void testGetTopPartiesByBalance() throws Exception {
        //Arrange
        int limit = 5;
        double[] expectedTopNegativeBalOfParties = { -155.0,-99.0,-65.0,-59.0,-3.0};
        double[] expectedTopPositiveBalOfParties = { 169.0 ,93.0 ,91.0 ,37.0 ,13.0};
        String[] expectedTopNegativeBalPartyNames = {"Test Party8","Test Party7","Test Party9","Test Party5","Test Party4"};
        String[] expectedTopPositiveBalPartyNames = {"Test Party0","Test Party1","Test Party3","Test Party2","Test Party6" };

        //Act
        Analytics.PartyData crPartyData = analytics.getTopPartiesByBalance(Analytics.TOP_NEG_BAL, limit);
        Analytics.PartyData drPartyData = analytics.getTopPartiesByBalance(Analytics.TOP_POS_BAL, limit);

        //Assert
        for(int pos= 0 ; pos < limit ; pos++){
            assertEquals("Negative balance mismatch", expectedTopNegativeBalOfParties[pos], crPartyData.parties[pos].calculateBalances());
            assertEquals("Positive balance mismatch", expectedTopPositiveBalOfParties[pos], drPartyData.parties[pos].calculateBalances());
            assertEquals("Party Name for Negative balance mismatch", expectedTopNegativeBalPartyNames[pos], crPartyData.parties[pos].getName());
            assertEquals("Party Name for Positive balance mismatch", expectedTopPositiveBalPartyNames[pos], drPartyData.parties[pos].getName());
        }
    }


}