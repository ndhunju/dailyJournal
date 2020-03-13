//
//  Android PDF Writer
//  http://coderesearchlabs.com/androidpdfwriter
//
//  by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package crl.android.pdfwriter;

import java.io.FileOutputStream;
import java.io.IOException;

public abstract class Base {
	public abstract void clear();

    /**
     * Child class must release resources and do clean up when this method is called.
     */
	public abstract void release();
	public abstract String toPDFString();

    /**
     * This method is called before {@link this#writePdfStringTo(FileOutputStream)} is called.
     * Here, object can write bytes to passed {@code fileOutputStream} if needed.
     * @param fileOutputStream: Write to this object. Consumer should not close this stream
     * @return Should return number of bytes that were written to passed {@code fileOutputStream}
     */
	public long preWritePdfStringTo(FileOutputStream fileOutputStream) throws IOException {
		return 0;
	}

    /**
     * Write PDF string to passed {@code fileOutputStream}
     * @param fileOutputStream: Write to this object. Consumer should not close this stream
     * @return Should return number of bytes that were written to passed {@code fileOutputStream}
     */
	public abstract long writePdfStringTo(FileOutputStream fileOutputStream) throws IOException;

    /**
     * This method is called after {@link this#writePdfStringTo(FileOutputStream)} is called.
     * Here, object can write bytes to passed {@code fileOutputStream} if needed.
     * @param fileOutputStream: Write to this object. Consumer should not close this stream
     * @return Should return number of bytes that were written to passed {@code fileOutputStream}
     * @throws IOException:
     */
	public long postWritePdfStringTo(FileOutputStream fileOutputStream) throws IOException {
		return 0;
	}
}
