package com.jbeeb.localfs;

import com.jbeeb.util.Util;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface LfsElement {
    String getName();
    LfsElementType getType();

    int getLoadAddress();
    int getExecAddress();

    int length();

    Optional<? extends LfsElement> getParent();

    default Optional<? extends LfsElement> findAny(final String name) {
        return list().stream().filter(e -> e.matchesName(name)).findFirst();
    }

    default boolean matchesName(String name) {
        return Objects.equals(Util.firstElementOf(name.trim(), " ").toUpperCase(), getName().toUpperCase());
    }

    List<? extends LfsElement> list();

    RandomAccessData getData() throws IOException;

    boolean isDirectory();

    default boolean isFile() {
        return !isDirectory();
    }
}
