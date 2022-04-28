package com.jbeeb;

import com.jbeeb.cpu.AddressMode;
import com.jbeeb.cpu.Instruction;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class AbsoluteModeTests {

    @Test
    void testReadAbsolute() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.LDA,
                AddressMode.ABSOLUTE,
                0x2000,
                0,
                value,
                false
        );
        assertThat(context.getA()).isEqualTo(value);

        context = TestContext.createAbsolute(
                Instruction.LDA,
                AddressMode.ABSOLUTE,
                0x20FF,
                0,
                value,
                false
        );
        assertThat(context.getA()).isEqualTo(value);
    }

    @Test
    void testReadAbsoluteX() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.LDA,
                AddressMode.ABSOLUTE_X,
                0x2000,
                5,
                value,
                false
        );
        AssertionsForClassTypes.assertThat(context.getA()).isEqualTo(value);

        context = TestContext.createAbsolute(
                Instruction.LDA,
                AddressMode.ABSOLUTE_X,
                0x20FE,
                5,
                value,
                false
        );
        AssertionsForClassTypes.assertThat(context.getA()).isEqualTo(value);
    }

    @Test
    void testReadAbsoluteY() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.LDA,
                AddressMode.ABSOLUTE_Y,
                0x2000,
                5,
                value,
                false
        );
        AssertionsForClassTypes.assertThat(context.getA()).isEqualTo(value);

        context = TestContext.createAbsolute(
                Instruction.LDA,
                AddressMode.ABSOLUTE_Y,
                0x20FE,
                5,
                value,
                false
        );
        AssertionsForClassTypes.assertThat(context.getA()).isEqualTo(value);
    }

    @Test
    void testWriteAbsolute() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.STA,
                AddressMode.ABSOLUTE,
                0x2000,
                0,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2000)).isEqualTo(value);

        context = TestContext.createAbsolute(
                Instruction.STA,
                AddressMode.ABSOLUTE,
                0x20FF,
                0,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x20FF)).isEqualTo(value);
    }

    @Test
    void testWriteAbsoluteX() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.STA,
                AddressMode.ABSOLUTE_X,
                0x2000,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2005)).isEqualTo(value);

        context = TestContext.createAbsolute(
                Instruction.STA,
                AddressMode.ABSOLUTE_X,
                0x20FE,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2103)).isEqualTo(value);
    }

    @Test
    void testWriteAbsoluteY() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.STA,
                AddressMode.ABSOLUTE_Y,
                0x2000,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2005)).isEqualTo(value);

        context = TestContext.createAbsolute(
                Instruction.STA,
                AddressMode.ABSOLUTE_Y,
                0x20FE,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2103)).isEqualTo(value);
    }

    @Test
    void testReadModifyWriteAbsolute() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.ASL,
                AddressMode.ABSOLUTE,
                0x2000,
                0,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2000)).isEqualTo(value << 1);

        context = TestContext.createAbsolute(
                Instruction.ASL,
                AddressMode.ABSOLUTE,
                0x20FF,
                0,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x20FF)).isEqualTo(value << 1);
    }

    @Test
    void testReadModifyWriteAbsoluteX() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.ASL,
                AddressMode.ABSOLUTE_X,
                0x2000,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2005)).isEqualTo(value << 1);

        context = TestContext.createAbsolute(
                Instruction.ASL,
                AddressMode.ABSOLUTE_X,
                0x20FE,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2103)).isEqualTo(value << 1);
    }

    @Test
    void testReadModifyWriteAbsoluteY() {
        final int value = 42;
        TestContext context = TestContext.createAbsolute(
                Instruction.ROR,
                AddressMode.ABSOLUTE_Y,
                0x2000,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2005)).isEqualTo(value >>> 1);

        context = TestContext.createAbsolute(
                Instruction.ROR,
                AddressMode.ABSOLUTE_Y,
                0x20FE,
                5,
                value,
                false
        );
        assertThat(context.getMemory().readByte(0x2103)).isEqualTo(value >>> 1);
    }
}
