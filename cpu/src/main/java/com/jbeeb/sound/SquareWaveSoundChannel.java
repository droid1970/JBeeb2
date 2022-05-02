package com.jbeeb.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class SquareWaveSoundChannel extends Thread {

    private static final int SAMPLE_RATE = 44_100;
    private final SourceDataLine line;

    private volatile boolean stopRequested;

    private static final int BUFFER_SIZE = 2_205;

    public SquareWaveSoundChannel(int initialFrequency) throws Exception {
        freq.set(initialFrequency);
        vol.set(0.0);
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        line = AudioSystem.getSourceDataLine(af);
        line.open(af, BUFFER_SIZE);
        line.start();
    }

    private AtomicInteger freq = new AtomicInteger();
    private AtomicReference<Double> vol = new AtomicReference<>(0.0);

    private int lastPeriod;
    private double lastVolume;
    private byte[] data;

    @Override
    public void run() {
        try {
            while (!stopRequested) {
                final double volume = vol.get();
                final int period = SAMPLE_RATE / freq.get();
                if (lastVolume != volume || lastPeriod != period) {
                    // TODO: Re-use a byte array
                    data = new byte[period * 2];
                    for (int i = 0; i < data.length; i++) {
                        final boolean on = ((i / period) & 1) != 0;
                        final byte value = (byte) (volume * (on ? (byte) 127 : (byte) -127));
                        data[i] = value;
                    }
                    lastPeriod = period;
                    lastVolume = volume;
                }
                line.write(data, 0, data.length);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setVolume(final double volume) {
        vol.set(volume);
    }

    public void setFrequency(final int frequency) {
        freq.set(frequency);
    }
}
