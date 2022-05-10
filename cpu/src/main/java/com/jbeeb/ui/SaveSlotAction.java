package com.jbeeb.ui;

import com.jbeeb.main.BBCMicro;

public final class SaveSlotAction implements Action {

    private final int slotIndex;

    public SaveSlotAction(final int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    public String getText() {
        return Integer.toString(slotIndex);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void performAction(final BBCMicro bbc) {

    }
}
