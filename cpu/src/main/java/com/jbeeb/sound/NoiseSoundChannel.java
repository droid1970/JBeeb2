package com.jbeeb.sound;

import java.util.Arrays;
import java.util.Random;

public final class NoiseSoundChannel extends AbstractSoundChannel {

    // This needs some serious work!

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
    protected boolean shouldRecomputeData(final int frequency, final double volume) {
        return true;
    }

//    @Override
//    protected void sendData(final byte[] data, final int count, final boolean changed) {
//        this.line.write(data, 0, count);
////        if (changed) {
////            this.lineWriter.setData(data, count, changed);
////        }
//    }

    @Override
    protected int computeData(int sampleRate, int frequency, double volume) {
        if (volume <= 0.0) {
            Arrays.fill(data, 0, 32, (byte) 0);
            return 32;
        }
        final int ind = rawPeriod % PERIOD.length;
        final int period = (rawPeriod == 7) ? sampleRate / frequency : PERIOD[ind];
        final int limit = LIMIT[ind];
        final int count = 32;
        for (int i = 0; i < count; i++) {
            double v = gaussians[index % limit];
            data[i] = (byte) (-127 + 254 * v);
            if ((i % period) == 0) {
                index++;
            }
        }
        return count;
    }

    private static final int[] PERIOD = {
            1,
            2,
            3,
            3,

            1,
            10,
            20,
            4,
    };

    private static final int[] LIMIT = {
            32,
            32,
            32,
            32,

            44_100,
            44_100,
            44_100,
            44_100
    };
}
