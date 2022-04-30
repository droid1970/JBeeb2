package com.jbeeb.main;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.memory.Memory;
import com.jbeeb.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class BBCFile {

    private final String name;
    private final int loadAddress;
    private final int execAddress;

    public BBCFile(final String name, final int loadAddress, final  int execAddress) {
        this.name = Objects.requireNonNull(name);
        this.loadAddress = loadAddress;
        this.execAddress = execAddress;
    }

    public void load(final Cpu cpu, final Memory memory) throws IOException {
        final File file = new File(JavaBeeb.FILES, name);
        if (file.exists()) {
            Util.load(cpu, memory, file, loadAddress);
        }
    }
    public void run(final Cpu cpu, final Memory memory) throws IOException {
        final File file = new File(JavaBeeb.FILES, name);
        if (file.exists()) {
            Util.run(cpu, memory, file, loadAddress, execAddress);
        }
    }
}
