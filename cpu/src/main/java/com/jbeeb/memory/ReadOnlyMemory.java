package com.jbeeb.memory;

import java.io.*;

public final class ReadOnlyMemory extends AbstractMemory {
    public ReadOnlyMemory(int start, int[] data) {
        super(start, data, true);
    }

    public static ReadOnlyMemory fromFile(final int codeStart, final File file) throws IOException {
        final int size = (int) file.length();
        final int[] data = new int[size];
        int pc = 0;
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            for (int i = 0; i < size; i++) {
                data[pc++] = in.read();
            }
        }

        int x = 1;
        return new ReadOnlyMemory(codeStart, data);
    }
}
