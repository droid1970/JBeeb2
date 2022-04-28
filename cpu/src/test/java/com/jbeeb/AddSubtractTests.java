package com.jbeeb;

import com.jbeeb.cpu.Flag;
import com.jbeeb.util.Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AddSubtractTests {
    @Test
    void testADC() {
        final TestContext context = TestContext.create();
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                for (boolean carryIn : new boolean[]{false, true}) {
                    context.resetAndAssemble(
                            carryIn ? "SEC" : "CLC",
                            "LDA #" + a,
                            "ADC #" + b
                    );
                    context.run();
                    final int actualResult = context.getA();
                    final boolean actualCarry = context.isFlagSet(Flag.CARRY);
                    final boolean actualOverflow = context.isFlagSet(Flag.OVERFLOW);
                    final int sum = a + b + (carryIn ? 1 : 0);
                    final int signedSum = Util.signed(a) + Util.signed(b) + (carryIn ? 1 : 0);
                    final int expectedResult = sum & 0xFF;
                    final boolean expectedCarry = sum > 255;
                    final boolean expectedOverflow = signedSum < -128 || signedSum > 127;
                    assertThat(actualResult).isEqualTo(expectedResult);
                    assertThat(actualCarry).isEqualTo(expectedCarry);
                    assertThat(actualOverflow).isEqualTo(expectedOverflow);
                }
            }
        }
    }

    @Test
    void testSBC() {
        final TestContext context = TestContext.create();
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                for (boolean carryIn : new boolean[]{false, true}) {
                    context.resetAndAssemble(
                            carryIn ? "SEC" : "CLC",
                            "LDA #" + a,
                            "SBC #" + b,
                            "HLT"
                    );
                    context.run();
                    final int actualResult = context.getA();
                    final boolean actualCarry = context.isFlagSet(Flag.CARRY);
                    final boolean actualOverflow = context.isFlagSet(Flag.OVERFLOW);
                    final int subtract = a - b - (carryIn ? 0 : 1);
                    final int signedSubtract = Util.signed(a) - Util.signed(b) - (carryIn ? 0 : 1);
                    final int expectedResult = (subtract & 0xFF);
                    final boolean expectedCarry = !(subtract < 0);
                    final boolean expectedOverflow = (signedSubtract < -128 || signedSubtract > 127);
                    assertThat(actualResult).isEqualTo(expectedResult);
                    assertThat(actualCarry).isEqualTo(expectedCarry);
                    assertThat(actualOverflow).isEqualTo(expectedOverflow);
                }
            }
        }
    }
}
