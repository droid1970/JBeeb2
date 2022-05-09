package com.jbeeb.localfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class ByteRandomAccessData implements RandomAccessData {

    private final byte[] data;
    private final ByteBuffer buffer;

    public ByteRandomAccessData(final byte[] data) {
        this.data = Objects.requireNonNull(data);
        this.buffer = ByteBuffer.wrap(data);
        this.buffer.position(0);
    }

    @Override
    public byte read() throws IOException {
        return buffer.get();
    }

    @Override
    public void seek(int position) {
        buffer.position(position);
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public boolean isEOF() {
        return buffer.position() >= data.length;
    }

    @Override
    public void close() {
        // Nothing to do here
    }
}
