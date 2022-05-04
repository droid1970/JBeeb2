package com.jbeeb.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestRom  extends ReadOnlyMemory {

    public TestRom(final String name, final String copyright) {
        super(0x8000, createData(name, copyright));
    }

    private static int[] createData(final String name, final String copyright) {
        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // Language entry
        buf.put((byte) 0x4c);
        buf.put((byte) 0);
        buf.put((byte) 0);

        // Service entry
        buf.put((byte) 0x4c);
        buf.put((byte) 0x00);
        buf.put((byte) 0x90);

        buf.put((byte) 0x81); // rom type
        buf.put((byte) (9 + name.length())); // copyright offset
        buf.put((byte) 1);

        buf.put(stringToBytes(name));
        buf.put((byte) 0);

        buf.put(stringToBytes(copyright));
        buf.put((byte) 0);

        return toIntArray(buf.array());
    }

    private static int[] toIntArray(final byte[] bytes) {
        final int[] ret = new int[0x4000];
        for (int i = 0; i < 0x4000; i++) {
            ret[i] = (i < bytes.length) ? ((int) bytes[i]) & 0xFF : 0;
        }
        return ret;
    }

    private static byte[] stringToBytes(final String s) {
        final byte[] bytes = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= 32 && c <= 127) {
                bytes[i] = (byte) c;
            } else {
                bytes[i] = (byte) 32;
            }
        }
        return bytes;
    }
}
