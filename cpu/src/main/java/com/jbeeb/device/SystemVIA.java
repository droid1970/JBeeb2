package com.jbeeb.device;

import com.jbeeb.keymap.ColRow;
import com.jbeeb.keymap.KeyMap;
import com.jbeeb.keymap.TargetKey;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SystemVIA extends VIA {

    private final KeyMap keyMap = KeyMap.DEFAULT_KEY_MAP;
    private final Set<ColRow> downKeys = new HashSet<>();
    private final boolean[][] keyDown = new boolean[16][16];
    private final Boolean[][] keyDownShift = new Boolean[16][16];
    private ColRow lastKeyDown = null;

    private int IC32;
    private boolean capslockLight;
    private boolean shiftlockLight;

    public SystemVIA(String name, int startAddress, int size) {
        super(name, startAddress, size);
    }

    public final void characterDown(final char c) {
        keyDown(keyMap.get(c));
    }

    public final void characterUp(final char c) {
        if (keyUp(keyMap.get(c))) {
            updateKeys();
        }
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

    private void updateKeys_OLD(final boolean forceInterrupt) {
        final int numcols = 10;
        if ((IC32 & 8) != 0) {
            for (int i = 0; i < numcols; i++) {
                for (int j = 1; j < 8; j++) {
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
            if (!keyDown[keycol][keyrow]) {
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

    @Override
    public void portAUpdated() {
        updateKeys();
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

        capslockLight = (IC32 & 0x40) == 0;
        shiftlockLight = (IC32 & 0x80) == 0;

        // Screen address

        recalculatePortAPins();
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
