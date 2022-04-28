package com.jbeeb;

import com.jbeeb.cpu.Flag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

class AccumulatorModeTests {

    private static Stream<Arguments> source() {
        return Stream.concat(
                IntStream.range(0, 256).boxed().map(i -> Arguments.of(i, false)),
                IntStream.range(0, 256).boxed().map(i -> Arguments.of(i, true))
        );
    }

    @ParameterizedTest
    @MethodSource("source")
    void testAccumulatorASL(final int value, final boolean carryIn) {
        final TestContext context = createContext("ASL", value, carryIn);
        context.assertNZCorrect(context.getA());
        OpAssert.assertASL(context, value, carryIn, context.getA(), context.isFlagSet(Flag.CARRY));
    }

    @ParameterizedTest
    @MethodSource("source")
    void testAccumulatorLSR(final int value, final boolean carryIn) {
        final TestContext context = createContext("LSR", value, carryIn);
        context.assertNZCorrect(context.getA());
        OpAssert.assertLSR(context, value, carryIn, context.getA(), context.isFlagSet(Flag.CARRY));
    }

    @ParameterizedTest
    @MethodSource("source")
    void testAccumulatorROL(final int value, final boolean carryIn) {
        final TestContext context = createContext("ROL", value, carryIn);
        context.assertNZCorrect(context.getA());
        OpAssert.assertROL(context, value, carryIn, context.getA(), context.isFlagSet(Flag.CARRY));
    }

    @ParameterizedTest
    @MethodSource("source")
    void testAccumulatorROR(final int value, final boolean carryIn) {
        final TestContext context = createContext("ROR", value, carryIn);
        context.assertNZCorrect(context.getA());
        OpAssert.assertROR(context, value, carryIn, context.getA(), context.isFlagSet(Flag.CARRY));
    }

    private static TestContext createContext(final String instruction, final int value, final boolean carryIn) {
        return TestContext.createAndRun(
                "LDA #" + value,
                (carryIn) ? "SEC" : "CLC",
                instruction + " A"
        ).run();
    }
}
