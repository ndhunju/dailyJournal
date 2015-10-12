package com.ndhunju.dailyjournal.service;

import android.app.Activity;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 9/29/2015.
 * This is a services class that deals with retrieving
 * contact details
 */
public class ImportContacts {

    //Sub class to hold basic details of a contact
    public static class Contact{
        public String id;
        public String name;
        public boolean hasPhoneNum;

        Contact(String id, String name, boolean hasPhoneNum){
            this.id = id;
            this.name = name;
            this.hasPhoneNum = hasPhoneNum;
        }
    }

    /**
     * Returns List of {@link com.ndhunju.dailyjournal.service.ImportContacts.Contact}
     * There is no need to call this method in background thread as it utilizes
     * {@link CursorLoader} to load the data from ContentProvider
     * @param activity
     * @return
     */
    public static List<Contact> getContacts(Activity activity){

        String[] projection = {  ContactsContract.Contacts._ID,
                                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                                ContactsContract.Contacts.HAS_PHONE_NUMBER};

        /*Cursor cursor = contentResolver.query(
                                ContactsContract.Contacts.CONTENT_URI,
                                projection, null, null,
                                ContactsContract.Contacts.DISPLAY_NAME);*/

        //CursorLoader loads data in worker thread as opposed to other option
        CursorLoader cl = new CursorLoader(activity,
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME);

        Cursor cursor = cl.loadInBackground();

        List<Contact> contacts = new ArrayList<>();
        if(!cursor.moveToFirst())
            return contacts;


        do{
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            boolean hasPhoneNum = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) >0;
            contacts.add(new Contact(id, name, hasPhoneNum));
        }while(cursor.moveToNext());

        return contacts;

    }

    /**
     * This method reads ContentProvider for Contacts and creates respective Parties and saves it to
     * the database. <Note>This method should be called in the background thread</Note>
     * @param activity
     * @param contactsToImport
     */
    public static void importContacts(Activity activity, List<Contact> contactsToImport){
        Contact currentContact;
        for(int i = 0 ; i < contactsToImport.size() ; i++) {
            currentContact = contactsToImport.get(i);
            Party newParty = new Party(currentContact.name);

            if (currentContact.hasPhoneNum) {
                String[] contactSelection = {ContactsContract.CommonDataKinds.Phone.NUMBER,
                                             ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI};

                Cursor c = activity.getContentResolver()
                        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, contactSelection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + currentContact.id,
                        null, null);


                if(!c.moveToFirst()) return;

                String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                newParty.setPhone(number);

                String photoUri = c.getString(
                        c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));

                if(photoUri != null){
                    Uri photo = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(currentContact.id));
                    photo = Uri.withAppendedPath( photo, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY );
                    try {
                        Bitmap bitmap =  MediaStore.Images.Media.getBitmap(activity.getContentResolver(), photo);
                        File localPic = UtilsFile.getPartyPicture(newParty, activity);
                        UtilsFile.storeImage(bitmap, localPic, activity);
                        newParty.setPicturePath(localPic.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                c.close();
            }
            Services mServices = Services.getInstance(activity);
            mServices.addParty(newParty);

        }



    }

    /**
     * Helper method that return list of names present in
     * Contact class
     * @param contacts
     * @return
     */
    public static  CharSequence[] getNames(List<Contact> contacts){
        CharSequence[] names = new CharSequence[contacts.size()];
        for(int i = 0; i < contacts.size(); i++){
            names[i] = contacts.get(i).name;
        }

        return names;
    }
}
