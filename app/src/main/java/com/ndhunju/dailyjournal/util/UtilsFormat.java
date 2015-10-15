package com.ndhunju.dailyjournal.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class UtilsFormat {

    public static final String TAG = UtilsFormat.class.getSimpleName();

    public static final int NUM_OF_DIGITS = 7; // Number of digits for ID

    //Date Formats
    private static final String DATE_FORMAT = "MMM-d-yyy";
    public static final String DATE_FORMAT_DASH = "M-d-yyy";
    public static final String DATE_FORMAT_NEPALI = "d/M/yyy";
    public static final String DATE_FORMAT_DAY = "EEEE, MMM dd";
    public static final String DATE_FORMAT_SHORT = "EEE, MMM dd";
    public static final String DATE_FORMAT_FOR_FILE = "M-d-yyy-kk-mm-ss";            //kk for 24 hours format
    public static final String DATE_FORMAT_FULL = "EEEE, MMM dd, yyy @ kk:mm a";

    private static final String DEFAULT_COUNTRY = "US";
    public static final String DEFAULT_LANG = "en";

    /**
     * Formats the passed date object in passed format.
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date, Context context) {
        //Log.d(TAG, "date format :" + format);
        return DateFormat.format(getDateFormatFromPref(context), date).toString();
    }

    public static String formatDate(Date date, String format) {
        return DateFormat.format(format, date).toString();
    }

    /**
     * Formats the passed double based on default Language of the system.
     * Eg. Rs. if Nepal
     *
     * @param currency
     * @param context
     * @return
     */
    public static String formatCurrency(Double currency, Context context) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(getLocaleFromPref(context));
        return nf.format(currency);
    }

    public static String formatDecimal(Double amount, Context context){
        NumberFormat nf = NumberFormat.getNumberInstance(getLocaleFromPref(context));
        nf.setMinimumFractionDigits(2);
        return nf.format(amount);
    }

    private static Locale getLocaleFromPref(Context context){
        PreferenceService ps = PreferenceService.from(context);
        String country = ps.getVal(R.string.key_pref_currency, DEFAULT_COUNTRY);
        Locale locale = new Locale(Locale.getDefault().getLanguage(), country);
        //Log.d(TAG, lang + " " + country);
        return  locale;
    }

    private static String getDateFormatFromPref(Context context){
        PreferenceService ps = PreferenceService.from(context);
        return ps.getVal(R.string.key_pref_date_format, DATE_FORMAT);
    }

    public static double parseCurrency(String currency, Context context) throws NumberFormatException {
        NumberFormat nf = NumberFormat.getCurrencyInstance(getLocaleFromPref(context));
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
