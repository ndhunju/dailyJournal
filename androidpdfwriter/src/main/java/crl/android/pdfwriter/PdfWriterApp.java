package crl.android.pdfwriter;

import android.app.Application;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PdfWriterApp {

    private static final String FILE_NAME_PREFIX = PdfWriterApp.class.getSimpleName() + "-";

    private static PdfWriterApp sInstance;
    private static Application sApplication;

    /**
     * Returns instance of {@link PdfWriterApp}. Called would need this
     * to create new {@link PDFWriter} object.
     * @param context: application context. Needed to read and write file
     */
    public static PdfWriterApp getInstance(Application context) {
        sApplication = context;
        return getInstance();
    }

    /*package*/ static PdfWriterApp getInstance() {
        if (sInstance == null) {
            sInstance = new PdfWriterApp();
        }

        return sInstance;
    }

    private PdfWriterApp() {}

    public PDFWriter newPDFWriter(int pageWidth, int pageHeight) {
        return new PDFWriter(pageWidth, pageHeight);
    }

    public FileOutputStream openFileOutput(String fileName) throws FileNotFoundException {
        return sApplication.openFileOutput(FILE_NAME_PREFIX + fileName, Context.MODE_PRIVATE);
    }

    public FileInputStream openFileInput(String fileName) throws FileNotFoundException {
        return sApplication.openFileInput(FILE_NAME_PREFIX + fileName);
    }

    public void deleteFile(String fileName) {
        sApplication.deleteFile(FILE_NAME_PREFIX + fileName);
    }
}
