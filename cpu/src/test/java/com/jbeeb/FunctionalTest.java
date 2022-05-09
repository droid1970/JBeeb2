package com.jbeeb;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.memory.AbstractMemory;
import com.jbeeb.memory.MemoryUtils;
import com.jbeeb.memory.RandomAccessMemory;
import com.jbeeb.util.DefaultScheduler;
import com.jbeeb.util.SystemStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FunctionalTest {

    @Test
    void functionalTests() throws Exception {
        final AbstractMemory memory = new RandomAccessMemory(0, 65536);
        MemoryUtils.loadS19(memory, getClass().getResourceAsStream("/6502_functional_test.s19"), 0);
        memory.addModifyWatch(0x200, v -> {});
        final Cpu cpu = new Cpu(new SystemStatus(), new DefaultScheduler(), memory);
        cpu.setHaltIfPCLoop(true);
        cpu.setPC(0x400);
        cpu.run();
        assertThat(memory.readByte(0x200)).isEqualTo(0xF0);
    }


}
