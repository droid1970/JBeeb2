package com.jbeeb.localfs;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public abstract class LocalFileElement implements LfsElement {

    private final File file;

    public LocalFileElement(final File file) {
        this.file = Objects.requireNonNull(file);
    }

    public final String getName() {
        return file.getName();
    }

    @Override
    public Optional<LocalFileElement> getParent() {
        final File parent = file.getParentFile();
        return (parent == null) ? Optional.empty() : Optional.of(new LocalDirectory(parent));
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    public static LfsElement of(final File file) {
        if (file.isDirectory()) {
            return new LocalDirectory(file);
        } else {
            if (file.getName().toLowerCase().endsWith(".ssd")) {
                try {
                    final LfsElement parent = LocalFileElement.of(file.getParentFile());
                    return DiskImage.of(parent, file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return (file.isDirectory()) ? new LocalDirectory(file) : new LocalFile(file);
    }

    protected File file() {
        return file;
    }
}
