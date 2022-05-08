package com.jbeeb;

import com.jbeeb.cpu.Cpu;
import com.jbeeb.util.Util;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FunctionalTests {

    @Test
    void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/test.a65")) {
            int x = 1;
        }
    }

    @Test
    void testLabels() throws Exception {
        final Cpu cpu = Util.runCpu(1000, TestUtils.stream("/test.a65"));
        System.err.println("halt code = " + cpu.getHaltCode());
    }
}
