package com.jbeeb.ui;

import com.jbeeb.main.BBCMicro;

public interface Action {
    String getText();
    boolean isEnabled();
    boolean isSelected();
    void performAction(BBCMicro bbc);
}
