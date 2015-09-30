package com.ndhunju.dailyjournal.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.UUID;

public class UtilsFile {

	//Folder Names that the App uses
	public static final String HIDE_FOLDER = ".";
	public static final String APP_CACHE_FOLDER_NAME ="Cache";
	public static final String APP_FOLDER_NAME = "DailyJournal";
	public static final String ATTCH_FOLDER_NAME = "attachments";


	//File Extension types
    public static final String ZIP_EXT = ".zip";
	public static final String IMG_EXT = ".png";
	public static final String ZIP_EXT_OLD = ".dj";
	public static final String BACK_FILE_TYPE = "application/zip";
    public static final String TEMP_IMG_FILE_NAME = "temp" + IMG_EXT;

	//Variables
	private static String appDir;

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
     * Use this method to export files to SD Card later if users want.
	 */
	public static File getAppFolder(boolean hide) {
		// Create an app folder
		File appFolder = new File(Environment.getExternalStorageDirectory(), hide ?
				HIDE_FOLDER + UtilsFile.APP_FOLDER_NAME : UtilsFile.APP_FOLDER_NAME);
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
				HIDE_FOLDER + UtilsFile.APP_FOLDER_NAME );
		return oldAppFolder.exists();

	}

	/**
	 * Returns app's folder in an internal storage if exists otherwise creates a new one
	 * @param context
	 * @return
	 */
	public static File getAppFolder(Context context){
		File appFolder = context.getDir(UtilsFile.APP_FOLDER_NAME, Context.MODE_PRIVATE);
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
		return con.getDir(UtilsFile.APP_CACHE_FOLDER_NAME, Context.MODE_PRIVATE);
	}

    /**
     * Clears up all the content of the Cache Folder excluding the Folder.
     * It is recursive.
     * @param activity
     * @return
     */
	public static boolean cleanCacheDir(Activity activity){
		File file = activity.getDir(UtilsFile.APP_CACHE_FOLDER_NAME, Context.MODE_PRIVATE);
		boolean success = false;
		try{
			success = UtilsFile.deleteDirectory(file);
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
			path = path.replace(oldPath, UtilsFile.getAppDir());
		}
		return path;
	}

    public static File getAttachmentFolder(File appFolder, boolean hide) {
		File attchFolder = new File(appFolder.getAbsolutePath(), hide ? HIDE_FOLDER + UtilsFile.ATTCH_FOLDER_NAME :
			UtilsFile.ATTCH_FOLDER_NAME); //. makes it invisible
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

	/**
	 * Create a temporary image file in public Picture directory. It can be used to save an
	 * image taken by camera app since camera app cannot access thus save image to app's
	 * private folder
	 * @return
	 */
	public static File createFileInDocumentFolder(String fileName) {
		File path = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOCUMENTS);
		// Make sure the Pictures directory exists.
		path.mkdirs();
		File file = new File(path, fileName);
		return file;
	}

    public static String getPublicDocumentDir(){
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
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

		//the file returned is inside the app folder which can be accessed by
		//the app only. So Camera app cannot stream photo to this file

		File attachmentFolder = getAttachmentFolder(getAppFolder(context), true);
		
		File partyFolder = getPartyFolder(attachmentFolder, party.getName(), true);

		String fileName = UUID.randomUUID().toString();

		try {
			File pic = null;
			pic = new File(partyFolder.getAbsolutePath(), fileName + IMG_EXT);
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

	public static void copyInputStream( BufferedReader in, BufferedWriter out )throws IOException {
		char[] buffer=new char[1024];
		int len;
		while ( ( len=in.read(buffer) ) >= 0 )
		{
			out.write(buffer, 0, len);
		}
	}

	public static void copyFile(File from, File to) throws IOException {
		InputStreamReader fromIsr = new FileReader(from);
		OutputStreamWriter toOsr = new OutputStreamWriter(new FileOutputStream(to));

		char[] buffer = new char[2048];
		int c;
		while ((c = fromIsr.read(buffer)) != -1) {
			toOsr.write(buffer, 0, c);
		}

		toOsr.flush();
		toOsr.close();
		fromIsr.close();
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
		return "dailyJournal-" + UtilsFormat.formatDate(new Date(), UtilsFormat.DATE_FORMAT_FOR_FILE) + ".json";
	}

	public static String getZipFileName(){
		return "dailyJournal-" + UtilsFormat.formatDate(new Date(), UtilsFormat.DATE_FORMAT_FOR_FILE) + UtilsFile.ZIP_EXT;
	}

	public static File getPartyPicture(Party party, Context con){
		File partyFolder = getPartyFolder(getAttachmentFolder(getAppFolder(con),false), party.getName(), false);
		File partyPic = new File(partyFolder, party.getName() + IMG_EXT);
		if(partyPic.exists()) return  partyPic;
		else try {
			partyPic.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return partyPic;
	}

    /**
     * Stores the bitmap image into passed file. The operation in done in background thread
     * @param imageData
     * @param pic
     * @param c
     */
    public static void storeImage(final Bitmap imageData, final File pic, final Context c) {
		new Thread(new Runnable() {
			@Override
			public void run() {
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
					UtilsView.alert(c, "Error saving the file.");
				}
			}
		}).start();
    }

}
