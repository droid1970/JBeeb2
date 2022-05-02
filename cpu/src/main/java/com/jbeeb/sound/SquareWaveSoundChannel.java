package com.jbeeb.sound;

public final class SquareWaveSoundChannel extends AbstractSoundChannel {

    public SquareWaveSoundChannel(int initialFrequency) throws Exception {
        super(initialFrequency);
    }

    protected int computeData(final int sampleRate, final int frequency, final double volume) {
        final int period = sampleRate / frequency;
        final int count = period * 2;
        for (int i = 0; i < count; i++) {
            final boolean on = ((i / period) & 1) != 0;
            final byte value = (byte) (volume * (on ? (byte) 127 : (byte) -127));
            data[i] = value;
        }
        return count;
    }
}
