package com.feng.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
    public static ByteArrayOutputStream getByteArrayOutputStream(InputStream in) throws IOException {
        byte[] bcache = new byte[1024];
        int readSize = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((readSize = in.read(bcache)) != -1) {
            outputStream.write(bcache, 0, readSize);
        }
        return outputStream;
    }
}
