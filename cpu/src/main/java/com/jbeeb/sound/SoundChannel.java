package com.jbeeb.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Objects;

public class SoundChannel extends Thread {

    private static final int SAMPLE_RATE = 44_100;
    private static final int BUFFER_SIZE = SAMPLE_RATE / 100;

    private final WaveGenerator waveGenerator;
    private final SourceDataLine line;

    private final byte[] data = new byte[BUFFER_SIZE];

    private double volume = 0.0;

    private volatile boolean stopRequested = false;

    public SoundChannel(final WaveGenerator waveGenerator) throws LineUnavailableException {
        this.waveGenerator = Objects.requireNonNull(waveGenerator);
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        line = AudioSystem.getSourceDataLine(af);
        line.open(af, BUFFER_SIZE * 4);
        line.start();
    }

    public void setVolume(final double volume) {
        this.volume = Math.max(-1.0, Math.min(1.0, volume));
    }

    public void setPeriod(final int period) {
        this.waveGenerator.setPeriod(period);
    }

    public void setFrequency(final int frequency) {
        this.waveGenerator.setFrequency(frequency, SAMPLE_RATE);
    }

    @Override
    public void run() {
        try {
            while (!stopRequested) {
                for (int i = 0; i < data.length; i++) {
                    final double wv = waveGenerator.next();
                    final byte b = (byte) (127 * wv * volume);
                    data[i] = b;
                }
                line.write(data, 0, data.length);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
