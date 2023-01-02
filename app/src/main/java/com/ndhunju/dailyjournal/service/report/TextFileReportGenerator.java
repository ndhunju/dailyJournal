package com.ndhunju.dailyjournal.service.report;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.text.TextUtils;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by ndhunju on 12/27/16.
 * This class generates a report in text file
 */

public class TextFileReportGenerator extends ReportGenerator<File> {

    protected static final String FILE_EXT = ".txt";

    public TextFileReportGenerator(Context context, long partyId) {
        super(context, partyId);
    }

    public TextFileReportGenerator(Context context, Party party) {
        super(context, party);
    }

    @Override
    public String getReportType() {
        return "text/plain";
    }

    @Override
    public File getReport(File folder) {
        super.getReport(folder);

        // build the report as needed
        Builder builder = new Builder();
        makeAppBanner(builder);
        makePartySummary(builder);
        builder.appendText(getString(R.string.msg_text_format_issue))
                .writeTextLn()
                .newLine();
        makeReport(builder);
        makeCustomNoteSection(builder);

        // write the report in a file
        try {
            // create a new unique file inside the folder if exists. otherwise in public download folder
            File textFile = new File(folder != null && folder.exists()
                    ? folder.getAbsolutePath() : UtilsFile.getPublicDownloadDir(),
                    getSubject() + "-" + String.valueOf(System.currentTimeMillis()).substring(8,12) + FILE_EXT);
            textFile.createNewFile();

            // write to file
            OutputStream os = new FileOutputStream(textFile);
            os.write(builder.asString().getBytes(StandardCharsets.UTF_8));
            os.close();

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mContext, new String[]{textFile.getAbsolutePath()}, null, null);
            return textFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addAttachments(ReportGenerator.Builder builder) {
        // Not supported for now
    }

    @Override
    public void onAppendNote(ReportGenerator.Builder builder, Journal journal) {
        //super.onAppendNote(builder, journal);
        // This file will make custom note section.
    }

    private void makeCustomNoteSection(Builder builder) {
        // After report is made/rendered, add notes in separate section
        int noteLineWidth = builder.highestLineWidth - mIdColumnSize;
        builder.writeTextLn();
        builder.appendText(getString(R.string.str_notes));
        builder.writeTextLn();
        builder.writeTextLn();
        builder.appendText(addGap(getString(R.string.str_id_only), mIdColumnSize));
        builder.appendText(
                "",
                getString(R.string.str_note),
                addGap("", 0),
                noteLineWidth
        );

        for (Journal journal: mJournals) {
            if (TextUtils.isEmpty(journal.getNote())) {
                continue;
            }
            builder.appendText(addGap("" + journal.getId(), mIdColumnSize));
            builder.appendText(
                    addGap("", mIdColumnSize),
                    journal.getNote(),
                    addGap("", 0),
                    noteLineWidth
            );
        }
    }

    static class Builder extends ReportGenerator.Builder {

        StringBuilder localSb = new StringBuilder();

        /**
         * This method adds passed {@code text} without exceeding the {@code maxWidth} value for
         * the line by breaking the overflow text to next line.
         * @param preText: pretext to add from second line if overflows
         * @param text: main text to add
         * @param postText: post text to append at the end
         * @param maxWidth: maximum width of the line
         */
        public Builder appendText(String preText, String text, String postText, int maxWidth) {
            // Add main text
            localSb.append(text);

            // If total string size is greater than maxWidth
            // break the String and start in new line
            if (localSb.length() > maxWidth) {
                // completePostText might have hyphen also
                String completePostText = postText;

                // If we are breaking a "word", add hyphen
                if (!Character.isWhitespace(localSb.charAt(maxWidth - 1 - postText.length()))
                        && !Character.isWhitespace(localSb.charAt(maxWidth - postText.length()))) {
                    completePostText = "-" + postText;
                }

                String overflowText = localSb.substring(maxWidth - completePostText.length());
                localSb.setLength(localSb.length() - overflowText.length());
                localSb.trimToSize();

                // Add completePostText now, main text is already added
                localSb.append(completePostText);
                writeTextLn();
                // Add preText for next line now.
                sb.append(preText);
                // Main text will be added by recursively calling this function
                appendText(preText, overflowText, postText, maxWidth);
                return this;
            } else {
                // Add spaces till the line reach to maxWidth
                int add = 0;
                if (maxWidth > localSb.length()) {
                    add = maxWidth - localSb.length() - postText.length();
                }

                for(int i = add; i > 0; i--) {
                    localSb.append(" ");
                }

                localSb.append(postText);
            }

            writeTextLn();
            return this;
        }

        @Override
        public ReportGenerator.Builder writeTextLn() {
            appendText(localSb.toString());
            localSb.setLength(0);
            localSb.trimToSize();
            return super.writeTextLn();
        }
    }
}
