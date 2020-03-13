//
//  Android PDF Writer
//  http://coderesearchlabs.com/androidpdfwriter
//
//  by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package crl.android.pdfwriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class IndirectObject extends Base {

	private EnclosedContent mContent;
	private Dictionary mDictionaryContent;
	private Stream mStreamContent;
	private IndirectIdentifier mID;
	private int mByteOffset;
	private boolean mInUse;

	public IndirectObject() {
		clear();
	}
	
	public void setNumberID(int Value) {
		mID.setNumber(Value);
	}

	public int getNumberID() {
		return mID.getNumber();
	}

	public void setGeneration(int Value) {
		mID.setGeneration(Value);
	}

	public int getGeneration() {
		return mID.getGeneration();
	}
	
	public String getIndirectReference() {
		return mID.toPDFString() + " R";
	}

	public void setByteOffset(int Value) {
		mByteOffset = Value;
	}
	
	public int getByteOffset() {
		return mByteOffset;
	}

	public void setInUse(boolean Value) {
		mInUse = Value;
	}
	
	public boolean getInUse() {
		return mInUse;
	}
	
	public void addContent(String Value) {
		mContent.addContent(Value);		
	}

	public void setContent(String Value) {
		mContent.setContent(Value);		
	}

	public String getContent() {
		return mContent.getContent();
	}
	
	public void addDictionaryContent(String Value) {
		mDictionaryContent.addContent(Value);		
	}

	public void setDictionaryContent(String Value) {
		mDictionaryContent.setContent(Value);		
	}

	public String getDictionaryContent() {
		return mDictionaryContent.getContent();		
	}
	
	public void addStreamContent(String Value) {
		mStreamContent.addContent(Value);		
	}

	public void setStreamContent(String Value) {
		mStreamContent.setContent(Value);		
	}

	public String getStreamContent() {
		return mStreamContent.getContent();
	}
	
	protected String render() {
		StringBuilder sb = new StringBuilder();
		sb.append(mID.toPDFString());
		sb.append(" ");
		// j-a-s-d: this can be performed in inherited classes DictionaryObject and StreamObject
		if (mDictionaryContent.hasContent()) {
			mContent.setContent(mDictionaryContent.toPDFString());
			if (mStreamContent.hasContent())
				mContent.addContent(mStreamContent.toPDFString());
		}
		sb.append(mContent.toPDFString());
		return sb.toString();
	}

	@Override
	public void clear() {
		mID = new IndirectIdentifier();
		mByteOffset = 0;
		mInUse = false;
		mContent = new EnclosedContent();
		mContent.setBeginKeyword("obj", false, true);
		mContent.setEndKeyword("endobj", false, true);
		mDictionaryContent = new Dictionary();
		mStreamContent = new Stream();
	}

	@Override
	public String toPDFString() {
		return render();
	}

	/**
	 * This method uses the same logic as in {@link this#render()} but puts the strings in
	 * {@code fileOutputStream} in the right order
	 * @param fileOutputStream: Write to this object. Consumer should not close this stream
	 * @return Total number of bytes written
	 */
	@Override
	public long writePdfStringTo(FileOutputStream fileOutputStream) throws IOException {
		long totalByteSize = 0;
		byte[] idBytes = mID.toPDFString().getBytes();
		fileOutputStream.write(idBytes);
		totalByteSize += idBytes.length;

		byte[] spaceBytes = " ".getBytes();
		fileOutputStream.write(spaceBytes);
		totalByteSize += spaceBytes.length;

		totalByteSize += mContent.preWritePdfStringTo(fileOutputStream);

		// j-a-s-d: this can be performed in inherited classes DictionaryObject and StreamObject
		if (mDictionaryContent.hasContent()) {
			totalByteSize += mDictionaryContent.preWritePdfStringTo(fileOutputStream);
			totalByteSize += mDictionaryContent.writePdfStringTo(fileOutputStream);
			totalByteSize += mDictionaryContent.postWritePdfStringTo(fileOutputStream);
			if (mStreamContent.hasContent()) {
				totalByteSize += mStreamContent.preWritePdfStringTo(fileOutputStream);
				totalByteSize += mStreamContent.writePdfStringTo(fileOutputStream);
				totalByteSize += mStreamContent.postWritePdfStringTo(fileOutputStream);
			}
		}

		totalByteSize += mContent.writePdfStringTo(fileOutputStream);

		totalByteSize += mContent.postWritePdfStringTo(fileOutputStream);

		return totalByteSize;
	}

    @Override
    public void release() {
        mContent.release();
        mDictionaryContent.release();
        mStreamContent.release();
    }
}
