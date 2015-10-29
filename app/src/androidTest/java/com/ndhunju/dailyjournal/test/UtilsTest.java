package com.ndhunju.dailyjournal.test;

import com.ndhunju.dailyjournal.model.Attachment;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Services;

import java.util.Calendar;

/**
 * Created by dhunju on 10/28/2015.
 */
public class UtilsTest {


    /**
     * Helper method to fill database with arbitrary data
     */
    public static void fillDatabase(Services services){

        //Initialize instances here to reuse the objects
        Attachment newAttachment = new Attachment(0);
        Party newParty = new Party("Test Party");
        Journal newJournal = new Journal(0);

        long newPartyId;
        long newJournalId;


        for(int i = 0; i < 10; i++){
            newParty.setName("Test Party" + i);
            newPartyId = services.addParty(newParty);
            for(int j=0; j < 10; j++){
                newJournal.setPartyId(newPartyId);
                newJournal.setDate(Calendar.getInstance().getTimeInMillis() + i + j);
                //make the values as diverse as possible but predictable
                newJournal.setAmount((j + i) % 3 == 0 ? 10 + i + (j * 9) : 10 + (i * 5) - j);
                newJournal.setType( ((j + i) % 3) == 0 ? (  Journal.Type.Debit):Journal.Type.Credit);
                newJournalId = services.addJournal(newJournal);
                for(int z = 0; z < 2; z++){
                    newAttachment.setJournalId(newJournalId);
                    newAttachment.setPath(i + j + z + "");
                    services.addAttachment(newAttachment);
                }
            }
        }
    }
}
