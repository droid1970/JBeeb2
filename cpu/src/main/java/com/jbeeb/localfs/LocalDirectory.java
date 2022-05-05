package com.jbeeb.localfs;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class LocalDirectory extends LocalFileElement implements LfsElement {

    public LocalDirectory(final File file) {
        super(file);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(file + ": not a directory");
        }
    }

    @Override
    public LfsElementType getType() {
        return LfsElementType.DIRECTORY;
    }

    @Override
    public int getLoadAddress() {
        return 0;
    }

    @Override
    public int getExecAddress() {
        return 0;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public List<? extends LfsElement> list() {
        final File[] files = file().listFiles();
        return (files == null) ?
                Collections.emptyList() :
                Arrays.stream(files).map(LocalFileElement::of).collect(Collectors.toList());
    }

    @Override
    public RandomAccessData getData() throws IOException {
        throw new IOException("Cannot open directory for read");
    }

    @Override
    public final String toString() {
        return "(" + getName() + ")";
    }
}
