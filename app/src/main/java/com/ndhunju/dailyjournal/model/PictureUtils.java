package com.ndhunju.dailyjournal.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import com.ndhunju.dailyjournal.R;

public class PictureUtils {
	/**
	 * Get a BitmapDrawable from a local file that is scaled down to fit the
	 * current Window size.
	 */
	@SuppressWarnings("deprecation")
	public static BitmapDrawable getScaledDrawable(Activity a, String path) {
		Display display = a.getWindowManager().getDefaultDisplay();
		float destWidth = display.getWidth();
		float destHeight = display.getHeight();

		// read in the dimensions of the image on disk
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // when set to true, it doesn't load
											// the image on memory but just the
											// dimension
		BitmapFactory.decodeFile(path, options);

		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;

		int inSampleSize = 1;
		if (srcHeight > destHeight || srcWidth > destWidth) {
			if (srcWidth > srcHeight) {
				inSampleSize = Math.round((float) srcHeight
						/ (float) destHeight);
			} else {
				inSampleSize = Math.round((float) srcWidth / (float) destWidth);
			}
		}

		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;

		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		return new BitmapDrawable(a.getResources(), bitmap);
	}

	public static void cleanImageView(ImageView imageView) {
		if (!(imageView.getDrawable() instanceof BitmapDrawable))
			return;

		// clean up the view's image for the sake of memory
		BitmapDrawable b = (BitmapDrawable) imageView.getDrawable();
		if (b.getBitmap() == null)
			return;
		b.getBitmap().recycle();
		imageView.setImageDrawable(null);
	}

	public static void storeImage(final Bitmap imageData, final File pic, final Context c) {
		/*// get path to external storage (SD card)
		File sdIconStorageDir = new File(filename);

		// create storage directories, if they don't exist
		sdIconStorageDir.mkdirs();
*/
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
										
					FileOutputStream fileOutputStream = new FileOutputStream(pic);
					//Log.i("path", c.getFilesDir().getAbsolutePath());
					BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

					// choose another format if PNG doesn't suit you
					imageData.compress(CompressFormat.PNG, 100, bos);
					bos.flush();
					bos.close();
					//to let know that a new file has been created so that it appears in the computer
					MediaScannerConnection.scanFile(c, new String[]{ pic.getAbsolutePath()}, null	, null);
					Log.i("store image" , pic.getName() + " was stored successfully.");

				} catch (Exception e) {
					Log.w("TAG", "Error saving image file: " + e.getMessage());
					Utils.alert(c, "Error saving the file.");
				}
				return null;
			}
		}.execute();
		
	}
	
	public static boolean fileExist(String fileName, Context con){
		File file = con.getFileStreamPath(fileName);
		boolean exist = file.exists();
		if(exist)
			Log.i("File exist", fileName);
		
		return exist;
	}
	
	

	public static Bitmap getSavedImage(Context c, String fullPath) {

		return BitmapFactory.decodeFile(fullPath);
	}

	public static String getFullPath(Context c, String merchantName, String fileName){
		return c.getString(R.string.app_name) + merchantName + fileName;
	}
}
