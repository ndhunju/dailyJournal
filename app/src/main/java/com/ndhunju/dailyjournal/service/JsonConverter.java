package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.util.Log;

import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

/**
 * Created by dhunju on 9/21/2015.
 */
public final class JsonConverter {
    
    private static final String TAG = JsonConverter.class.getSimpleName();

    //Keys used for respective properties
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE="type";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_DEBIT = "debit";
    private static final String KEY_CREDIT = "credit";
    private static final String KEY_JOURNALS = "journals";


    //Keys used for respective properties
    private static final String KEY_DATE = "date";
    private static final String KEY_NOTE = "note";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_PARTY_ID = "partyId";
    private static final String KEY_ADDED_DATE = "addedDate";
    private static final String KEY_ATTACHMENTS = "attachments";


    //Keys used for respective properties
    private static final String KEY_PATH = "path";
    private static final String KEY_JOURNAL_ID= "journalId";

    //Variables
    Services mServices;

    public JsonConverter(Services ser){
        mServices = ser;
    }

    /**
     * Creates and returns JSON object for this party
     * @return
     */
    public JSONObject toJSON(Party party){
        JSONObject json = new JSONObject();
        try{
            json.put(KEY_ID, party.getId());
            json.put(KEY_NAME, party.getName());
            json.put(KEY_PHONE, party.getPhone());
            json.put(KEY_DEBIT, party.getDebitTotal());
            json.put(KEY_CREDIT, party.getCreditTotal());
            json.put(KEY_TYPE, party.getType().toString());

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
            j.put(KEY_ADDED_DATE , journal.getAddedDate());
            j.put(KEY_TYPE , journal.getType().toString());
            j.put(KEY_PARTY_ID, journal.getPartyId());
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
        JSONObject j = new JSONObject();
        try{
            j.put(KEY_ID, attachment.getId());
            j.put(KEY_PATH, attachment.getPath());
            j.put(KEY_JOURNAL_ID, attachment.getJournalId());
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return j;
    }

    /**
     * Creates a Party Object from passed json parameter
     * @param json
     * @return
     */
    public Party insetPartyIntoDb(JSONObject json){

        try {
            Party newParty = getParty(json);
            if(newParty == null) return null;
            //while adding the journal, credit and debit amount as increment respectively
            //In other words, credit and debit will be calculated again
            newParty.setCreditTotal(0);
            newParty.setDebitTotal(0);
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
    public Journal insertJournalIntoDb(JSONObject json, long partyId){
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

    public Attachment insertAttchIntoDb(JSONObject jsonObject, long journalId) {
        Attachment newAttachment = getAttachment(jsonObject);
        newAttachment.setJournalId(journalId);
        mServices.addAttachment(newAttachment);

        return newAttachment;

    }

    /**
     * Creates a Party Object from passed json parameter. It will not
     * return Journals
     * @param json
     * @return
     */
    public Party getParty(JSONObject json){

        try {
            int id = json.getInt(KEY_ID);
            String name = json.getString(KEY_NAME);
            String phone = json.getString(KEY_PHONE);
            String type = json.getString(KEY_TYPE);
            double debit = json.getDouble(KEY_DEBIT);
            double credit = json.getDouble(KEY_CREDIT);

            Party newParty = new Party(name);
            newParty.setId(id);
            newParty.setPhone(phone);
            newParty.setDebitTotal(debit);
            newParty.setCreditTotal(credit);
            newParty.setType(Party.Type.valueOf(type));

            return newParty;
        } catch (JSONException e) {	e.printStackTrace();}

        return null;
    }

    /**
     * Creates a Journal Object from passed json parameter. <b>Note</b> This
     * will not give attachments
     * @param json
     * @return
     */
    public Journal getJournal(JSONObject json){
        try {
            int id = json.getInt("id");
            long date = json.getLong(KEY_DATE);
            String note = json.getString(KEY_NOTE);
            long partyId = json.getLong(KEY_PARTY_ID);
            double amount = json.getDouble(KEY_AMOUNT);
            long added_date = json.getLong(KEY_ADDED_DATE);
            Journal.Type type = Journal.Type.valueOf(json.getString(KEY_TYPE));

            Journal newJournal = new Journal(date);
            newJournal.setAddedDate(added_date);
            newJournal.setAmount(amount);
            newJournal.setType(type);
            newJournal.setNote(note);
            newJournal.setId(id);

            return newJournal;
        } catch (JSONException e) {e.printStackTrace();}

        return null;

    }

    public Attachment getAttachment(JSONObject jsonObject){
        Attachment attachment = null;
        try {
            int id = jsonObject.getInt(KEY_ID);
            String path = jsonObject.getString(KEY_PATH);
            int journalId = jsonObject.getInt(KEY_JOURNAL_ID);
            attachment = new Attachment(journalId);
            attachment.setPath(path);
            attachment.setId(id);
        } catch (JSONException e) {e.printStackTrace();}

        return  attachment;

    }

    /**
     * Creates a JSON file containing the data into predefined ({@link UtilsFile#getAppFolder(Context)} ) path
     * in sd card. <b>It deletes the old JSON file</b>
     * @return If successful, returns the absolute path of the file. Otherwise, null.
     */
    public String createJSONFile() throws IOException {

        try {
            // Create a app folder
            File appFolder = UtilsFile.getAppFolder(mServices.getContext());

            //Store old json files in an array so that it can be deleted once
            //new json file is created successfully
            ArrayList<File> filesToDelete = new ArrayList<>();
            for(File f : appFolder.listFiles()){
                if(f.getName().endsWith(".json"))
                    filesToDelete.add(f);
            }

            //Create new json file
            String fileName = UtilsFile.getJSONFileName();
            File jsonFile = new File(appFolder.getAbsolutePath(),fileName );

            if(!jsonFile.createNewFile())
                Log.d(TAG, "Fail to create file " + jsonFile.getAbsolutePath());

            //Write data to newly created JSON file in json format
            FileOutputStream fileOutputStream = new FileOutputStream(jsonFile.getAbsoluteFile());
            fileOutputStream.write(getJSONDb().toString().getBytes());
            fileOutputStream.close();

            Log.i(TAG, "JSON backup created");

            //Delete Old files so that there is always one latest copy
            for(File f : filesToDelete){
                f.delete();
            }

            return jsonFile.getAbsolutePath();

        } catch (IOException e) {
            Log.w(TAG, "Error creating json backup file: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Returns all the data in JSON format
     * @return
     */
    public JSONArray getJSONDb(){
        JSONArray partyJSONs = new JSONArray();
        for (Party party : mServices.getParties()) {
            partyJSONs.put(toJSON(party));
        }

        return partyJSONs;

    }

    /**
     * Parses JSON data and creates corresponding objects (eg. Party, Journal etc)
     * @param filePath : path of the json file
     * @return
     */
    public boolean parseJSONFile(String filePath)throws InputMismatchException{

        //check if the file extension matches
        if(!filePath.endsWith(".json"))
        {throw new InputMismatchException(filePath + " not a json file.");}

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
                jsonString.append(line );

            boolean success = insertIntoDB(jsonString.toString());
            if(!success) new OldJsonConverter().insertIntoDB(jsonString.toString());


            return true;
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse JSON file. " + e.getMessage());

            e.printStackTrace();
        }
        return false;

    }

    public boolean insertIntoDB(String jsonString){
        JSONArray partyJSONArray = null;
        try {
            partyJSONArray = new JSONArray(jsonString);
            for (int i = 0; i < partyJSONArray.length(); i++) {
                if (insetPartyIntoDb(partyJSONArray.getJSONObject(i)) == null)
                    return false;
            }
        } catch (JSONException e) {
            Log.w(TAG, "Error parsing json file" );
            return false;
        }
        return true;
    }


    //Json converter for old backup file
    class OldJsonConverter{

        public boolean insertIntoDB(String jsonString){
            Log.i("OLD " + TAG , "Attempting to parse json with old keys");
            JSONArray partyJSONArray = null;
            try {
                partyJSONArray = new JSONArray(jsonString);
                for (int i = 0; i < partyJSONArray.length(); i++) {
                    if (insetPartyIntoDb(partyJSONArray.getJSONObject(i)) == null)
                        return false;
                }
            } catch (JSONException e) {
                Log.w("OLD " + TAG , "Error parsing json file" );
                return false;
            }
            return true;
        }

        public Party insetPartyIntoDb(JSONObject json){

            try {
                Party newParty = getParty(json);
                //while adding the journal, credit and debit amount as increment respectively
                //In other words, credit and debit will be calculated again
                newParty.setCreditTotal(0);
                newParty.setDebitTotal(0);
                long id = mServices.addParty(newParty);

                JSONArray journalJSONS = json.getJSONArray("journals");
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
        public Journal insertJournalIntoDb(JSONObject json, long partyId){
            try {
                Journal newJournal = getJournal(json);
                newJournal.setPartyId(partyId);
                long newId = mServices.addJournal(newJournal);

                JSONArray attachmentJSONS = json.getJSONArray("attachments");
                for(int i = 0; i < attachmentJSONS.length(); i++){
                    insertAttchIntoDb(attachmentJSONS.getString(i), newId);
                }
                return newJournal;
            } catch (JSONException e) {e.printStackTrace();}

            return null;

        }

        public Attachment insertAttchIntoDb(String path, long journalId) {
            Attachment attachment = new Attachment(journalId);
            //Since app's data such as attachments are now stored in internal storage
            //we need to check if the path for attachments are still referring to external(old)
            //storage. If it is, then change it to the new one
            path = UtilsFile.replaceOldDir(path);
            attachment.setJournalId(journalId);
            attachment.setPath(path);

            mServices.addAttachment(attachment);

            return attachment;

        }

        /**
         * Creates a Journal Object from passed json parameter
         * @param json
         * @return
         */
        public  Journal getJournal(JSONObject json){
            try {
                int id = json.getInt("id");
                long date = json.getLong("date");
                long added_date = json.getLong("added_date");
                Journal.Type type = Journal.Type.valueOf(json.getString("type"));
                double amount = json.getDouble("amount");
                String note = json.getString("mNote");


                Journal newJournal = new Journal(date);
                newJournal.setAddedDate(added_date);
                newJournal.setAmount(amount);
                newJournal.setType(type);
                newJournal.setNote(note);

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
                String type = json.getString("type");
                Party.Type t = type.equals("Debitors") ? Party.Type.Debtors : Party.Type.valueOf(type);
                newParty.setPhone(phone);
                newParty.setType(t);

                return newParty;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

}
