package com.jbeeb.sound;

import java.util.function.IntConsumer;

public final class MultiSoundChip implements IntConsumer  {

    private int[] register = new int[4];
    private int latchedRegister;

    private final NoiseGenerator noiseGenerator = new NoiseGenerator();
    private final MultiSoundChannel soundChannel;

    public MultiSoundChip() throws Exception {
        final WaveGenerator[] waveGenerators = new WaveGenerator[4];
        for (int i = 0; i < 3; i++) {
            waveGenerators[i] = new SquareWaveGenerator(20);
        }
        waveGenerators[3] = noiseGenerator;
        this.soundChannel = new MultiSoundChannel(waveGenerators);
        this.soundChannel.start();
    }

    @Override
    public void accept(final int value) {
        int command;
        int channel;
        if ((value & 0x80) != 0) {
            latchedRegister = (value & 0x70);
            command = (value & 0xF0);
        } else {
            command = latchedRegister;
        }

        channel = ((command >>> 5) & 0x03);

        if ((command & 0x10) != 0) {
            // Volume
            int newVolume = 15 - (value & 0xF);
            final double vol = newVolume / 15.0;
            soundChannel.setVolume(channel, vol);
        } else if ((command & 0x80) != 0) {
            if (channel == 3) {
                register[channel] = value & 0x7;
                noiseGenerator.setNoiseTypeIndex(register[channel]);
            } else {
                register[channel] = (register[channel] & ~0x0f) | (value & 0x0f);
            }
        } else {
            register[channel] = (register[channel] & 0x0f) | ((value & 0x3f) << 4);
            soundChannel.setFrequency(channel, (int) freq(register[channel]));
            if (channel == 2) {
                // Set the noise generator's period from Channel 1
                soundChannel.setPeriod(3, register[2] / 2);
            }
        }
    }

    private static double freq(final int freq) {
        return (4_000_000.0 / 32.0) / freq;
    }
}
