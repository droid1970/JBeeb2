package com.jbeeb.sound;

import java.util.function.IntConsumer;

public final class SoundChip implements IntConsumer  {

    // TODO: One sound thread with multiple channels

    private int[] register = new int[4];
    private int latchedRegister;

    private final SoundChannel[] soundChannels = new SoundChannel[4];
    private final NoiseGenerator noiseGenerator = new NoiseGenerator();

    public SoundChip() {
        try {
            for (int i = 0; i < 3; i++) {
                soundChannels[i] = new SoundChannel(new SquareWaveGenerator(20));
                soundChannels[i].start();
            }
            this.soundChannels[3] = new SoundChannel(noiseGenerator);
            this.soundChannels[3].start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            soundChannels[channel].setVolume((channel == 3) ? 0.0 : vol);
        } else if ((command & 0x80) != 0) {
            if (channel == 3) {
                register[channel] = value & 0x7;
                noiseGenerator.setNoiseTypeIndex(register[channel]);
            } else {
                register[channel] = (register[channel] & ~0x0f) | (value & 0x0f);
            }
        } else {
            register[channel] = (register[channel] & 0x0f) | ((value & 0x3f) << 4);
            soundChannels[channel].setFrequency((int) freq(register[channel]));
            if (channel == 2) {
                // Set the noise generator's period from Channel 1
                soundChannels[3].setPeriod(register[2] / 2);
            }
        }
    }

    private static double freq(final int freq) {
        return (4_000_000.0 / 32.0) / freq;
    }
}
