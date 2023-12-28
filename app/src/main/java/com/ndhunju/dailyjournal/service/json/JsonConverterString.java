package com.ndhunju.dailyjournal.service.json;

import android.content.Context;
import android.util.Log;

import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by dhunju on 9/21/2015.
 * This class converts POJO into equivalent json format
 * and vice versa. It uses String to do so. The better
 * alternative is to use {@link JsonConverterStream}
 */
public final class JsonConverterString extends JsonConverter{

    //Constants
    private static final String TAG = JsonConverterString.class.getSimpleName();

    private static JsonConverterString mJsonConverterString;


    public static JsonConverterString getInstance(Context context){
        if(mJsonConverterString == null)
            mJsonConverterString = new JsonConverterString(context);
        return mJsonConverterString;
    }

    private JsonConverterString(Context context){
        super(context);
    }

    @Override
    public boolean readFromJSON(String jsonFilePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath));
            StringBuilder jsonString = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null)
                jsonString.append(line );

            boolean success = insertIntoDB(jsonString.toString());
            if(!success) success |= OldJsonConverter.from(mServices.getContext()).insertIntoDB(jsonString.toString());
            if(! success) throw new Exception("Error parsing json file.");
            Log.i(TAG, "Json file parsed");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Failed to parse JSON file. ");
        return false;
    }

    @Override
    public boolean writeToJSON(String jsonFilePath) {
        try {
            // Write data to JSON file in json format
            FileOutputStream fileOutputStream = new FileOutputStream(jsonFilePath);
            fileOutputStream.write(getAllDataInJson().toString().getBytes());
            fileOutputStream.close();
            Log.i(TAG, "JSON backup created");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "JSON backup failed");
            return false;
        }
    }

    /**
     * Creates and returns JSON object for this party. The JSON entails
     * journals and attachments belongs to the party
     * @return
     */
    public JSONObject toJSON(Party party){
        JSONObject json = new JSONObject();
        try{
            json.put(KEY_ID, party.getId());
            json.put(KEY_NAME, party.getName());
            json.put(KEY_NOTE, party.getNote());
            json.put(KEY_PHONE, party.getPhone());
            json.put(KEY_DEBIT, party.getDebitTotal());
            json.put(KEY_CREDIT, party.getCreditTotal());
            json.put(KEY_TYPE, party.getType().toString());
            json.put(KEY_PICTURE, party.getPicturePath());

            JSONArray journalJSONs = new JSONArray();
            List<Journal> mJournals = mServices.getJournals(party.getId());

            for(Journal j: mJournals){
                journalJSONs.put(toJSON(j));
            }

            json.put(KEY_JOURNALS, journalJSONs);
        }catch(Exception e){
            e.printStackTrace();
        }

        return json;
    }

    public JSONObject toJSON(Journal journal){
        JSONObject j = new JSONObject();
        try{
            j.put(KEY_ID,  journal.getId());
            j.put(KEY_DATE, journal.getDate());
            j.put(KEY_NOTE , journal.getNote());
            j.put(KEY_AMOUNT, journal.getAmount());
            j.put(KEY_PARTY_ID, journal.getPartyId());
            j.put(KEY_CREATED_DATE, journal.getCreatedDate());
            j.put(KEY_TYPE, journal.getType().toString());

            JSONArray attachmentJSONs = new JSONArray();
            List<Attachment> attachments = mServices.getAttachments(journal.getId());
            for(Attachment a : attachments){
                attachmentJSONs.put(toJSON(a));
            }

            j.put(KEY_ATTACHMENTS, attachmentJSONs);

        }catch(Exception e){
            e.printStackTrace();
        }

        return j;
    }

    public JSONObject toJSON(Attachment attachment){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put(KEY_ID, attachment.getId());
            jsonObject.put(KEY_PATH, attachment.getPath());
            jsonObject.put(KEY_JOURNAL_ID, attachment.getJournalId());
        }catch (JSONException e) {e.printStackTrace();}

        return jsonObject;
    }

    /**
     * Creates a Party Object from passed json parameter.<br></br>
     * <b>NOTE: Returned Party will not entail Journals and attachments</b>
     * @param json
     * @return
     */
    public static Party getParty(JSONObject json){

        try {
            int id = json.getInt(KEY_ID);
            String name = json.getString(KEY_NAME);
            String type = json.getString(KEY_TYPE);
            String phone = json.getString(KEY_PHONE);
            double debit = json.getDouble(KEY_DEBIT);
            double credit = json.getDouble(KEY_CREDIT);
            String picPath = json.getString(KEY_PICTURE);
            String note = json.has(KEY_NOTE) ? json.getString(KEY_NOTE) : "";

            Party newParty = new Party(name);

            newParty.setId(id);
            newParty.setNote(note);
            newParty.setPhone(phone);
            newParty.setDebitTotal(debit);
            newParty.setCreditTotal(credit);
            newParty.setPicturePath(picPath);
            newParty.setType(Party.Type.valueOf(type));

            return newParty;
        } catch (JSONException e) {
            Log.i(TAG, "Failed parsing json party");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a Journal Object from passed json parameter.
     * <b>NOTE: Returned Journal will not entail attachments</b>
     * @param json
     * @return
     */
    public static Journal getJournal(JSONObject json){
        try {
            int id = json.getInt(KEY_ID);
            long date = json.getLong(KEY_DATE);
            String note = json.getString(KEY_NOTE);
            long partyId = json.getLong(KEY_PARTY_ID);
            double amount = json.getDouble(KEY_AMOUNT);
            long added_date = json.getLong(KEY_CREATED_DATE);
            Journal.Type type = Journal.Type.valueOf(json.getString(KEY_TYPE));

            Journal newJournal = new Journal(partyId);
            newJournal.setCreatedDate(added_date);
            newJournal.setAmount(amount);
            newJournal.setType(type);
            newJournal.setNote(note);
            newJournal.setDate(date);
            newJournal.setId(id);

            return newJournal;
        } catch (JSONException e) {
            Log.i(TAG, "Failed parsing json journal");
            e.printStackTrace();
        }

        return null;

    }

    public static Attachment getAttachment(JSONObject jsonObject){
        Attachment attachment = null;
        try {
            int id = jsonObject.getInt(KEY_ID);
            String path = jsonObject.getString(KEY_PATH);
            int journalId = jsonObject.getInt(KEY_JOURNAL_ID);

            attachment = new Attachment(journalId);
            attachment.setPath(path);
            attachment.setId(id);
        } catch (JSONException e) {
            Log.i(TAG, "Failed parsing json attachment ");
            e.printStackTrace();
        }

        return  attachment;

    }

    /**
     * Returns all the data in JSON format
     * @return
     */
    public JSONArray getAllDataInJson(){
        JSONArray partyJSONs = new JSONArray();

        for (Party party : mServices.getParties()) {
            partyJSONs.put(toJSON(party));
        }

        return partyJSONs;

    }

    private boolean insertIntoDB(String jsonString){
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


    /**
     * Creates a Party Object from passed json parameter
     * @param json
     * @return
     */
    private Party insetPartyIntoDb(JSONObject json){

        try {
            Party newParty = getParty(json);
            if(newParty == null) return null;
            //while adding the journal, credit and debit amount as increment respectively
            //In other words, credit and debit will be calculated again
            newParty.setDebitTotal(0);
            newParty.setCreditTotal(0);

            long id = mServices.addParty(newParty);

            JSONArray journalJSONS = json.getJSONArray(KEY_JOURNALS);
            for(int i = 0 ; i < journalJSONS.length(); i++){
                insertJournalIntoDb(journalJSONS.getJSONObject(i), id);
            }

            return newParty;
        } catch (JSONException e) {	e.printStackTrace();}

        return null;
    }

    /**
     * Creates a Journal Object from passed json parameter
     * @param json
     * @return
     */
    private Journal insertJournalIntoDb(JSONObject json, long partyId){
        try {
            Journal newJournal = getJournal(json);
            newJournal.setPartyId(partyId);
            long newId = mServices.addJournal(newJournal);

            JSONArray attachmentJSONS = json.getJSONArray(KEY_ATTACHMENTS);
            for(int i = 0; i < attachmentJSONS.length(); i++){
                insertAttchIntoDb(attachmentJSONS.getJSONObject(i), newId);
            }

            return newJournal;
        } catch (JSONException e) {e.printStackTrace();}

        return null;

    }

    private Attachment insertAttchIntoDb(JSONObject jsonObject, long journalId) {
        Attachment newAttachment = getAttachment(jsonObject);
        newAttachment.setJournalId(journalId);
        mServices.addAttachment(newAttachment);
        return newAttachment;
    }









}
