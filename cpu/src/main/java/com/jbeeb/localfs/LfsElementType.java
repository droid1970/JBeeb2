package com.jbeeb.localfs;

import java.util.Objects;

public enum LfsElementType {
    FILE("file", ""),
    DIRECTORY("directory", "<DIR>"),
    IMAGE("disk image", "<IMG>");

    final String description;
    final String shortDescription;


    LfsElementType(final String description, final String shortDescription) {
        this.description = Objects.requireNonNull(description);
        this.shortDescription = Objects.requireNonNull(shortDescription);
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }
}
