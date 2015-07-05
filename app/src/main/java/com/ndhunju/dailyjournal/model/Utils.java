package com.ndhunju.dailyjournal.model;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.NotificationService;

public class Utils {
	
	public static final int REQUEST_PICK_JSON_CODE = 1;
	public static final int REQUEST_TAKE_PHOTO = 2;
	public static final int REQUEST_IMAGE = 4;
	public static final int REQUEST_JOURNAL_CHGD = 5;
	public static final int REQUEST_CHGED_DATE = 6;
	public static final int REQUEST_CHGD_ATTACHMENTS = 7;
	public static final int  REQUEST_PICK_BACKUP_CODE = 8;
	
	public static final int NO_PARTY = -1;
	public static final int ID_NEW_JOURNAL = -2;
	
	private static final int NUM_OF_DIGITS = 7; // Number of digits for
												// merchant's ID
	
	public static final String GREEN = "#5CB85C";
	public static final String RED = "#f63752";
	
	
	public static final String APP_PREFIX = "com.ndhunju.dailyJournal";
	public static final String APP_FOLDER_NAME = "DailyJournal"; 
	public static final String ATTCH_FOLDER_NAME = "attachments";
	public static final String HIDE_FOLDER = "."; //putting . makes the dir hidden, thus not appearing on "Photos" app
	
	//Date Formats
	public static final String DATE_FORMAT_FULL = "EEEE, MMM dd, yyy @ kk:mm a";
	public static final String DATE_FORMAT = "MMM-d-yyy";
	public static final String DATE_FORMAT_SLASH = "M-d-yyy";
	public static final String DATE_FORMAT_SHORT = "EEE, MMM dd";
	public static final String DATE_FORMAT_DAY = "EEEE, MMM dd";
	public static final String DATE_FORMAT_NEPALI = "d/M/yyy";
	
	public static final String NAME_JOURNAL_CHGD = APP_PREFIX + "nameJournalChanged";
	public static final String KEY_JOURNAL_ID = Utils.APP_PREFIX + "journalId";
	public static final String KEY_PARTY_ID = Utils.APP_PREFIX	+ "merchantId";
	public static final String KEY_ATTACHMENTS = Utils.APP_PREFIX + "keyAttachments";
	public static final String KEY_ATTACHMENTS_IS_CHGD = APP_PREFIX + "isAttachmentChanged";
	
	
	private static final String IMG_EXT = ".png";
	public static final String ZIP_EXT = ".dj";
	
	private static String lastPicturePath;
	
	public static String parseDate(Date date, String format) {
		return DateFormat.format(format, date).toString(); 
	}

	public static String getStringId(int id) {
		String nextId = String.valueOf(id);
		String zeros = "";
		for (int i = 0; i <= NUM_OF_DIGITS - nextId.length(); i++)
			zeros += "0";

		return zeros + nextId;
	}
	
	public static void toast(Context context, String msg){
		Toast.makeText(context, msg	, Toast.LENGTH_SHORT).show();;
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
	
	public static String getNepalDate(long date){
		long difference = 1789603921348l;
		return parseDate(new Date(date+difference), DATE_FORMAT_NEPALI);
	}
	
	public static void setNotification(Context con){
		if (Alarm.isServiceAlarmOn(con, NotificationService.class))
			return;
       // set the alarm to notify user to fill the journal
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 21); // 9 PM
		cal.set(Calendar.MINUTE, 30); // 30 mins
			
		Alarm.setAlarmForNotification(con, NotificationService.class, Calendar.getInstance(),
				con.getString(R.string.str_reminder),
				con.getString(R.string.msg_reminder), true, 1000 * 60 * 60 * 24);
	}
	
	public static Intent getPictureFromCam(Activity activity, Journal journal){
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile(activity, journal);
				// Continue only if the File was successfully created
				if (photoFile != null) {
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,	Uri.fromFile(photoFile));
					lastPicturePath = photoFile.getAbsolutePath();
					return takePictureIntent;
				}
			} catch (Exception ex) {
				Log.i("Picture from camera", "Error occurred while creating the File.");
			}
		}
		return null;
	}
	
	public static String getLastPicturePath(){
		return lastPicturePath;
	}
	
	public static File getAppFolder(boolean hide) {
		// Create an app folder
		File appFolder = new File(Environment.getExternalStorageDirectory(), hide ?
				HIDE_FOLDER + Utils.APP_FOLDER_NAME : Utils.APP_FOLDER_NAME);
		if (!appFolder.exists())
			appFolder.mkdir();
		

		return appFolder;
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
	
	public static File createImageFile(Activity activity, Journal journal) {
		
		File attachmentFolder = getAttachmentFolder(getAppFolder(true), true);
		
		File partyFolder = getPartyFolder(attachmentFolder, 
				Storage.getInstance(activity).getParty(journal.getPartyId()).getName(), true);

		//create a file to store image
		String fileName = journal.getPartyId() + "-" + journal.getId() + "-"
				+ journal.getAttachmentPaths().size();
		try {
			File pic = new File(partyFolder.getAbsolutePath(), fileName + IMG_EXT);
			pic.createNewFile();
			return pic;
		} catch (Exception e) {	e.printStackTrace();}

		return null;
	}
	
	public static boolean deleteFile(String path){
		File f = new File(path);
		return f.delete();
	}
	
	public static void zip(File directory, File zipfile) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = out;
		ZipOutputStream zout = null ;
		try {
			zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
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

	public static void unzip(File zipfile, File directory) throws IOException {
		ZipFile zfile = new ZipFile(zipfile);
		Enumeration<? extends ZipEntry> entries = zfile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File file = new File(directory, entry.getName());
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

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
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

}
