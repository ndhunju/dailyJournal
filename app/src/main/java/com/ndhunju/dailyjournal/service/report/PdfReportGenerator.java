package com.ndhunju.dailyjournal.service.report;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.support.annotation.Nullable;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.PaperSize;
import crl.android.pdfwriter.StandardFonts;

/**
 * Created by dhunju on 9/27/2015.
 * This class can be used to Generate PDF Report files.
 */
public class PdfReportGenerator extends ReportGenerator<File>{

    protected static final int PAGE_HEIGHT = PaperSize.A4_HEIGHT;
    protected static final int PAGE_WIDTH = PaperSize.A4_WIDTH;
    protected static final String FILE_EXT = ".pdf";
    protected static final int DEF_TEXT_SIZE = 12;
    protected static final int MARGIN = 25;


    public PdfReportGenerator(Context context, long partyId) {
        super(context, partyId);
    }

    public PdfReportGenerator(Context context, Party party) {
        super(context, party);
    }

    public PdfReportGenerator(Context context, Party party, List<Journal> journals) {
        super(context, party, journals);
    }


    public static class Builder extends ReportGenerator.Builder{

        // variables
        int textSize;
        StringBuilder sb;
        PDFWriter pdfWriter;
        int currentXPosFromLeft, currentYPosFromTop;

        public Builder() {
            textSize = DEF_TEXT_SIZE;
            sb = new StringBuilder();
            currentYPosFromTop = MARGIN;
            currentXPosFromLeft = MARGIN;
            pdfWriter = new PDFWriter(PAGE_WIDTH, PAGE_HEIGHT);
            pdfWriter.setFont(StandardFonts.COURIER, StandardFonts.COURIER);
        }

        public Builder newPage() {
            pdfWriter.newPage();
            // reset y position
            currentYPosFromTop = MARGIN;
            // user monospaced font style
            pdfWriter.setFont(StandardFonts.COURIER, StandardFonts.COURIER);
            return this;
        }

        public Builder setTextSize(int size) {
            textSize = size;
            return this;
        }

        public Builder newLine() {
            currentYPosFromTop += textSize;
            currentXPosFromLeft = MARGIN;

            if (currentYPosFromTop + MARGIN > PAGE_HEIGHT) {
                // y position has extended the page height, start new page
                newPage();
            }
            return this;
        }

        public Builder appendText(String string) {
            sb.append(string);
            return this;
        }

        public Builder writeTextLn() {
            pdfWriter.addText(currentXPosFromLeft, PAGE_HEIGHT - currentYPosFromTop, textSize, sb.toString());
            clearString();
            newLine();
            return this;
        }

        private void clearString() {
            sb.setLength(0);
            sb.trimToSize();
        }

        public Builder addImage(int x, int y, Bitmap bitmap, String afterText) {
            pdfWriter.addImage(currentXPosFromLeft, PAGE_HEIGHT - currentYPosFromTop, x, y, bitmap);
            currentXPosFromLeft += x;
            pdfWriter.addText(currentXPosFromLeft, PAGE_HEIGHT - currentYPosFromTop, DEF_TEXT_SIZE, afterText);
            currentYPosFromTop += y;
            return this;
        }

        public String asString() {
            return pdfWriter.asString();
        }

        public byte[] asBytes() {
            return pdfWriter.asString().getBytes();
        }
    }

    public void makePdfLibReference(Builder builder) {
        builder.appendText(getString(R.string.msg_pdf_lib_ref)).writeTextLn();
        builder.appendText(getString(R.string.msg_pdf_courtesy)).writeTextLn().newLine();
    }

    @Override
    public File getReport(@Nullable File folder) {
        super.getReport(folder);

        // build the report as needed
        Builder builder = new Builder();
        builder.setTextSize(DEF_TEXT_SIZE + 2);
        makeAppBanner(builder);

        builder.setTextSize(DEF_TEXT_SIZE + 1);
        makePdfLibReference(builder);

        builder.setTextSize(DEF_TEXT_SIZE);
        makePartySummary(builder);
        makeReport(builder);

        // write the report in a file
        try{
            // create a new unique file inside the folder if exists. otherwise in public download folder
            File pdfFile = new File(folder != null && folder.exists()
                    ? folder.getAbsolutePath() : UtilsFile.getPublicDownloadDir(),
                    getSubject() + "-" + String.valueOf(System.currentTimeMillis()).substring(8,12) + FILE_EXT);
            pdfFile.createNewFile();

            // write to file
            OutputStream os = new FileOutputStream(pdfFile);
            os.write(builder.asString().getBytes("UTF-8"));
            os.close();

            // notify user
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(mContext.getString(R.string.msg_report_created_title, getString(R.string.app_name)),
                    mContext.getString(R.string.msg_report_created_desc, mParty.getName()), true, "application/pdf", pdfFile.getAbsolutePath(), pdfFile.length(), true);

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext, new String[]{pdfFile.getAbsolutePath()}, null, null);
            return pdfFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
