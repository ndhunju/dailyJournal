package com.ndhunju.dailyjournal.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by dhunju on 1/25/2016.
 */
public class Utils {

    public static boolean isKitKat(){
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
    }

    public static boolean contains(long[] array, long value) {
        for (long item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }

    /** Removes hours, minutes, seconds and milliseconds from {@code calendar}*/
    public static Calendar removeValuesBelowHours(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * Scales down the Bitmap from {@code path} to match as much as possible with {@code newWidth}
     * and {@code newHeight}
     */
    public static Bitmap scaleBitmap(String path, int newWidth, int newHeight) {
        Bitmap scaledBitmap = null;
        try {
            // find how much we need to scale
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;  // don't decode whole Bitmap now
            File file = new File(path);
            InputStream inputStream = new FileInputStream(file);
            BitmapFactory.decodeStream(inputStream, null, options);

            /** This method will cause OOM error for large images **/
            //scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            /** This method reduces quality of image a little lower**/
            //scaledBitmap = Picasso.with(getContext()).load(imageUri).resize(newWidth, newHeight).onlyScaleDown().get();

            // config best quality option
            /** The sample size computed here does not compute the value correctly. Hence the changed is made in the scaleRatio which guarantees
             * a sample size of atleast 2, reducing the image size by half. */
            options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, newWidth, newHeight);
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            options.inTargetDensity = 0;
            options.inScaled = false;

            // decode file to bitmap
            scaledBitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return scaledBitmap;
    }

    /**
     * We want to decode the smallest bitmap possible over the minimum size.
     * Return the highest power of 2 that produces a size greater than or equal to the minimum size.
     * @param imgWidth the width of the unsampled bitmap
     * @param imgHeight the height of the unsampled bitmap
     * @param minWidth the desired width
     * @param minHeight the desired height, if 0 then use minWidth and the imgWidth/imgHeight aspect ratio to calculate a corresponding minHeight
     * @return value for BitmapFactory.Options.inSampleSize
     */
    public static int calculateSampleSize(int imgWidth, int imgHeight, int minWidth, int minHeight) {
        if (minWidth <= 0) {
            return 1;
        }

        if (minHeight <= 0) {
            float r = (float) imgHeight / (float) imgWidth;
            minHeight = Math.round(r * minWidth);
        }

        // if bitmap size is already less then minimum then use 1 to not sample
        if (imgWidth < minWidth || imgHeight < minHeight) {
            return 1;
        }

        int sampleSize = 1;
        while (minWidth <= imgWidth / (2 * sampleSize) && minHeight <= imgHeight / (2 * sampleSize)) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

}
