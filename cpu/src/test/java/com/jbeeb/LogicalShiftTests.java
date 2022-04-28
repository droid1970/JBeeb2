package com.jbeeb;

import com.jbeeb.cpu.AddressMode;
import com.jbeeb.cpu.Flag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LogicalShiftTests {

    private static Stream<Arguments> source_Abs_AbsX() {
        return source(AddressMode.ABSOLUTE, AddressMode.ABSOLUTE_X);
    }

    private static Stream<Arguments> source_Abs_AbsX_AbsY() {
        return source(AddressMode.ABSOLUTE, AddressMode.ABSOLUTE_X, AddressMode.ABSOLUTE_Y);
    }

    private static Stream<Arguments> source(AddressMode... addressModes) {
        final List<Arguments> args = new ArrayList<>();
        for (AddressMode addressMode : Arrays.asList(addressModes)) {
            for (int value = 0; value < 256; value++) {
                for (boolean carryIn : new boolean[]{false, true}) {
                    args.add(Arguments.of(addressMode, value, carryIn));
                }
            }
        }
        return args.stream();
    }

    @Test
    void testASL() {
        final TestContext context = TestContext.create();
        for (int a = 0; a < 256; a++) {
            for (boolean carryIn : new boolean[]{false, true}) {
                context.resetAndAssemble(
                        carryIn ? "SEC" : "CLC",
                        "LDA #" + a,
                        "ASL A"
                );
                context.run();
                final int actualResult = context.getA();
                final boolean actualCarry = context.isFlagSet(Flag.CARRY);
                final int expectedResult = (a << 1) & 0xFF;
                final boolean expectedCarry = (a & 0x80) != 0;
                assertThat(actualResult).isEqualTo(expectedResult);
                assertThat(actualCarry).isEqualTo(expectedCarry);
            }
        }
    }

    @Test
    void testLSR() {
        final TestContext context = TestContext.create();
        for (int a = 0; a < 256; a++) {
            for (boolean carryIn : new boolean[]{false, true}) {
                context.resetAndAssemble(
                        carryIn ? "SEC" : "CLC",
                        "LDA #" + a,
                        "LSR A"
                );
                context.run();
                final int actualResult = context.getA();
                final boolean actualCarry = context.isFlagSet(Flag.CARRY);
                final int expectedResult = (a >>> 1) & 0xFF;
                final boolean expectedCarry = (a & 0x01) != 0;
                assertThat(actualResult).isEqualTo(expectedResult);
                assertThat(actualCarry).isEqualTo(expectedCarry);
            }
        }
    }

    @Test
    void testROL() {
        final TestContext context = TestContext.create();
        for (int a = 0; a < 256; a++) {
            for (boolean carryIn : new boolean[]{false, true}) {
                context.resetAndAssemble(
                        carryIn ? "SEC" : "CLC",
                        "LDA #" + a,
                        "ROL A"
                );
                context.run();
                final int actualResult = context.getA();
                final boolean actualCarry = context.isFlagSet(Flag.CARRY);
                final int expectedResult = ((a << 1) & 0xFF) | (carryIn ? 0x01 : 0x00);
                final boolean expectedCarry = (a & 0x80) != 0;
                assertThat(actualResult).isEqualTo(expectedResult);
                assertThat(actualCarry).isEqualTo(expectedCarry);
            }
        }
    }

    @Test
    void testROR() {
        final TestContext context = TestContext.create();
        for (int a = 0; a < 256; a++) {
            for (boolean carryIn : new boolean[]{false, true}) {
                context.resetAndAssemble(
                        carryIn ? "SEC" : "CLC",
                        "LDA #" + a,
                        "ROR A"
                );
                context.run();
                final int actualResult = context.getA();
                final boolean actualCarry = context.isFlagSet(Flag.CARRY);
                final int expectedResult = ((a >>> 1) & 0xFF) | (carryIn ? 0x80 : 0x00);
                final boolean expectedCarry = (a & 0x01) != 0;
                assertThat(actualResult).isEqualTo(expectedResult);
                assertThat(actualCarry).isEqualTo(expectedCarry);
            }
        }
    }

}
