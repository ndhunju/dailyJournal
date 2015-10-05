package com.ndhunju.dailyjournal.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by dhunju on 9/24/2015.
 */
public class UtilsZip {
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
