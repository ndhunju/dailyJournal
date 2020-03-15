package crl.android.pdfwriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Utils {

    static int read(InputStream in, StringBuilder out) throws IOException {
        int totalByteSize = 0;
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            if (buffer.length == read) {
                out.append(new String(buffer));
            } else {
                out.append(new String(Arrays.copyOf(buffer, read)));
            }
            totalByteSize += read;
        }

        return totalByteSize;
    }

    static int copy(InputStream ios, OutputStream outputStream) throws IOException {
        int totalByteSize = 0;
        byte[] buffer = new byte[4096];
        int read;
        while ((read = ios.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
            totalByteSize += read;
        }

        return totalByteSize;
    }
}
