package com.jbeeb.ui;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SaveSlotSetting implements Setting {

    @Override
    public String getText() {
        return "Save to slot";
    }

    @Override
    public List<Action> getActions() {
        return IntStream.range(0, 10).mapToObj(SaveSlotAction::new).collect(Collectors.toList());
    }
}
