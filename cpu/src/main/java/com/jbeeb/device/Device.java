package com.jbeeb.device;

import com.jbeeb.util.StateSupplier;
import com.jbeeb.util.TypedMap;

public interface Device {
    String getId();
    String getName();
    TypedMap getProperties();
}
