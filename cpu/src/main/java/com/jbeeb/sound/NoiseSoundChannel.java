package com.jbeeb.sound;

import java.util.Arrays;
import java.util.Random;

public final class NoiseSoundChannel extends AbstractSoundChannel {

    private final Random rnd = new Random();

    private final LineWriter lineWriter;

    private final double[] gaussians;

    private volatile boolean computed;

    public NoiseSoundChannel(int initialFrequency) throws Exception {
        super(initialFrequency);
        this.lineWriter = new LineWriter(this.line);
        this.lineWriter.start();
        this.gaussians = new double[65536];
        for (int i = 0; i < gaussians.length; i++) {
            gaussians[i] = rnd.nextGaussian();
        }
    }

    private int index = 0;

    @Override
    protected void sendData(final byte[] data, final int count, final boolean changed) {
        if (changed) {
            this.lineWriter.setData(data, count, changed);
        }
    }

    @Override
    protected int computeData(int sampleRate, int frequency, double volume) {
        if (volume <= 0.0) {
            Arrays.fill(data, 0, 32, (byte) 0);
            return 32;
        }
        for (int i = 0; i < 5000; i++) {
            double v = gaussians[index % gaussians.length];
            data[i] = (volume == 0.0) ? 0 : (byte) (-127 + 254 * v * volume);
            //if ((i & 7) == 0) {
                index++;
            //}
        }
        return 5000;
    }
}
