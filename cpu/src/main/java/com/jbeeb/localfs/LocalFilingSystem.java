package com.jbeeb.localfs;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.cpu.CpuUtil;
import com.jbeeb.cpu.Flag;
import com.jbeeb.main.JavaBeeb;
import com.jbeeb.memory.Memory;
import com.jbeeb.util.Util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LocalFilingSystem extends FilingSystem {

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

    private LfsElement currentDirectory;

    private final RandomAccessData[] openFiles = new RandomAccessData[256]; // Indexed by file handle

    private int getFreeFileHandle(final Cpu cpu) {
        for (int i = 1; i < openFiles.length; i++) {
            if (openFiles[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public LocalFilingSystem(final String name, final String copyright) {
        super(name, copyright);
        try {
            currentDirectory = LocalFileElement.of(new File(JavaBeeb.FILES, "images"));
        } catch (Exception ex) {
            ex.printStackTrace();
            currentDirectory = LocalFileElement.of(JavaBeeb.FILES);
        }
    }

    @Override
    protected void initialiseCommandHandlers(Cpu cpu, Memory memory, final Map<String, FilingSystem.CommandHandler> handlers) {
        handlers.put(LS_COMMAND_NAME, (args) -> listFiles(cpu));
        handlers.put(DIR_COMMAND_NAME, (args) -> listFiles(cpu));
        handlers.put(CD_COMMAND_NAME, (args) -> changeDirectory(cpu, memory, args));
        handlers.put(UP_COMMAND_NAME, (args) -> changeDirectory(cpu, memory, new String[]{CD_COMMAND_NAME, ".."}));
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
            CpuUtil.println(cpu, Util.padRight(e.getName(), 16) + " - " + e.getType().getDescription());
        }
        dirs.sort(Comparator.comparing(LfsElement::getName));
        files.sort(Comparator.comparing(LfsElement::getName));

        if (!dirs.isEmpty()) {
            CpuUtil.osnewl(cpu);
        }

        for (LfsElement e : files) {
            CpuUtil.println(cpu, Util.padRight(e.getName(), 16) + " - " + formatFileSize(e.length(), 1000));
        }
    }

    private static String[] toArgs(final String command) {
        return command.trim().split(" ");
    }

    private void changeDirectory(final Cpu cpu, final Memory memory, final String[] args) {
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

    protected void serviceRoutine(final Cpu cpu, final Memory memory) {
        switch (cpu.getA()) {
            case 0: // NOP
            case 1: // Absolute workspace claim
            case 2: // Private workspace claim
                return;

            case 3: {
                // Auto-boot
                final boolean autoBoot = (cpu.getY() == 0); // Not implemented yet
                initialiseFilesystem(cpu, memory);
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

    private void fileNotFound(final Cpu cpu, final String filename) {
        CpuUtil.newlineMessage(cpu, "File not found - " + filename);
    }

    @Override
    protected void osfileImpl(final Cpu cpu, final Memory memory, final OsFileParameters parms) {
        try {
            System.err.println("OSFILE: A = " + cpu.getA() + " parms = " + parms);
            final LfsElement file = findFile(parms.getFileName());
            if (file == null) {
                fileNotFound(cpu, parms.getFileName());
                return;
            }
            final int fileLoadAddress = file.getLoadAddress();
            final int fileExecAddress = file.getExecAddress();

            switch (cpu.getA()) {
                case 255: {
                    final int effectiveLoadAddress = (((parms.getExecAddress() & 0xFF) == 0) ? parms.getLoadAddress() : fileLoadAddress) & 0xFFFF;
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
            memory.writeByte(loadAddress + i, (fileData.read() & 0xFF));
        }
    }

    private void loadAndRunFile(final Cpu cpu, final Memory memory, final LfsElement file, final int loadAddress, final int execAddress) throws IOException {
        load(memory, file, loadAddress);
        cpu.setPC(execAddress & 0xFFFF);
    }

    @Override
    protected void osargs(final Cpu cpu, final Memory memory) {
        System.err.println("OSARGS");
    }

    @Override
    protected void osbget(final Cpu cpu, final Memory memory) {
        final int handle = cpu.getY();
        final RandomAccessData file = (handle >= 0 && handle <= 255) ? openFiles[handle] : null;
        if (file == null) {
            cpu.setA(0, true);
            cpu.setFlag(Flag.CARRY, true);
            CpuUtil.newlineMessage(cpu, "Bad file handle - " + handle);
            return;
        }

        if (file.isEOF()) {
            cpu.setA(0, true);
            cpu.setFlag(Flag.CARRY, true);
            return;
        }

        try {
            cpu.setA(file.read(), true);
            cpu.setFlag(Flag.CARRY, false);
        } catch (Exception ex) {
            CpuUtil.newlineMessage(cpu, "Exception - " + ex.getMessage());
            cpu.setA(0, true);
            cpu.setFlag(Flag.CARRY, true);
        }
    }

    @Override
    protected void osbput(final Cpu cpu, final Memory memory) {
        int x = 1;
        System.err.println("OSBPUT");
    }

    @Override
    protected void osgbpb(final Cpu cpu, final Memory memory) {
        System.err.println("OSGBPB");
    }

    @Override
    protected void osfind(final Cpu cpu, final Memory memory) {
        if (cpu.getA() == 0) {
            // Close a file
            final int handle = cpu.getY();
            if (openFiles[handle] != null) {
                openFiles[handle].close();
            }
        } else {
            final String fileName = CpuUtil.readStringAbsolute(memory, (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8));
            final LfsElement element = findFile(fileName);
            if (element == null) {
                CpuUtil.newlineMessage(cpu, "File not found - " + fileName);
                cpu.setA(0, true);
                return;
            }
            System.err.println("OSFIND: fileName = " + fileName + " action = " + cpu.getA());
            final int handle = getFreeFileHandle(cpu);
            if (handle < 0) {
                CpuUtil.newlineMessage(cpu, "Too many open files");
                cpu.setA(0, true);
                return;
            }

            try {
                openFiles[handle] = element.getData();
            } catch (Exception ex) {
                CpuUtil.newlineMessage(cpu, "Exception - " + ex.getMessage());
                cpu.setA(0, true);
                return;
            }

            switch (cpu.getA()) {
                case 0x40: {
                    cpu.setA(handle, true);
                    break;
                }
                case 0x80: {
                    // Output only
                    cpu.setA(handle, true);
                    break;
                }
                case 0xc0: {
                    // Random access
                    cpu.setA(handle, true);
                    break;
                }
            }
        }
    }

    @Override
    protected void osfsc(final Cpu cpu, final Memory memory) {
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
                runCommandHandler(command, args, () -> CpuUtil.badCommand(cpu));
                break;
            }
            case 2:
            case 4:
                // *RUN
                final String fileName = CpuUtil.readStringAbsolute(memory, (cpu.getX() & 0xFF) | ((cpu.getY() & 0xFF) << 8));
                final LfsElement file = findFile(fileName);
                if (file == null) {
                    fileNotFound(cpu, fileName);
                    return;
                } else {
                    try {
                        loadAndRunFile(cpu, memory, file, file.getLoadAddress() & 0xFFFF, file.getExecAddress() & 0xFFFF);
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

