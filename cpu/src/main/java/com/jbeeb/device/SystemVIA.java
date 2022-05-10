package com.jbeeb.device;

import com.jbeeb.keymap.ColRow;
import com.jbeeb.keymap.KeyMap;
import com.jbeeb.keymap.TargetKey;
import com.jbeeb.sound.MultiSoundChip;
import com.jbeeb.sound.NopSoundChip;
import com.jbeeb.sound.SoundChip;
import com.jbeeb.util.StateKey;
import com.jbeeb.util.SystemStatus;

import java.util.Objects;

//
// Mostly based on  https://github.com/mattgodbolt/jsbeeb/via.js by Matt Godbolt
//

@StateKey(key = "systemVIA")
public class SystemVIA extends VIA {

    private KeyMap keyMap = KeyMap.DEFAULT;

    private final boolean[][] keyDown = new boolean[16][16];
    private final Boolean[][] keyDownShift = new Boolean[16][16];
    private ColRow lastKeyDown = null;
    private final SoundChip soundChip;

    @StateKey(key = "IC32")
    private int IC32;

    @StateKey(key = "capsLockLight")
    private boolean capslockLight;

    @StateKey(key = "shiftLockLight")
    private boolean shiftlockLight;

    private Runnable capsLockChangedCallback;
    private Runnable shiftLockChangedCallback;

    public static SoundChip createSoundChip() {
        try {
            return new MultiSoundChip();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new NopSoundChip();
        }
    }

    public SystemVIA(
            final SystemStatus systemStatus,
            final SoundChip soundChip,
            final String name,
            final int startAddress,
            final int size
    ) {
        super(systemStatus, name, startAddress, size);
        this.soundChip = Objects.requireNonNull(soundChip);
    }

    @Override
    public void setPaused(boolean paused) {
        super.setPaused(paused);
        if (soundChip != null) {
            soundChip.setPaused(paused);
        }
    }

    public void swapKeyMap() {
        this.keyMap = (keyMap == KeyMap.DEFAULT) ? KeyMap.PHYSICAL_KEY_MAP : KeyMap.DEFAULT;
    }

    public final int getScreenStartAddress() {
        final int n = (IC32 >>> 4) & 0x3;
        switch (n) {
            case 0:
                return 0x4000;
            case 1:
                return 0x6000;
            case 2:
                return 0x3000;
            default:
            case 3:
                return 0x5800;
        }
    }

    public final void characterDown(final char c) {
        keyDown(keyMap.get(c));
    }

    public final void characterUp(final char c) {
        if (keyUp(keyMap.get(c))) {
            updateKeys();
        }
    }

    public final void clearKeys() {
        for (int i = 0; i < keyDown.length; i++) {
            for (int j = 0; j < keyDown[i].length; j++) {
                keyDown[i][j] = false;
            }
        }
        updateKeys();
    }

    public final void keyDown(final int keycode, final boolean shiftDown) {
        keyDown(keyMap.get(keycode, shiftDown));
    }

    private void keyDown(final TargetKey target) {
        if (target != null) {
            final ColRow colrow = target.getColRow();
            final Boolean shift = target.getShift();
            keyDown[colrow.col][colrow.row] = true;
            keyDownShift[colrow.col][colrow.row] = shift;
            lastKeyDown = colrow;
            updateKeys();
        }
    }

    public final void keyUp(final int keycode) {
        if (keyUp(keyMap.get(keycode, true)) | keyUp(keyMap.get(keycode, false))) {
            updateKeys();
        }
    }

    private boolean keyUp(final TargetKey target) {
        if (target != null) {
            final ColRow colrow = target.getColRow();
            keyDown[colrow.col][colrow.row] = false;
            keyDownShift[colrow.col][colrow.row] = null;
            if (Objects.equals(lastKeyDown, colrow)) {
                lastKeyDown = null;
            }
            return true;
        }
        return false;
    }

    private void updateKeys() {
        final int numcols = 10;
        if ((IC32 & 8) != 0) {
            for (int i = 0; i < numcols; i++) {
                loop: for (int j = 1; j < 8; j++) {
                    if (keyDown[i][j]) {
                        setCA2(true);
                        return;
                    }
                }
            }
        } else {
            int portapins = portAPins;
            int keyrow = (portapins >>> 4) & 7;
            int keycol = portapins & 0xf;
            final boolean down = (keycol == 0 && keyrow == 0) ? isShiftDown() : keyDown[keycol][keyrow];
            if (!down) {
                portAPins &= 0x7f;
            } else if ((ddra & 0x80) == 0) {
                portAPins |= 0x80;
            }

            if (keycol < numcols) {
                for (int j = 1; j < 8; j++) {
                    if (keyDown[keycol][j]) {
                        setCA2(true);
                        return;
                    }
                }
            }
        }
        setCA2(false);
    }

    private boolean isShiftDown() {
        Boolean shift = null;
        if (lastKeyDown != null) {
            shift = keyDownShift[lastKeyDown.col][lastKeyDown.row];
        }
        if (shift == null) {
            return keyDown[0][0];
        } else {
            return shift;
        }
    }

    @Override
    public void portAUpdated() {
        updateKeys();
        if ((IC32 & 1) == 0) {
            soundChip.accept(portAPins);
        }

    }

    private int screenAddress;

    public int getScreenAddress() {
        return screenAddress;
    }

    @Override
    public void portBUpdated() {
        int portbpins = this.portBPins;
        if ((portbpins & 8) != 0) {
            IC32 |= (1 << (portbpins & 7));
        } else {
            IC32 &= ~(1 << (portbpins & 7));
        }

        this.screenAddress = ((IC32 & 16) != 0 ? 2 : 0) | ((IC32 & 32) != 0 ? 1 : 0);

        setCapsLockLight((IC32 & 0x40) == 0);
        setShiftLockLight(shiftlockLight = (IC32 & 0x80) == 0);

        // Screen address

        recalculatePortAPins();
    }

    public void setCapsLockChangedCallback(final Runnable capsLockChangedCallback) {
        this.capsLockChangedCallback = capsLockChangedCallback;
    }

    public void setShiftLockChangedCallback(final Runnable shiftLockChangedCallback) {
        this.shiftLockChangedCallback = shiftLockChangedCallback;
    }

    public boolean isCapslockLightOn() {
        return capslockLight;
    }

    private void setCapsLockLight(final boolean on) {
        if (this.capslockLight != on) {
            this.capslockLight = on;
            if (capsLockChangedCallback != null) {
                capsLockChangedCallback.run();
            }
        }
    }

    public boolean isShiftLockLightOn() {
        return shiftlockLight;
    }

    private void setShiftLockLight(final boolean on) {
        if (this.shiftlockLight != on) {
            this.shiftlockLight = on;
            if (shiftLockChangedCallback != null) {
                shiftLockChangedCallback.run();
            }
        }
    }

    @Override
    public void drivePortA() {
        int busval = 0xFF;
        portAPins &= busval;
        updateKeys();
    }

    @Override
    public void drivePortB() {
        // Nothing to do here
    }
}
