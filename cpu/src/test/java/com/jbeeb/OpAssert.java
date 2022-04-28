package com.jbeeb;

import com.jbeeb.cpu.Flag;
import com.jbeeb.util.Util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public final class OpAssert {

    public static void assertASL(final TestContext context, final int operand, final boolean carryIn, final int result, final boolean carryOut) {
        assertThat(carryOut).isEqualTo((operand & 0x80) != 0);
        assertThat(result).isEqualTo((operand << 1) & 0xFF);
    }

    public static void assertLSR(final TestContext context, final int operand, final boolean carryIn, final int result, final boolean carryOut) {
        assertThat(carryOut).isEqualTo((operand & 0x01) != 0);
        assertThat(result).isEqualTo((operand >>> 1) & 0xFF);
    }

    public static void assertROL(final TestContext context, final int operand, final boolean carryIn, final int result, final boolean carryOut) {
        assertThat((result & 0x01) != 0).isEqualTo(carryIn);
        assertThat(carryOut).isEqualTo(Util.isNegative(operand));
        final int expected = ((operand << 1) & 0xFF) | (carryIn ? 0x01 : 0x00);
        assertThat(result).isEqualTo(expected);
    }

    public static void assertROR(final TestContext context, final int operand, final boolean carryIn, final int result, final boolean carryOut) {
        assertThat((result & 0x80) != 0).isEqualTo(carryIn);
        assertThat(carryOut).isEqualTo((operand & 1) != 0);
        final int expected = ((operand >>> 1) & 0xFF) | (carryIn ? 0x80 : 0x00);
        assertThat(result).isEqualTo(expected);
    }
}
