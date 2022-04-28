package com.jbeeb;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.util.Util;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LDATests {

    @Test
    void testImmediate() {
        final TestContext context = TestContext.createAndRun("LDA #10");
        assertThat(context.getA()).isEqualTo(10);
    }
}
