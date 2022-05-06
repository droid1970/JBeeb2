package com.jbeeb.keymap;

import java.util.HashMap;
import java.util.Map;

public enum BBCKey {
    SEMICOLON_PLUS(7, 5),
    MINUS(7, 1),
    LEFT_SQUARE_BRACKET(8, 3),
    RIGHT_SQUARE_BRACKET(8, 5),
    COMMA(6, 6),
    PERIOD(7, 6),
    SLASH(8, 6),
    SHIFTLOCK(0, 5),
    TAB(0, 6),
    RETURN(9, 4),
    DELETE(9, 5),
    COPY(9, 6),
    SHIFT(0, 0),
    ESCAPE(0, 7),
    CTRL(1, 0),
    CAPSLOCK(0, 4),
    LEFT(9, 1),
    UP(9, 3),
    RIGHT(9, 7),
    DOWN(9, 2),
    K0(7, 2),
    K1(0, 3),
    K2(1, 3),
    K3(1, 1),
    K4(2, 1),
    K5(3, 1),
    K6(4, 3),
    K7(4, 2),
    K8(5, 1),
    K9(6, 2),

    Q(0, 1),
    W(1, 2),
    E(2, 2),
    R(3, 3),
    T(3, 2),
    Y(4, 4),
    U(5, 3),
    I(5, 2),
    O(6, 3),
    P(7, 3),

    A(1, 4),
    S(1, 5),
    D(2, 3),
    F(3, 4),
    G(3, 5),
    H(4, 5),
    J(5, 4),
    K(6, 4),
    L(6, 5),

    Z(1, 6),
    X(2, 4),
    C(2, 5),
    V(3, 6),
    B(4, 6),
    N(5, 5),
    M(5, 6),

    F0(0, 2),
    F1(1, 7),
    F2(2, 7),
    F3(3, 7),
    F4(4, 1),
    F5(4, 7),
    F6(5, 7),
    F7(6, 1),
    F8(6, 7),
    F9(7, 7),

    SPACE(2, 6),

    UNDERSCORE_POUND(8, 2),
    AT(7, 4),
    COLON_STAR(8, 4),
    PIPE_BACKSLASH(8, 7),
    HAT_TILDE(8, 1),

    // row 1
    NUMPADPLUS(10, 3),
    NUMPADMINUS(11, 3),
    NUMPADSLASH(10, 4),
    NUMPADASTERISK(11, 5),

    // row 2
    NUMPAD7(11, 1),
    NUMPAD8(10, 2),
    NUMPAD9(11, 2),
    NUMPADHASH(10, 5),
    // row 3
    NUMPAD4(10, 7),
    NUMPAD5(11, 7),
    NUMPAD6(10, 1),
    NUMPAD_DELETE(11, 4),
    //row4
    NUMPAD1(11, 6),
    NUMPAD2(12, 7),
    NUMPAD3(12, 6),
    NUMPADCOMMA(12, 5),

    //row 5
    NUMPAD0(10, 6),
    NUMPAD_DECIMAL_POINT(12, 4),
    NUMPADENTER(12, 3);

    private static final Map<ColRow, BBCKey> CODE_TO_KEY = new HashMap<>();
    static {
        for (BBCKey key : values()) {
            CODE_TO_KEY.put(new ColRow(key.col, key.row), key);
        }
    }

    final int col;
    final int row;
    
    BBCKey(final int col, final int row) {
        this.col = col;
        this.row = row;

    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public static BBCKey forInternalCode(final int code) {
        return CODE_TO_KEY.get(new ColRow(code & 0xF, (code >>> 4) & 0xF));
    }
}
