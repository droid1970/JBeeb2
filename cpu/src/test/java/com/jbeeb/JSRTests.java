package com.jbeeb;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.util.Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JSRTests {

    @Test
    void testImmediate() {
        final Cpu cpu = Util.runCpu(0x1000,
                "JSR $1006",
                "LDA #42",
                "HLT",
                "LDX #13",
                "LDY #21",
                "RTS"
        );
        assertThat(cpu.getA()).isEqualTo(42);
        assertThat(cpu.getX()).isEqualTo(13);
        assertThat(cpu.getY()).isEqualTo(21);
    }
}
