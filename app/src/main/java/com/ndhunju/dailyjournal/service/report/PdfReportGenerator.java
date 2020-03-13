package com.ndhunju.dailyjournal.service.report;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import androidx.annotation.Nullable;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.DailyJournalApplication;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.Utils;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import crl.android.pdfwriter.PDFWriter;
import crl.android.pdfwriter.PaperSize;
import crl.android.pdfwriter.PdfWriterApp;
import crl.android.pdfwriter.StandardFonts;

/**
 * Created by dhunju on 9/27/2015.
 * This class can be used to Generate PDF Report files.
 */
public class PdfReportGenerator extends ReportGenerator<File>{

    private static final int PAGE_HEIGHT = PaperSize.A4_HEIGHT;
    private static final int PAGE_WIDTH = PaperSize.A4_WIDTH;
    private static final int IMG_MAX_HEIGHT = PAGE_HEIGHT / 2;
    private static final int IMG_MAX_WIDTH = PAGE_WIDTH / 2;
    private static final String FILE_EXT = ".pdf";
    private static final int DEF_TEXT_SIZE = 12;
    private static final int MARGIN = 25;
    private static final int MARGIN_BETWEEN_IMGS = 15;


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
            pdfWriter = PdfWriterApp
                    .getInstance(DailyJournalApplication.getInstance())
                    .newPDFWriter(PAGE_WIDTH, PAGE_HEIGHT);
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

        @Override
        public ReportGenerator.Builder appendImage(Bitmap bitmap) {

            // Validate args
            if (bitmap == null) {
                return this;
            }

            // Get measurements
            float maxWidth = PAGE_WIDTH - (2 * MARGIN);
            float maxHeight = PAGE_HEIGHT - (2 * MARGIN);
            float widthScale = maxWidth / bitmap.getWidth();
            float heightScale = maxHeight / bitmap.getHeight();
            float scaleToUse = Math.min(widthScale, heightScale);
            // Only scale down, not scale up to prevent blurring
            scaleToUse = Math.min(scaleToUse, 1);
            int x = (int) (bitmap.getWidth() * scaleToUse);
            int y = (int) (bitmap.getHeight() * scaleToUse);

            int remainingHeight = PAGE_HEIGHT - currentYPosFromTop - MARGIN_BETWEEN_IMGS - MARGIN;
            if (remainingHeight < y) {
                // Image height is more than space left in the page height
                // So, start new page
                newPage();
            }

            pdfWriter.addImage(
                    currentXPosFromLeft,
                    PAGE_HEIGHT - currentYPosFromTop - y,
                    x,
                    y,
                    bitmap
            );

            currentYPosFromTop += y + MARGIN_BETWEEN_IMGS;
            currentXPosFromLeft = MARGIN;

            return this;
        }

        public String asString() {
            return pdfWriter.asString();
        }

        public void asString(FileOutputStream fileOutputStream) throws IOException {
            pdfWriter.asString(fileOutputStream);
            pdfWriter.release();
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
    public String getReportType() {
        return "application/pdf";
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

        if (shouldAppendAttachments()) {
            addAttachments(builder);
        }

        // write the report in a file
        try{
            // create a new unique file inside the folder if exists. otherwise in public download folder
            File pdfFile = new File(folder != null && folder.exists()
                    ? folder.getAbsolutePath() : UtilsFile.getPublicDownloadDir(),
                    getSubject() + "-" + String.valueOf(System.currentTimeMillis()).substring(8,12) + FILE_EXT);
            pdfFile.createNewFile();

            // write to file
            FileOutputStream os = new FileOutputStream(pdfFile);
            //os.write(builder.asString().getBytes("UTF-8"));
            builder.asString(os);
            os.close();

            // To let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext, new String[]{pdfFile.getAbsolutePath()}, null, null);
            return pdfFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addAttachments(ReportGenerator.Builder builder) {

        Services mServices = Services.getInstance(mContext);

        // Add "Attachment" Text
        builder.newLine();
        builder.appendText(getString(R.string.str_attachment));
        builder.writeTextLn();

        // Loop through all Journal for this party
        for (Journal journal: mServices.getJournals(getParty().getId())) {

            if (mServices.getAttachments(journal.getId()).size() <= 0) {
                // There are no attachments for this journal
                continue;
            }
            // Add Journal ID
            builder.appendText(
                    getString(R.string.str_journal)
                    + " "
                    + getString(R.string.str_id)
                    + journal.getId()
            );
            builder.writeTextLn();

            // Add each attachment image to the file
            for (Attachment attachment : mServices.getAttachments(journal.getId())) {
                Bitmap bitmap = Utils.scaleBitmap(
                        attachment.getPath(),
                        IMG_MAX_WIDTH,
                        IMG_MAX_HEIGHT
                );
                builder.appendImage(bitmap);
            }
        }

    }
}
