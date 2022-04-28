package com.jbeeb.keymap;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public final class KeyMap {

    private final Map<Character, TargetKey> charMap = new HashMap<>();
    private final Map<Integer, TargetKey> shiftUpMap = new HashMap<>();
    private final Map<Integer, TargetKey> shiftDownMap = new HashMap<>();

    public static KeyMap DEFAULT_KEY_MAP = newBuilder()

            .map(BBCKey.HAT_TILDE, true, 520, true)

            .mapChars(BBCKey.A, null, "Aa")
            .mapChars(BBCKey.B, null, "Bb")
            .mapChars(BBCKey.C, null, "Cc")
            .mapChars(BBCKey.D, null, "Dd")
            .mapChars(BBCKey.E, null, "Ee")
            .mapChars(BBCKey.F, null, "Ff")
            .mapChars(BBCKey.G, null, "Gg")
            .mapChars(BBCKey.H, null, "Hh")
            .mapChars(BBCKey.I, null, "Ii")
            .mapChars(BBCKey.J, null, "Jj")
            .mapChars(BBCKey.K, null, "Kk")
            .mapChars(BBCKey.L, null, "Ll")
            .mapChars(BBCKey.M, null, "Mm")
            .mapChars(BBCKey.N, null, "Nn")
            .mapChars(BBCKey.O, null, "Oo")
            .mapChars(BBCKey.P, null, "Pp")
            .mapChars(BBCKey.Q, null, "Qq")
            .mapChars(BBCKey.R, null, "Rr")
            .mapChars(BBCKey.S, null, "Ss")
            .mapChars(BBCKey.T, null, "Tt")
            .mapChars(BBCKey.U, null, "Uu")
            .mapChars(BBCKey.V, null, "Vv")
            .mapChars(BBCKey.W, null, "Ww")
            .mapChars(BBCKey.X, null, "Xx")
            .mapChars(BBCKey.Y, null, "Yy")
            .mapChars(BBCKey.Z, null, "Zz")

            .mapChars(BBCKey.K0, false, "0")
            .mapChars(BBCKey.K1, false, "1")
            .mapChars(BBCKey.K2, false, "2")
            .mapChars(BBCKey.K2, true, "\"")
            .mapChars(BBCKey.K3, false, "3")

            .mapChars(BBCKey.K4, false, "4")
            .mapChars(BBCKey.K4, true, "$")

            .mapChars(BBCKey.K5, false, "5")

            .mapChars(BBCKey.K6, false, "6")


            .mapChars(BBCKey.K7, false, "7")
            .mapChars(BBCKey.K8, false, "8")
            .mapChars(BBCKey.K9, false, "9")

            .mapChars(BBCKey.UNDERSCORE_POUND, false, "")

            .mapChars(BBCKey.SEMICOLON_PLUS, false, ";")
            .mapChars(BBCKey.SEMICOLON_PLUS, true, "+")
            .mapChars(BBCKey.SPACE, null, " ")

            .map(BBCKey.A, KeyEvent.VK_A)
            .map(BBCKey.B, KeyEvent.VK_B)
            .map(BBCKey.C, KeyEvent.VK_C)
            .map(BBCKey.D, KeyEvent.VK_D)
            .map(BBCKey.E, KeyEvent.VK_E)
            .map(BBCKey.F, KeyEvent.VK_F)
            .map(BBCKey.G, KeyEvent.VK_G)
            .map(BBCKey.H, KeyEvent.VK_H)
            .map(BBCKey.I, KeyEvent.VK_I)
            .map(BBCKey.J, KeyEvent.VK_J)
            .map(BBCKey.K, KeyEvent.VK_K)
            .map(BBCKey.L, KeyEvent.VK_L)
            .map(BBCKey.M, KeyEvent.VK_M)
            .map(BBCKey.N, KeyEvent.VK_N)
            .map(BBCKey.O, KeyEvent.VK_O)
            .map(BBCKey.P, KeyEvent.VK_P)
            .map(BBCKey.Q, KeyEvent.VK_Q)
            .map(BBCKey.R, KeyEvent.VK_R)
            .map(BBCKey.S, KeyEvent.VK_S)
            .map(BBCKey.T, KeyEvent.VK_T)
            .map(BBCKey.U, KeyEvent.VK_U)
            .map(BBCKey.V, KeyEvent.VK_V)
            .map(BBCKey.W, KeyEvent.VK_W)
            .map(BBCKey.X, KeyEvent.VK_X)
            .map(BBCKey.Y, KeyEvent.VK_Y)
            .map(BBCKey.Z, KeyEvent.VK_Z)

            .map(BBCKey.K0, false, KeyEvent.VK_0, false)
            .map(BBCKey.K9, true, KeyEvent.VK_0, true)

            .map(BBCKey.K1, KeyEvent.VK_1)
            .map(BBCKey.K2, KeyEvent.VK_2)

            .map(BBCKey.K3, false, KeyEvent.VK_3, false)
            .map(BBCKey.K3, true, 520, false)

            .map(BBCKey.UNDERSCORE_POUND, true, KeyEvent.VK_3, true)
            .map(BBCKey.K4, KeyEvent.VK_4)
            .map(BBCKey.K5, KeyEvent.VK_5)
            .map(BBCKey.K6, false, KeyEvent.VK_6, false)
            .map(BBCKey.HAT_TILDE, false, KeyEvent.VK_6, true)

            .map(BBCKey.K6, true, KeyEvent.VK_7, true)
            .map(BBCKey.K7, false,  KeyEvent.VK_7, false)

            .map(BBCKey.K8, false, KeyEvent.VK_8, false)
            .map(BBCKey.COLON_STAR, true, KeyEvent.VK_8, true)
            .map(BBCKey.K9, false, KeyEvent.VK_9, false)
            .map(BBCKey.K8, true, KeyEvent.VK_9, true)

            .map(BBCKey.MINUS, true, KeyEvent.VK_EQUALS, false)
            .map(BBCKey.SEMICOLON_PLUS, true, KeyEvent.VK_EQUALS, true)
            .map(BBCKey.SEMICOLON_PLUS, false, KeyEvent.VK_SEMICOLON, false)

            .map(BBCKey.SPACE, KeyEvent.VK_SPACE)
            .map(BBCKey.RETURN, KeyEvent.VK_ENTER)
            .map(BBCKey.DELETE, KeyEvent.VK_BACK_SPACE)

            .map(BBCKey.SHIFT, KeyEvent.VK_SHIFT)
            .map(BBCKey.ESCAPE, KeyEvent.VK_ESCAPE)
            .map(BBCKey.PIPE_BACKSLASH, KeyEvent.VK_BACK_SLASH)
            .map(BBCKey.RIGHT, KeyEvent.VK_RIGHT)
            .map(BBCKey.LEFT, KeyEvent.VK_LEFT)
            .map(BBCKey.UP, KeyEvent.VK_UP)
            .map(BBCKey.DOWN, KeyEvent.VK_DOWN)
            .map(BBCKey.RIGHT, KeyEvent.VK_RIGHT)
            .map(BBCKey.MINUS, false, KeyEvent.VK_MINUS, false)
            .map(BBCKey.UNDERSCORE_POUND, false, KeyEvent.VK_MINUS, true)
            .map(BBCKey.COMMA, KeyEvent.VK_COMMA)
            .map(BBCKey.COLON_STAR, KeyEvent.VK_SLASH)

            .map(BBCKey.CAPSLOCK, KeyEvent.VK_CAPS_LOCK)
            .map(BBCKey.AT, false, KeyEvent.VK_QUOTE, true)
            .map(BBCKey.K7, true, KeyEvent.VK_QUOTE, false)

            .map(BBCKey.LEFT_SQUARE_BRACKET, KeyEvent.VK_OPEN_BRACKET)
            .map(BBCKey.RIGHT_SQUARE_BRACKET, KeyEvent.VK_CLOSE_BRACKET)

            .map(BBCKey.COLON_STAR, false, KeyEvent.VK_SEMICOLON, true)
            .map(BBCKey.PERIOD, KeyEvent.VK_PERIOD)
            .map(BBCKey.SLASH, KeyEvent.VK_SLASH)
            .map(BBCKey.CTRL, KeyEvent.VK_CONTROL)

            .map(BBCKey.F0, KeyEvent.VK_F1)
            .map(BBCKey.F1, KeyEvent.VK_F2)
            .map(BBCKey.F2, KeyEvent.VK_F3)
            .map(BBCKey.F3, KeyEvent.VK_F4)
            .map(BBCKey.F4, KeyEvent.VK_F5)
            .map(BBCKey.F5, KeyEvent.VK_F6)
            .map(BBCKey.F6, KeyEvent.VK_F7)
            .map(BBCKey.F7, KeyEvent.VK_F8)
            .map(BBCKey.F8, KeyEvent.VK_F9)
            .map(BBCKey.F9, KeyEvent.VK_F10)
            .map(BBCKey.TAB, KeyEvent.VK_TAB)

            .build();

    private KeyMap(final Map<Integer, TargetKey> shiftDownMap, final Map<Integer, TargetKey> shiftUpMap, final Map<Character, TargetKey> charMap) {
        this.shiftDownMap.putAll(shiftDownMap);
        this.shiftUpMap.putAll(shiftUpMap);
        this.charMap.putAll(charMap);
    }

    public TargetKey get(final int keycode, final boolean shiftDown) {
        return (shiftDown) ? shiftDownMap.get(keycode) : shiftUpMap.get(keycode);
    }

    public TargetKey get(final char c) {
        return charMap.get(c);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private final Map<Character, TargetKey> charMap = new HashMap<>();
        private final Map<Integer, TargetKey> shiftUpMap = new HashMap<>();
        private final Map<Integer, TargetKey> shiftDownMap = new HashMap<>();

        public Builder map(final BBCKey bbc, final int keycode, char... chars) {
            final TargetKey target = new TargetKey(bbc.getCol(), bbc.getRow(), null);
            shiftDownMap.put(keycode, target);
            shiftUpMap.put(keycode, target);
            return this;
        }

        public Builder map(final BBCKey bbc, final Boolean bbcShift, final int keycode, final Boolean shiftDown) {
            final TargetKey target = new TargetKey(bbc.getCol(), bbc.getRow(), bbcShift);
            if (shiftDown == null || shiftDown) {
                shiftDownMap.put(keycode, target);
            }
            if (shiftDown == null || !shiftDown) {
                shiftUpMap.put(keycode, target);
            }
            return this;
        }

        public Builder mapChars(final BBCKey bbc, final Boolean bbcShift, final String chars) {
            if (chars != null) {
                final TargetKey target = new TargetKey(bbc.getCol(), bbc.getRow(), bbcShift);
                for (char c : chars.toCharArray()) {
                    charMap.put(c, target);
                }
            }
            return this;
        }

        public KeyMap build() {
            return new KeyMap(this.shiftDownMap, this.shiftUpMap, this.charMap);
        }
    }
}
