package com.jbeeb.device;

import com.jbeeb.util.InterruptSource;
import com.jbeeb.util.SystemStatus;
import com.jbeeb.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class FloppyDiskController extends AbstractMemoryMappedDevice implements InterruptSource {

    private static final int UNDEFINED_INT = -1;
    private static final int[] UNDEFINED_ARRAY = new int[]{};

    private interface Disk {
        void write(int a, int b, boolean c, int d);
        void read(int a, int b, boolean c, int d);
        void address(int a, boolean b, int c);
        void format(int a, boolean b, int c);
        int seek(int seek);
        boolean writeProt();
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private int status;
    private int curData;
    private int result;
    private int curCommand;
    private int curDrive;
    private int drvout;
    private int paramNum;
    private int paramReq;
    private boolean verify;
    private boolean written;
    private int[] realTrack;
    private Disk[] drives;
    private int phase;

    private boolean nmiRequested;

    public FloppyDiskController(SystemStatus systemStatus, String name, int startAddress) {
        super(systemStatus, name, startAddress, 8);
    }

    @Override
    public boolean isIRQ() {
        return false;
    }

    @Override
    public boolean isNMI() {
        return (status & 8) != 0;
    }

    @Override
    public int readRegister(int index) {
        switch (index & 0x7) {
            case 0:
                return status & 0xFF;
            case 1:
                this.status &= 0x1B;
                return result;
            case 4:
            case 5:
            case 6:
            case 7:
                this.status &= ~0x0C;
                return this.curData;
        }
        return 0;
    }



    private void handleCommand(int val) {
        if ((this.status & 0x80) != 0) {
            return;
        }

        this.curCommand = val & 0x3f;
        if (this.curCommand == 0x17) {
            this.curCommand = 0x13;
        }
        this.curDrive = ((val & 0x80) != 0) ? 1 : 0;
        if (this.curCommand < 0x2c) {
            this.drvout &= ~(0x80 | 0x40);
            this.drvout |= (val & (0x80 | 0x40));
        }
        this.paramNum = 0;
        this.paramReq = this.numParams(this.curCommand);
        this.status = 0x80;
        if (this.paramReq > 0) {
            if (this.curCommand == 0x2c) {
                // read drive status
                this.status = 0x10;
                this.result = 0x80;
                this.result |= ((this.realTrack[this.curDrive] != 0) ? 0 : 2);
                this.result |= (this.drives[this.curDrive].writeProt() ? 0x08 : 0);
                if ((this.drvout & 0x40) != 0) {
                    this.result |= 0x04;
                }
                if ((this.drvout & 0x80) != 0) {
                    this.result |= 0x40;
                }
            } else {
                this.result = 0x18;
                this.status = 0x18;
            }
        }
    }

    private void acceptParmeter(final int parm) {

    }

    private void reset(final int cmd) {

    }

    @Override
    public void writeRegister(int index, int value) {
        switch (index & 7) {
            case 0:
                this.handleCommand(value);
                break;
            case 1:
                this.acceptParmeter(value);
                break;
            case 2:
                this.reset(value);
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                this.data(value);
                break;
        }
    }

    private void error(int result) {
        this.result = result;
        this.status = 0x18;
        if (callbackTask != null) {
            callbackTask.cancel(true);
        }
        //this.callbackTask.cancel();
        this.setspindown();
    }

    private void notFound() {
        this.error(0x18);
    }

    private void writeProtect() {
        this.error(0x12);
    }

    private void headerCrcError() {
        this.error(0x0c);
    }

    private void dataCrcError() {
        this.error(0x0e);
    }

    private void discData(int data) {
        if (this.verify) return;
        this.curData = data & 0xFF;
        this.status = 0x8c;
        this.result = 0;
    }

    private int readDiscData(boolean last) {
        if (!this.written) return 0x00;
        if (!last) {
            this.status = 0x8c;
            this.result = 0;
        }
        this.written = false;
        return this.curData;
    }

    private static final Map<Integer, Integer> PARAMS_MAP = new HashMap<>();
    static {
        PARAMS_MAP.put(0x35, 4);
        PARAMS_MAP.put(0x29, 1);
        PARAMS_MAP.put(0x2C, 0);
        PARAMS_MAP.put(0x3D, 1);
        PARAMS_MAP.put(0x3A, 2);
        PARAMS_MAP.put(0x13, 3);
        PARAMS_MAP.put(0x0B, 3);
        PARAMS_MAP.put(0x1B, 3);
        PARAMS_MAP.put(0x1F, 2);
        PARAMS_MAP.put(0x23, 5);
    }

    private int[] curTrack;

    private void writeSpecial(int reg, int val) {
        this.status = 0;
        switch (reg) {
            case 0x17:
                break; // apparently "mode register"
            case 0x12:
                this.curTrack[0] = val;
                break;
            case 0x1a:
                this.curTrack[1] = val;
                break;
            case 0x23:
                this.drvout = val;
                break;
            default:
                this.result = this.status = 0x18;
                break;
        }
    }

    private void readSpecial(int reg) {
        this.status = 0x10;
        this.result = 0;
        switch (reg) {
            case 0x06:
                break;
            case 0x12:
                this.result = this.curTrack[0];
                break;
            case 0x1a:
                this.result = this.curTrack[1];
                break;
            case 0x23:
                this.result = this.drvout;
                break;
            default:
                this.result = this.status = 0x18;
                break;
        }
    }


    private static final int DiscTimeSlice = 16 * 16;

    private void spinup() {
        int time = DiscTimeSlice;

//        if (!this.motorOn[this.curDrive]) {
//            // Half a second.
//            time = (0.5 * this.cpu.peripheralCyclesPerSecond) | 0;
//            this.motorOn[this.curDrive] = true;
//            this.noise.spinUp();
//        }
//

        //int time = 2_000_000 / 2;
        scheduleCallback(time);
        //this.callbackTask.reschedule(time);
        //this.motorSpinDownTask[this.curDrive].cancel();
        this.phase = 0;
    }

    private final long TICK_NANOS = 500L;

    private ScheduledFuture callbackTask;

    private void scheduleCallback(final int ticks) {
        if (callbackTask != null) {
            callbackTask.cancel(true);
        }
        callbackTask = scheduler.schedule(this::callback, ticks * TICK_NANOS, TimeUnit.NANOSECONDS);
    }

    private void setspindown() {
//        if (this.motorOn[this.curDrive]) {
//            this.motorSpinDownTask[this.curDrive].reschedule(this.cpu.peripheralCyclesPerSecond * 2);
//        }
    }



    private void seek(int track) {
        int realTrack = this.realTrack[this.curDrive];
        realTrack += (track - this.curTrack[this.curDrive]);
        if (realTrack < 0)
            realTrack = 0;
        if (realTrack > 79) {
            realTrack = 79;
        }
        this.realTrack[this.curDrive] = realTrack;
        final int diff = this.drives[this.curDrive].seek(realTrack);
        // Let disc noises overlap by ~10%
        //final int seekLen = (this.noise.seek(diff) * 0.9 * this.cpu.peripheralCyclesPerSecond) | 0;
        scheduleCallback(10_000);
        //this.callbackTask.reschedule(Math.max(DiscTimeSlice, seekLen));
        this.phase = 1;
    }

    private void prepareSectorIO(int track, int sector, int numSectors) {
        if (numSectors != UNDEFINED_INT) this.sectorsLeft = numSectors & 31;
        if (sector != UNDEFINED_INT) this.curSector = sector;
        this.spinup(); // State: spinup -> seek.
    }

    private int[] params;

    private void parameter(int val) {
        if (this.paramNum < 5) this.params[this.paramNum++] = val;
        if (this.paramNum != this.paramReq) return;
        switch (this.curCommand) {
            case 0x35: // Specify.
                this.status = 0;
                break;
            case 0x29: // Seek
                this.spinup(); // State: spinup -> seek.
                break;
            case 0x1f: // Verify
            case 0x13: // Read
            case 0x0b: // Write
                this.prepareSectorIO(this.params[0], this.params[1], this.params[2]);
                break;
            case 0x1b: // Read ID
                this.prepareSectorIO(this.params[0], UNDEFINED_INT, this.params[2]);
                break;
            case 0x23: // Format
                this.prepareSectorIO(this.params[0], UNDEFINED_INT, UNDEFINED_INT);
                break;
            case 0x3a: // Special register write
                this.writeSpecial(this.params[0], this.params[1]);
                break;
            case 0x3d: // Special register read
                this.readSpecial(this.params[0]);
                break;
            default:
                this.result = 0x18;
                this.status = 0x18;
                break;
        }
    }

    private void data(int val) {
        this.curData = val;
        this.written = true;
        this.status &= ~0x0c;
    }

    private int numParams(int command) {
        return PARAMS_MAP.getOrDefault(command, 0);
    }

    private boolean density() {
        return (this.drvout & 0x20) == 0;
    }

    private void update(int status) {
        this.status = status;
        this.result = 0;
    }

    private void done() {
        this.update(0x18);
        this.setspindown();
        this.verify = false;
    }

    private void discFinishRead() {
        scheduleCallback(DiscTimeSlice);
        //this.callbackTask.reschedule(DiscTimeSlice);
    }

    private int curSector;
    private int sectorsLeft;

    private void callback() {
        if (this.phase == 0) {
            // Spinup complete.
            this.seek(this.params[0]);
            return;
        }

        switch (this.curCommand) {
            case 0x29: // Seek
                this.curTrack[this.curDrive] = this.params[0];
                this.done();
                break;

            case 0x0b: // Write
                if (this.phase == 1) {
                    this.curTrack[this.curDrive] = this.params[0];
                    this.phase = 2;
                    this.drives[this.curDrive].write(this.curSector, this.params[0], this.density(), 0);
                    this.update(0x8c);
                    return;
                }
                if (--this.sectorsLeft == 0) {
                    this.done();
                    return;
                }
                this.curSector++;
                this.drives[this.curDrive].write(this.curSector, this.params[0], this.density(), 0);
                this.update(0x8c);
                break;

            case 0x13: // Read
            case 0x1f: // Verify
                if (this.phase == 1) {
                    this.curTrack[this.curDrive] = this.params[0];
                    this.phase = 2;
                    this.drives[this.curDrive].read(this.curSector, this.params[0], this.density(), 0);
                    return;
                }
                if (--this.sectorsLeft == 0) {
                    this.done();
                    return;
                }
                this.curSector++;
                this.drives[this.curDrive].read(this.curSector, this.params[0], this.density(), 0);
                break;

            case 0x1b: // Read ID
                if (this.phase == 1) {
                    this.curTrack[this.curDrive] = this.params[0];
                    this.phase = 2;
                    this.drives[this.curDrive].address(this.params[0], this.density(), 0);
                    return;
                }
                if (--this.sectorsLeft == 0) {
                    this.done();
                    return;
                }
                this.drives[this.curDrive].address(this.params[0], this.density(), 0);
                break;

            case 0x23: // Format
                switch (this.phase) {
                    case 1:
                        this.curTrack[this.curDrive] = this.params[0];
                        this.drives[this.curDrive].write(this.curSector, this.params[0], this.density(), 0);
                        this.update(0x8c);
                        this.phase = 2;
                        break;
                    case 2:
                        this.drives[this.curDrive].format(this.params[0], this.density(), 0);
                        this.phase = 3;
                        break;
                    case 3:
                        this.done();
                        break;
                }
                break;

            case 0xff:
                break;
            default:
                Util.log("ERK bad command: " + Util.formatHexByte(this.curCommand), 0);
                break;
        }
    }
}
