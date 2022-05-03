package com.jbeeb.sound;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

public class NoiseGenerator implements WaveGenerator {

    private static final int P1 = 7;
    private static final int P2 = 13;
    private static final int P3 = 26;

    private enum NoiseType {

        PERIODIC_HIGH(NoiseGenerator::shiftLfsrPeriodicNoise, p -> P1),
        PERIODIC_MEDIUM(NoiseGenerator::shiftLfsrPeriodicNoise, p -> P2),
        PERIODIC_LOW(NoiseGenerator::shiftLfsrPeriodicNoise, p -> P3),
        PERIODIC_CHANNEL_1(NoiseGenerator::shiftLfsrPeriodicNoise, p -> p),

        WHITE_HIGH(NoiseGenerator::shiftLfsrWhiteNoise, p -> 2),
        WHITE_MEDIUM(NoiseGenerator::shiftLfsrWhiteNoise, p -> 8),
        WHITE_LOW(NoiseGenerator::shiftLfsrWhiteNoise, p -> 16),
        WHITE_CHANNEL_1(NoiseGenerator::shiftLfsrWhiteNoise, p -> p)
        ;

        final IntUnaryOperator lfsrShifter;
        final IntUnaryOperator incOperator;

        NoiseType(final IntUnaryOperator lfsrShifter, final IntUnaryOperator incOperator) {
            this.lfsrShifter = Objects.requireNonNull(lfsrShifter);
            this.incOperator = Objects.requireNonNull(incOperator);
        }

        int shiftLFSR(final int lsr) {
            return lfsrShifter.applyAsInt(lsr);
        }

        int getPeriod(final int period) {
            return incOperator.applyAsInt(period);
        }
    };

    private static final NoiseType[] NOISE_TYPES = NoiseType.values();
    private int lfsr;

    private static int shiftLfsrWhiteNoise(int lfsr) {
        var bit = (lfsr & 1) ^ ((lfsr & (1 << 1)) >>> 1);
        lfsr = (lfsr >>> 1) | (bit << 14);
        return lfsr;
    }

    private static int shiftLfsrPeriodicNoise(int lfsr) {
        lfsr >>= 1;
        if (lfsr == 0) lfsr = 1 << 14;
        return lfsr;
    }

    private long tick = 0;
    private long lastTransition = 0;
    private NoiseType noiseType = NoiseType.WHITE_HIGH;
    private int period;
    private int periodLatch = 100;
    private boolean output = true;

    @Override
    public double next() {
        period = noiseType.getPeriod(periodLatch);
        if (tick >= (lastTransition + period)) {
            output = !output;
            lastTransition = tick;
            lfsr = noiseType.shiftLFSR(lfsr);
        }
        tick++;
        if ((lfsr & 1) != 0) {
            return 1.0;
        } else {
            return -1.0;
        }
    }

    public void setNoiseTypeIndex(final int noiseTypeIndex) {
        this.noiseType = NOISE_TYPES[noiseTypeIndex & 0x7];
        this.period = noiseType.getPeriod(periodLatch);
    }

    @Override
    public void setPeriod(long period) {
        // This is for the *_CHANNEL_1 types
        this.periodLatch = (int) (period & 0xF);
    }
}
