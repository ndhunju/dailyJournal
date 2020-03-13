//
//  Android PDF Writer
//  http://coderesearchlabs.com/androidpdfwriter
//
//  by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package crl.android.pdfwriter;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EnclosedContent extends Base {

	// Static Members
	private static long sGlobalCurrentId = 0;

	private String mBegin;
	private String mEnd;

	// Variables needed to write to output stream
	private String mId;
	private PdfWriterApp mPdfWriterApp;
	private FileOutputStream mFileOutputStream;
	private boolean mIsFileOutputStreamClosed;
	
	public EnclosedContent() {
		mId = generateId();
		mPdfWriterApp = PdfWriterApp.getInstance();
		clear();
	}

	private String generateId() {
		return this.getClass().getSimpleName() + (sGlobalCurrentId++);
	}
	
	public void setBeginKeyword(String Value, boolean NewLineBefore, boolean NewLineAfter) {
		if (NewLineBefore)
			mBegin = "\n" + Value;
		else
			mBegin = Value;
		if (NewLineAfter)
			mBegin += "\n";
	}

	public void setEndKeyword(String Value, boolean NewLineBefore, boolean NewLineAfter) {		
		if (NewLineBefore)
			mEnd = "\n" + Value;
		else
			mEnd = Value;
		if (NewLineAfter)
			mEnd += "\n";
	}
	
	public boolean hasContent() {
		try {
			return mFileOutputStream.getChannel().size() > 0;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void setContent(String Value) {
		clear();
		writeToFileOutputStream(Value);
	}
	
	public String getContent() {
		try {
			// First, close Output Stream to open Input Stream
			closeFileOutputStream();

			// Second, open Input Stream to read content
			FileInputStream inputStream = mPdfWriterApp.openFileInput(mId);
			StringBuilder out = new StringBuilder();
			Utils.read(inputStream, out);
			inputStream.close();
			return out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	public void addContent(String Value) {
		writeToFileOutputStream(Value);
	}
	
	public void addNewLine() {
		writeToFileOutputStream("\n");
	}

	public void addSpace() {
		writeToFileOutputStream(" ");
	}

	public void writeToFileOutputStream(String value) {
		try {
			// If output stream was closed, reopen it
			if (mIsFileOutputStreamClosed) {
				mFileOutputStream = mPdfWriterApp.openFileOutput(mId);
				mIsFileOutputStreamClosed = false;
			}

			mFileOutputStream.write(value.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeFileOutputStream() throws IOException {
		if (!mIsFileOutputStreamClosed) {
			mFileOutputStream.close();
			mIsFileOutputStreamClosed = true;
		}
	}
	
	@Override
	public void clear() {
		try {
			mPdfWriterApp.deleteFile(mId);
			mFileOutputStream = mPdfWriterApp.openFileOutput(mId);
			mIsFileOutputStreamClosed = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toPDFString() {
		String all = mBegin + getContent() + mEnd;
		try {
			closeFileOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return all;
	}

	public long preWritePdfStringTo(FileOutputStream fileOutputStream) throws  IOException {
		byte[] beginBytes = mBegin.getBytes();
		fileOutputStream.write(beginBytes);
		return beginBytes.length;
	}

	@Override
	public long writePdfStringTo(FileOutputStream fileOutputStream) throws IOException {
		closeFileOutputStream();
		FileInputStream inputStream = mPdfWriterApp.openFileInput(mId);
		long size = Utils.copy(inputStream, fileOutputStream);
		inputStream.close();
		return size;
	}

	public long postWritePdfStringTo(FileOutputStream fileOutputStream) throws  IOException {
		byte[] endBytes = mEnd.getBytes();
		fileOutputStream.write(endBytes);
		return endBytes.length;
	}

	@Override
	public void release() {
		try {
			closeFileOutputStream();
			mPdfWriterApp.deleteFile(mId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
