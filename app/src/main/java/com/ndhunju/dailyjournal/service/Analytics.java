package com.ndhunju.dailyjournal.service;

import android.content.Context;

import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by dhunju on 10/17/2015.
 * This class has methods to produce analytical reports
 */
public class Analytics {

    //Constants
    public static int TOP_DR_BAL = 0;
    public static int TOP_CR_BAL = 1;
    public static int TOP_POS_BAL = 2;
    public static int TOP_NEG_BAL = 3;

    //Variables
    private Services mServices;
    private static Analytics analytics;

    public static Analytics from(Context context){
        if(analytics == null)
            analytics = new Analytics(context);
        return analytics;
    }

    private Analytics(Context context){
        mServices = Services.getInstance(context);
    }

    /**
     * Returns an array of party have highest Credit or Debit balance
     * For, Highest Negative(Credit) or Positive(Debit) balance use
     * {@link #getTopPartiesByBalance(Journal.Type, int)}
     * @param type : Highest Credit or Debit
     * @param size : Limit the list
     * @return
     */
    public Party[] getTopDrCrOnly(int type, int size){

        Party[] topParties = new Party[size];
        ArrayList<Party> parties = mServices.getParties();

        size = sortTopDebtors(parties, size, type);
        if(size == 0) return null;

        for(int i = 0; i < size; i++){
            topParties[i] = parties.get(i);
        }

        return topParties;
    }


    /**
     * Returns {@link PartyData} object that
     * contains Party list, Name list, Balance list, Total Party Balance sum in
     * the order determined by @param type and of size @param size
     * @param type : Credit = Negative balance, Debit = Positive balance
     * @param size : size of the list
     * @return
     */
    public PartyData getTopPartiesByBalance(int type, int size){

        ArrayList<Party> parties = mServices.getParties();

        size = sortTopDebtors(parties, size, type);
        if(size == 0) return null;

        PartyData partyData = new PartyData(size);

        for(int i = 0; i < size; i++){
            partyData.parties[i] = parties.get(i);
            partyData.balances[i] = parties.get(i).calculateBalances();

            partyData.names[i] = parties.get(i).getName() + "\n"
                    + UtilsFormat.formatCurrency(partyData.balances[i], mServices.getContext());
            partyData.balanceSum += partyData.balances[i];
        }
        return partyData;
    }

    /**
     * Sorts passed Party ArrayList such that Party with the highest
     * positive(Debit) balance is at first, second highest at second
     * till k th position. This method uses selection sort algorithm
     * which is efficient if k is small
     * @param values : array to sort
     * @param k : position
     */
    public int sortTopDebtors(ArrayList<Party> values, int k, int type){
        //when values null or of size 0
        if(values == null || values.size() == 0) return 0;
        //id the the size of values is smaller than k, k = size
        if(values.size() < k) k = values.size();
        int newSize = 0;
        for(int i = 0; i < k; i++){
            int maxIndex = i;
            double maxVal = getVal(values.get(i), type);
            for(int j = i+1; j < values.size(); j++){
                if(getVal(values.get(j), type) > maxVal){
                    maxIndex = j;
                    maxVal = getVal(values.get(j), type);
                }
            }

            //if the max value is negative or zero end sorting
            if(maxVal <= 0) return newSize;

            swap(values, i, maxIndex);
            newSize++;
        }
        return newSize;
    }

    public double getVal(Party party, int type){
        if(type == TOP_POS_BAL) return party.calculateBalances();
        else if(type == TOP_NEG_BAL) return -party.calculateBalances();
        else if(type == TOP_CR_BAL) return party.getCreditTotal();
        else if(type == TOP_DR_BAL) return party.getDebitTotal();
        return -1;
    }



    /**
     * Sorts passed Party ArrayList such that Party with the highest
     * positive(Debit) balance is at first, second highest at second
     * till k th position. This method uses selection sort algorithm
     * which is efficient if k is small
     * @param values : array to sort
     * @param k : position
     */
    public int sortTopCreditors(ArrayList<Party> values, int k){
        if(values.size() < k) k = values.size();
        int newSize = 0;
        for(int i = 0; i < k; i++){
            int minIndex = i;
            double minVal = values.get(i).calculateBalances();
            for(int j = i+1; j < values.size(); j++){
                if(values.get(j).calculateBalances() < minVal){
                    minIndex = j;
                    minVal = values.get(j).calculateBalances();
                }
            }

            //if the minimun values is positive then stop sorting
            if(minVal >= 0) return newSize;
            Collections.swap(values, i, minIndex);
            //swap(values, i, minIndex);
            newSize++;
        }
        return newSize;
    }


    public void swap(ArrayList<Party> parties, int i, int j){
        Party party = parties.get(i);
        parties.set(i, parties.get(j));
        parties.set(j, party);
    }


    /**
     * Helper class that stores parties along with corresponding
     * name, balance and also the sum of balance of all
     * parties. It was created to minimize the for loop for
     * displaying data in the chart
     */
    public final class PartyData {
        public Party[] parties;
        public String[] names;
        public double[] balances;
        public double balanceSum;

        public PartyData(int size){
            parties = new Party[size];
            names = new String[size];
            balances = new double[size];
        }
    }
}
