package com.jbeeb.memory;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.CpuUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class FilingSystemROM extends ReadOnlyMemory {

    private static final Set<String> SELECTION_COMMANDS = new HashSet<>(Arrays.asList(
            "LOCALFS",
            "LFS"
    ));

    private static final List<String> HELP_TEXT = Arrays.asList(
            "*LS / *DIR - list current directory",
            "*CD <DIR>  - change directory",
            "*UP        - move to parent directory"
    );

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

    private Map<String, Runnable> serviceCommandHandlers = new HashMap<>();
    private Map<String, Runnable> osfscCommandHandlers = new HashMap<>();

    public FilingSystemROM(final String name, final String copyright) {
        super(0x8000, createData(name, copyright));
    }

    private static int[] createData(final String name, final String copyright) {
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

        buf.put(stringToBytes(name));
        buf.put((byte) 0);

        buf.put(stringToBytes(copyright));
        buf.put((byte) 0);

        return toIntArray(buf.array());
    }

    private void initialiseFilesystem(final Memory memory, final Cpu cpu) {
        CpuUtil.osfsc(memory, cpu, 6);
        initVectors(memory, cpu, cpu.getX());
        CpuUtil.osbyte(cpu, 0x8F, 0xF, 0x0);
    }

    private void initVectors(final Memory memory, final Cpu cpu, final int romNumber) {
        initVector(memory, cpu, OSFILE_VECTOR, OSFILE_ENTRY, romNumber);
        initVector(memory, cpu, OSARGS_VECTOR, OSARGS_ENTRY, romNumber);
        initVector(memory, cpu, OSBGET_VECTOR, OSBGET_ENTRY, romNumber);
        initVector(memory, cpu, OSBPUT_VECTOR, OSBPUT_ENTRY, romNumber);
        initVector(memory, cpu, OSGBPB_VECTOR, OSGBPB_ENTRY, romNumber);
        initVector(memory, cpu, OSFIND_VECTOR, OSFIND_ENTRY, romNumber);
        initVector(memory, cpu, OSFSC_VECTOR, OSFSC_ENTRY, romNumber);

    }

    public void initialise(final Memory memory, final Cpu cpu) {
        installIntercept(cpu, SERVICE_ENTRY, () -> serviceRoutine(memory, cpu));
        installIntercept(cpu, OSFILE_ENTRY, () -> osfile(cpu));
        installIntercept(cpu, OSARGS_ENTRY, () -> osargs(cpu));
        installIntercept(cpu, OSBGET_ENTRY, () -> osbget(cpu));
        installIntercept(cpu, OSBPUT_ENTRY, () -> osbput(cpu));
        installIntercept(cpu, OSGBPB_ENTRY, () -> osgbpb(cpu));
        installIntercept(cpu, OSFIND_ENTRY, () -> osfind(cpu));
        installIntercept(cpu, OSFSC_ENTRY, () -> osfsc(memory, cpu));

        osfscCommandHandlers.put("LS", () -> listFiles(cpu));
        osfscCommandHandlers.put("DIR", () -> listFiles(cpu));
        osfscCommandHandlers.put("CD", () -> changeDirectory(memory, cpu));
    }

    private File dir = new File(System.getProperty("user.home"), "jbeeb/files");

    private void listFiles(final Cpu cpu) {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                CpuUtil.println(cpu, f.getName() + " - " + f.length());
            }
        }
    }

    private void changeDirectory(final Memory memory, final Cpu cpu) {
        final int addr = (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8);
        final String commandName = readString(memory, addr);
        System.err.println("CD " + commandName);
    }

    private void installIntercept(final Cpu cpu, final int address, final Runnable runnable) {
        installIntercept(address, new AtomicFetchIntercept(cpu, runnable));
    }

    private void serviceRoutine(final Memory memory, final Cpu cpu) {
        System.err.println("service routine: A = " + cpu.getA() + " X = " + cpu.getX() + " Y = " + cpu.getY());
        switch (cpu.getA()) {
            case 0: // NOP
            case 1: // Absolute workspace claim
            case 2: // Private workspace claim
                return;

            case 3: {
                // Auto-boot
                final boolean autoBoot = (cpu.getY() == 0); // Not implemented yet
                initialiseFilesystem(memory, cpu);
                cpu.setA(0, true);
                return;
            }

            case 4: {
                // Unrecognised command
                final int addr = memory.readWord(0xF2) + cpu.getY();
                final String command = readString(memory, addr).toUpperCase();
                if (SELECTION_COMMANDS.contains(command)) {
                    // This filing system selected
                    CpuUtil.println(cpu, "Local filesystem selected");
                    cpu.setA(0, true);
                } else if (serviceCommandHandlers.containsKey(command)) {
                    serviceCommandHandlers.get(command).run();
                    cpu.setA(0, true);
                }
                return;
            }

            case 5: // Unrecognised interrupt
            case 6: // Break
            case 7: // Unrecognised OSBYTE
            case 8: // Unrecognised OSWORD
                return;

            case 9: {
                // *HELP expansion
                final int addr = memory.readWord(0xF2) + cpu.getY();
                final String command = readString(memory, addr);
                if (!command.isEmpty()) {
                    final String[] toks = command.split(" ");
                    if (SELECTION_COMMANDS.contains(toks[0].toUpperCase())) {
                        CpuUtil.println(cpu, "");
                        for (String s : HELP_TEXT) {
                            CpuUtil.println(cpu, s);
                        }
                        cpu.setA(0, true);
                    }
                }
                return;
            }

            case 0x0A: // Claim static workspace
            case 0x0B: // NMI release
            case 0x0C: // NMI claim
            case 0x0D: // ROM filing system inisialise
            case 0x0E: // ROM filing system byte get
            case 0x0F: { // Vectors claimed TODO: (IMPLEMENT THIS)
                return;
            }
            case 0x10: // SPOOL/EXEC file closure warning
            case 0x11: // Font implosion/explosion warning
            case 0x12: // Initilialise filing system
            case 0xFE: // Tube system post initialisation
            case 0xFF: // Tube system main initialisation
                return;
        }

    }

    private void osfile(final Cpu cpu) {
        System.err.println("OSFILE");
    }

    private void osargs(final Cpu cpu) {
        System.err.println("OSARGS");
    }

    private void osbget(final Cpu cpu) {
        System.err.println("OSBGET");
    }

    private void osbput(final Cpu cpu) {
        System.err.println("OSBPUT");
    }

    private void osgbpb(final Cpu cpu) {
        System.err.println("OSGBPB");
    }

    private void osfind(final Cpu cpu) {
        System.err.println("OSFIND");
    }

    private void osfsc(final Memory memory, final Cpu cpu) {
        System.err.println("OSFSC: A = " + cpu.getA() + " X = " + cpu.getX() + " Y = " + cpu.getY());
        switch (cpu.getA()) {
            case 0: {
                System.err.println("OSFSC: *OPT " + cpu.getX() + "," + cpu.getY());
                break;
            }
            case 1: {
                System.err.println("OSFSC: EOF check " + cpu.getX());
                break;
            }
            case 2:
            case 3: {
                // Unrecognised command
                final int addr = (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8);
                final String commandLine = readString(memory, addr);
                final String[] toks = commandLine.split(" ");
                final String command = toks[0].toUpperCase();
                if (osfscCommandHandlers.containsKey(command)) {

                }
                break;
            }
            case 4:
                break;
            case 5:
                System.err.println("OSFSC: *CAT");
                listFiles(cpu);
                break;
            case 6:
            case 7:
            case 8:
        }
    }

    private static String readString(final Memory memory, int address) {
        final StringBuilder s = new StringBuilder();
        int v;
        while ((v = memory.readByte(address++)) != 0xD) {
            s.append((char) v);
        }
        return s.toString();
    }

    private void initVector(final Memory memory, final Cpu cpu, final int vector, final int addr, final int romNumber) {
        final int n = (vector - 0x200) / 2;
        final int a = 0xFF00 + 3 * n;
        memory.writeByte(vector, (a & 0xFF));
        memory.writeByte(vector + 1, ((a >>> 8) & 0xFF));
        CpuUtil.osbyte(cpu, 0xa8, 0x00, 0xFF);
        final int v = (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8);
        final int va = v + 3 * n;
        memory.writeByte(va, addr & 0xFF);
        memory.writeByte(va + 1, (addr >>> 8) & 0xFF);
        memory.writeByte(va + 2, romNumber & 0xFF);
    }

    private static int[] toIntArray(final byte[] bytes) {
        final int[] ret = new int[0x4000];
        for (int i = 0; i < 0x4000; i++) {
            ret[i] = (i < bytes.length) ? ((int) bytes[i]) & 0xFF : 0;
        }
        return ret;
    }

    private static byte[] stringToBytes(final String s) {
        final byte[] bytes = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= 32 && c <= 127) {
                bytes[i] = (byte) c;
            } else {
                bytes[i] = (byte) 32;
            }
        }
        return bytes;
    }
}
