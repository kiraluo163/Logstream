package com.test.log.logserver.utils;

import java.io.*;

public class ReversedLineInputStream extends InputStream {
    private RandomAccessFile in;

    private long currentLineStart = -1;
    private long currentLineEnd = -1;
    private long currentPos = -1;

    public ReversedLineInputStream(File file) throws FileNotFoundException {
        in = new RandomAccessFile(file, "r");
        currentLineStart = file.length();
        currentLineEnd = file.length();
        currentPos = currentLineEnd;
    }

    private void nextLine() throws IOException {

        currentLineEnd = currentLineStart;

        if (currentLineEnd == 0 || currentLineStart < 2) {
            currentLineEnd = -1;
            currentLineStart = -1;
            currentPos = -1;
            return;
        }

        long filePointer = currentLineStart -2;
        in.seek(filePointer);
        while (filePointer >= 0 && in.readByte() != 0xA) {
            if(--filePointer < 0 ) break;
            in.seek(filePointer);
        }
        currentLineStart = filePointer + 1;
        currentPos = currentLineStart;
    }

    public int read() throws IOException {

        if (currentPos < currentLineEnd ) {
            in.seek(currentPos++);
            int readByte = in.readByte();
            return readByte;

        }
        else if (currentPos < 0) {
            return -1;
        }
        else {
            nextLine();
            return read();
        }
    }
}
