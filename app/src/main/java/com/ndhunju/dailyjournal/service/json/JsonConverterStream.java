package com.ndhunju.dailyjournal.service.json;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by dhunju on 10/15/2015.
 *  This class converts POJO into equivalent json format
 * and vice versa using Stream
 */
public class JsonConverterStream extends JsonConverter {

    private static final String TAG = JsonConverterStream.class.getSimpleName();

    private static JsonConverterStream jsonConverterStream;

    public static JsonConverterStream getInstance(Context context){
        if(jsonConverterStream == null)
            jsonConverterStream = new JsonConverterStream(context);
        return jsonConverterStream;
    }

    protected JsonConverterStream(Context context) {
        super(context);
    }

    @Override
    public boolean readFromJSON(String jsonFilePath) {
        File jsonFile = new File(jsonFilePath);
        try(InputStream inputStream = new FileInputStream(jsonFile)) {
            parseJSONStream(inputStream);
            Log.i(TAG, "Finished reading from stream : " + jsonFilePath);
            return true;
        } catch (IOException e) {
            Log.i(TAG, "Failed reading Ofrom stream : " + jsonFilePath);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean writeToJSON(String jsonFilePath) {
        File jsonFile = new File(jsonFilePath);
        try(OutputStream outputStream = new FileOutputStream(jsonFile)) {
            writeJSONToStream(outputStream);
            Log.i(TAG, "Finished writing to stream : " + jsonFilePath);
            return true;
        } catch (IOException e) {
            Log.i(TAG, "Failed writing to stream : " + jsonFilePath);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Writes all the data from Database to passed outputstream in
     * JSON format
     * @param outputStream
     * @throws IOException
     */
    public void writeJSONToStream(OutputStream outputStream) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream));
        writeParties(writer);
        writer.close();
        outputStream.close();
    }

    public void writeParties(JsonWriter writer) throws IOException {
        //print open square bracket [
        writer.beginArray();
        for(Party party : mServices.getParties()){
            writeParty(writer, party);
        }
        //print close square bracket ]
        writer.endArray();
    }

    /**
     * Write passed party to passed Json writer
     * @param writer
     * @param party
     * @throws IOException
     */
    public void writeParty(JsonWriter writer, Party party) throws IOException {
        //print open curly braces {
        writer.beginObject();

        //print keys and values
        writer.name(KEY_ID).value(party.getId());
        writer.name(KEY_NAME).value(party.getName());
        writer.name(KEY_PHONE).value(party.getPhone());
        writer.name(KEY_DEBIT).value(party.getDebitTotal());
        writer.name(KEY_CREDIT).value(party.getCreditTotal());
        writer.name(KEY_TYPE).value(party.getType().toString());
        writer.name(KEY_PICTURE).value(party.getPicturePath());

        //print journals
        writeJournals(writer, party.getId());

        //print close curly braces }
        writer.endObject();
    }

    public void writeJournals(JsonWriter writer, long partyId) throws IOException {
        writer.name(KEY_JOURNALS);
        writer.beginArray();
        for(Journal journal: mServices.getJournals(partyId)){
            writeJournal(writer, journal);
        }

        writer.endArray();
    }

    public void writeJournal(JsonWriter writer, Journal journal) throws IOException {
        writer.beginObject();
        writer.name(KEY_ID).value(journal.getId())
              .name(KEY_DATE).value(journal.getDate())
                .name(KEY_NOTE).value(journal.getNote())
                .name(KEY_AMOUNT).value(journal.getAmount())
                .name(KEY_PARTY_ID).value(journal.getPartyId())
                .name(KEY_CREATED_DATE).value(journal.getCreatedDate())
                .name(KEY_TYPE).value(journal.getType().toString());

        writeAttachments(writer, journal.getId());

        writer.endObject();
    }

    public void writeAttachments(JsonWriter writer, long journalId) throws IOException {
        writer.name(KEY_ATTACHMENTS);
        writer.beginArray();
        for(Attachment attachment : mServices.getAttachments(journalId)){
            writeAttachment(writer, attachment);
        }
        writer.endArray();
    }

    public void writeAttachment(JsonWriter writer, Attachment attachment) throws IOException {
        writer.beginObject();
        writer.name(KEY_ID).value(attachment.getId())
                .name(KEY_PATH).value(attachment.getPath())
                .name(KEY_JOURNAL_ID).value(attachment.getJournalId());
        writer.endObject();
    }

    /**
     * Write data from the database to the passed input stream
     * @param stream : input stream
     * @return
     * @throws IOException
     */
    public boolean parseJSONStream(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        JsonReader jsonReader = new JsonReader(reader);
        insertPartiesIntoDb(jsonReader);
        reader.close();
        jsonReader.close();

        return true;
    }

    /**
     * Inserts list of Party objects into the database
     * @param jsonReader
     * @throws IOException
     */
    private void insertPartiesIntoDb(JsonReader jsonReader) throws IOException {

        Party newParty = new Party("");
        //Consume the open square bracket [
        jsonReader.beginArray();

        //loop through Parties list
        while (jsonReader.hasNext())
        {insertParty(jsonReader, newParty);}

        //consume close square bracket ]
        jsonReader.endArray();
    }

    /**
     * Inserts a Party and it's Journals and attachments into the database
     * @param jsonReader
     * @param newParty
     * @throws IOException
     */
    private void insertParty(JsonReader jsonReader, Party newParty) throws IOException {
        //consume open curly braces {
        jsonReader.beginObject();

        //loop through keys/names which represents variable
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            //if the value is null, continue to next key
            if (jsonReader.peek() == JsonToken.NULL) continue;

            if (key.equals(KEY_ID)) newParty.setId(jsonReader.nextLong());
            else if (key.equals(KEY_TYPE)) newParty.setType(Party.Type.valueOf(jsonReader.nextString()));
            else if (key.equals(KEY_NAME)) newParty.setName(jsonReader.nextString());
            else if (key.equals(KEY_PHONE)) newParty.setPhone(jsonReader.nextString());
            //Debit and Credit balance will be calculated as Journals are added
            //else if (key.equals(KEY_DEBIT)) newParty.setDebitTotal(jsonReader.nextDouble());
            //else if (key.equals(KEY_CREDIT)) newParty.setCreditTotal(jsonReader.nextDouble());
            else if (key.equals(KEY_PICTURE)) newParty.setPicturePath(jsonReader.nextString());
            else if (key.equals(KEY_JOURNALS)) {
                //this logic assumes that KEY_JOURNALS comes at the end
                long id = mServices.addParty(newParty);
                Log.d(TAG, newParty.getName() + " created");
                insertJournalsIntoDb(jsonReader, id);
            } else jsonReader.skipValue(); //skip unknown key
        }

        //consume close curly braces }
        jsonReader.endObject();
    }


    private void insertJournalsIntoDb(JsonReader jsonReader, long partyId) throws IOException {

        jsonReader.beginArray();
        Journal journal = new Journal(partyId);
        while(jsonReader.hasNext()){
            insertJournal(jsonReader, journal);
        }

        jsonReader.endArray();

    }

    private void insertJournal(JsonReader jsonReader, Journal journal) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String key = jsonReader.nextName();
            if(jsonReader.peek() == JsonToken.NULL) continue;
            if (key.equals(KEY_ID)) journal.setId(jsonReader.nextLong());
            else if (key.equals(KEY_DATE)) journal.setDate(jsonReader.nextLong());
            else if (key.equals(KEY_NOTE)) journal.setNote(jsonReader.nextString());
            else if (key.equals(KEY_AMOUNT)) journal.setAmount(jsonReader.nextDouble());
            else if (key.equals(KEY_CREATED_DATE)) journal.setCreatedDate(jsonReader.nextLong());
            else if (key.equals(KEY_TYPE)) journal.setType(Journal.Type.valueOf(jsonReader.nextString()));
            else if (key.equals(KEY_ATTACHMENTS)) {
                //this logic assumes that key_attachments comes at the end
                long newId = mServices.addJournal(journal);
                insertAttachmentsIntoDB(jsonReader, newId);
            }
            else jsonReader.skipValue();
        }
        jsonReader.endObject();
    }

    private void insertAttachmentsIntoDB(JsonReader jsonReader, long journalId) throws IOException {

        Attachment attachment = new Attachment(journalId);
        jsonReader.beginArray();
        while (jsonReader.hasNext()){
            insertAttachment(jsonReader, attachment);
        }

        jsonReader.endArray();
    }

    private void insertAttachment(JsonReader jsonReader, Attachment attachment) throws IOException {
        if(jsonReader.peek() == JsonToken.NAME) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String key = jsonReader.nextName();
                if (jsonReader.peek() == JsonToken.NULL) continue;
                if (key.equals(KEY_ID)) attachment.setId(jsonReader.nextLong());
                else if (key.equals(KEY_PATH)) {
                    attachment.setPath(jsonReader.nextString());
                    correctAttachmentPathIfInvalid(mServices, attachment);
                }
                else jsonReader.skipValue();
            }
            mServices.addAttachment(attachment);
            jsonReader.endObject();
        }else{
            //for old json format
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) continue;
                else {
                    attachment.setPath(jsonReader.nextString());
                    correctAttachmentPathIfInvalid(mServices, attachment);
                }
            }
            mServices.addAttachment(attachment);
        }
    }
}
