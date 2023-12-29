package com.ndhunju.dailyjournal.model

import java.io.Serializable
import java.io.StringReader
import java.io.StringWriter
import java.util.Properties

/**
 * Created by dhunju on 9/18/2015.
 * Class for representing Attachments of a Journal
 * Attachments are generally picture so far
 */
class Attachment(var journalId: Long) : Serializable {
    var id: Long = 0
    var path: String? = null
    var linkedAttachmentId: Long? = null

    var attachmentGuid: String? = null
        private set
        get() {
            val path = path ?: return null
            // path -> /data/user/0/com.ndhunju.dailyjournalplus/
            // app_DailyJournal/.attachments/b9f89c9c-91eb-49e0-b27d-d08ef0d314ac.png
            return path.substring(path.indexOf(".attachments/") + 13)
        }

    /**
     * This property should be used only by Database for storing and retrieving extra props
     */
    fun parseValueFromExtraColumn(value: String?) {

        if (value == null) {
            return
        }

        // When the extra column from DB is passed here, parse it and set
        // the value it stores to respective properties
        val prop = Properties()
        prop.load(StringReader(value))
        linkedAttachmentId = (prop[EXTRA_LINKED_ATTACHMENT_ID] as? String)?.toLongOrNull()
    }

    fun makeValueForExtraColumn(): String {
        // Construct the value to be stored to extra column in DB for Attachment
        val prop = Properties()
        linkedAttachmentId?.let { prop.setProperty(EXTRA_LINKED_ATTACHMENT_ID, it.toString()) }
        val stringWriter = StringWriter(0)
        prop.store(stringWriter, null)
        stringWriter.close()
        return stringWriter.toString()
    }

    companion object {
        const val EXTRA_LINKED_ATTACHMENT_ID = "linkedAttachmentId";
    }

}