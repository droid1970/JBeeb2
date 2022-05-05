package com.jbeeb;

import com.jbeeb.localfs.ByteRandomAccessData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ByteBufferRandomAccessDataTest {

    @Test
    void testByteBufferRandomAccessData() throws Exception {
        final byte[] data = {
                2, 4, 6, 8, 10
        };

        final ByteRandomAccessData d = new ByteRandomAccessData(data);
        assertThat(d.get()).isEqualTo(data[0]);
        assertThat(d.isEOF()).isFalse();

        assertThat(d.get()).isEqualTo(data[1]);
        assertThat(d.isEOF()).isFalse();

        assertThat(d.get()).isEqualTo(data[2]);
        assertThat(d.isEOF()).isFalse();

        assertThat(d.get()).isEqualTo(data[3]);
        assertThat(d.isEOF()).isFalse();

        assertThat(d.get()).isEqualTo(data[4]);
        assertThat(d.isEOF()).isTrue();
    }
}
