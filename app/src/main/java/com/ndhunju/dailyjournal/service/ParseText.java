package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.util.Log;

import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ParseText {

    private static final String TAG = ParseText.class.getCanonicalName();

    private static ParseText mParseText;
    private Context mContext;
    private Journal mJournal;

    //keywords
    private String[] debits = { "gave", "give", "got"};
    private String[] credits = { "received", "receive"};
    private String[] notes = {"for"};

    private HashMap<String, Integer> months;

    public static ParseText from(Context context){

        if(context == null)
            throw new NullPointerException("Params cannot be null");

        if(mParseText == null){
            mParseText = new ParseText(context);
        }
        return mParseText;
    }

    private ParseText(Context context){
        mContext = context;
        mJournal = Services.getInstance(mContext).getNewJournal();
    }

    public Journal extractJournal(List<String> texts){

        //fill the hashmap
        months = new HashMap<>(12);
        months.put( "january", 0);
        months.put( "february", 1);
        months.put( "march", 2);
        months.put( "april", 3);
        months.put( "may", 4);
        months.put( "june",5);
        months.put( "july",6);
        months.put( "august",7);
        months.put( "september",8);
        months.put( "october", 9);
        months.put( "november", 10);
        months.put( "december", 11);
        //some acronyms
        months.put( "jan", 1);
        months.put( "feb", 2);

        //flags to determine if respected keywords have already been found
        //so that every words are not checked
        boolean[] found = new boolean[5];
        String[] words = texts.get(0).toLowerCase().split(" ");

        //iterate through all notes
        for(int i=0; i <  words.length; i++ ){
            if(!found[0] && setType(words[i])){ found[0] = true;}
            else if(!found[1] && setParty(words[i])){found[1] = true; }
            else if (!found[2] && setAmount(words[i])){found[2] = true; }
            else if(!found[3] && setDate(i, words)){found[3] = true; i++;} //setDate immediately looks for next word
            else if(!found[4] && setNote(words[i], i, words)){
                found[4] = true;
                break; //here we are assuming that the note will come at the end
            }
        }

        return  mJournal;
    }

    private boolean setNote(String word, int currentPos, String[] texts){
        //iterate through all notes keyword
        for(String noteFlag: notes){
            //if the word equals note keyword, add the word plus the remaining as note
            if(word.equals(noteFlag)){
                StringBuilder notes = new StringBuilder(word);
                for(int i = currentPos+1; i < texts.length; i++){
                    notes.append(" ");
                    notes.append(texts[i]);
                }
                mJournal.setNote(notes.toString());
                return true;
            }
        }
        return false;

    }

    private boolean setParty(String name){
        //iterate through all parties
        for(Party party: Services.getInstance(mContext).getParties()){
            //split party name into first and last
            for(String names : party.getName().split(" ")){
                //check if the passed name matches
                if(names.toLowerCase().equals(name)){
                    mJournal.setPartyId(party.getId());
                    Log.d(TAG, "Found Party name");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean setDate(int pos, String[] words){
        Calendar calendar = Calendar.getInstance();

        //find if the word is the name of a month, eg. January
        if(months.containsKey(words[pos] )){
            calendar.set(Calendar.MONTH, months.get(words[pos] ));
            Log.d(TAG, "Found month");
            //assuming the next word (if exits) to be a day of the month
            if (pos + 1 < words.length ) {
                String day = words[pos + 1];
                String digits = "";
                //checking first two character if they are digits
                for (int i = 0; i < 2; i++) {
                    if (Character.isDigit(day.charAt(i)))
                        digits += day.charAt(i);
                }

                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(digits));
                Log.d(TAG, "Found day of the month");
            }

            mJournal.setDate(calendar.getTimeInMillis());
            return true;
        }

        return false;
    }

    private boolean setAmount(String word){
        try{
            //try the regular parse
            mJournal.setAmount(Double.parseDouble(word));
            return true;
        }catch (Exception e){
            try{
                //if regular parse fails, try parsing as a currency
                mJournal.setAmount(UtilsFormat.parseCurrency(word, mContext));
                return true;
            }catch (NumberFormatException ex){
                return false;
            }
        }
    }

    private boolean setType(String word){
        //iterate through all keywords for debit
        for(String debit : debits){
            if(word .contains(debit)){
                mJournal.setType(Journal.Type.Debit);
                return true;
            }
        }

        //iterate through all keywords for credit
        for(String credit: credits){
            if(word .contains(credit)){
                mJournal.setType(Journal.Type.Credit);
                return true;
            }
        }

        return false;
    }
}
