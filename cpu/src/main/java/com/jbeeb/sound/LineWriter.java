package com.jbeeb.sound;

import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class LineWriter extends Thread {

    private final SourceDataLine line;

    private byte[] data;
    private int count;

    private volatile boolean stopRequested;

    public LineWriter(final SourceDataLine line) {
        this.line = Objects.requireNonNull(line);
    }

    private final AtomicBoolean waitForNewData = new AtomicBoolean();
    private final Object lock = new Object();

    @Override
    public void run() {
        try {
            while (!stopRequested) {
                line.write(data, 0, count);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    synchronized void setData(final byte[] data, final int count, final boolean stopLine) {
//        if (stopLine) {
//            line.flush();
//        }
        if (stopLine) {
//            line.stop();
//            line.flush();
//            line.start();
        }
        this.data = Arrays.copyOf(data, count);
        this.count = count;
    }
}
