package com.jbeeb.sound;

import com.jbeeb.sound.SquareWaveSoundChannel;

import java.util.function.IntConsumer;

public final class SoundChip implements IntConsumer  {

    private int[] register = new int[4];
    private int latchedRegister;

    private SquareWaveSoundChannel[] soundChannel;

    public SoundChip() {
        try {
            this.soundChannel = new SquareWaveSoundChannel[4];
            for (int i = 0; i < 4; i++) {
                soundChannel[i] = new SquareWaveSoundChannel(256);
                soundChannel[i].start();
            }
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
            soundChannel[channel].setVolume((channel == 3) ? 0.0 : vol);
        } else if (channel == 3) {
            // Noise not supported yet
        } else if ((command & 0x80) != 0) {
            register[channel] = (register[channel] & ~0x0f) | (value & 0x0f);
        } else {
            register[channel] = (register[channel] & 0x0f) | ((value & 0x3f) << 4);
            soundChannel[channel].setFrequency((int) freq(register[channel]));
        }
    }

    private static double freq(final int freq) {
        return (4_000_000.0 / 32.0) / freq;
    }
}
