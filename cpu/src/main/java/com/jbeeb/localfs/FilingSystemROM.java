package com.jbeeb.localfs;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.CpuUtil;
import com.jbeeb.main.JavaBeeb;
import com.jbeeb.memory.AtomicFetchIntercept;
import com.jbeeb.memory.Memory;
import com.jbeeb.memory.ReadOnlyMemory;
import com.jbeeb.util.Util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class FilingSystemROM extends ReadOnlyMemory {

    private static final String CD_COMMAND_NAME = "CD";
    private static final String UP_COMMAND_NAME = "UP";
    private static final String DIR_COMMAND_NAME = "DIR";
    private static final String LS_COMMAND_NAME = "LS";

    private static final Set<String> SELECTION_COMMANDS = new HashSet<>(Arrays.asList(
            "LOCALFS",
            "LFS",
            "DISC",
            "DISK"
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

    private final Map<String, CommandHandler> osfscCommandHandlers = new HashMap<>();

    @FunctionalInterface
    private interface CommandHandler {
        void run(final String[] args);
    }

    private LfsElement currentDirectory;

    public FilingSystemROM(final String name, final String copyright) {
        super(0x8000, createData(name, copyright));
        try {
            currentDirectory = LocalFileElement.of(new File(JavaBeeb.FILES, "images"));
        } catch (Exception ex) {
            ex.printStackTrace();
            currentDirectory = LocalFileElement.of(JavaBeeb.FILES);
        }
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

        buf.put(Util.stringToBytes(name));
        buf.put((byte) 0);

        buf.put(Util.stringToBytes(copyright));
        buf.put((byte) 0);

        return Util.toIntArray(buf.array(), 0x4000);
    }

    public void initialise(final Memory memory, final Cpu cpu) {
        installIntercept(cpu, SERVICE_ENTRY, () -> serviceRoutine(memory, cpu));
        installIntercept(cpu, OSFILE_ENTRY, () -> osfile(memory, cpu));
        installIntercept(cpu, OSARGS_ENTRY, () -> osargs(memory, cpu));
        installIntercept(cpu, OSBGET_ENTRY, () -> osbget(memory, cpu));
        installIntercept(cpu, OSBPUT_ENTRY, () -> osbput(memory, cpu));
        installIntercept(cpu, OSGBPB_ENTRY, () -> osgbpb(memory, cpu));
        installIntercept(cpu, OSFIND_ENTRY, () -> osfind(memory, cpu));
        installIntercept(cpu, OSFSC_ENTRY, () -> osfsc(memory, cpu));

        osfscCommandHandlers.put(LS_COMMAND_NAME, (args) -> listFiles(cpu));
        osfscCommandHandlers.put(DIR_COMMAND_NAME, (args) -> listFiles(cpu));
        osfscCommandHandlers.put(CD_COMMAND_NAME, (args) -> changeDirectory(memory, cpu, args));
        osfscCommandHandlers.put(UP_COMMAND_NAME, (args) -> changeDirectory(memory, cpu, new String[]{CD_COMMAND_NAME, ".."}));
    }

    private void installIntercept(final Cpu cpu, final int address, final Runnable runnable) {
        installIntercept(address, new AtomicFetchIntercept(cpu, runnable), true);
    }

    private void initialiseFilesystem(final Memory memory, final Cpu cpu) {
        CpuUtil.osfsc(memory, cpu, 6);
        writeVectors(memory, cpu, cpu.getX());
        CpuUtil.osbyte(cpu, 0x8F, 0xF, 0x0);
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

    private static final NumberFormat SIZE_FORMAT = new DecimalFormat("#,###");

    private static String formatFileSize(final int fileSize, final int kbSize) {
        if (fileSize >= kbSize) {
            int kb = (int) Math.round((double) fileSize / kbSize);
            return Util.padLeft(SIZE_FORMAT.format(kb), 10) + " Kb";
        } else {
            return Util.padLeft(SIZE_FORMAT.format(fileSize), 10) + " bytes";
        }
    }

    private void listFiles(final Cpu cpu) {
        final List<? extends LfsElement> list = currentDirectory.list();
        final List<? extends LfsElement> dirs = list.stream().filter(LfsElement::isDirectory).collect(Collectors.toList());
        final List<? extends LfsElement> files = list.stream().filter(LfsElement::isFile).collect(Collectors.toList());
        for (LfsElement e : dirs) {
            CpuUtil.println(cpu, Util.pad(e.getName(), 16) + " - " + e.getType().getDescription());
        }
        dirs.sort(Comparator.comparing(LfsElement::getName));
        files.sort(Comparator.comparing(LfsElement::getName));

        if (!dirs.isEmpty()) {
            CpuUtil.osnewl(cpu);
        }

        for (LfsElement e : files) {
            CpuUtil.println(cpu, Util.pad(e.getName(), 16) + " - " + formatFileSize(e.length(), 1000));
        }
    }

    private static String[] toArgs(final String command) {
        return command.trim().split(" ");
    }

    private void changeDirectory(final Memory memory, final Cpu cpu, final String[] args) {
        if (args.length == 0 || !Objects.equals(args[0], CD_COMMAND_NAME)) {
            CpuUtil.newlineMessage(cpu, "Bad CD arguments");
            return;
        }
        if (args.length == 1) {
            // Print current directory name
            CpuUtil.message(cpu, currentDirectory.getName());
            return;
        }

        if (args.length != 2) {
            CpuUtil.newlineMessage(cpu, "Too many arguments");
            return;
        }

        final String dirName = args[1];
        if (Objects.equals(dirName, "..")) {
            changeToParent(cpu);
            return;
        }

        final Optional<? extends LfsElement> optDir = currentDirectory.findAny(dirName);
        if (optDir.isEmpty()) {
            CpuUtil.newlineMessage(cpu, "Directory not found");
            return;
        }

        final LfsElement dir = optDir.get();
        if (!dir.isDirectory()) {
            CpuUtil.newlineMessage(cpu, "Not a directory");
            return;
        }
        this.currentDirectory = dir;
    }

    private void changeToParent(final Cpu cpu) {
        final Optional<? extends LfsElement> parent = currentDirectory.getParent();
        if (parent.isEmpty()) {
            CpuUtil.newlineMessage(cpu, "Cannot change to parent directory");
            return;
        }
        this.currentDirectory = parent.get();
    }

    private void serviceRoutine(final Memory memory, final Cpu cpu) {
        //System.err.println("service routine: A = " + cpu.getA() + " X = " + cpu.getX() + " Y = " + cpu.getY());
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
                final String command = CpuUtil.readStringIndirect(memory, 0xF2, cpu.getY()).toUpperCase();
                if (SELECTION_COMMANDS.contains(command)) {
                    // This filing system selected
                    CpuUtil.println(cpu, "Local filesystem selected");
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
                final String command = CpuUtil.readStringIndirect(memory, 0xF2, cpu.getY());
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

    private static final class osfileParameters {
        final String fileName;
        final int loadAddress;
        final int execAddress;
        final int saveStartAddress;
        final int saveEndAddress;

        osfileParameters(final Memory memory, final int x, final int y) {
            final int address = (x & 0xFF) | ((y & 0xFF) << 8);
            fileName = CpuUtil.readStringAbsolute(memory, memory.readWord(address));
            loadAddress = memory.readWord(address + 2) | (memory.readWord(address + 4) << 16);
            execAddress = memory.readWord(address + 6) | (memory.readWord(address + 8) << 16);
            saveStartAddress = memory.readWord(address + 10) | (memory.readWord(address + 12) << 16);
            saveEndAddress = memory.readWord(address + 14) | (memory.readWord(address + 16) << 16);
        }

        public String toString() {
            return "filename = " + fileName + " load = " + Util.formatHexWord(loadAddress & 0xFFFF) + " exec = " + Util.formatHexWord(execAddress & 0xFFFF);
        }
    }

    private void fileNotFound(final Cpu cpu, final String filename) {
        CpuUtil.newlineMessage(cpu, "File not found - " + filename);
    }

    private void osfile(final Memory memory, final Cpu cpu) {
        try {
            final osfileParameters parms = new osfileParameters(memory, cpu.getX(), cpu.getY());
            System.err.println("OSFILE: A = " + cpu.getA() + " parms = " + parms);
            String effectiveFilename = parms.fileName;
            if (effectiveFilename.contains(" ")) {
                effectiveFilename = effectiveFilename.substring(0, effectiveFilename.indexOf(" "));
            }
            final LfsElement file = findFile(parms.fileName);
            if (file == null) {
                fileNotFound(cpu, parms.fileName);
                return;
            }
            final int fileLoadAddress = file.getLoadAddress();
            final int fileExecAddress = file.getExecAddress();

            switch (cpu.getA()) {
                case 255: {
                    final int effectiveLoadAddress = (((parms.execAddress & 0xFF) == 0) ? parms.loadAddress : fileLoadAddress) & 0xFFFF;
                    load(memory, file, effectiveLoadAddress);
                    break;
                }

                default:
                    // Not implemented/supported
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            CpuUtil.newlineMessage(cpu, "error - " + ex.getMessage());
        }
    }

    private LfsElement findFile(final String fileName) {
        return currentDirectory.findAny(fileName.replace("\"", "")).orElse(null);
    }

    private void load(final Memory memory, final LfsElement file, final int loadAddress) throws IOException {
        final RandomAccessData fileData = file.getData();
        for (int i = 0; i < fileData.length(); i++) {
            memory.writeByte(loadAddress + i, (fileData.get() & 0xFF));
        }
    }

    private void run(final Memory memory, final Cpu cpu, final LfsElement file, final int loadAddress, final int execAddress) throws IOException {
        load(memory, file, loadAddress);
        cpu.setPC(execAddress & 0xFFFF);
    }

    private void osargs(final Memory memory, final Cpu cpu) {
        System.err.println("OSARGS");
    }

    private void osbget(final Memory memory, final Cpu cpu) {
        System.err.println("OSBGET");
    }

    private void osbput(final Memory memory, final Cpu cpu) {
        System.err.println("OSBPUT");
    }

    private void osgbpb(final Memory memory, final Cpu cpu) {
        System.err.println("OSGBPB");
    }

    private void osfind(final Memory memory, final Cpu cpu) {
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

            case 3: {
                // Unrecognised command
                final String commandLine = CpuUtil.readStringAbsolute(memory, (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8));
                final String[] args = toArgs(commandLine);
                final String command = args[0].toUpperCase();
                if (osfscCommandHandlers.containsKey(command)) {
                    osfscCommandHandlers.get(command).run(args);
                } else {
                    CpuUtil.badCommand(cpu);
                }
                break;
            }
            case 2:
            case 4:
                // *RUN
                final String fileName = CpuUtil.readStringAbsolute(memory, (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8));
                final LfsElement file = findFile(fileName);
                if (fileName.toLowerCase().equals("d.repton2")) {
                    int x = 1;
                }
                if (file == null) {
                    fileNotFound(cpu, fileName);
                    return;
                } else {
                    try {
                        run(memory, cpu, file, file.getLoadAddress() & 0xFFFF, file.getExecAddress() & 0xFFFF);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        CpuUtil.newlineMessage(cpu, "error - " + ex.getMessage());
                    }
                }
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
}

