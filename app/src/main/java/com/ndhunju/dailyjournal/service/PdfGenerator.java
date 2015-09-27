package com.ndhunju.dailyjournal.service;

import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by dhunju on 9/27/2015.
 */
public class PdfGenerator {



    public PdfDocument generatePd(View content, File outputFile){
        // create a new document
        PdfDocument document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(100,100, 1).create();

        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);

        // draw something on the page
        content.draw(page.getCanvas());

        // finish the page
        document.finishPage(page);

        // add more pages
        // write the document content
        try{
            OutputStream os = new FileOutputStream(outputFile);
            document.writeTo(os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // close the document
        document.close();

        return document;
    }


}
