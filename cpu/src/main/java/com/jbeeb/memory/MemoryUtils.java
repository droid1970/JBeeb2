package com.jbeeb.memory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MemoryUtils {

    public static void loadS19(final Memory memory, final InputStream in, final int offset) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = r.readLine()) != null) {
                processS19Line(memory, line, offset);
            }
        }
    }

    private static void processS19Line(final Memory memory, final String line, final int offset) {
        if ("S109FFFA9D37A337AB376D".equals(line)) {
            int x = 1;
        }
        final int code = Integer.parseInt("" + line.charAt(1), 16);
        final int count = Integer.parseInt(line.substring(2, 4), 16) - 3;
        switch (code) {
            case 1: {
                final int addr = Integer.parseInt(line.substring(4, 8), 16) + offset;
                for (int i = 0; i < count; i++) {
                    final int pos = 8 + i * 2;
                    try {
                        final int v = Integer.parseInt(line.substring(pos, pos + 2), 16);
                        memory.writeByte(addr + i, v);
                    } catch (Exception ex) {
                        int x = 1;
                    }
                }
                break;
            }
            default:
                // Ignored

        }
    }
}
