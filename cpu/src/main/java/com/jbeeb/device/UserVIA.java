package com.jbeeb.device;

import com.jbeeb.util.StateKey;
import com.jbeeb.util.SystemStatus;

@StateKey(key = "userVIA")
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
