package com.jbeeb.disk;

import com.jbeeb.util.ScheduledTask;

import java.util.Objects;

public class EmptyDisk implements Disk {

    private final FloppyDiskController fdc;
    private final ScheduledTask notFoundTask;

    public EmptyDisk(final FloppyDiskController fdc) {
        this.fdc = Objects.requireNonNull(fdc);
        this.notFoundTask = fdc.getScheduler().newTask(() -> {
            fdc.notFound();
        });
    }

    @Override
    public void write(int sector, int track, int side, boolean density) {
        this.notFoundTask.reschedule(500 * FloppyDiskController.DISC_TIME_SLICE);
    }

    @Override
    public void read(int sector, int track, int side, boolean density) {
        this.notFoundTask.reschedule(500 * FloppyDiskController.DISC_TIME_SLICE);
    }

    @Override
    public void address(int track, int side, boolean density) {
        this.notFoundTask.reschedule(500 * FloppyDiskController.DISC_TIME_SLICE);
    }

    @Override
    public void format(int track, int side, boolean density) {
        this.notFoundTask.reschedule(500 * FloppyDiskController.DISC_TIME_SLICE);
    }

    @Override
    public int seek(int seek) {
        return 0;
    }

    @Override
    public boolean writeProt() {
        return true;
    }
}
