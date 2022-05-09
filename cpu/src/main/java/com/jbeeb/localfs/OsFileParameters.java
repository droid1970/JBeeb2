package com.jbeeb.localfs;

import com.jbeeb.cpu.CpuUtil;
import com.jbeeb.memory.Memory;
import com.jbeeb.util.Util;

public final class OsFileParameters {

    private final int address;
    private final String fileName;
    private final int loadAddress;
    private final int execAddress;
    private final int saveStartAddress;
    private final int saveEndAddress;

    public OsFileParameters(final Memory memory, final int addressLO, final int addressHI) {
        this.address = (addressLO & 0xFF) | ((addressHI & 0xFF) << 8);
        fileName = CpuUtil.readStringAbsolute(memory, memory.readWord(address));
        loadAddress = memory.readWord(address + 2) | (memory.readWord(address + 4) << 16);
        execAddress = memory.readWord(address + 6) | (memory.readWord(address + 8) << 16);
        saveStartAddress = memory.readWord(address + 10) | (memory.readWord(address + 12) << 16);
        saveEndAddress = memory.readWord(address + 14) | (memory.readWord(address + 16) << 16);
    }

    public String getFileName() {
        return fileName;
    }

    public int getLoadAddress() {
        return loadAddress;
    }

    public int getExecAddress() {
        return execAddress;
    }

    public int getSaveStartAddress() {
        return saveStartAddress;
    }

    public int getSaveEndAddress() {
        return saveEndAddress;
    }

    @Override
    public String toString() {
        return "address =  " + Util.formatHexWord(address) +
                " fileName = " + fileName +
                " loadAddress = " + Util.formatHexWord(loadAddress) +
                " execAddress = " + Util.formatHexWord(execAddress) +
                " saveStartAddress = " + Util.formatHexWord(saveStartAddress) +
                " saveEndAddress = " + Util.formatHexWord(saveEndAddress);
    }
}
