package com.jbeeb.localfs;

import java.util.List;
import java.util.Optional;

public interface LfsElement {
    String getName();

    Optional<? extends LfsElement> getParent();

    default Optional<? extends LfsElement> findAny(final String name) {
        return Optional.empty();
    }

    List<? extends LfsElement> list();

    boolean isDirectory();

    default boolean isFile() {
        return !isDirectory();
    }
}
