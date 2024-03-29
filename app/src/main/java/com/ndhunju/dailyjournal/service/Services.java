package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaScannerConnection;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.database.AttachmentDAO;
import com.ndhunju.dailyjournal.database.DailyJournalContract;
import com.ndhunju.dailyjournal.database.DbHelper;
import com.ndhunju.dailyjournal.database.IAttachmentDAO;
import com.ndhunju.dailyjournal.database.IJournalDAO;
import com.ndhunju.dailyjournal.database.IPartyDAO;
import com.ndhunju.dailyjournal.database.JournalDAO;
import com.ndhunju.dailyjournal.database.PartyDAO;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.json.JsonConverter;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.EnglishToNepaliDateConverter;
import com.ndhunju.dailyjournal.util.ProgressListener;
import com.ndhunju.dailyjournal.util.Utils;
import com.ndhunju.dailyjournal.util.UtilsDate;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import kotlin.Triple;


public class Services {

    // Constants
    private static final String KEY_COMPANY_NAME = "KEY_COMPANY_NAME";
    private static final String KEY_FINANCIAL_YEAR = "KEY_FINANCIAL_YEAR";
    private static final String KEY_LAST_PASSED_DATE_ALERT_SHOWN = "KEY_LAST_PASSED_DATE_ALERT_SHOWN";
    private static final String FILE_COMPANY_INFO = "info.properties";
    private static final String TAG = Services.class.getSimpleName();

    // Variables
    private Context mContext;

    private String mCompanyName;
    private Date mCurrentFinancialYear;
    /**
     * Should {@link com.ndhunju.dailyjournal.controller.home.HomeActivity} refresh when
     * user comes to that activity
     */
    public boolean shouldRefreshHomeScreen = true;

    private double mTodaysTotalDr;
    private double mTodaysTotalCr;
    private int mTodaysJouranlCount;

    private SQLiteOpenHelper mSqLiteOpenHelper;
    private List<Listener> mListeners;

    // DAOs
    private IPartyDAO partyDAO;
    private IJournalDAO journalDAO;
    private IAttachmentDAO attachmentDAO;

    // Static variable
    private static Services mServices;

    public static Services getInstance(Context con) {
        if (mServices == null) {
            mServices = new Services(con);
        }

        if (mServices.mCurrentFinancialYear == null) {
            mServices.loadCompanyInfoFromPreferences();
        }

        return mServices;
    }

    public static Services getInstance(Context con, SQLiteOpenHelper sqLiteOpenHelper) {
        if (mServices == null)	mServices = new Services(con, sqLiteOpenHelper);
        return mServices;
    }

    //Constructor
    private Services(@NonNull Context context) {
        init(context, new DbHelper(context));
    }

    private Services(Context context, SQLiteOpenHelper sqLiteOpenHelper){
        init(context, sqLiteOpenHelper);
    }

    private void init(Context context, SQLiteOpenHelper sqLiteOpenHelper) {
        mContext = context;
        mListeners = new ArrayList<>();
        mSqLiteOpenHelper = sqLiteOpenHelper;
        partyDAO = new PartyDAO(mSqLiteOpenHelper);
        journalDAO = new JournalDAO(mSqLiteOpenHelper);
        attachmentDAO = new AttachmentDAO(mSqLiteOpenHelper);

        loadCompanyInfoFromPreferences();
    }

    public String createBackUp(@NonNull String dir) throws IOException {
        return createBackUp(dir, null);
    }

    /**
     * Creates a backup file of existing data along with attachments.
     * @return : absolute path of the backup file.
     * @throws IOException
     */
    public String createBackUp(
            @NonNull String dir,
            ProgressListener progressListener
    ) throws IOException {

        Log.d(TAG, "createBackUp() called with: " +
                "dir = [" + dir + "], " +
                "progressListener = [" + progressListener + "]" +
                "mContext = " + mContext );

        //1.Create JSON with latest data
        JsonConverterString converter = JsonConverterString.getInstance(mContext);
        String jsonFilePath = converter.createJSONFile();
        Log.d(TAG, "createBackUp: jsonFilePath=" + jsonFilePath);

        //2. Zip app folder as all the attachments and json file are here
        //2.1 Get the app folder
        File directoryToZip = UtilsFile.getAppFolder(mContext);

        File infoFile = new File(directoryToZip.getPath(), FILE_COMPANY_INFO);
        if (!infoFile.exists()) {
            boolean success = infoFile.createNewFile();
            storeCompanyInfoInFile(infoFile);
            Log.d(TAG, "createBackUp: createNewFile() success=" + success);
        }

        //2.2 get backup Folder. Backup file will be created here
        File backupFolder = new File(dir);
        //if the file doesn't exit choose default folder
        if(!backupFolder.exists())  backupFolder = UtilsFile.getAppFolder(false);
        Log.d(TAG, "createBackUp: backupFolder created=" + backupFolder.getPath());


        //2.3 create a zip file in not hidden app folder so that user can use it
        String fileName = getCompanyName() + "-" + UtilsFile.getZipFileName();
        File zipFile = new File(backupFolder.getAbsoluteFile(), fileName);
        boolean zipFileCreationSuccess = zipFile.createNewFile();
        Log.d(TAG, "createBackUp: zipFileCreationSuccess=" + zipFileCreationSuccess);

        //3 zip the directory file into zipFile
        boolean zipFileCreated = UtilsZip.zip(directoryToZip, zipFile, progressListener);
        Log.d(TAG, "createBackUp: zipFileCreated=" + zipFileCreated);

        // Let know that a new file has been created so that it appears in the computer
        // Note: Files created in Downloads folder is shown in Downloads type. But in other
        // places, you have to go inside the "Internal Storage" or "SD Card" folder and find it
        MediaScannerConnection.scanFile(
                mContext,
                new String[]{backupFolder.getAbsolutePath(), zipFile.getAbsolutePath()},
                new String[]{UtilsFile.BACK_FILE_TYPE},
                null
        );

        Log.i(TAG, "Backup file created");

        return zipFile.getAbsolutePath();
    }

    public boolean loadFromJsonAndPropertiesFile(File[] files, JsonConverter converter) {
        boolean foundJson = false, foundProperties = false;
        for (int i = files.length - 1; i >= 0; i--) {
            if (!foundJson && files[i].isFile() && files[i].getName().endsWith(".json")) {
                if (!converter.readFromJSON(files[i].getAbsolutePath()))
                    return false;
                //takes the first json file from the last
                //name of json file has date on it so the latest json file
                //wil likely be at the bottom of the list
                foundJson = true;
            } else if (!foundProperties && files[i].isFile() && files[i].getName().endsWith(".properties")) {
                loadCompanyInfoFromFile(files[i]);
                foundProperties = true;
            }

            if (foundJson && foundProperties) {
                break;
            }
        }

        return true;
    }

    public void storeCompanyInfoInFile(File infoFile) {
        try {
            OutputStream infoFileOutputStream = new FileOutputStream(infoFile);
            Properties properties = new Properties();
            properties.setProperty(KEY_COMPANY_NAME, getCompanyName());
            properties.setProperty(KEY_FINANCIAL_YEAR, String.valueOf(getFinancialYear().getTime()));
            properties.store(infoFileOutputStream, "");
            infoFileOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadCompanyInfoFromFile(File infoFile) {
        try {
            Properties properties = new Properties();
            InputStream inputStream = new FileInputStream(infoFile);
            properties.load(inputStream);
            inputStream.close();
            setCompanyName(properties.getProperty(KEY_COMPANY_NAME));
            // When loading company info from file, mCurrentFinancialYear is already set. So the one
            // from the file is not set unless forced.
            forceSetFinancialYear(new Date(Long.parseLong(properties.getProperty(KEY_FINANCIAL_YEAR))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Return the reference for the {@link Context} this class holds
     * @return
     */
    public Context getContext(){
        return  mContext;
    }

    /********************PARTY SERVICES ************************/

    public void registerPartyObserver(PartyDAO.Observer observer) {
        partyDAO.registerObserver(observer);
    }

    public void unregisterPartyObserver(PartyDAO.Observer observer) {
        partyDAO.unregisterObserver(observer);
    }

    public Party getParty(long partyId) {
        return partyDAO.find(partyId);
    }

    // Possible logic for caching and using cached value in the memory
//	public Party getPartyFromCache(Long partyId) {
//		return partyDAO.findInCacheFirst(partyId);
//	}


    public ArrayList<Party> getParties() {
        ArrayList<Party> parties = (ArrayList<Party>) partyDAO.findAll();
        if(parties != null) return parties;
        return new ArrayList<>();

    }

    public String[] getPartyNameAsArray(){
        return partyDAO.getNamesAsArray();
    }

    /**
     * Adds passed party to the database
     * @param party
     */
    public long addParty(Party party) {
        return partyDAO.create(party);
    }

    /**
     * Adds party with passed name argument and returns a party
     * object with its id in the database
     * @param name
     * @return
     */
    public Party addParty(String name){
        Party party = new Party(name);
        long partyId = addParty(party);
        party.setId(partyId);
        return party;
    }

    /**
     * Deletes the party along with its Journals
     * and attachments
     * @param party
     * @return
     */
    public boolean deleteParty(Party party){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        boolean success = false;
        db.beginTransaction();
        try {
            deleteAllJournals(party.getId());
            UtilsFile.deleteFile(party.getPicturePath());
            partyDAO.delete(party);
            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public boolean updateParty(Party party){
        return (partyDAO.update(party) > 0);
    }

    public boolean addBalanceAsOpeningJournalAndDeleteParty(long startingDate) {
        List<Party> parties = getParties();
        double balance;
        Journal journal;

        for (Party party : parties) {
            balance = party.calculateBalances();
            if (balance != 0) {
                journal = new Journal(party.getId());
                journal.setDate(startingDate);
                journal.setType(balance < 0 ? Journal.Type.Credit : Journal.Type.Debit );
                if (balance < 0) balance *= -1; // get absolute value of balance
                journal.setAmount(balance);
                journal.setNote(mContext.getString(R.string.str_opening_balance));
                // at this point, clear party's Debit and Credit balance
                long rowId = partyDAO.resetDrCrBalance(party.getId());
                if (rowId < 1) {
                    Log.d("test", "resetDrCrBalance() failed");
                }
                // now add opening balance journal
                rowId = addJournal(journal);
                if (rowId < 1) {
                    Log.d("test", "addJournal() failed");
                }
            }
        }

        return true;
    }

    /********************JOURNAL SERVICES ************************/

    public void registerJournalObserver(JournalDAO.Observer observer) {
        journalDAO.registerObserver(observer);
    }

    public void unregisterJournalObserver(JournalDAO.Observer observer) {
        journalDAO.unregisterObserver(observer);
    }

    /**
     * Returns a Journal with passed id.
     * @param journalId
     * @return
     */
    public Journal getJournal(long journalId) {
        return journalDAO.find(journalId);
    }

    /**
     * Returns an instance of new journal which doesn't belong to any party
     * @return
     */
    public Journal getNewJournal(){
        //get the last created new journal's id
        long rowId = getNewJournalId();
        //get the journal from the table
        Journal newJournal = null;
        if(rowId != 0){newJournal= journalDAO.find(rowId);}
        //check if this journal has been associated with a party
        if(newJournal == null || (newJournal.getPartyId() != Constants.NO_PARTY)){
            //if the journal is null or has not been associated with a party, create new
            newJournal = new Journal(Constants.NO_PARTY);
            long id = journalDAO.create(newJournal);
            newJournal.setId(id);
            //save the id of new journal.
            setNewJournalId(id);
        }

        //update the date to Todays
        newJournal.setDate(Calendar.getInstance().getTimeInMillis());

        return newJournal;
    }

    /**
     * Returns Id for the new Journal
     * @return
     */
    public long getNewJournalId(){
        //get the last created new journal's id
        KeyValPersistence persistence = KeyValPersistence.from(mContext);
        // return last save row id
        return persistence.getLong(String.valueOf(Constants.ID_NEW_JOURNAL), 0);
    }

    /**
     * Stores the id of the new journal
     * @param id
     */
    public void setNewJournalId(long id){
        //get the last created new journal's id
        KeyValPersistence persistence = KeyValPersistence.from(mContext);
        persistence.putLong(String.valueOf(Constants.ID_NEW_JOURNAL), id);
    }

    /**
     * Deletes newly created Journal that is not associated with any party.
     * To delete a journal that belongs to a party use {@link #deleteJournal(Journal}
     * @param newJournal
     */
    public boolean deleteNewJournal(Journal newJournal){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success =false;
        try{
            //delete the relevant attachments
            for(Attachment attch : attachmentDAO.findAll(newJournal.getId()))
                deleteAttachment(attch);
            //delete the journal
            journalDAO.delete(newJournal);
            db.setTransactionSuccessful();
            success = true;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }

        return  success;
    }

    /**
     * Updates new journal and makes necessary changes to Party table like modify the
     * balance. This method should be used only with the Journal that is not associated
     * any Party in the table
     * @param newJournal
     */
    public void updateNewJournal(Journal newJournal){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try{
            journalDAO.update(newJournal);
            updatePartyAmount(newJournal, "+");
            db.setTransactionSuccessful();
            shouldRefreshHomeScreen = true;
        }catch (Exception e){
            //can have custom exception such as negative balance
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public ArrayList<Journal> getJournals(long partyId){
        ArrayList<Journal> journals = (ArrayList<Journal>)journalDAO.findAll(partyId);
        if(journals != null) {
            return journals;
        }

        return new ArrayList<>();
    }

    public ArrayList<Journal> getJournalsWithBalance(long partyId){
        ArrayList<Journal> journals = (ArrayList<Journal>)journalDAO.findAll(partyId);
        // Calculate the balances for journals
        if(journals != null) {
            double balance = 0;
            for (Journal journal : journals) {
                if (journal.getType() == Journal.Type.Debit) {
                    balance += journal.getAmount();
                } else {
                    balance -= journal.getAmount();
                }
                journal.setBalance(balance);
            }

            return journals;
        }

        return new ArrayList<>();
    }

// Possible logic for caching and using cached value in the memory
//	public ArrayList<Journal> getJournalsFromCache(long partyId){
//		ArrayList<Journal> journals = (ArrayList<Journal>)journalDAO.findAllInCacheFirst(partyId);
//		if(journals != null) return journals;
//		return new ArrayList<>();
//	}

    public ArrayList<Journal> getJournals(){
        return (ArrayList<Journal>)journalDAO.findAll();
    }

    /**
     * Adds Journal that is associated with a Party
     * @param journal
     * @return
     */
    public long addJournal(Journal journal){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        long id = Constants.ID_NEW_JOURNAL;
        try{
            updatePartyAmount(journal, "+");
            id = journalDAO.create(journal);
            db.setTransactionSuccessful();
            shouldRefreshHomeScreen = true;
        }catch (Exception e){
            //can have custom exception such as negative balance
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }

        return id;
    }

    /**
     * Deletes the journal from the table along with associated Attachments
     * @param journal
     * @return
     */
    public boolean deleteJournal(Journal journal){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success =false;
        try {
            // Delete the relevant attachments
            for (Attachment attch : attachmentDAO.findAll(journal.getId())) {
                deleteAttachment(attch);
            }

            // Delete the journal
            journalDAO.delete(journal);

            // Subtract from the Dr/Cr column in party table
            if(!(updatePartyAmount(journal, "-") > 0)) {
                throw new Exception("No rows updated");
            }

            db.setTransactionSuccessful();
            shouldRefreshHomeScreen = true;
            success = true;
        } catch (Exception e){
            e.printStackTrace();
            AnalyticsService.INSTANCE.logEvent("didFailDeleteJournal", e.getMessage());
        } finally {
            db.endTransaction();
        }

        return  success;

    }

    /**
     * Helper method that updates the debit or credit column
     * of a party based on journal type
     * @param updatedJournal : journal that has been updated
     * @param operation : + or - ; + performs similar to x += x;
     */
    private int updatePartyAmount(Journal updatedJournal, String operation){
        if(updatedJournal.getType() == Journal.Type.Debit){
            return partyDAO.updateDr(updatedJournal, operation);
        }else{
            return partyDAO.updateCr(updatedJournal, operation);
        }
    }

    /**
     * Deletes all the journal of passed party
     * @param partyId
     * @return
     */
    private boolean deleteAllJournals(long partyId){
        List<Journal> journals = journalDAO.findAll(partyId);
        int deletedJournalCount = 0;
        for (Journal journal : journals) {
            boolean isSuccess = deleteJournal(journal);
            if (isSuccess) {
                deletedJournalCount++;
            }
        }

        if (deletedJournalCount != journals.size()) {
            AnalyticsService.INSTANCE.logEvent(
                    "didFailDeleteAllJournal",
                    "Deleted only " + deletedJournalCount + "/" + journals.size()
            );
        }

        return deletedJournalCount == journals.size();
    }

    /**
     * Updates the journal and associated party table
     */
    public boolean updateJournal(Journal journal) {
        /*
        * Possible changes in the journal,
     * 1. Amount Change => if(type == dr) drAmt -= amount : crAmt -= amount
     * 2. Date change => just change the date in journal row
     * 3. Type (Dr/Cr) change =>  if(type == dr) drAmt -= amount & crAmt += amount
     * 4. Combination of above things
     * For most effecient way you can handle cases separately
     * but for now we will just delete old journal and add changed one
     * as a new journal
         */
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            //make changes to party
            //when the journal type is changed, the logic fails. So,
            //1. get old journal from database and use it to update the party table
            Journal oldJournal = journalDAO.find(journal.getId());
            //subtract from the Dr/Cr column in party table
            if (!(updatePartyAmount(oldJournal, "-") > 0))
                throw new Exception("No rows updated");

            //update the party table with new values of the journal
            updatePartyAmount(journal, "+");

            //update the journal
            journalDAO.update(journal);

            db.setTransactionSuccessful();
            shouldRefreshHomeScreen = true;
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return success;
    }

    public List<Journal> findByNotes(String keyword) {
        return journalDAO.findByNotes(keyword);
    }

    public List<Journal> findByDate(long start, long end) {
        return journalDAO.findByDate(start, end, getNewJournalId());
    }

    public List<Journal> findByPartyAndDate(long partyId, long date) {
        List<Journal> journals = journalDAO.findByPartyAndDate(partyId, date, getNewJournalId());

        // Calculate the balances for journals
        if(journals != null) {
            double balance = 0;
            for (Journal journal : journals) {
                if (journal.getType() == Journal.Type.Debit) {
                    balance += journal.getAmount();
                } else {
                    balance -= journal.getAmount();
                }
                journal.setBalance(balance);
            }

            return journals;
        }

        return new ArrayList<>();
    }

    // declare calendar outside the scope of isWithinFinancialYear() so that we initialize it only once
    private final Calendar calendar = Calendar.getInstance();

    /**
     * Whether passed {@code date} for a Journal is allowed or not based on user's preference
     * or if it is within the current financial year
     */
    public boolean isAllowedDateForJournal(long date) {
        boolean isOutOfRangeEntryAllowed = PreferenceService
                .from(mContext)
                .getVal(R.string.key_pref_allow_out_of_range_entry, false);

        if (isOutOfRangeEntryAllowed) {
            return true;
        }

        return isWithinFinancialYear(date);
    }

    public boolean isWithinFinancialYear(long date) {

        calendar.setTime(getFinancialYear());
        int startDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int startYear = calendar.get(Calendar.YEAR);

        calendar.add(Calendar.YEAR, 1);
        int endDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int endYear = calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(date);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);

        return (year == startYear && dayOfYear >= startDayOfYear)
                || (year == endYear && dayOfYear < endDayOfYear);

    }

    /**
     * Returns true if {@code time} has passed current financial year period as returned by
     * {@link this#getFinancialYear()} and last time this function return true has passed a day.
     */
    public boolean shouldShowAlertForPassingFinancialYearDate(long time) {

        long lastShownTime = PreferenceService.from(mContext).getVal(
                KEY_LAST_PASSED_DATE_ALERT_SHOWN,
                0
        );

        long timeElapsed = (System.currentTimeMillis() - lastShownTime);

        // Check if it's been more than a day since this function was called and returned true
        if (timeElapsed > DateUtils.DAY_IN_MILLIS
                // Check if param, date, has passed current financial year
                &&  mServices.numOfDaySinceCurrentFinancialYear(time) > 0) {
            // Store current time when this function returned true
            PreferenceService.from(mContext).putVal(
                    KEY_LAST_PASSED_DATE_ALERT_SHOWN,
                    System.currentTimeMillis()
            );
            return true;
        }

        return false;
    }

    /**
     * Returns the number of days that {@code time} has passed current financial year time.
     */
    public int numOfDaySinceCurrentFinancialYear(long time) {

        calendar.setTime(getFinancialYear());

        calendar.add(Calendar.YEAR, 1);
        int endDayOfFinancialYear = calendar.get(Calendar.DAY_OF_YEAR);
        int endYearOfFinancialYear = calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(time);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);

        boolean isInTheFuture = year >= endYearOfFinancialYear && dayOfYear > endDayOfFinancialYear;

        if (isInTheFuture) {
            int yearsInTheFuture = year - endYearOfFinancialYear;
            int daysInTheFuture = yearsInTheFuture * 365;
            daysInTheFuture += dayOfYear - endDayOfFinancialYear;
            return daysInTheFuture;
        } else {
            return -1;
        }

    }

    /********************COMPANY SERVICES ************************/

    private void loadCompanyInfoFromPreferences() {
        PreferenceService preferenceService = PreferenceService.from(mContext);
        mCompanyName = preferenceService.getVal(KEY_COMPANY_NAME, "");

        long now = System.currentTimeMillis();
        long financialYear = preferenceService.getVal(KEY_FINANCIAL_YEAR, now /* default value */);
        if (mCurrentFinancialYear == null || financialYear != now) {
            // set mCurrentFinancialYear only if not same as default value
            mCurrentFinancialYear = new Date(financialYear);
        }
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String name) {
        mCompanyName = name;
        PreferenceService.from(mContext).putVal(KEY_COMPANY_NAME, name);
        shouldRefreshHomeScreen = true;
    }

    public Date getFinancialYear() {
        if (mCurrentFinancialYear == null) {
            loadCompanyInfoFromPreferences();
        }
        return mCurrentFinancialYear;
    }

    public void setFinancialYear(Date financialYear) throws RuntimeException {
        if (financialYear == null) {
            throw new NullPointerException("Financial year can not be empty");
        }

        // When restoring from backup, mCurrentFinancialYear is null
        if (mCurrentFinancialYear != null
               && !UtilsDate.isSameDay(financialYear, mCurrentFinancialYear)) {
            // don't allow to change mCurrentFinancialYear if already set because there could be
            // journal with date that might be outside the range of this new financial year
            if (getJournals().size() > 0) {
                throw new IllegalStateException("Current financial year is already set.");
            }
        }

        mCurrentFinancialYear = financialYear;
        PreferenceService.from(mContext).putVal(KEY_FINANCIAL_YEAR, financialYear.getTime());
        shouldRefreshHomeScreen = true;
    }

    public void forceSetFinancialYear(Date financialYear) {
        mCurrentFinancialYear = financialYear;
        PreferenceService.from(mContext).putVal(KEY_FINANCIAL_YEAR, financialYear.getTime());
        shouldRefreshHomeScreen = true;
    }

    public boolean hasValidCompanyInfo() {
        return !TextUtils.isEmpty(mCompanyName) && mCurrentFinancialYear != null;
    }

    public void clearCompanyInfo() {
        mCompanyName = null;
        mCurrentFinancialYear = null;
        shouldRefreshHomeScreen = true;
    }

    /********************ATTACHMENT SERVICES ************************/

    public Attachment getAttachment(long attchID){
        return attachmentDAO.find(attchID);
    }

    public List<Attachment> getAttachments(long journalId){
        return attachmentDAO.findAll(journalId);
    }

    public ArrayList<String> getAttachmentPaths(long journalId){
        ArrayList<String> paths = new ArrayList<>();
        for(Attachment a : getAttachments(journalId))
            paths.add(a.getPath());
        return paths;
    }


    public long addAttachment(Attachment attachment){
       return attachmentDAO.create(attachment);
    }

    public void addAttachments(List<Attachment> attachments){
        attachmentDAO.bulkInsert(attachments);
    }

    /**
     * Deletes the attachment from physical storage as
     * well as from the table. Use {@link #deleteAttachment(Attachment)}
     * if you have the attachment object.
     * @return
     */
    public boolean deleteAttachment(Attachment attch)
    {
        // 1. See if there is an attachment that shares file with attch
        Attachment linkedAttachment = null;
        if (attch.getLinkedAttachmentId() != null) {
            linkedAttachment = attachmentDAO.find(attch.getLinkedAttachmentId());
        }

        // 2. Delete the physical file from storage only,
        // if the file is not shared between 2 attachments
        if (linkedAttachment == null) {
            if (!UtilsFile.deleteFile(attch.getPath())) {
                AnalyticsService.INSTANCE.logEvent(
                        "didFailDeleteFile",
                        "Path=" + attch.getPath()
                );
                return false;
            }
        }

        // 3. Delete from the database
        attachmentDAO.delete(attch);

        // 4. Update linkedAttachment's extra value
        if (linkedAttachment != null) {
            linkedAttachment.setLinkedAttachmentId(null);
            attachmentDAO.update(linkedAttachment);
        }

        return true;
    }

    public boolean deleteAllAttachments(long journalId){
        for(Attachment attach : attachmentDAO.findAll(journalId))
            if(!deleteAttachment(attach))
                return false;
        return true;
    }

    public boolean updateAttachment(Attachment attachment){
        return (attachmentDAO.update(attachment) > 0);
    }

    /********************MISCELLANEOUS*******************************/

    public double getDebitTotal() {
        double totalUserDebit = 0.0;
        for (Party party: getParties()) {
            totalUserDebit += party.getCreditTotal();
        }

        return totalUserDebit;
    }

    public double getCreditTotal() {
        double totalUserCredit = 0.0;
        for (Party party : getParties()) {
            totalUserCredit += party.getDebitTotal();
        }

        return totalUserCredit;
    }

    public int getTotalJournalCount() {
        return getNewJournalId() > 0 ? journalDAO.findAll().size() - 1 : journalDAO.findAll().size();
    }

    public void calculateTodaysDrCrTotal() {
        mTodaysTotalDr = 0;
        mTodaysTotalCr = 0;

        List<Journal> todaysJournal = findByPartyAndDate(Constants.NO_PARTY, Utils.removeValuesBelowHours(Calendar.getInstance()).getTimeInMillis());
        mTodaysJouranlCount = todaysJournal.size();

        // Calculate the balances for journals
        if(todaysJournal != null) {
            for (Journal journal : todaysJournal) {
                if (journal.getType() == Journal.Type.Debit) {
                    mTodaysTotalCr += journal.getAmount();
                } else {
                    mTodaysTotalDr += journal.getAmount();
                }
            }
        }
    }

    public double getTodaysDebit() {
        return mTodaysTotalDr;
    }

    public double getTodaysCredit() {
        return mTodaysTotalCr;
    }

    public int getTodaysJournalCount() {
        return mTodaysJouranlCount;
    }


    /********************DELETION FUNCTIONS ************************/
    /**
     * Deletes all the rows in the tables but not the schema. It
     * doesn't reset any autoincrement columns
     * @return
     */
    public boolean truncateAllTables(){
        //doesn't reset the table ids
        partyDAO.truncateTable();
        journalDAO.truncateTable();
        attachmentDAO.truncateTable();
        return true;
    }

    /**
     * Deletes all the table and recreates them
     * @return
     */
    public boolean recreateDB(){
        return dropAllTables() &&
                createDB();
    }

    public boolean recreateJournalDB() {
        return dropJournalTable() && createJournalDB();
    }

    private boolean dropJournalTable() {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.execSQL(DailyJournalContract.JournalColumns.SQL_DROP_ENTRIES_JOURNALS);
        shouldRefreshHomeScreen = true;
        return true;
    }

    private boolean createJournalDB() {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.execSQL(DailyJournalContract.JournalColumns.SQL_CREATE_ENTRIES_JOURNALS);
        return true;
    }

    public boolean recreateAttachmentsDB() {
        return dropAttachmentTable() && createAttachmentDB();
    }

    private boolean dropAttachmentTable() {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.execSQL(DailyJournalContract.AttachmentColumns.SQL_DROP_ENTRIES_ATTACHMENTS);
        return true;
    }

    private boolean createAttachmentDB() {
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.execSQL(DailyJournalContract.AttachmentColumns.SQL_CREATE_ENTRIES_ATTACHMENTS);
        return true;
    }


    /**
     * Creates tables in the database
     * @return
     */
    public boolean createDB(){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        db.execSQL(DailyJournalContract.PartyColumns.SQL_CREATE_ENTRIES_PARTY);
        db.execSQL(DailyJournalContract.JournalColumns.SQL_CREATE_ENTRIES_JOURNALS);
        db.execSQL(DailyJournalContract.AttachmentColumns.SQL_CREATE_ENTRIES_ATTACHMENTS);
        return true;
    }

    /**
     * Clears all parties. <b>Note: </b> It doesn't delete corresponding files/attachments
     * from app folder. For that, {@link #eraseAll()} should be called
     * @return
     */
    public boolean dropAllTables(){
        SQLiteDatabase db = mSqLiteOpenHelper.getWritableDatabase();
        try{
            db.execSQL(DailyJournalContract.AttachmentColumns.SQL_DROP_ENTRIES_ATTACHMENTS);
            db.execSQL(DailyJournalContract.JournalColumns.SQL_DROP_ENTRIES_JOURNALS);
            db.execSQL(DailyJournalContract.PartyColumns.SQL_DROP_ENTRIES_PARTY);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Deletes all parties, journals along with the attachments from the storage as well as
     * from the database. However, it recreates the DB schema
     * @return
     */
    public boolean eraseAll(){
        boolean success = true;
        try{
            success &= recreateDB();
            success &= KeyValPersistence.from(mContext).clear();
            success &= PreferenceService.from(mContext).clear();
            success &= UtilsFile.deleteDirectory(UtilsFile.getAppFolder(mContext));
            success &= UtilsFile.cleanCacheDir(mContext);
            clearCompanyInfo();
            notifyEraseAllListener();
        }catch (Exception e){
            e.printStackTrace();
        }

        return success;
    }

    /**
     * @see Services#eraseAllJournalsOnly()
     */
    public boolean eraseAllJournalsAndResetPartyBalance() {
        boolean success = true;
        try {
            success &= recreateJournalDB();
            success &= recreateAttachmentsDB();
            success &= eraseAllAttachmentFiles();
            success &= partyDAO.resetDrCrBalance() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * This one is similar to {@link Services#eraseAllJournalsAndResetPartyBalance()}
     * but unlike that this one carries over the balances for the party. This is
     * specifically designed to be used for starting a new year in the app.
     */
    public boolean eraseAllJournalsOnly() {
        boolean success = true;
        try {
            success &= recreateJournalDB();
            success &= recreateAttachmentsDB();
            success &= eraseAllAttachmentFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean eraseAllAttachmentFiles() {
        boolean success = true;
        File[] files = UtilsFile.getAttachmentFolder(
                UtilsFile.getAppFolder(getContext()),
                true
        ).listFiles();

        if (files == null) return false;

        for (File attch : files) {
            // make sure it is a file. Could be party folder
            if (attch.isFile()) success &= UtilsFile.deleteFile(attch.getAbsolutePath());
        }

        return success;
    }

    public void convertEnglishDateToNepaliAndPrefixToNotes(ProgressListener progressListener) {

        ArrayList<Journal> journals = getJournals();
        Calendar currentCalendar = Calendar.getInstance();
        Triple<Integer, Integer, Integer> result;
        Journal currentJournal;
        String currentNote;
        String nepaliDate;
        long currentDate;

        for (int i = 0; i < journals.size(); i++)
        {
            currentJournal = journals.get(i);
            currentNote = currentJournal.getNote();
            currentDate = currentJournal.getDate();
            currentCalendar.setTimeInMillis(currentDate);

            result = EnglishToNepaliDateConverter.INSTANCE
                    .convertToNepali(
                            currentCalendar.get(Calendar.YEAR),
                            currentCalendar.get(Calendar.MONTH),
                            currentCalendar.get(Calendar.DAY_OF_MONTH)
            );

            nepaliDate = result.getFirst() + "/" + result.getSecond() + "/" + result.getThird();

            // Prefix note with Nepali date only if it's not already done so
            if (!currentNote.startsWith(nepaliDate)) {
                currentJournal.setNote(nepaliDate + " - " + currentNote);
            }

            updateJournal(currentJournal);

            //float per = i / (float) journals.size();
            //percentage = (int) (i / (float) journals.size() * 100);

            //Log.d(TAG, "convertEnglishDateToNepaliAndPrefixToNotes: float=" + per + "percentage=" + percentage);

            progressListener.onProgress(
                    ProgressListener.PROGRESS_DETERMINATE,
                    (int) (i / (float) journals.size() * 100),
                    currentJournal.getDate() + "->" + nepaliDate,
                    ProgressListener.RESULT_OK
            );
        }

        progressListener.onProgress(
                ProgressListener.PROGRESS_DETERMINATE,
                100,
                getContext().getString(R.string.str_finished),
                ProgressListener.RESULT_OK
        );
    }

    public void addListener(Listener listener) {
        if (listener == null) return;
        mListeners.add(listener);
    }

    public boolean removeListener(Listener listener) {
        if (listener == null) return false;
        return mListeners.remove(listener);
    }

    private void notifyEraseAllListener() {
        for (Listener listener : mListeners) {
            listener.onEraseAll();
        }
    }

    public interface Listener {
        void onEraseAll();
    }
}
