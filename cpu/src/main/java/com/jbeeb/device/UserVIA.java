package com.jbeeb.device;

import com.jbeeb.util.SystemStatus;

public class UserVIA extends VIA {
    public UserVIA(
            final SystemStatus systemStatus,
            final String name,
            final int startAddress,
            final int size
    ) {
        super(systemStatus, name, startAddress, size);
    }
}
