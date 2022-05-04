package com.jbeeb.disk;

public final class DiskDetails {

    private final boolean dsd;
    private final int size;

    public DiskDetails(boolean dsd, int size) {
        this.dsd = dsd;
        this.size = size;
    }

    public boolean isDsd() {
        return dsd;
    }

    public int getSize() {
        return size;
    }
}
