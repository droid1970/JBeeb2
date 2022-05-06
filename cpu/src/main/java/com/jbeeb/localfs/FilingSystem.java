package com.jbeeb.localfs;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.CpuUtil;
import com.jbeeb.memory.AtomicFetchIntercept;
import com.jbeeb.memory.Memory;
import com.jbeeb.memory.ReadOnlyMemory;
import com.jbeeb.util.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public abstract class FilingSystem extends ReadOnlyMemory  {

    private static final int OSFILE_VECTOR =    0x212;
    private static final int OSARGS_VECTOR =    0x214;
    private static final int OSBGET_VECTOR =    0x216;
    private static final int OSBPUT_VECTOR =    0x218;
    private static final int OSGBPB_VECTOR =    0x21A;
    private static final int OSFIND_VECTOR =    0x21C;
    private static final int OSFSC_VECTOR =     0x21E;

    private static final int SERVICE_ENTRY =    0x9000;
    private static final int OSFILE_ENTRY =     0x9002;
    private static final int OSARGS_ENTRY =     0x9004;
    private static final int OSBGET_ENTRY =     0x9006;
    private static final int OSBPUT_ENTRY =     0x9008;
    private static final int OSGBPB_ENTRY =     0x900A;
    private static final int OSFIND_ENTRY =     0x900C;
    private static final int OSFSC_ENTRY =      0x900E;

    private final Map<String, CommandHandler> osfscCommandHandlers = new HashMap<>();

    public FilingSystem(final String name, final String copyright) {
        super(0x8000, createRomImage(name, copyright));
    }

    @FunctionalInterface
    protected interface CommandHandler {
        void run(final String[] args);
    }

    protected void runCommandHandler(final String command, final String[] args, final Runnable notFound) {
        if (osfscCommandHandlers.containsKey(command)) {
            osfscCommandHandlers.get(command).run(args);
        } else {
            notFound.run();
        }
    }

    private static int[] createRomImage(final String name, final String copyright) {
        ByteBuffer buf = ByteBuffer.allocate(1000);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // Language entry
        buf.put((byte) 0x4c);
        buf.put((byte) 0);
        buf.put((byte) 0);

        // Service entry
        buf.put((byte) 0x4c);
        buf.put((byte) (SERVICE_ENTRY & 0xFF));
        buf.put((byte) ((SERVICE_ENTRY >>> 8) & 0xFF));

        buf.put((byte) 0x81); // rom type
        buf.put((byte) (9 + name.length())); // copyright offset
        buf.put((byte) 1);

        buf.put(Util.stringToBytes(name));
        buf.put((byte) 0);

        buf.put(Util.stringToBytes(copyright));
        buf.put((byte) 0);

        return Util.toIntArray(buf.array(), 0x4000);
    }

    public final void initialiseFilesystem(final Cpu cpu, final Memory memory) {
        CpuUtil.osfsc(memory, cpu, 6);
        writeVectors(memory, cpu, cpu.getX());
        CpuUtil.osbyte(cpu, 0x8F, 0xF, 0x0);
    }

    public final void initialise(final Cpu cpu, final Memory memory) {
        installIntercept(cpu, SERVICE_ENTRY, () -> serviceRoutine(cpu, memory));
        installIntercept(cpu, OSFILE_ENTRY, () -> osfile(cpu, memory));
        installIntercept(cpu, OSARGS_ENTRY, () -> osargs(cpu, memory));
        installIntercept(cpu, OSBGET_ENTRY, () -> osbget(cpu, memory));
        installIntercept(cpu, OSBPUT_ENTRY, () -> osbput(cpu, memory));
        installIntercept(cpu, OSGBPB_ENTRY, () -> osgbpb(cpu, memory));
        installIntercept(cpu, OSFIND_ENTRY, () -> osfind(cpu, memory));
        installIntercept(cpu, OSFSC_ENTRY, () -> osfsc(cpu, memory));

        initialiseCommandHandlers(cpu, memory, osfscCommandHandlers);
    }

    protected abstract void initialiseCommandHandlers(final Cpu cpu, final Memory memory, final Map<String, CommandHandler> handlers);

    private void installIntercept(final Cpu cpu, final int address, final Runnable runnable) {
        installIntercept(address, new AtomicFetchIntercept(cpu, runnable), true);
    }

    private void writeVectors(final Memory memory, final Cpu cpu, final int romNumber) {
        CpuUtil.writeExtendedVector(memory, cpu, OSFILE_VECTOR, OSFILE_ENTRY, romNumber);
        CpuUtil.writeExtendedVector(memory, cpu, OSARGS_VECTOR, OSARGS_ENTRY, romNumber);
        CpuUtil.writeExtendedVector(memory, cpu, OSBGET_VECTOR, OSBGET_ENTRY, romNumber);
        CpuUtil.writeExtendedVector(memory, cpu, OSBPUT_VECTOR, OSBPUT_ENTRY, romNumber);
        CpuUtil.writeExtendedVector(memory, cpu, OSGBPB_VECTOR, OSGBPB_ENTRY, romNumber);
        CpuUtil.writeExtendedVector(memory, cpu, OSFIND_VECTOR, OSFIND_ENTRY, romNumber);
        CpuUtil.writeExtendedVector(memory, cpu, OSFSC_VECTOR, OSFSC_ENTRY, romNumber);
    }


    protected abstract void serviceRoutine(Cpu cpu, Memory memory);

    //
    // OSFILE
    //
    protected final void osfile(Cpu cpu, Memory memory) {
        final OsFileParameters parms = new OsFileParameters(memory, cpu.getX(), cpu.getY());
        osfileImpl(cpu, memory, parms);
    };

    protected abstract void osfileImpl(Cpu cpu, Memory memory, OsFileParameters parms);

    protected abstract void osargs(Cpu cpu, Memory memory);
    protected abstract void osbget(Cpu cpu, Memory memory);
    protected abstract void osbput(Cpu cpu, Memory memory);
    protected abstract void osgbpb(Cpu cpu, Memory memory);
    protected abstract void osfind(Cpu cpu, Memory memory);
    protected abstract void osfsc(Cpu cpu, Memory memory);
}
