package com.jbeeb.localfs;

import com.jbeeb.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LocalFile extends LocalFileElement implements LfsElement {

    public LocalFile(final File file) {
        super(file);
    }

    @Override
    public LfsElementType getType() {
        return LfsElementType.FILE;
    }

    @Override
    public int getLoadAddress() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getExecAddress() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int length() {
        return (int) Math.min(65536L, file().length());
    }

    @Override
    public List<? extends LfsElement> list() {
        return Collections.emptyList();
    }

    @Override
    public final String toString() {
        return getName();
    }

    @Override
    public RandomAccessData getData() throws IOException {
        return new ByteRandomAccessData(Util.readFileAsbytes(file()));
    }
}
