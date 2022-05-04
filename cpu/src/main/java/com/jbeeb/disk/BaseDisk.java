package com.jbeeb.disk;

import com.jbeeb.util.ScheduledTask;
import com.jbeeb.util.Scheduler;
import com.jbeeb.util.Util;

import java.util.Objects;

public class BaseDisk implements Disk {

    private final FloppyDiskController fdc;
    private int[] data;
    private final String name;
    private Runnable flusher;

    private boolean isDsd;
    private int byteWithinSector;
    private boolean writeProt = false;
    private int seekOffset;
    private int sectorOffset;
    private int formatSector;
    private int rsector;
    private int track;
    private int side;

    private final ScheduledTask notFoundTask;
    private final ScheduledTask readTask;
    private final ScheduledTask writeTask;
    private final ScheduledTask readAddrTask;
    private final ScheduledTask formatTask;

    private final Scheduler scheduler;

    public BaseDisk(FloppyDiskController fdc, Scheduler scheduler, String name, int[] data, Runnable flusher) {
        this.fdc = Objects.requireNonNull(fdc);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.name = name;
        this.flusher = flusher;

        DiskDetails details = Util.getDiskDetails(name);
        this.isDsd = details.isDsd();
        int size = details.getSize();
        if (data.length > size && !this.isDsd) {
            details = Util.getDiskDetails(name + ".dsd");
            this.isDsd = true;
            size = details.getSize();;
        }

        this.data = Util.resizeArray(data, size);

        this.writeProt = (flusher == null);
        this.byteWithinSector = 0;
        this.seekOffset = 0;
        this.sectorOffset = 0;
        this.formatSector = 0;
        this.rsector = 0;
        this.track = 0;
        this.side = -1;

        notFoundTask = scheduler.newTask(() -> {
            this.fdc.notFound();
        });

        readTask = scheduler.newTask(() -> {
            this.fdc.discData(this.data[this.seekOffset + this.sectorOffset + this.byteWithinSector]);
            if (++this.byteWithinSector == 256) {
                this.fdc.discFinishRead();
            } else {
                rescheduleReadTask(FloppyDiskController.DISC_TIME_SLICE);
            }
        });

        writeTask = scheduler.newTask(() -> {
            if (this.writeProt) {
                this.fdc.writeProtect();
                return;
            }
            this.data[this.seekOffset + this.sectorOffset + this.byteWithinSector] = this.fdc.readDiscData(this.byteWithinSector == 255);
            if (++this.byteWithinSector == 256) {
                this.fdc.discFinishRead();
                this.flush();
            } else {
                rescheduleWriteTask(FloppyDiskController.DISC_TIME_SLICE);
            }
        });

        readAddrTask = scheduler.newTask(() -> {
            switch (this.byteWithinSector) {
                case 0:
                    this.fdc.discData(this.track);
                    break;
                case 1:
                    this.fdc.discData(this.side);
                    break;
                case 2:
                    this.fdc.discData(this.rsector);
                    break;
                case 3:
                    this.fdc.discData(1);
                    break;
                case 4:
                case 5:
                    this.fdc.discData(0);
                    break;
                case 6:
                    this.fdc.discFinishRead();
                    this.rsector++;
                    if (this.rsector == 10) this.rsector = 0;
                    return;
            }
            this.byteWithinSector++;
            rescheduleReadAddrTask(FloppyDiskController.DISC_TIME_SLICE);
        });

        formatTask = scheduler.newTask(() -> {
            if (this.writeProt) {
                this.fdc.writeProtect();
                return;
            }
            this.data[this.seekOffset + this.sectorOffset + this.byteWithinSector] = 0;
            if (++this.byteWithinSector == 256) {
                this.byteWithinSector = 0;
                this.sectorOffset += 256;
                if (++this.formatSector == 10) {
                    this.fdc.discFinishRead();
                    this.flush();
                    return;
                }
            }
            rescheduleFormatTask(FloppyDiskController.DISC_TIME_SLICE);
        });
    }

    private void rescheduleReadTask(final long delay) {
        this.readTask.reschedule(delay);
    }

    private void rescheduleWriteTask(final long delay) {
        this.writeTask.reschedule(delay);
    }

    private void rescheduleReadAddrTask(final long delay) {
        this.readAddrTask.reschedule(delay);
    }

    private void rescheduleFormatTask(final long delay) {
        this.formatTask.reschedule(delay);
    }

    void flush() {
        if (this.flusher != null) {
            this.flusher.run();
        }
    }
    private boolean check(final int track, final int side, final boolean density) {
        if (this.track != track || density || ((side != 0) && !this.isDsd)) {
            this.notFoundTask.reschedule(500 * FloppyDiskController.DISC_TIME_SLICE);
            return false;
        }
        return true;
    }

    @Override
    public void read(int sector, int track, int side, boolean density) {
        if (!this.check(track, side, density)) return;
        this.side = side;
        this.readTask.reschedule(FloppyDiskController.DISC_TIME_SLICE);
        this.sectorOffset = sector * 256 + ((side != 0)  ? 10 * 256 : 0);
        this.byteWithinSector = 0;
    }

    @Override
    public void write(int sector, int track, int side, boolean density) {
        if (!this.check(track, side, density)) return;
        this.side = side;
        // NB in old code this used to override "time" to be -1000, which immediately forced a write.
        // I'm not sure why that was required. So I'm ignoring it here. Any funny disc write bugs might be
        // traceable to this change.
        this.writeTask.reschedule(FloppyDiskController.DISC_TIME_SLICE);
        this.sectorOffset = sector * 256 + ((side != 0) ? 10 * 256 : 0);
        this.byteWithinSector = 0;
    }

    @Override
    public void address(int track, int side, boolean density) {
        if (!this.check(track, side, density)) return;
        this.side = side;
        this.readAddrTask.reschedule(FloppyDiskController.DISC_TIME_SLICE);
        this.byteWithinSector = 0;
        this.rsector = 0;
    }

    @Override
    public void format(int track, int side, boolean density) {
        if (!this.check(track, side, density)) return;
        this.side = side;
        this.formatTask.reschedule(FloppyDiskController.DISC_TIME_SLICE);
        this.formatSector = 0;
        this.sectorOffset = (side != 0) ? 10 * 256 : 0;
        this.byteWithinSector = 0;
    }

    @Override
    public int seek(int seek) {
        this.seekOffset = track * 10 * 256;
        if (this.isDsd) this.seekOffset <<= 1;
        final int oldTrack = this.track;
        this.track = track;
        return this.track - oldTrack;
    }

    @Override
    public boolean writeProt() {
        return writeProt;
    }
}
