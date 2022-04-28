package com.jbeeb;

import com.jbeeb.cpu.AddressMode;
import com.jbeeb.cpu.Flag;
import com.jbeeb.cpu.Instruction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class AbsoluteModeShiftTests {

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

    @ParameterizedTest
    @MethodSource("source_Abs_AbsX")
    void testAbsoluteASL(final AddressMode addressMode, final int value, final boolean carryIn) {
        final TestContext context = TestContext.createAbsolute(Instruction.ASL, addressMode, 0x20FF, 0, value, carryIn);
        final int result = context.getMemory().readByte(0x20FF);
        context.assertNZCorrect(result);
        OpAssert.assertASL(context, value, carryIn, result, context.isFlagSet(Flag.CARRY));
    }

    @ParameterizedTest
    @MethodSource("source_Abs_AbsX")
    void testAbsoluteLSR(final AddressMode addressMode, final int value, final boolean carryIn) {
        final TestContext context = TestContext.createAbsolute(Instruction.LSR, addressMode, 0x20FF, 0, value, carryIn);
        final int result = context.getMemory().readByte(0x20FF);
        context.assertNZCorrect(result);
        OpAssert.assertLSR(context, value, carryIn, result, context.isFlagSet(Flag.CARRY));
    }

    @ParameterizedTest
    @MethodSource("source_Abs_AbsX")
    void testAbsoluteROL(final AddressMode addressMode, final int value, final boolean carryIn) {
        final TestContext context = TestContext.createAbsolute(Instruction.ROL, addressMode, 0x20FF, 0, value, carryIn);
        final int result = context.getMemory().readByte(0x20FF);
        context.assertNZCorrect(result);
        OpAssert.assertROL(context, value, carryIn, result, context.isFlagSet(Flag.CARRY));
    }

    @ParameterizedTest
    @MethodSource("source_Abs_AbsX")
    void testAbsoluteROR(final AddressMode addressMode, final int value, final boolean carryIn) {
        final TestContext context = TestContext.createAbsolute(Instruction.ROR, addressMode, 0x20FF, 0, value, carryIn);
        final int result = context.getMemory().readByte(0x20FF);
        context.assertNZCorrect(result);
        OpAssert.assertROR(context, value, carryIn, result, context.isFlagSet(Flag.CARRY));
    }
}
