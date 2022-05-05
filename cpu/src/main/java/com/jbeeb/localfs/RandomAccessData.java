package com.jbeeb.localfs;

import java.io.IOException;

public interface RandomAccessData {
    byte get() throws IOException;
    void seek(int position) throws IOException;
    int length();
    boolean isEOF();
}
