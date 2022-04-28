package com.jbeeb.teletext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public final class TeletextCharacterDefinitions {

    public static final PathSpec SPACE = new PathSpec();
    public static final PathSpec EXCLAMATION_MARK = new PathSpec().moveTo(2.5, 0.5).lineTo(2.5, 4.5).moveTo(2.5, 6.5).lineTo(2.5, 6.5);
    public static final PathSpec DOUBLE_QUOTE = new PathSpec().moveTo(1.5, 0.5).lineTo(1.5, 2.5).moveTo(3.5, 0.5).lineTo(3.5, 2.5);
    public static final PathSpec HASH = new PathSpec().moveTo(1.5, 0.5).lineTo(1.5, 6.5).moveTo(3.5, 0.5).lineTo(3.5, 6.5).moveTo(0.5, 2.5).lineTo(4.5, 2.5).moveTo(0.5, 4.5).lineTo(4.5, 4.5);
    public static final PathSpec DOLLAR = new PathSpec().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 2.5).lineTo(1.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 4.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).moveTo(2.5, 0.5).moveTo(2.5, 0.5).lineTo(2.5, 6.5);
    public static final PathSpec PERCENT = new PathSpec().moveTo(0.5, 5.5).lineTo(4.5, 1.5).moveTo(0.5, 0.5).lineTo(1.5, 0.5).lineTo(1.5, 1.5).lineTo(0.5, 1.5).lineTo(0.5, 0.5).moveTo(3.5, 5.5).lineTo(4.5, 5.5).lineTo(4.5, 6.5).lineTo(3.5, 6.5).lineTo(3.5, 5.5);
    public static final PathSpec AMPERSAND = new PathSpec().moveTo(4.5, 1.5).moveTo(4.5, 6.5).lineTo(0.5, 2.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(2.5, 1.5).lineTo(2.5, 2.5).lineTo(0.5, 4.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5);
    public static final PathSpec SINGLE_QUOTE = new PathSpec().moveTo(2.5, 0.5).lineTo(2.5, 2.5);
    public static final PathSpec OPEN_BRACKET = new PathSpec().moveTo(3.5, 0.5).lineTo(1.5, 2.5).lineTo(1.5, 4.5).moveTo(3.5, 6.5).moveTo(1.5, 4.5).lineTo(3.5, 6.5);
    public static final PathSpec CLOSE_BRACKET = new PathSpec().moveTo(1.5, 0.5).lineTo(3.5, 2.5).lineTo(3.5, 4.5).lineTo(1.5, 6.5);
    public static final PathSpec STAR = new PathSpec().moveTo(2.5, 0.5).lineTo(2.5, 6.5).moveTo(0.5, 1.5).lineTo(4.5, 5.5).moveTo(4.5, 1.5).lineTo(0.5, 5.5);
    public static final PathSpec PLUS = new PathSpec().moveTo(2.5, 1.5).lineTo(2.5, 5.5).moveTo(0.5, 3.5).lineTo(4.5, 3.5);
    public static final PathSpec COMMA = new PathSpec().moveTo(1.5, 7.5).lineTo(2.5, 6.5).lineTo(2.5, 5.5);
    public static final PathSpec MINUS = new PathSpec().moveTo(1.5, 3.5).lineTo(3.5, 3.5);
    public static final PathSpec DOT = new PathSpec().moveTo(2.5, 6.5).lineTo(2.5, 6.5);
    public static final PathSpec FORWARD_SLASH = new PathSpec().moveTo(0.5, 5.5).lineTo(4.5, 1.5);
    public static final PathSpec ZERO = new PathSpec().moveTo(2.5, 0.5).lineTo(0.5, 2.5).lineTo(0.5, 4.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5).lineTo(4.5, 2.5).lineTo(2.5, 0.5);
    public static final PathSpec ONE = new PathSpec().moveTo(1.5, 1.5).moveTo(1.5, 1.5).lineTo(2.5, 0.5).lineTo(2.5, 6.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5);
    public static final PathSpec TWO = new PathSpec().moveTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(2.5, 3.5).lineTo(0.5, 5.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final PathSpec THREE = new PathSpec().moveTo(0.5, 0.5).lineTo(4.5, 0.5).lineTo(4.5, 1.5).lineTo(2.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 4.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final PathSpec FOUR = new PathSpec().moveTo(3.5, 0.5).lineTo(0.5, 3.5).lineTo(0.5, 4.5).lineTo(4.5, 4.5).moveTo(3.5, 6.5).lineTo(3.5, 0.5);
    public static final PathSpec FIVE = new PathSpec().moveTo(4.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final PathSpec SIX = new PathSpec().moveTo(3.5, 0.5).lineTo(2.5, 0.5).lineTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 4.5).lineTo(3.5, 3.5).lineTo(0.5, 3.5);
    public static final PathSpec SEVEN = new PathSpec().moveTo(0.5, 0.5).lineTo(4.5, 0.5).lineTo(4.5, 1.5).lineTo(1.5, 4.5).lineTo(1.5, 6.5);
    public static final PathSpec EIGHT = new PathSpec().moveTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(1.5, 3.5).lineTo(0.5, 2.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).moveTo(0.5, 4.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 4.5).lineTo(3.5, 3.5).moveTo(1.5, 3.5).lineTo(0.5, 4.5);
    public static final PathSpec NINE = new PathSpec().moveTo(1.5, 6.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 2.5).lineTo(1.5, 3.5).lineTo(4.5, 3.5);
    public static final PathSpec COLON = new PathSpec().moveTo(2.5, 2.5).lineTo(2.5, 2.5).moveTo(2.5, 6.5).lineTo(2.5, 6.5);
    public static final PathSpec SEMICOLON = new PathSpec().moveTo(1.5, 7.5).lineTo(2.5, 6.5).lineTo(2.5, 5.5).moveTo(2.5, 2.5).lineTo(2.5, 2.5);
    public static final PathSpec LESS_THAN = new PathSpec().moveTo(3.5, 6.5).lineTo(0.5, 3.5).lineTo(3.5, 0.5);
    public static final PathSpec EQUALS = new PathSpec().moveTo(0.5, 2.5).lineTo(4.5, 2.5).moveTo(0.5, 4.5).lineTo(4.5, 4.5);
    public static final PathSpec GREATER_THAN = new PathSpec().moveTo(0.5, 6.5).lineTo(3.5, 3.5).lineTo(0.5, 0.5);
    public static final PathSpec QUESTION_MARK = new PathSpec().moveTo(2.5, 6.5).lineTo(2.5, 6.5).moveTo(2.5, 4.5).lineTo(2.5, 3.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5);
    public static final PathSpec AT = new PathSpec().moveTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 4.5).lineTo(2.5, 4.5).lineTo(2.5, 2.5).lineTo(4.5, 2.5);
    public static final PathSpec UPPER_A = new PathSpec().moveTo(0.5, 6.5).lineTo(0.5, 2.5).lineTo(2.5, 0.5).lineTo(4.5, 2.5).lineTo(4.5, 6.5).moveTo(0.5, 4.5).lineTo(4.5, 4.5);
    public static final PathSpec UPPER_B = new PathSpec().moveTo(0.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(0.5, 3.5).lineTo(0.5, 0.5).moveTo(0.5, 3.5).lineTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 4.5).lineTo(3.5, 3.5);
    public static final PathSpec UPPER_C = new PathSpec().moveTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 1.5).lineTo(1.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5);
    public static final PathSpec UPPER_D = new PathSpec().moveTo(3.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5);
    public static final PathSpec UPPER_E = new PathSpec().moveTo(4.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5).moveTo(3.5, 3.5).lineTo(0.5, 3.5);
    public static final PathSpec UPPER_F = new PathSpec().moveTo(4.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(0.5, 3.5).lineTo(3.5, 3.5);
    public static final PathSpec UPPER_G = new PathSpec().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5).lineTo(4.5, 4.5).lineTo(3.5, 4.5);
    public static final PathSpec UPPER_H = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(4.5, 0.5).lineTo(4.5, 6.5).moveTo(0.5, 3.5).lineTo(4.5, 3.5);
    public static final PathSpec UPPER_I = new PathSpec().moveTo(3.5, 0.5).lineTo(1.5, 0.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5).moveTo(2.5, 0.5).lineTo(2.5, 6.5);
    public static final PathSpec UPPER_J = new PathSpec().moveTo(4.5, 0.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final PathSpec UPPER_K = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(4.5, 0.5).lineTo(1.5, 3.5).moveTo(4.5, 6.5).lineTo(1.5, 3.5);
    public static final PathSpec UPPER_L = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final PathSpec UPPER_M = new PathSpec().moveTo(0.5, 6.5).lineTo(0.5, 0.5).lineTo(2.5, 2.5).lineTo(4.5, 0.5).lineTo(4.5, 6.5).moveTo(2.5, 3.5).lineTo(2.5, 2.5);
    public static final PathSpec UPPER_N = new PathSpec().moveTo(0.5, 6.5).lineTo(0.5, 0.5).moveTo(4.5, 6.5).lineTo(4.5, 0.5).moveTo(0.5, 1.5).lineTo(4.5, 5.5);
    public static final PathSpec UPPER_O = new PathSpec().moveTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5);
    public static final PathSpec UPPER_P = new PathSpec().moveTo(0.5, 6.5).lineTo(0.5, 0.5).lineTo(3.5, 0.5).lineTo(4.5, 1.5).lineTo(4.5, 2.5).lineTo(3.5, 3.5).lineTo(0.5, 3.5);
    public static final PathSpec UPPER_Q = new PathSpec().moveTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 6.5).lineTo(4.5, 4.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).moveTo(2.5, 4.5).lineTo(4.5, 6.5);
    public static final PathSpec UPPER_R = new PathSpec().moveTo(3.5, 0.5).lineTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(0.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 2.5).lineTo(4.5, 1.5).lineTo(3.5, 0.5).moveTo(4.5, 6.5).lineTo(1.5, 3.5);
    public static final PathSpec UPPER_S = new PathSpec().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(1.5, 0.5).lineTo(0.5, 1.5).lineTo(0.5, 2.5).lineTo(1.5, 3.5).lineTo(3.5, 3.5).lineTo(4.5, 4.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5);
    public static final PathSpec UPPER_T = new PathSpec().moveTo(0.5, 0.5).lineTo(4.5, 0.5).moveTo(2.5, 6.5).lineTo(2.5, 0.5);
    public static final PathSpec UPPER_U = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 0.5);
    public static final PathSpec UPPER_V = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 1.5).lineTo(2.5, 6.5).lineTo(4.5, 1.5).lineTo(4.5, 0.5);
    public static final PathSpec UPPER_W = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 5.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 0.5).moveTo(2.5, 4.5).lineTo(2.5, 5.5);
    public static final PathSpec UPPER_X = new PathSpec().moveTo(0.5, 6.5).lineTo(0.5, 5.5).moveTo(4.5, 6.5).lineTo(4.5, 5.5).moveTo(4.5, 0.5).lineTo(4.5, 1.5).moveTo(0.5, 0.5).lineTo(0.5, 1.5).moveTo(0.5, 1.5).lineTo(4.5, 5.5).moveTo(4.5, 1.5).lineTo(0.5, 5.5);
    public static final PathSpec UPPER_Y = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 1.5).moveTo(4.5, 0.5).lineTo(4.5, 1.5).moveTo(0.5, 1.5).lineTo(2.5, 3.5).moveTo(4.5, 1.5).lineTo(2.5, 3.5).moveTo(2.5, 3.5).lineTo(2.5, 6.5);
    public static final PathSpec UPPER_Z = new PathSpec().moveTo(0.5, 0.5).lineTo(4.5, 0.5).lineTo(4.5, 1.5).lineTo(0.5, 5.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final PathSpec LEFT_ARROW = new PathSpec().moveTo(4.5, 3.5).lineTo(0.5, 3.5).moveTo(2.5, 1.5).lineTo(0.5, 3.5).moveTo(2.5, 5.5).lineTo(0.5, 3.5);
    public static final PathSpec HALF = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 4.5).moveTo(2.5, 4.5).lineTo(3.5, 4.5).lineTo(4.5, 5.5).lineTo(2.5, 7.5).lineTo(2.5, 8.5).lineTo(4.5, 8.5);
    public static final PathSpec RIGHT_ARROW = new PathSpec().moveTo(0.5, 3.5).lineTo(4.5, 3.5).moveTo(2.5, 1.5).lineTo(4.5, 3.5).moveTo(2.5, 5.5).lineTo(4.5, 3.5);
    public static final PathSpec UP_ARROW = new PathSpec().moveTo(2.5, 1.5).lineTo(2.5, 5.5).moveTo(0.5, 3.5).lineTo(2.5, 1.5).moveTo(4.5, 3.5).lineTo(2.5, 1.5);
    public static final PathSpec UNDERSCORE = new PathSpec().moveTo(0.5, 3.5).lineTo(4.5, 3.5);
    public static final PathSpec POUND = new PathSpec().moveTo(4.5, 1.5).lineTo(3.5, 0.5).lineTo(2.5, 0.5).lineTo(1.5, 1.5).lineTo(1.5, 6.5).moveTo(0.5, 6.5).lineTo(4.5, 6.5).moveTo(0.5, 3.5).lineTo(2.5, 3.5);
    public static final PathSpec LOWER_A = new PathSpec().moveTo(1.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(1.5, 4.5).lineTo(4.5, 4.5);
    public static final PathSpec LOWER_B = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 3.5).lineTo(3.5, 2.5).lineTo(0.5, 2.5);
    public static final PathSpec LOWER_C = new PathSpec().moveTo(4.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(4.5, 2.5);
    public static final PathSpec LOWER_D = new PathSpec().moveTo(4.5, 0.5).lineTo(4.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(4.5, 2.5);
    public static final PathSpec LOWER_E = new PathSpec().moveTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 4.5).lineTo(0.5, 4.5);
    public static final PathSpec LOWER_F = new PathSpec().moveTo(3.5, 0.5).lineTo(2.5, 1.5).lineTo(2.5, 6.5).moveTo(1.5, 3.5).lineTo(3.5, 3.5);
    public static final PathSpec LOWER_G = new PathSpec().moveTo(1.5, 8.5).lineTo(3.5, 8.5).lineTo(4.5, 7.5).lineTo(4.5, 2.5).lineTo(1.5, 2.5).lineTo(0.5, 3.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5);
    public static final PathSpec LOWER_H = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 6.5).moveTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 6.5);
    public static final PathSpec LOWER_I = new PathSpec().moveTo(2.5, 0.5).lineTo(2.5, 0.5).moveTo(1.5, 2.5).lineTo(2.5, 2.5).lineTo(2.5, 6.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5);
    public static final PathSpec LOWER_J = new PathSpec().moveTo(2.5, 0.5).lineTo(2.5, 0.5).moveTo(2.5, 2.5).lineTo(2.5, 7.5).lineTo(1.5, 8.5);
    public static final PathSpec LOWER_K = new PathSpec().moveTo(1.5, 0.5).lineTo(1.5, 6.5).moveTo(4.5, 2.5).lineTo(1.5, 5.5).moveTo(4.5, 6.5).lineTo(1.5, 3.5);
    public static final PathSpec LOWER_L = new PathSpec().moveTo(1.5, 0.5).lineTo(2.5, 0.5).lineTo(2.5, 6.5).moveTo(1.5, 6.5).lineTo(3.5, 6.5);
    public static final PathSpec LOWER_M = new PathSpec().moveTo(0.5, 6.5).lineTo(0.5, 2.5).lineTo(1.5, 2.5).lineTo(2.5, 3.5).lineTo(2.5, 6.5).moveTo(4.5, 6.5).lineTo(4.5, 3.5).lineTo(3.5, 2.5).lineTo(2.5, 3.5);
    public static final PathSpec LOWER_N = new PathSpec().moveTo(0.5, 6.5).lineTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 6.5);
    public static final PathSpec LOWER_O = new PathSpec().moveTo(1.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(1.5, 6.5).lineTo(0.5, 5.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5);
    public static final PathSpec LOWER_P = new PathSpec().moveTo(0.5, 8.5).lineTo(0.5, 2.5).lineTo(3.5, 2.5).lineTo(4.5, 3.5).lineTo(4.5, 5.5).lineTo(3.5, 6.5).lineTo(0.5, 6.5);
    public static final PathSpec LOWER_Q = new PathSpec().moveTo(4.5, 8.5).lineTo(4.5, 2.5).lineTo(1.5, 2.5).lineTo(0.5, 3.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5);
    public static final PathSpec LOWER_R = new PathSpec().moveTo(1.5, 6.5).lineTo(1.5, 2.5).moveTo(4.5, 2.5).lineTo(3.5, 2.5).lineTo(1.5, 4.5);
    public static final PathSpec LOWER_S = new PathSpec().moveTo(0.5, 6.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(3.5, 4.5).lineTo(1.5, 4.5).lineTo(0.5, 3.5).lineTo(1.5, 2.5).lineTo(4.5, 2.5);
    public static final PathSpec LOWER_T = new PathSpec().moveTo(2.5, 0.5).lineTo(2.5, 5.5).lineTo(3.5, 6.5).moveTo(3.5, 2.5).lineTo(1.5, 2.5);
    public static final PathSpec LOWER_U = new PathSpec().moveTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5).lineTo(4.5, 2.5);
    public static final PathSpec LOWER_V = new PathSpec().moveTo(0.5, 2.5).lineTo(2.5, 6.5).lineTo(4.5, 2.5);
    public static final PathSpec LOWER_W = new PathSpec().moveTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(2.5, 5.5).lineTo(3.5, 6.5).lineTo(4.5, 5.5).lineTo(4.5, 2.5).moveTo(2.5, 4.5).lineTo(2.5, 5.5);
    public static final PathSpec LOWER_X = new PathSpec().moveTo(0.5, 2.5).lineTo(4.5, 6.5).moveTo(4.5, 2.5).lineTo(0.5, 6.5);
    public static final PathSpec LOWER_Y = new PathSpec().moveTo(0.5, 2.5).lineTo(0.5, 5.5).lineTo(1.5, 6.5).lineTo(4.5, 6.5).lineTo(4.5, 2.5).moveTo(1.5, 8.5).lineTo(3.5, 8.5).lineTo(4.5, 7.5).lineTo(4.5, 2.5);
    public static final PathSpec LOWER_Z = new PathSpec().moveTo(0.5, 2.5).lineTo(4.5, 2.5).lineTo(0.5, 6.5).lineTo(4.5, 6.5);
    public static final PathSpec QUARTER = new PathSpec().moveTo(0.5, 0.5).lineTo(0.5, 4.5).moveTo(4.5, 4.5).lineTo(2.5, 6.5).lineTo(2.5, 7.5).lineTo(4.5, 7.5).moveTo(4.5, 4.5).lineTo(4.5, 8.5);
    public static final PathSpec PIPE = new PathSpec().moveTo(1.5, 0.5).lineTo(1.5, 6.5).moveTo(3.5, 0.5).lineTo(3.5, 6.5);
    public static final PathSpec THREE_QUARTERS = new PathSpec().moveTo(0.5, 0.5).lineTo(1.5, 0.5).lineTo(2.5, 1.5).lineTo(1.5, 2.5).lineTo(0.5, 2.5).moveTo(0.5, 4.5).lineTo(1.5, 4.5).lineTo(2.5, 3.5).lineTo(1.5, 2.5).moveTo(4.5, 4.5).lineTo(2.5, 6.5).lineTo(2.5, 7.5).lineTo(4.5, 7.5).lineTo(4.5, 4.5).moveTo(4.5, 8.5).lineTo(4.5, 4.5);
    public static final PathSpec DIVIDE = new PathSpec().moveTo(0.5, 3.5).lineTo(4.5, 3.5).moveTo(2.5, 1.5).lineTo(2.5, 1.5).moveTo(2.5, 5.5).lineTo(2.5, 5.5);

    public static final PathSpec BLOCK = new PathSpec();
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

    public static BufferedImage createCharacterImage(final PathSpec p, final Color colour) {
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
