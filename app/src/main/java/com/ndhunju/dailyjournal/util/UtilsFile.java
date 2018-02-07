package com.ndhunju.dailyjournal.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.ndhunju.dailyjournal.model.Party;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
	private static final String HIDE_FOLDER = ".";
	private static final String APP_CACHE_FOLDER_NAME ="Cache";
	public static final String APP_FOLDER_NAME = "DailyJournal";
	private static final String ATTCH_FOLDER_NAME = "attachments";
	private static final String AUTO_BACKUP_FOLDER_NAME = "backups";


	//File Extension types
    public static final String ZIP_EXT = ".zip";
	private static final String IMG_EXT = ".png";
	public static final String ZIP_EXT_OLD = ".dj";
	public static final String BACK_FILE_TYPE = "application/zip";
    private static final String TEMP_IMG_FILE_NAME = "temp" + IMG_EXT;

	//Variables
	private static String appDir;

	public static Intent getPictureFromCam(Activity activity){
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
	public static String getCacheDir(Context con){
		return con.getDir(UtilsFile.APP_CACHE_FOLDER_NAME, Context.MODE_PRIVATE)
				.getAbsolutePath();
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

	public static String getAutoBackupDir(Context con){
		return con.getDir(AUTO_BACKUP_FOLDER_NAME, Context.MODE_PRIVATE)
				.getAbsolutePath();
	}

	public static File[] getAutoBackUpFiles(Context con){
		File backupFolder = new File(getAutoBackupDir(con));
		return backupFolder.listFiles();
	}

	private static String getAppDir(){
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
	
	private static File getPartyFolder(File attchFolder, String partyName, boolean hide) {
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
	 * @deprecated Document directory is not guaranteed to be present in all device. Rather
	 * use {@link UtilsFile#getPublicDownloadDir()}
     *
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

    public static String getPublicDownloadDir(){
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadDir.exists()) {
        	downloadDir.mkdirs();
		}

		return downloadDir.getAbsolutePath();
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

    public static File createImageFile(Context context) {

		//the file returned is inside the app folder which can be accessed by
		//the app only. So Camera app cannot stream photo to this file

		File attachmentFolder = getAttachmentFolder(getAppFolder(context), true);
		
		String fileName = UUID.randomUUID().toString();

		try {
			File pic = null;
			pic = new File(attachmentFolder.getAbsolutePath(), fileName + IMG_EXT);
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

		return directory.delete();
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
			Log.i("store image", pic.getName() + " was stored successfully.");

		} catch (Exception e) {
			Log.w("TAG", "Error saving image file: " + e.getMessage());
			UtilsView.alert(c, "Error saving the file.");
		}
	}

    /**Get a file path from a Uri. */
    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            String contentId;
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                contentId = DocumentsContract.getDocumentId(uri);
                final String[] split = contentId .split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                contentId = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(contentId));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                contentId = DocumentsContract.getDocumentId(uri);
                final String[] split = contentId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
