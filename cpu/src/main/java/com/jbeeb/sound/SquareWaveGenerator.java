package com.jbeeb.sound;

public class SquareWaveGenerator implements WaveGenerator {

    private long tick = 0;
    private long lastTransition = 0;
    private long period = 20;

    private double currentValue = 1.0;

    public SquareWaveGenerator(final int initialPeriod) {
        setPeriod(initialPeriod);
    }

    @Override
    public double next() {
        if (tick >= (lastTransition + period)) {
            currentValue = -currentValue;
            lastTransition = tick;
        }
        tick++;
        return currentValue;
    }

    @Override
    public void setPeriod(long period) {
        this.period = Math.max(1L, period);
    }
}
