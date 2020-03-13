//
//  Android PDF Writer
//  http://coderesearchlabs.com/androidpdfwriter
//
//  by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package crl.android.pdfwriter;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public abstract class List extends Base {

	protected ArrayList<String> mList;

	// Variables needed to write to output stream
	private String mId;
	private PdfWriterApp mPdfWriterApp;
	private boolean mIsFileOutStreamClosed;
	private FileOutputStream mFileOutputStream;

	public List() {
		mList = new ArrayList<String>();
		mPdfWriterApp = PdfWriterApp.getInstance();
		mId = generateId();
	}

	protected String renderList() {
		StringBuilder sb = new StringBuilder();
		int x = 0;
		while (x < mList.size()) {
			sb.append(mList.get(x).toString());
			x++;
		}
		return sb.toString();
	}

	protected void renderList(FileOutputStream fileOutputStream) throws IOException {
		writeItemsTo(fileOutputStream);
	}

	public long writeItemsTo(FileOutputStream fileOutputStream) throws IOException {
		// First close output stream
		closeFileOutputStream();

		// Open input stream
		FileInputStream fileInputStream = mPdfWriterApp.openFileInput(mId);
		long size = Utils.copy(fileInputStream, fileOutputStream);
		fileInputStream.close();
		return size;
	}

	public void add(String item) {

		try {
			// If file out stream is closed, reopen it
			if (mIsFileOutStreamClosed) {
				mFileOutputStream = mPdfWriterApp.openFileOutput(mId);
				mIsFileOutStreamClosed = false;
			}
			mFileOutputStream.write(item.getBytes());
			mFileOutputStream.write("\n".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeFileOutputStream() throws IOException {
		if (!mIsFileOutStreamClosed) {
			mFileOutputStream.close();
			mIsFileOutStreamClosed = true;
		}
	}
	
	@Override
	public void clear() {
		mList.clear();
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

	// Static members
	private static long sGlobalIdCount = 0;

	private String generateId() {
		return this.getClass().getSimpleName() + sGlobalIdCount++;
	}

}
