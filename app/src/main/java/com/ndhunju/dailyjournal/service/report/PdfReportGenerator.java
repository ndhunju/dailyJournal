package com.ndhunju.dailyjournal.service.report;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.text.TextUtils;

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
    /* Total number of Characters that can fit in A4 Size paper.*/
    private static final int CHAR_COUNT_PER_LINE = 75;


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

            // If total string size is greater than CHAR_COUNT_PER_LINE
            // break the String and start in new line
            if (sb.length() > CHAR_COUNT_PER_LINE + 1) {
                String overflownText = sb.substring(CHAR_COUNT_PER_LINE);
                sb.setLength(CHAR_COUNT_PER_LINE);
                sb.trimToSize();

                // If we are breaking a "word", add hyphen
                if (!Character.isWhitespace(sb.charAt(sb.length() - 1))
                        && !Character.isWhitespace(overflownText.charAt(0))) {
                    sb.append("-");
                }
                writeTextLn();
                // Call method recursively
                appendText(overflownText);
                return this;
            }

            return this;
        }

        public Builder appendText(String preText, String text, String posText, int maxWidth) {
            // Add main text
            sb.append(text);

            // If total string size is greater than maxWidth
            // break the String and start in new line
            if (sb.length() > maxWidth) {
                // completePostText might have hyphen also
                String completePostText = posText;

                // If we are breaking a "word", add hyphen
                if (!Character.isWhitespace(sb.charAt(maxWidth - 1 - posText.length()))
                        && !Character.isWhitespace(sb.charAt(maxWidth - posText.length()))) {
                    completePostText = "-" + posText;
                }

                String overflowText = sb.substring(maxWidth - completePostText.length());
                sb.setLength(sb.length() - overflowText.length());
                sb.trimToSize();

                // Add completePostText now, main text is already added
                sb.append(completePostText);
                writeTextLn();
                // Add preText. Main text will be added then
                sb.append(preText);
                // Call method recursively
                appendText(preText, overflowText, posText, maxWidth);
                return this;
            } else {
                // Add spaces till the line reach to maxWidth
                int add = 0;
                if (maxWidth > sb.length()) {
                    add = maxWidth - sb.length() - posText.length();
                }

                for(int i = add; i > 0; i--) {
                    sb.append(" ");
                }

                sb.append(posText);
            }

            writeTextLn();
            return this;
        }

        public Builder writeTextLn() {
            pdfWriter.addText(
                    currentXPosFromLeft,
                    PAGE_HEIGHT - currentYPosFromTop,
                    textSize,
                    sb.toString()
            );

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
        public ReportGenerator.Builder appendImage(Bitmap bitmap, int topMargin) {

            // Validate args
            if (bitmap == null) {
                return this;
            }

            // Get measurements
            float maxAvailableWidth = PAGE_WIDTH - (2 * MARGIN);
            float maxAvailableHeight = PAGE_HEIGHT - (2 * MARGIN);
            float widthScale = maxAvailableWidth / bitmap.getWidth();
            float heightScale = maxAvailableHeight / bitmap.getHeight();
            float scaleToUse = Math.min(widthScale, heightScale);
            // Only scale down, not scale up to prevent blurring
            scaleToUse = Math.min(scaleToUse, 1);
            int finalWidth = (int) (bitmap.getWidth() * scaleToUse);
            int finalHeight = (int) (bitmap.getHeight() * scaleToUse);

            int remainingHeight = PAGE_HEIGHT
                    - currentYPosFromTop
                    - topMargin
                    - MARGIN;

            if (remainingHeight < 0) {
                newPage();
                // topMargin = 0 since starting a new page
                appendImage(bitmap, 0);
                return this;
            } else if (remainingHeight < finalHeight) {
                // Image's height is greater than the space remaining.
                // Break image into two or more to completely use the space left in the page
                Bitmap upperPortionOfBitmapThatFits = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.getWidth(),
                        remainingHeight
                );

                Bitmap remainingBitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        upperPortionOfBitmapThatFits.getHeight(),
                        bitmap.getWidth(),
                        bitmap.getHeight() - upperPortionOfBitmapThatFits.getHeight()
                );

                // Recursively call this function. It should return immediately since we
                // are passing bitmap that should fit perfectly in the remaining space
                appendImage(upperPortionOfBitmapThatFits, topMargin);
                newPage();
                // Recursively call this function.
                appendImage(remainingBitmap, 0);
                return this;
            }

            int fromLeft = currentXPosFromLeft;
            int yPosFromBottom = PAGE_HEIGHT - currentYPosFromTop;
            // In PDF, the origin is at bottom left. fromBottom = y coordinate from origin.
            // y starts from 0 at origin and increases as it goes closer to the top
            int fromBottom = yPosFromBottom - topMargin - finalHeight;

            pdfWriter.addImage(fromLeft, fromBottom, finalWidth, finalHeight, bitmap);

            currentYPosFromTop = PAGE_HEIGHT - fromBottom;
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

        // After report is made/rendered,
        // add notes in separate section
        int idColWidth = 4;
        // Hard coding this value to make it same at report/ledger width
        int noteLineWidth = CHAR_COUNT_PER_LINE - 2;
        builder.writeTextLn();
        builder.appendText(getString(R.string.str_notes));
        builder.writeTextLn();
        builder.writeTextLn();
        builder.appendText(addGap(getString(R.string.str_id_only), idColWidth));
        builder.appendText("", getString(R.string.str_note), addGap("", 0), noteLineWidth);

        for (Journal journal: mJournals) {
            if (TextUtils.isEmpty(journal.getNote())) {
                continue;
            }

            builder.appendText(addGap("" + journal.getId(), idColWidth));
            builder.appendText(addGap("", idColWidth), journal.getNote(), addGap("", 0), noteLineWidth);
        }

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
    public void onAppendNote(ReportGenerator.Builder builder, Journal journal) {
        //super.onAppendNote(builder, journal);
        // Don't use super class implementation to add notes
    }

    @Override
    public boolean shouldAppendJournalId() {
        return true;
    }

    @Override
    public void addAttachments(ReportGenerator.Builder builder0) {

        PdfReportGenerator.Builder builder = (Builder) builder0;
        Services mServices = Services.getInstance(mContext);

        // Add "Attachment" Text
        builder.writeTextLn();
        builder.writeTextLn();
        builder.appendText(getString(R.string.str_attachments));
        builder.writeTextLn();

        // Loop through all Journal for this party
        for (Journal journal: mServices.getJournals(getParty().getId())) {

            if (mServices.getAttachments(journal.getId()).size() <= 0) {
                // There are no attachments for this journal
                continue;
            }
            builder.writeTextLn();
            // Add Journal ID
            builder.appendText(
                    getString(R.string.str_journal)
                    + " "
                    + getString(R.string.str_id)
                    + journal.getId()
            );
            builder.writeTextLn();

            // Add each attachment image to the file
            List<Attachment> attachments = mServices.getAttachments(journal.getId());
            for (int i = 0; i < attachments.size(); i++) {
                Bitmap bitmap = Utils.scaleBitmap(
                        attachments.get(i).getPath(),
                        IMG_MAX_WIDTH,
                        IMG_MAX_HEIGHT
                );
                // Add topMargin starting from second image only
                builder.appendImage(bitmap, i == 0 ? 0 : MARGIN_BETWEEN_IMGS);
            }

            builder.writeTextLn();
        }

    }
}
