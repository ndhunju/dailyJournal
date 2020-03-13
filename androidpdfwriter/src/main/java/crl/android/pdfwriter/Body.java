//
//  Android PDF Writer
//  http://coderesearchlabs.com/androidpdfwriter
//
//  by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package crl.android.pdfwriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Body extends List {

	private int mByteOffsetStart;
	private int mObjectNumberStart;
	private int mGeneratedObjectsCount;
	private ArrayList<IndirectObject> mObjectsList;
	
	public Body() {
		super();
		clear();
	}
	
	public int getObjectNumberStart() {
		return mObjectNumberStart;
	}
	
	public void setObjectNumberStart(int Value) {
		mObjectNumberStart = Value;
	}
	
	public int getByteOffsetStart() {
		return mByteOffsetStart;
	}
	
	public void setByteOffsetStart(int Value) {
		mByteOffsetStart = Value;
	}
	
	public int getObjectsCount() {
		return mObjectsList.size();
	}
	
	private int getNextAvailableObjectNumber() {
		return ++mGeneratedObjectsCount + mObjectNumberStart;
	}
	
	public IndirectObject getNewIndirectObject() {
		return getNewIndirectObject(getNextAvailableObjectNumber(), 0, true);
	}
	
	public IndirectObject getNewIndirectObject(int Number, int Generation, boolean InUse) {
		IndirectObject iobj = new IndirectObject();
		iobj.setNumberID(Number);
		iobj.setGeneration(Generation);
		iobj.setInUse(InUse);
		return iobj;
	}
	
	public IndirectObject getObjectByNumberID(int Number) {
		IndirectObject iobj;
		int x = 0;
		while (x < mObjectsList.size()) {
			iobj = mObjectsList.get(x);
			if (iobj.getNumberID() == Number)
				return iobj;
			x++;
		}
		return null;
	}
	
	public void includeIndirectObject(IndirectObject iobj) {
		mObjectsList.add(iobj);
	}
	
	private String render() {
		int x = 0;
		int offset = mByteOffsetStart;
		while (x < mObjectsList.size()) {
			IndirectObject iobj = getObjectByNumberID(++x);
			String s = "";
			if (iobj != null)
				s = iobj.toPDFString() + "\n";
			mList.add(s);
			iobj.setByteOffset(offset);
			offset += s.length();
		}
		return renderList();
	}
	
	@Override
	public String toPDFString() {
		return render();
	}

	@Override
	public long writePdfStringTo(FileOutputStream fileOutputStream) throws IOException {
		int x = 0;
		int offset = mByteOffsetStart;
		int totalSize = 0;
		while (x < mObjectsList.size()) {
			IndirectObject iobj = getObjectByNumberID(++x);

			if (iobj != null) {
				long iobjSize = iobj.writePdfStringTo(fileOutputStream);
				byte[] newLineBytes = "\n".getBytes();
				fileOutputStream.write(newLineBytes);
				//super.add();
				iobj.setByteOffset(offset);
				totalSize += iobjSize + newLineBytes.length;
				offset += iobjSize + newLineBytes.length;
			}
		}

		//renderList(fileOutputStream);
		return totalSize;
	}

	@Override
	public void clear() {
		super.clear();
		mByteOffsetStart = 0;
		mObjectNumberStart = 0;
		mGeneratedObjectsCount = 0;
		mObjectsList = new ArrayList<IndirectObject>();
	}

    @Override
    public void release() {
        for (IndirectObject object: mObjectsList) {
            object.release();
        }
    }
}
