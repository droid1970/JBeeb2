package com.jbeeb.disk;

public interface Disk {
    void write(int sector, int track, int side, boolean density);
    void read(int sector, int track, int side, boolean density);
    void address(int track, int side, boolean density);
    void format(int track, int side, boolean density);
    int seek(int seek);
    boolean writeProt();
}