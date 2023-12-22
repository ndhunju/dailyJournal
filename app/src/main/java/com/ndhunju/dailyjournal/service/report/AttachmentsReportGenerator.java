package com.ndhunju.dailyjournal.service.report;

import android.content.Context;
import android.media.MediaScannerConnection;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import androidx.annotation.Nullable;

public class AttachmentsReportGenerator extends ReportGenerator<File> {

    public AttachmentsReportGenerator(Context context, long partyId) {
        super(context, partyId);
    }

    public AttachmentsReportGenerator(Context context, Party party) {
        super(context, party);
    }

    public AttachmentsReportGenerator(Context context, Party party, List<Journal> journals) {
        super(context, party, journals);
    }

    @Override
    public File getReport(@Nullable File folder) {
        // super.getReport(folder);

        // copy images
        try {
            // Create a new unique file inside the folder if exists.
            // Otherwise in public download folder
            String selectedFolderPath = (folder != null && folder.exists())
                    ? folder.getAbsolutePath()
                    : UtilsFile.getPublicDownloadDir();

            Services services = Services.getInstance(mContext);
            Party party = mParty;

            for (Journal journal: services.getJournals(party.getId())) {
                List<Attachment> attachments = services.getAttachments(journal.getId());

                if (attachments.size() < 1) {
                    // No attachments.
                    // Skip this party or journal
                    continue;
                }

                // Create a folder for each party
                File partyFolder = new File(selectedFolderPath, party.getName());
                if (!partyFolder.exists() && !partyFolder.mkdir()) {
                    return null;
                }

                int attachmentCount = 1;
                for (Attachment attachment: attachments) {
                    // Copy attachments to respective party folder
                    String attachmentName = getString(R.string.str_journal)
                            + getString(R.string.str_id_only) + "-"
                            + journal.getId() + "-"
                            + attachmentCount++
                            + UtilsFile.IMG_EXT;

                    File attachmentFile = new File(partyFolder, attachmentName);
                    if (!attachmentFile.exists()) {
                        // File doesn't exit, create one
                        if (!attachmentFile.createNewFile()) {
                            // Create file failed
                            return null;
                        }
                    }

                    try {
                        // Catch FileNotFoundException: app_DailyJournal/.attachments/e011516f-b99e-4b51-a65a-6aed8a111c60.png:
                        // So that app continues with next attachment
                        FileInputStream picFileIS = new FileInputStream(attachment.getPath());
                        FileOutputStream toExportImageOS = new FileOutputStream(attachmentFile);
                        UtilsZip.copy(picFileIS, toExportImageOS);
                        picFileIS.close();
                        toExportImageOS.close();
                    } catch (FileNotFoundException ex) {
                        // That attachment file doesn't exist, delete it from database too.
                        services.deleteAttachment(attachment);
                        AnalyticsService.INSTANCE.logEvent("didNotFindFileAttachment");
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                // to let know that a new file has been created so that it appears in the computer
                MediaScannerConnection.scanFile(mContext,
                        new String[]{partyFolder.getAbsolutePath()},
                        null,
                        null
                );
            }

            return folder;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getReportType() {
        return "resource/folder";
    }

    @Override
    public void addAttachments(Builder builder) {}
}
