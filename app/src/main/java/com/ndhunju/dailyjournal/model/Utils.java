package com.ndhunju.dailyjournal.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.ndhunju.dailyjournal.R;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Utils {

	public static final int NO_PARTY = -1;
	public static final int ID_NEW_JOURNAL=-2;
	public static final int NUM_OF_DIGITS = 7; // Number of digits for ID

	//Hex Value of colors
	public static final String RED = "#f63752";
	public static final String GREEN = "#5CB85C";
	public static final String BLACK = "#000000";

	//Folder Names that the App uses
	public static final String HIDE_FOLDER = ".";
	public static final String APP_CACHE_FOLDER_NAME ="Cache";
	public static final String APP_FOLDER_NAME = "DailyJournal";
	public static final String ATTCH_FOLDER_NAME = "attachments";

	//Date Formats
	public static final String DATE_FORMAT = "MMM-d-yyy";
	public static final String DATE_FORMAT_DASH = "M-d-yyy";
	public static final String DATE_FORMAT_NEPALI = "d/M/yyy";
	public static final String DATE_FORMAT_DAY = "EEEE, MMM dd";
	public static final String DATE_FORMAT_SHORT = "EEE, MMM dd";
	public static final String DATE_FORMAT_FOR_FILE = "M-d-yyy-kk-mm-ss"; 			//kk for 24 hours format
	public static final String DATE_FORMAT_FULL="EEEE, MMM dd, yyy @ kk:mm a";


	//Constant Keys usu used to get and put data in an Intent or Bundle
	public static final String APP_PREFIX = "com.ndhunju.dailyJournal";
	public static final String KEY_PARTY_NAME = APP_PREFIX + "partyName";
	public static final String KEY_IMPORTED = APP_PREFIX + "KeyImported";
	public static final String KEY_REQUEST_CODE = APP_PREFIX + "RequestCode";
	public static final String KEY_JOURNAL_ID = Utils.APP_PREFIX + "journalId";
	public static final String KEY_PARTY_ID = Utils.APP_PREFIX	+ "merchantId";
	public static final String KEY_JOURNAL_CHGD = APP_PREFIX + "nameJournalChanged";
	public static final String KEY_ATTACHMENTS = Utils.APP_PREFIX + "keyAttachments";
	public static final String KEY_PARTY_INFO_CHGD = APP_PREFIX + "partyInfoChanged";
	public static final String KEY_IMPORT_OLD_DATA = APP_PREFIX + "KeyImportedOLdData";
	public static final String KEY_ATTACHMENTS_IS_CHGD = APP_PREFIX + "isAttachmentChanged";

	//File Extension types
    public static final String ZIP_EXT = ".zip";
    private static final String IMG_EXT = ".png";
	public static final String ZIP_EXT_OLD = ".dj";
    public static final String TEMP_IMG_FILE_NAME = "temp" + IMG_EXT;

	//Variables
	private static String appDir;

	/**
	 * Formats the passed date object in passed format.
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
     * @param currency
     * @return
     */
    public static String formatCurrency(Double currency){
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        return nf.format(currency);
    }

    public static double parseCurrency(String currency) throws NumberFormatException {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        double doubleCurrency = 0;
        try{
            doubleCurrency = nf.parse(currency).doubleValue();
        }catch(ParseException pe){
            //try parsing it for regular double string
            try{
                doubleCurrency  = Double.parseDouble(currency);
            }catch(NumberFormatException nfe){
                Log.d("Format", "Incorrect format " + currency);
                throw new NumberFormatException("Incorrect number format");
            }
        }

        return doubleCurrency;
    }

	/**
	 * Returns String representation of an int Id.
	 * Eg. 1 = "00000001"
	 * @param id
	 * @return
	 */
	public static String getStringId(int id, int noOfDigits) {
		String nextId = String.valueOf(id);
		String zeros = "";
		for (int i = 1; i <= noOfDigits - nextId.length(); i++)
			zeros += "0";
		return zeros + nextId;
	}

	public static void toast(Context context, String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static void toast(Context con, String msg, int length){
		Toast.makeText(con, msg, length).show();
	}
	
	/**
	 * This method shows a message to users. Similar to alert function
	 * in JavaScript
	 * @param msg : message to show in the alert dialog
	 * @param con : context
	 */
	public static void alert(Context con,String msg){
		new AlertDialog.Builder(con).setMessage(msg)
		.setTitle(con.getString(R.string.str_alert))
		.setPositiveButton(android.R.string.ok, null)
		.create().show();
	}

	public static void alert(Context con,String msg, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener CancelLister){
		new AlertDialog.Builder(con).setMessage(msg)
				.setTitle(con.getString(R.string.str_alert))
				.setPositiveButton(android.R.string.ok, listener)
				.setNegativeButton(android.R.string.cancel, CancelLister)
				.create().show();
	}

	public static void alert(Context con,String msg, DialogInterface.OnClickListener OkListener){
		new AlertDialog.Builder(con).setMessage(msg)
				.setTitle(con.getString(R.string.str_alert))
				.setPositiveButton(android.R.string.ok, OkListener)
				.setCancelable(false)
				.create().show();
	}
	
	public static Intent getPictureFromCam(Activity activity, Journal journal){
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createExternalStoragePublicPicture();
				// Continue only if the File was successfully created
				if (photoFile != null) {
                    //Send the path of the picture via Intent
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,	Uri.fromFile(photoFile));
					return takePictureIntent;
				}
			} catch (Exception ex) {
				Log.i("Picture from camera", "Error occurred while creating the File.");
			}
		}
		return null;
	}
	
	/**
	 * Returns a folder that will store app data.
	 *  <b>NOTE:</b> <i>Since Android 4.4+, an app cannot delete files created in SD card
	 * Thus while deleting a party, it's respective attachment files
	 * cannot be deleted even though it was created by the app. Instead use
	 * {@link #getAppFolder(Context)}  </i>
	 * @param hide
	 * @return
     * TODO Use this method to export files to SD Card later if users want.
	 */
	public static File getAppFolder(boolean hide) {
		// Create an app folder
		File appFolder = new File(Environment.getExternalStorageDirectory(), hide ?
				HIDE_FOLDER + Utils.APP_FOLDER_NAME : Utils.APP_FOLDER_NAME);
		//when hide= true, it is asking for oldApp Folder, don't create dir
		if(hide) return appFolder;

		if (!appFolder.exists())
			appFolder.mkdir();

		return appFolder;
	}

    /**
     * Checks whether old app folder (v3.1) exists or not.
     * @return
     */
	public static boolean oldAppFolderExist(){
		// Create an app folder
		File oldAppFolder = new File(Environment.getExternalStorageDirectory(),
				HIDE_FOLDER + Utils.APP_FOLDER_NAME );
		return oldAppFolder.exists();

	}

	/**
	 * Returns app's folder in an internal storage if exists otherwise creates a new one
	 * @param context
	 * @return
	 */
	public static File getAppFolder(Context context){
		File appFolder = context.getDir(Utils.APP_FOLDER_NAME, Context.MODE_PRIVATE);
		if(!appFolder.exists()) appFolder.mkdir();
		appDir = appFolder.getAbsolutePath();
		return appFolder;
	}

	/**
	 * Unlike {@link Activity#getCacheDir()} this method doesn't limit storage size
	 * to 1 MB.
	 * @param con
	 * @return
	 */
	public static File getCacheDir(Context con){
		return con.getDir(Utils.APP_CACHE_FOLDER_NAME, Context.MODE_PRIVATE);
	}

    /**
     * Clears up all the content of the Cache Folder excluding the Folder.
     * It is recursive.
     * @param activity
     * @return
     */
	public static boolean cleanCacheDir(Activity activity){
		File file = activity.getDir(Utils.APP_CACHE_FOLDER_NAME, Context.MODE_PRIVATE);
		boolean success = false;
		try{
			success = Utils.deleteDirectory(file);
		} catch (IOException e) {
			Log.d("Cache Dir", "Error deleting cache folder");
			e.printStackTrace();
		}

		return  success;
	}

	public static String getAppDir(){
		return appDir;
	}

	/**
	 * Since app's data such as attachments are now stored in internal storage
	 * we need to check if the path for attachments are still referring to external(old)
	 * storage. If it is, then change it to the new one
	 * @param path
	 * @return
	 */
	public static String replaceOldDir(String path){
		String oldPath = getAppFolder(true).getAbsolutePath();
		if(path.contains(oldPath)){
			path = path.replace(oldPath, Utils.getAppDir());
		}
		return path;
	}

    public static File getAttachmentFolder(File appFolder, boolean hide) {
		File attchFolder = new File(appFolder.getAbsolutePath(), hide ? HIDE_FOLDER + Utils.ATTCH_FOLDER_NAME :
			Utils.ATTCH_FOLDER_NAME); //. makes it invisible
		if (!attchFolder.exists())
			attchFolder.mkdir();

		return attchFolder;
	}
	
	public static File getPartyFolder(File attchFolder, String partyName, boolean hide) {
		// Create a party folder
		File partyFolder = new File(attchFolder.getAbsolutePath(), hide ? HIDE_FOLDER +  partyName :
			partyName); //. makes it invisible
		if (!partyFolder.exists())
			partyFolder.mkdir();

		return partyFolder;
	}

    /**
     * Create a temporary image file in public Picture directory. It can be used to save an
     * image taken by camera app since camera app cannot access thus save image to app's
     * private folder
     * @return
     */
    public static File createExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory.  Note that you should be careful about
        // what you place here, since the user often manages these files.  For
        // pictures and other media owned by the application, consider
        // Context.getExternalMediaDir().
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, TEMP_IMG_FILE_NAME);

        // Make sure the Pictures directory exists.
        path.mkdirs();
        return file;
    }

    public static boolean deleteExternalStoragePublicPicture() {
        // Create a path where we will place our picture in the user's
        // public pictures directory and delete the file.  If external
        // storage is not currently mounted this will fail.
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, TEMP_IMG_FILE_NAME);
        return file.delete();
    }

    public static File createImageFile(Context context, Journal journal, Party party) {

		//TODO: the file returned is inside the app folder which can be accessed by
		//the app only. So Camera app cannot stream photo to this file

		File attachmentFolder = getAttachmentFolder(getAppFolder(context), true);
		
		File partyFolder = getPartyFolder(attachmentFolder, party.getName(), true);

		/*This is prolly the secure way but I want to keep name short and simple and avoid the
		overhead of imorting UUID class just for this purpose
		String fileName = UUID.randomUUID().toString();*/

		try {
			String fileName;
			File pic = null;
			int i = 0;
			do{
				/*This way of naming creates collision when let's say two files 1-1-0.png and 1-1-1.png
		 		exist. User deletes the first one. Later adds a new one which will be name 1-1-1.png*/
				fileName = journal.getPartyId() + "-" + journal.getId() + "-"
						+ (journal.getAttachmentPaths().size()+ i);
				pic = new File(partyFolder.getAbsolutePath(), fileName + IMG_EXT);
				i++;
			}
			while(pic.exists());

			pic.createNewFile();

			return pic;
		} catch (Exception e) {	e.printStackTrace();}

		return null;
	}

	/**
	 * Attempts to delete the file with passed path. Since Android 4.4, files in
	 * SD Card can't be deleted (using {@link #getAppFolder(boolean)} but there should
     * be no problem deleting files created using {@link #getAppFolder(Context)}
	 * @param path
	 * @return
	 */
	public static boolean deleteFile(String path){
		File f = new File(path);
		if(!f.exists())
			return true;
		boolean t = f.isFile();
		boolean u = f.isHidden();
		return f.delete();
	}

    /**
     * Deletes all the content inside the passed directory
     * @param directory : directory to clean
     * @return
     * @throws IOException : if not directory is found
     */
	public static boolean deleteDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			final String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
			}

		if (!directory.isDirectory()) {
		 final String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
			}

		final File[] files = directory.listFiles();
		if (files == null) {  // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
			}

		for (final File file : files) {
			//if it is a directory, go inside it with recursive call
			if(file.isDirectory())	deleteDirectory(file);
			else if(!file.delete()) return false;
			}

		if(!directory.delete()) return false;

		return true;
	}

	/**
	 * Zips passed File/Directory and writes the zipped content into passed finalZipFile
	 * @param directoryToZip
	 * @param finalZipFile
	 * @throws IOException
	 */
	public static void zip(File directoryToZip, File finalZipFile) throws IOException {
		URI base = directoryToZip.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directoryToZip);
		OutputStream out = new FileOutputStream(finalZipFile);
		Closeable res = out;
		ZipOutputStream zout = null ;
		try {
			zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directoryToZip = queue.pop();
				for (File kid : directoryToZip.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					} else {
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
			
		} catch(Exception e){
			e.printStackTrace();
		}finally {
			res.close();
			out.close();
			zout.close();
		}
	}

	/**
	 * Unzips passed zipped file into passed directory (directoryToUnzip)
	 * @param zipFile
	 * @param directoryToUnzip
	 * @throws IOException
	 */
	public static void unzip(File zipFile, File directoryToUnzip) throws IOException {
		ZipFile zfile = new ZipFile(zipFile.getAbsolutePath());
		Enumeration<? extends ZipEntry> entries = zfile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File file = new File(directoryToUnzip, entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				file.getParentFile().mkdirs();
				InputStream in = zfile.getInputStream(entry);
				try {
					copy(in, file);
				} finally {
					in.close();
				}
			}
		}
		zfile.close();
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[2048];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	public static void copyInputStream( BufferedReader in, BufferedWriter out )throws IOException {
		char[] buffer=new char[1024];
		int len;
		while ( ( len=in.read(buffer) ) >= 0 )
		{
			out.write(buffer, 0, len);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

	public static byte[] read(File file) throws IOException {

		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ( (read = ios.read(buffer)) != -1 ) {
				ous.write(buffer, 0, read);
			}
		} finally {
			try {
				if ( ous != null )
					ous.close();
			} catch ( IOException e) {
			}

			try {
				if ( ios != null )
					ios.close();
			} catch ( IOException e) {
			}
		}
		return ous.toByteArray();
	}

	public static byte[] read(InputStream in) throws IOException {

		ByteArrayOutputStream ous = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			int read = 0;
			while ( (read = in.read(buffer)) != -1 ) {
				ous.write(buffer, 0, read);
			}
		} finally {
			try {
				if ( ous != null )
					ous.close();
			} catch ( IOException e) {
			}

			try {
				if ( in != null )
					in.close();
			} catch ( IOException e) {
			}
		}
		return ous.toByteArray();
	}

	/**
	 * Returns file name for JSON file that will store data
	 * @return
	 */
	public static String getJSONFileName(){
		return "dailyJournal-" + formatDate(new Date(), Utils.DATE_FORMAT_FOR_FILE) + ".json";
	}

	public static String getZipFileName(){
		return "dailyJournal-" + Utils.formatDate(new Date(), Utils.DATE_FORMAT_FOR_FILE) + Utils.ZIP_EXT;
	}

    /**
     * Stores the bitmap image into passed file. The operation in done in background thread
     * @param imageData
     * @param pic
     * @param c
     */
    public static void storeImage(final Bitmap imageData, final File pic, final Context c) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(pic);
                    //Log.i("path", c.getFilesDir().getAbsolutePath());
                    BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

                    // choose another format if PNG doesn't suit you
                    imageData.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bos.flush();
                    bos.close();
                    //to let know that a new file has been created so that it appears in the computer
                    MediaScannerConnection.scanFile(c, new String[]{pic.getAbsolutePath()}, null, null);
                    Log.i("store image" , pic.getName() + " was stored successfully.");

                } catch (Exception e) {
                    Log.w("TAG", "Error saving image file: " + e.getMessage());
                    Utils.alert(c, "Error saving the file.");
                }
                return null;
            }
        }.execute();

    }
}
