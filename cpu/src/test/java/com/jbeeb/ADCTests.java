package com.jbeeb;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.util.Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ADCTests {
    @Test
    void testImmediate() {
        final Cpu cpu = Util.runCpu(0x1000, "LDA #10", "ADC #12");
        assertThat(cpu.getA()).isEqualTo(22);
    }

    @Test
    void testZeroPage() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #10",
                "STA $00",
                "LDA #20",
                "ADC $00"
        );
        assertThat(cpu.getA()).isEqualTo(30);
    }

    @Test
    void testZeroPageX() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #10",
                "STA 5",
                "LDA #20",
                "LDX #5",
                "ADC $00,X"
        );
        assertThat(cpu.getA()).isEqualTo(30);
    }

    @Test
    void testAbsolute() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #10",
                "STA $2000",
                "LDA #20",
                "ADC $2000"
        );
        assertThat(cpu.getA()).isEqualTo(30);
    }

    @Test
    void testAbsoluteX() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #10",
                "STA $2005",
                "LDX #5",
                "LDA #20",
                "ADC $2000,X"
        );
        assertThat(cpu.getA()).isEqualTo(30);
    }

    @Test
    void testAbsoluteY() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #10",
                "STA $2005",
                "LDY #5",
                "LDA #20",
                "ADC $2000,Y"
        );
        assertThat(cpu.getA()).isEqualTo(30);
        cpu.assertA(30);
    }

    @Test
    void testIndirectX() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #$01",
                "STA 10",
                "LDA #$20",
                "STA 11",
                "LDA #12",
                "STA $2001",
                "LDX #10",
                "LDA #20",
                "ADC (0,X)"
        );
        assertThat(cpu.getA()).isEqualTo(32);
    }

    @Test
    void testIndirectY() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #$01",
                "STA 10",
                "LDA #$20",
                "STA 11",
                "LDA #12",
                "STA $2005",
                "LDY #4",
                "LDA #20",
                "ADC (10),Y"
        );
        assertThat(cpu.getA()).isEqualTo(32);
    }

    @Test
    void testIndirectYCrossBoundary() {
        final Cpu cpu = Util.runCpu(0x1000,
                "LDA #$FF",
                "STA 10",
                "LDA #$20",
                "STA 11",
                "LDA #12",
                "STA $2103",
                "LDY #4",
                "LDA #20",
                "ADC (10),Y"
        );
        assertThat(cpu.getA()).isEqualTo(32);
    }
}
