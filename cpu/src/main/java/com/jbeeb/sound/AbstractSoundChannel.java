package com.jbeeb.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

abstract class AbstractSoundChannel extends Thread implements SoundChannel {

    private static final int SAMPLE_RATE = 44_100;
    protected final SourceDataLine line;

    private volatile boolean stopRequested;

    private static final int BUFFER_SIZE = 2_205;

    AbstractSoundChannel(int initialFrequency) throws Exception {
        freq.set(initialFrequency);
        vol.set(0.0);
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        line = AudioSystem.getSourceDataLine(af);
        line.open(af, BUFFER_SIZE);
        line.start();
    }

    private AtomicInteger freq = new AtomicInteger();
    private AtomicReference<Double> vol = new AtomicReference<>(0.0);

    private int lastFrequency;
    private double lastVolume;

    protected final byte[] data = new byte[SAMPLE_RATE];

    @Override
    public void run() {
        try {
            int count = 0;
            while (!stopRequested) {
                final double volume = vol.get();
                final int frequency = freq.get();
                boolean changed = false;
                if (lastVolume != volume || lastFrequency != frequency) {
                    count = computeData(SAMPLE_RATE, frequency, volume);
                    lastFrequency = frequency;
                    lastVolume = volume;
                    changed = true;
                }
                sendData(data, count, true);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void sendData(final byte[] data, final int count, final boolean changed) {
        line.write(data, 0, count);
    }

    protected abstract int computeData(final int sampleRate, final int frequency, final double volume);

    @Override
    public void setVolume(final double volume) {
        vol.set(volume);
    }

    @Override
    public void setFrequency(final int frequency) {
        freq.set(frequency);
    }
}
