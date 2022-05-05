package com.jbeeb.localfs;

import java.io.File;
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
    public List<? extends LfsElement> list() {
        final File[] files = file().listFiles();
        return (files == null) ?
                Collections.emptyList() :
                Arrays.stream(files).map(LocalFileElement::of).collect(Collectors.toList());
    }

    @Override
    public Optional<? extends LfsElement> findAny(final String name) {
        return list().stream().filter(e -> Objects.equals(e.getName().toUpperCase(), name.toUpperCase())).findFirst();
    }

    @Override
    public final String toString() {
        return "(" + getName() + ")";
    }
}
