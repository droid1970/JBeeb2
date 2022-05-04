package com.jbeeb.memory;

import java.io.*;
import java.util.Arrays;

public class ReadOnlyMemory extends AbstractMemory {
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

    public static ReadOnlyMemory fromResource(final int codeStart, final String resourceName) throws IOException {
        final int[] data = new int[65536];
        int size = 0;
        try (InputStream in = ReadOnlyMemory.class.getResourceAsStream(resourceName)) {
            int b;
            while ((b = in.read()) >= 0) {
                data[size++] = b;
            }
        }
        return new ReadOnlyMemory(codeStart, Arrays.copyOf(data, size));
    }
}
