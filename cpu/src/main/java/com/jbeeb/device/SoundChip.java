package com.jbeeb.device;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class SoundChip {

    private int[] volume = new int[4];
    private int[] register = new int[4];
    private int latchedRegister;

    private SoundChannel[] soundChannel;

    public void tick() {
        //soundChannel.tick();
    }

    public SoundChip() {
        try {
            this.soundChannel = new SoundChannel[4];
            for (int i = 0; i < 4; i++) {
                soundChannel[i] = new SoundChannel(SAMPLE_RATE, 256.0);
                soundChannel[i].start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
            volume[channel] = newVolume;
        } else if (channel == 3) {
            // Noise not supported yet
        } else if ((command & 0x80) != 0) {
            register[channel] = (register[channel] & ~0x0f) | (value & 0x0f);
        } else {
            register[channel] = (register[channel] & 0x0f) | ((value & 0x3f) << 4);
            soundChannel[channel].setFrequency(freq(register[channel]));
        }
    }

    private static double freq(final int freq) {
        return (4_000_000.0 / 32.0) / freq;
    }

    protected static final int SAMPLE_RATE = 16 * 1024;

    private static final class SampleGenerator {

        private final int sampleRate;
        private double frequency = 400.0;
        private double period;
        private double angle;

        private long index;

        SampleGenerator(final int sampleRate, final double frequency) {
            this.sampleRate = sampleRate;
            setFrequency(frequency);
        }

        void setFrequency(final double frequency) {
            this.frequency = frequency;
            this.period = (double) sampleRate / frequency;
        }

        double getFrequency() {
            return frequency;
        }

        void setAngle(double angle) {
            this.angle = angle;
        }

        public void nextFrame(final byte[] result, final double[] angles, final double volume) {
            for (int i = 0; i < result.length; i++) {
                angle += 2.0 * Math.PI / period;
                angles[i] = angle;
                final double sin = Math.sin(angle);
                result[i] = (byte) (Math.signum(sin) * volume * 127);
                index++;
            }
        }
    }

    private static final class SoundChannel extends Thread {

        final SampleGenerator sampleGenerator;
        final SourceDataLine line;

        volatile boolean stopRequested;

        double volume = 0.0;

        private final Object volumeLock = new Object();

        private final byte[] samples;
        private final double[] angles;

        SoundChannel(final int sampleRate, final double frequency) throws Exception {
            this.sampleGenerator = new SampleGenerator(sampleRate, frequency);
            final int millsPerFrame = 50;
            final int sampleCount = (millsPerFrame * SAMPLE_RATE) / 1000;
            samples = new byte[sampleCount];
            angles = new double[sampleCount];
            final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            line = AudioSystem.getSourceDataLine(af);
            line.open(af, SAMPLE_RATE);
            line.start();
        }

        @Override
        public void run() {
            try {
                while (!stopRequested) {
                    sampleGenerator.nextFrame(samples, angles, volume);
                    final int count = line.write(samples, 0, samples.length);
                    if (count > 0 && count < samples.length) {
                        sampleGenerator.setAngle(angles[count - 1]);
                    }
                    Thread.sleep(1);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void setVolume(final double volume) {
            if (this.volume != volume) {
                line.flush();
                this.volume = volume;
            }
        }

        public void setFrequency(final double frequency) {
            if (sampleGenerator.getFrequency() != frequency) {
                line.flush();
                this.sampleGenerator.setFrequency(frequency);
            }
        }
    }
}
