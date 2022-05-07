package com.jbeeb.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;

public class MultiSoundChannel extends Thread {

    private static final double MASTER_VOLUME = 0.05;

    private static final int SAMPLE_RATE = 44_100;
    private static final int FRAME_SIZE = SAMPLE_RATE / 100;
    private static final int BUFFER_SIZE = FRAME_SIZE * 4;

    private final int channelCount;
    private final WaveGenerator[] waveGenerators;
    private final SourceDataLine[] lines;

    private final byte[][] data;

    private double[] volume;

    private volatile boolean stopRequested = false;
    private volatile boolean paused;

    public MultiSoundChannel(final WaveGenerator[] waveGenerators) throws LineUnavailableException {
        this.channelCount = waveGenerators.length;
        this.waveGenerators = Arrays.copyOf(waveGenerators, waveGenerators.length);
        this.data = new byte[channelCount][FRAME_SIZE];
        final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        this.lines = new SourceDataLine[channelCount];
        for (int i = 0; i < channelCount; i++) {
            lines[i] = AudioSystem.getSourceDataLine(af);
            lines[i].open(af, BUFFER_SIZE);
            lines[i].start();
        }
        this.volume = new double[channelCount];
    }

    public void setVolume(final int channelIndex, final double volume) {
        this.volume[channelIndex] = MASTER_VOLUME * Math.max(-1.0, Math.min(1.0, volume));
    }

    public void setPeriod(final int channelIndex, final int period) {
        this.waveGenerators[channelIndex].setPeriod(period);
    }

    public void setFrequency(final int channelIndex, final int frequency) {
        this.waveGenerators[channelIndex].setFrequency(frequency, SAMPLE_RATE);
    }

    public void setPaused(final boolean paused) {
        this.paused = paused;
    }

    @Override
    public void run() {
        try {
            while (!stopRequested) {
                for (int c = 0; c < channelCount; c++) {
                    for (int i = 0; i < data[c].length; i++) {
                        final double wv = (paused) ? 0.0 : waveGenerators[c].next();
                        final byte b = (byte) (127 * wv * volume[c]);
                        data[c][i] = b;
                    }
                }
                for (int c = 0; c < channelCount; c++) {
                    lines[c].write(data[c], 0, data[c].length);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
