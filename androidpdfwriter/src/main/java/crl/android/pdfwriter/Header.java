//
//  Android PDF Writer
//  http://coderesearchlabs.com/androidpdfwriter
//
//  by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package crl.android.pdfwriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class Header extends Base {

	private String mVersion;
	private String mRenderedHeader;
	
	public Header() {
		clear();
	}
	
	public void setVersion(int Major, int Minor) {
		mVersion = Integer.toString(Major) + "." + Integer.toString(Minor);
		render();
	}
	
	public int getPDFStringSize() {
		return mRenderedHeader.length();
	}
	
	private void render() {
		mRenderedHeader = "%PDF-" + mVersion + "\n%ŠťŞľ\n";
	}
	
	@Override
	public String toPDFString() {
		return mRenderedHeader;
	}

	@Override
	public long writePdfStringTo(FileOutputStream fileOutputStream) throws IOException {
		byte[] bytes = toPDFString().getBytes();
		fileOutputStream.write(bytes);
		return bytes.length;
	}

	@Override
	public void clear() {
		setVersion(1, 4);
	}

	@Override
	public void release() {}
}
