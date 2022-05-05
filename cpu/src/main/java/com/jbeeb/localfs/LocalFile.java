package com.jbeeb.localfs;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class LocalFile extends LocalFileElement implements LfsElement {

    public LocalFile(final File file) {
        super(file);
    }

    @Override
    public List<? extends LfsElement> list() {
        return Collections.emptyList();
    }

    @Override
    public final String toString() {
        return getName();
    }
}
