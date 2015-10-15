package com.ndhunju.dailyjournal.service.json;

import android.content.Context;
import android.util.Log;

import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dhunju on 10/14/2015.
 * Json converter for old backup file
 */
public class OldJsonConverter {

    public final String TAG = OldJsonConverter.class.getSimpleName();

    Services mServices;
    private static OldJsonConverter mOldJsonConverter;

    public static OldJsonConverter from(Context context) {
        if(mOldJsonConverter == null)
            mOldJsonConverter = new OldJsonConverter(context);
        return mOldJsonConverter;
    }

    private OldJsonConverter(Context context) {
        mServices = Services.getInstance(context);
    }


    public boolean insertIntoDB(String jsonString) {
        Log.i(TAG, "Attempting to parse json with old keys");
        JSONArray partyJSONArray;

        try {
            partyJSONArray = new JSONArray(jsonString);
            for (int i = 0; i < partyJSONArray.length(); i++) {
                if (insetPartyIntoDb(partyJSONArray.getJSONObject(i)) == null)
                    return false;
            }
        } catch (JSONException e) {
            Log.w(TAG, "Error parsing json file");
            return false;
        }
        return true;
    }

    public Party insetPartyIntoDb(JSONObject json) {

        try {
            Party newParty = getParty(json);
            //while adding the journal, credit and debit amount as increment respectively
            //In other words, credit and debit will be calculated again
            newParty.setDebitTotal(0);
            newParty.setCreditTotal(0);

            long id = mServices.addParty(newParty);

            JSONArray journalJSONS = json.getJSONArray("journals");
            for (int i = 0; i < journalJSONS.length(); i++) {
                insertJournalIntoDb(journalJSONS.getJSONObject(i), id);
            }

            return newParty;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a Journal Object from passed json parameter
     *
     * @param json
     * @return
     */
    public Journal insertJournalIntoDb(JSONObject json, long partyId) {
        try {
            Journal newJournal = getJournal(json, partyId);
            newJournal.setPartyId(partyId);
            long newId = mServices.addJournal(newJournal);

            JSONArray attachmentJSONS = json.getJSONArray("attachments");
            for (int i = 0; i < attachmentJSONS.length(); i++) {
                insertAttchIntoDb(attachmentJSONS.getString(i), newId);
            }
            return newJournal;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public Attachment insertAttchIntoDb(String path, long journalId) {
        Attachment attachment = new Attachment(journalId);
        //Since app's data such as attachments are now stored in internal storage
        //we need to check if the path for attachments are still referring to external(old)
        //storage. If it is, then change it to the new one
        path = UtilsFile.replaceOldDir(path);
        attachment.setPath(path);

        mServices.addAttachment(attachment);

        return attachment;

    }

    /**
     * Creates a Journal Object from passed json parameter
     *
     * @param json
     * @return
     */
    public Journal getJournal(JSONObject json, long partyId) {
        try {
            int id = json.getInt("id");
            long date = json.getLong("date");
            long added_date = json.getLong("added_date");
            Journal.Type type = Journal.Type.valueOf(json.getString("type"));
            double amount = json.getDouble("amount");
            String note = json.getString("mNote");


            Journal newJournal = new Journal(partyId);
            newJournal.setCreatedDate(added_date);
            newJournal.setAmount(amount);
            newJournal.setDate(date);
            newJournal.setType(type);
            newJournal.setNote(note);
            newJournal.setId(id);

            return newJournal;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Creates a Party Object from passed json parameter
     *
     * @param json
     * @return
     */
    public Party getParty(JSONObject json) {

        try {
            int id = json.getInt("id");
            String name = json.getString("name");
            Party newParty = new Party(name, id);
            String phone = json.getString("phone");

            //Since Debitors was corrected to Debtors, Type.valueOf("Debitors") throws error
            newParty.setPhone(phone);
            String type = json.getString("type");
            Party.Type t = type.equals("Debitors") ? Party.Type.Debtors
                    : Party.Type.valueOf(type);
            newParty.setType(t);

            return newParty;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
