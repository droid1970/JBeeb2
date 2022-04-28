package com.jbeeb.teletext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

final class TeletextAlphaDefinition {

    public static final AlphaDefinition SPACE = new AlphaDefinition();
    public static final AlphaDefinition EXCLAMATION_MARK = new AlphaDefinition().moveTo(2.5, 0.5).lineTo(2.5, 4.5).moveTo(2.5, 6.5).lineTo(2.5, 6.5);
    public static final AlphaDefinition DOUBLE_QUOTE = new AlphaDefinition().moveTo(1.5, 0.5).lineTo(1.5, 2.5).moveTo(3.5, 0.5).lineTo(3.5, 2.5);
    public static final AlphaDefinition HASH = new AlphaDefinition().moveTo(1.5, 0.5).lineTo(1.5, 6.5).moveTo(3.5, 0.5).lineTo(3.5, 6.5).moveTo(0.5, 2.5).lineTo(4.5, 2.5).moveTo(0.5, 4.5).lineTo(4.5, 4.5);
    public static final AlphaDefinition DOLLAR = new AlphaDefinition().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 2.5).lineTo(1.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 4.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).moveTo(2.5, 0.5).moveTo(2.5, 0.5).lineTo(2.5, 6.5);
    public static final AlphaDefinition PERCENT = new AlphaDefinition().moveTo(0.5, 5.5).lineTo(4.5, 1.5).moveTo(0.5, 0.5).lineTo(1.5, 0.5).lineTo(1.5, 1.5).lineTo(0.5, 1.5).lineTo(0.5, 0.5).moveTo(3.5, 5.5).lineTo(4.5, 5.5).lineTo(4.5, 6.5).lineTo(3.5, 6.5).lineTo(3.5, 5.5);
    public static final AlphaDefinition AMPERSAND = new AlphaDefinition().moveTo(4.5, 1.5).moveTo(4.5, 6.5).lineTo(0.5, 2.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(2.5, 1.5).lineTo(2.5, 2.5).lineTo(0.5, 4.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5);
    public static final AlphaDefinition SINGLE_QUOTE = new AlphaDefinition().moveTo(2.5, 0.5).lineTo(2.5, 2.5);
    public static final AlphaDefinition OPEN_BRACKET = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(1.5, 2.5).lineTo(1.5, 4.5).moveTo(3.5, 6.5).moveTo(1.5, 4.5).lineTo(3.5, 6.5);
    public static final AlphaDefinition CLOSE_BRACKET = new AlphaDefinition().moveTo(1.5, 0.5).lineTo(3.5, 2.5).lineTo(3.5, 4.5).lineTo(1.5, 6.5);
    public static final AlphaDefinition STAR = new AlphaDefinition().moveTo(2.5, 0.5).lineTo(2.5, 6.5).moveTo(0.5, 1.5).lineTo(4.5, 5.5).moveTo(4.5, 1.5).lineTo(0.5, 5.5);
    public static final AlphaDefinition PLUS = new AlphaDefinition().moveTo(2.5, 1.5).lineTo(2.5, 5.5).moveTo(0.5, 3.5).lineTo(4.5, 3.5);
    public static final AlphaDefinition COMMA = new AlphaDefinition().moveTo(1.5, 7.5).lineTo(2.5, 6.5).lineTo(2.5, 5.5);
    public static final AlphaDefinition MINUS = new AlphaDefinition().moveTo(1.5, 3.5).lineTo(3.5, 3.5);
    public static final AlphaDefinition DOT = new AlphaDefinition().moveTo(2.5, 6.5).lineTo(2.5, 6.5);
    public static final AlphaDefinition FORWARD_SLASH = new AlphaDefinition().moveTo(0.5, 5.5).lineTo(4.5, 1.5);
    public static final AlphaDefinition ZERO = new AlphaDefinition().moveTo(2.5, 0.5).lineTo(0.5, 2.5).lineTo(0.5, 4.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5).lineTo(4.5, 2.5).lineTo(2.5, 0.5);
    public static final AlphaDefinition ONE = new AlphaDefinition().moveTo(1.5, 1.5).moveTo(1.5, 1.5).lineTo(2.5, 0.5).lineTo(2.5, 6.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5);
    public static final AlphaDefinition TWO = new AlphaDefinition().moveTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(2.5, 3.5).lineTo(0.5, 5.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition THREE = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(4.5, 0.5).lineTo(4.5, 1.5).lineTo(2.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 4.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final AlphaDefinition FOUR = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(0.5, 3.5).lineTo(0.5, 4.5).lineTo(4.5, 4.5).moveTo(3.5, 6.5).lineTo(3.5, 0.5);
    public static final AlphaDefinition FIVE = new AlphaDefinition().moveTo(4.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final AlphaDefinition SIX = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(2.5, 0.5).lineTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 4.5).lineTo(3.5, 3.5).lineTo(0.5, 3.5);
    public static final AlphaDefinition SEVEN = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(4.5, 0.5).lineTo(4.5, 1.5).lineTo(1.5, 4.5).lineTo(1.5, 6.5);
    public static final AlphaDefinition EIGHT = new AlphaDefinition().moveTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(1.5, 3.5).lineTo(0.5, 2.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).moveTo(0.5, 4.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 4.5).lineTo(3.5, 3.5).moveTo(1.5, 3.5).lineTo(0.5, 4.5);
    public static final AlphaDefinition NINE = new AlphaDefinition().moveTo(1.5, 6.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 2.5).lineTo(1.5, 3.5).lineTo(4.5, 3.5);
    public static final AlphaDefinition COLON = new AlphaDefinition().moveTo(2.5, 2.5).lineTo(2.5, 2.5).moveTo(2.5, 6.5).lineTo(2.5, 6.5);
    public static final AlphaDefinition SEMICOLON = new AlphaDefinition().moveTo(1.5, 7.5).lineTo(2.5, 6.5).lineTo(2.5, 5.5).moveTo(2.5, 2.5).lineTo(2.5, 2.5);
    public static final AlphaDefinition LESS_THAN = new AlphaDefinition().moveTo(3.5, 6.5).lineTo(0.5, 3.5).lineTo(3.5, 0.5);
    public static final AlphaDefinition EQUALS = new AlphaDefinition().moveTo(0.5, 2.5).lineTo(4.5, 2.5).moveTo(0.5, 4.5).lineTo(4.5, 4.5);
    public static final AlphaDefinition GREATER_THAN = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(3.5, 3.5).lineTo(0.5, 0.5);
    public static final AlphaDefinition QUESTION_MARK = new AlphaDefinition().moveTo(2.5, 6.5).lineTo(2.5, 6.5).moveTo(2.5, 4.5).lineTo(2.5, 3.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5);
    public static final AlphaDefinition AT = new AlphaDefinition().moveTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 4.5).lineTo(2.5, 4.5).lineTo(2.5, 2.5).lineTo(4.5, 2.5);
    public static final AlphaDefinition UPPER_A = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(0.5, 2.5).lineTo(2.5, 0.5).lineTo(4.5, 2.5).lineTo(4.5, 6.5).moveTo(0.5, 4.5).lineTo(4.5, 4.5);
    public static final AlphaDefinition UPPER_B = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(0.5, 3.5).lineTo(0.5, 0.5).moveTo(0.5, 3.5).lineTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 4.5).lineTo(3.5, 3.5);
    public static final AlphaDefinition UPPER_C = new AlphaDefinition().moveTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5);
    public static final AlphaDefinition UPPER_D = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5);
    public static final AlphaDefinition UPPER_E = new AlphaDefinition().moveTo(4.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5).moveTo(3.5, 3.5).lineTo(0.5, 3.5);
    public static final AlphaDefinition UPPER_F = new AlphaDefinition().moveTo(4.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(0.5, 3.5).lineTo(3.5, 3.5);
    public static final AlphaDefinition UPPER_G = new AlphaDefinition().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5).lineTo(4.5, 4.5).lineTo(3.5, 4.5);
    public static final AlphaDefinition UPPER_H = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(4.5, 0.5).lineTo(4.5, 6.5).moveTo(0.5, 3.5).lineTo(4.5, 3.5);
    public static final AlphaDefinition UPPER_I = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(1.5, 0.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5).moveTo(2.5, 0.5).lineTo(2.5, 6.5);
    public static final AlphaDefinition UPPER_J = new AlphaDefinition().moveTo(4.5, 0.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final AlphaDefinition UPPER_K = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(4.5, 0.5).lineTo(1.5, 3.5).moveTo(4.5, 6.5).lineTo(1.5, 3.5);
    public static final AlphaDefinition UPPER_L = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition UPPER_M = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(0.5, 0.5).lineTo(2.5, 2.5).lineTo(4.5, 0.5).lineTo(4.5, 6.5).moveTo(2.5, 3.5).lineTo(2.5, 2.5);
    public static final AlphaDefinition UPPER_N = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(0.5, 0.5).moveTo(4.5, 6.5).lineTo(4.5, 0.5).moveTo(0.5, 1.5).lineTo(4.5, 5.5);
    public static final AlphaDefinition UPPER_O = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5);
    public static final AlphaDefinition UPPER_P = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(0.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(0.5, 3.5);
    public static final AlphaDefinition UPPER_Q = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).moveTo(2.5, 4.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition UPPER_R = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(0.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 2.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).moveTo(4.5, 6.5).lineTo(1.5, 3.5);
    public static final AlphaDefinition UPPER_S = new AlphaDefinition().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 2.5).lineTo(1.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 4.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final AlphaDefinition UPPER_T = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(4.5, 0.5).moveTo(2.5, 6.5).lineTo(2.5, 0.5);
    public static final AlphaDefinition UPPER_U = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 0.5);
    public static final AlphaDefinition UPPER_V = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 1.5).lineTo(2.5, 6.5).lineTo(4.5, 1.5).lineTo(4.5, 0.5);
    public static final AlphaDefinition UPPER_W = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 5.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 0.5).moveTo(2.5, 4.5).lineTo(2.5, 5.5);
    public static final AlphaDefinition UPPER_X = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(0.5, 5.5).moveTo(4.5, 6.5).lineTo(4.5, 5.5).moveTo(4.5, 0.5).lineTo(4.5, 1.5).moveTo(0.5, 0.5).lineTo(0.5, 1.5).moveTo(0.5, 1.5).lineTo(4.5, 5.5).moveTo(4.5, 1.5).lineTo(0.5, 5.5);
    public static final AlphaDefinition UPPER_Y = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 1.5).moveTo(4.5, 0.5).lineTo(4.5, 1.5).moveTo(0.5, 1.5).lineTo(2.5, 3.5).moveTo(4.5, 1.5).lineTo(2.5, 3.5).moveTo(2.5, 3.5).lineTo(2.5, 6.5);
    public static final AlphaDefinition UPPER_Z = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(4.5, 0.5).lineTo(4.5, 1.5).lineTo(0.5, 5.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition LEFT_ARROW = new AlphaDefinition().moveTo(4.5, 3.5).lineTo(0.5, 3.5).moveTo(2.5, 1.5).lineTo(0.5, 3.5).moveTo(2.5, 5.5).lineTo(0.5, 3.5);
    public static final AlphaDefinition HALF = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 4.5).moveTo(2.5, 4.5).lineTo(3.5, 4.5).lineTo(4.5, 5.5).lineTo(2.5, 7.5).lineTo(2.5, 8.5).lineTo(4.5, 8.5);
    public static final AlphaDefinition RIGHT_ARROW = new AlphaDefinition().moveTo(0.5, 3.5).lineTo(4.5, 3.5).moveTo(2.5, 1.5).lineTo(4.5, 3.5).moveTo(2.5, 5.5).lineTo(4.5, 3.5);
    public static final AlphaDefinition UP_ARROW = new AlphaDefinition().moveTo(2.5, 1.5).lineTo(2.5, 5.5).moveTo(0.5, 3.5).lineTo(2.5, 1.5).moveTo(4.5, 3.5).lineTo(2.5, 1.5);
    public static final AlphaDefinition UNDERSCORE = new AlphaDefinition().moveTo(0.5, 3.5).lineTo(4.5, 3.5);
    public static final AlphaDefinition POUND = new AlphaDefinition().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(2.5, 0.5).lineTo(1.5, 1.5).lineTo(1.5, 6.5).moveTo(0.5, 6.5).lineTo(4.5, 6.5).moveTo(0.5, 3.5).lineTo(2.5, 3.5);
    public static final AlphaDefinition LOWER_A = new AlphaDefinition().moveTo(1.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(1.5, 4.5).lineTo(4.5, 4.5);
    public static final AlphaDefinition LOWER_B = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 3.5).lineTo(3.5, 2.5).lineTo(0.5, 2.5);
    public static final AlphaDefinition LOWER_C = new AlphaDefinition().moveTo(4.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(4.5, 2.5);
    public static final AlphaDefinition LOWER_D = new AlphaDefinition().moveTo(4.5, 0.5).lineTo(4.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(4.5, 2.5);
    public static final AlphaDefinition LOWER_E = new AlphaDefinition().moveTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 4.5).lineTo(0.5, 4.5);
    public static final AlphaDefinition LOWER_F = new AlphaDefinition().moveTo(3.5, 0.5).lineTo(2.5, 1.5).lineTo(2.5, 6.5).moveTo(1.5, 3.5).lineTo(3.5, 3.5);
    public static final AlphaDefinition LOWER_G = new AlphaDefinition().moveTo(1.5, 8.5).lineTo(3.5, 8.5).lineTo(4.5, 7.5).lineTo(4.5, 2.5).lineTo(1.5, 2.5).lineTo(0.5, 3.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition LOWER_H = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition LOWER_I = new AlphaDefinition().moveTo(2.5, 0.5).lineTo(2.5, 0.5).moveTo(1.5, 2.5).lineTo(2.5, 2.5).lineTo(2.5, 6.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5);
    public static final AlphaDefinition LOWER_J = new AlphaDefinition().moveTo(2.5, 0.5).lineTo(2.5, 0.5).moveTo(2.5, 2.5).lineTo(2.5, 7.5).lineTo(1.5, 8.5);
    public static final AlphaDefinition LOWER_K = new AlphaDefinition().moveTo(1.5, 0.5).lineTo(1.5, 6.5).moveTo(4.5, 2.5).lineTo(1.5, 5.5).moveTo(4.5, 6.5).lineTo(1.5, 3.5);
    public static final AlphaDefinition LOWER_L = new AlphaDefinition().moveTo(1.5, 0.5).lineTo(2.5, 0.5).lineTo(2.5, 6.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5);
    public static final AlphaDefinition LOWER_M = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(0.5, 2.5).lineTo(1.5, 2.5).lineTo(2.5, 3.5).lineTo(2.5, 6.5).moveTo(4.5, 6.5).lineTo(4.5, 3.5).lineTo(3.5, 2.5).lineTo(2.5, 3.5);
    public static final AlphaDefinition LOWER_N = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition LOWER_O = new AlphaDefinition().moveTo(1.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5);
    public static final AlphaDefinition LOWER_P = new AlphaDefinition().moveTo(0.5, 8.5).lineTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(0.5, 6.5);
    public static final AlphaDefinition LOWER_Q = new AlphaDefinition().moveTo(4.5, 8.5).lineTo(4.5, 2.5).lineTo(1.5, 2.5).lineTo(0.5, 3.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition LOWER_R = new AlphaDefinition().moveTo(1.5, 6.5).lineTo(1.5, 2.5).moveTo(4.5, 2.5).lineTo(3.5, 2.5).lineTo(1.5, 4.5);
    public static final AlphaDefinition LOWER_S = new AlphaDefinition().moveTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(3.5, 4.5).lineTo(1.5, 4.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(4.5, 2.5);
    public static final AlphaDefinition LOWER_T = new AlphaDefinition().moveTo(2.5, 0.5).lineTo(2.5, 5.5).lineTo(3.5, 6.5).moveTo(3.5, 2.5).lineTo(1.5, 2.5);
    public static final AlphaDefinition LOWER_U = new AlphaDefinition().moveTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5).lineTo(4.5, 2.5);
    public static final AlphaDefinition LOWER_V = new AlphaDefinition().moveTo(0.5, 2.5).lineTo(2.5, 6.5).lineTo(4.5, 2.5);
    public static final AlphaDefinition LOWER_W = new AlphaDefinition().moveTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 5.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 2.5).moveTo(2.5, 4.5).lineTo(2.5, 5.5);
    public static final AlphaDefinition LOWER_X = new AlphaDefinition().moveTo(0.5, 2.5).lineTo(4.5, 6.5).moveTo(4.5, 2.5).lineTo(0.5, 6.5);
    public static final AlphaDefinition LOWER_Y = new AlphaDefinition().moveTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5).lineTo(4.5, 2.5).moveTo(1.5, 8.5).lineTo(3.5, 8.5).lineTo(4.5, 7.5).lineTo(4.5, 2.5);
    public static final AlphaDefinition LOWER_Z = new AlphaDefinition().moveTo(0.5, 2.5).lineTo(4.5, 2.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final AlphaDefinition QUARTER = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(0.5, 4.5).moveTo(4.5, 4.5).lineTo(2.5, 6.5).lineTo(2.5, 7.5).lineTo(4.5, 7.5).moveTo(4.5, 4.5).lineTo(4.5, 8.5);
    public static final AlphaDefinition PIPE = new AlphaDefinition().moveTo(1.5, 0.5).lineTo(1.5, 6.5).moveTo(3.5, 0.5).lineTo(3.5, 6.5);
    public static final AlphaDefinition THREE_QUARTERS = new AlphaDefinition().moveTo(0.5, 0.5).lineTo(1.5, 0.5).lineTo(2.5, 1.5).lineTo(1.5, 2.5).lineTo(0.5, 2.5).moveTo(0.5, 4.5).lineTo(1.5, 4.5).lineTo(2.5, 3.5).lineTo(1.5, 2.5).moveTo(4.5, 4.5).lineTo(2.5, 6.5).lineTo(2.5, 7.5).lineTo(4.5, 7.5).lineTo(4.5, 4.5).moveTo(4.5, 8.5).lineTo(4.5, 4.5);
    public static final AlphaDefinition DIVIDE = new AlphaDefinition().moveTo(0.5, 3.5).lineTo(4.5, 3.5).moveTo(2.5, 1.5).lineTo(2.5, 1.5).moveTo(2.5, 5.5).lineTo(2.5, 5.5);

    public static final AlphaDefinition BLOCK = new AlphaDefinition();
    static {
        for (double y = 0.5; y < 7.0; y++) {
            BLOCK.moveTo(0.5, y).lineTo(4.5, y);
        }
    }

    private static final double CW2 = 5.0;
    private static final double CH2 = 9.0;
    private static final double IW2 = TeletextConstants.TELETEXT_CHAR_WIDTH;
    private static final double IH2 = TeletextConstants.TELETEXT_CHAR_HEIGHT;
    private static final double LEFT_MARGIN2 = 1.0;
    private static final double RIGHT_MARGIN2 = 0.0;
    private static final double TOP_MARGIN2 = 0.0;
    private static final double BOTTOM_MARGIN2 = 1.5;
    private static final double X_SCALE2 = (IW2 - LEFT_MARGIN2 - RIGHT_MARGIN2) / CW2;
    private static final double Y_SCALE2 = (IH2 - TOP_MARGIN2 - BOTTOM_MARGIN2) / CH2;

    private static double tX2(final double x) {
        return LEFT_MARGIN2 + x * X_SCALE2;
    }

    private static double tY2(final double y) {
        return TOP_MARGIN2 + y * Y_SCALE2;
    }

    public static BufferedImage createCharacterImage(final AlphaDefinition p, final Color colour) {
        final Path2D.Double path = p.toPath(px -> tX2(px), py -> tY2(py));
        return createPathImage(path, 1, colour);
    }

    private static BufferedImage createPathImage(final Path2D.Double path, final int scale, final Color colour) {
        final int iw = scale * TeletextConstants.TELETEXT_CHAR_WIDTH;
        final int ih = scale * TeletextConstants.TELETEXT_CHAR_HEIGHT;
        final BufferedImage charImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = charImage.createGraphics();
        final Path2D.Double scaledPath = new Path2D.Double(path);
        final AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        scaledPath.transform(transform);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(scale * 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(colour);
        g.draw(scaledPath);
        return charImage;
    }
}
