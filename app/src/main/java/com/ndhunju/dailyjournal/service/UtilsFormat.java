package com.ndhunju.dailyjournal.service;

import android.text.format.DateFormat;
import android.util.Log;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by dhunju on 9/24/2015.
 */
public class UtilsFormat {

    public static final int NUM_OF_DIGITS = 7; // Number of digits for ID

    //Date Formats
    public static final String DATE_FORMAT = "MMM-d-yyy";
    public static final String DATE_FORMAT_DASH = "M-d-yyy";
    public static final String DATE_FORMAT_NEPALI = "d/M/yyy";
    public static final String DATE_FORMAT_DAY = "EEEE, MMM dd";
    public static final String DATE_FORMAT_SHORT = "EEE, MMM dd";
    public static final String DATE_FORMAT_FOR_FILE = "M-d-yyy-kk-mm-ss";            //kk for 24 hours format
    public static final String DATE_FORMAT_FULL = "EEEE, MMM dd, yyy @ kk:mm a";

    /**
     * Formats the passed date object in passed format.
     *
     * @param date
     * @param format
     * @return
     */
    public static String formatDate(Date date, String format) {
        return DateFormat.format(format, date).toString();
    }

    /**
     * Formats the passed double based on default Language of the system.
     * Eg. Rs. if Nepal
     *
     * @param currency
     * @return
     */
    public static String formatCurrency(Double currency) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return nf.format(currency);
    }

    public static double parseCurrency(String currency) throws NumberFormatException {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        double doubleCurrency = 0;
        try {
            doubleCurrency = nf.parse(currency).doubleValue();
        } catch (ParseException pe) {
//try parsing it for regular double string
            try {
                doubleCurrency = Double.parseDouble(currency);
            } catch (NumberFormatException nfe) {
                Log.d("Format", "Incorrect format " + currency);
                throw new NumberFormatException("Incorrect number format");
            }
        }

        return doubleCurrency;
    }

    /**
     * Returns String representation of an int Id.
     * Eg. 1 = "00000001"
     *
     * @param id
     * @return
     */
    public static String getStringId(long id, int noOfDigits) {
        String nextId = String.valueOf(id);
        String zeros = "";
        for (int i = 1; i <= noOfDigits - nextId.length(); i++)
            zeros += "0";
        return zeros + nextId;
    }
}
