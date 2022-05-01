package com.jbeeb.main;

import com.jbeeb.util.ClockListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class SquareWaveSoundChannel extends Thread {

    private static final int SAMPLE_RATE = 16384;

    private final SourceDataLine line;

    private int counter;
    private int frequency;
    private int period;
    private double volume = 0.9;
    private byte state = (byte) 127;

    private static final byte[] SAMPLE_ON = new byte[1024];
    private static final byte[] SAMPLE_OFF = new byte[1024];
    static {
        for (int i = 0; i < SAMPLE_ON.length; i++) {
            SAMPLE_ON[i] = (byte) 127;
            SAMPLE_OFF[i] = (byte) -127;
        }
    }
    private static byte[] samples = SAMPLE_ON;

    private volatile boolean stopRequested;

    public SquareWaveSoundChannel(int initialFrequency) throws Exception {
        freq.set(initialFrequency);
        vol.set(0.0);
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        line = AudioSystem.getSourceDataLine(af);
        line.open(af, SAMPLE_RATE);
        gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        line.start();
    }

    private AtomicInteger freq = new AtomicInteger();
    private AtomicReference<Double> vol = new AtomicReference<>(0.0);

    private int lastPeriod;
    private double lastVolume;
    private byte[] data;
    private int dataWritten = 0;
    private final FloatControl gain;

    @Override
    public void run() {
        try {
            while (!stopRequested) {
                final double volume = vol.get();
                final int period = SAMPLE_RATE / freq.get();
                if (lastVolume != volume || lastPeriod != period) {
                    data = new byte[period * 32];
                    dataWritten = 0;
                    for (int i = 0; i < data.length; i++) {
                        final boolean on = ((i / period) & 1) != 0;
                        final byte value = (byte) (volume * (on ? (byte) 127 : (byte) -127));
                        data[i] = value;
                    }
                    line.flush();
                    lastPeriod = period;
                    lastVolume = volume;
                }

                final int avail = line.available();
                if (avail >= data.length) {
                    line.write(data, 0, data.length);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void tick() {

    }

    public void setVolume(final double volume) {
        vol.set(volume <= 0.01 ? 0.0 : 1.0);
        //gain.setValue(-30f + (float) (36 * volume));
    }

    public void setFrequency(final int frequency) {
        freq.set(frequency);
    }
}
