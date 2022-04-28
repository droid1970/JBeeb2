package com.jbeeb.display;

import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.memory.Memory;
import com.jbeeb.teletext.TeletextCharacterDefinitions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jbeeb.teletext.TeletextConstants.TELETEXT_CHAR_HEIGHT;
import static com.jbeeb.teletext.TeletextConstants.TELETEXT_CHAR_WIDTH;

public class TeletextDisplayRenderer extends AbstractDisplayRenderer {

    private Map<Integer, List<BufferedImage>> teletextCharImages = new HashMap<>();

    private static final Color[] COLORS = {
            Color.BLACK,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.BLUE,
            Color.MAGENTA,
            Color.CYAN,
            Color.WHITE
    };

    private static final int T_BLACK_TEXT = 128;
    private static final int T_RED_TEXT = 129;
    private static final int T_GREEN_TEXT = 130;
    private static final int T_YELLOW_TEXT = 131;
    private static final int T_BLUE_TEXT = 132;
    private static final int T_MAGENTA_TEXT = 133;
    private static final int T_CYAN_TEXT = 134;
    private static final int T_WHITE_TEXT = 135;

    private static final int T_BLACK_GRAPHICS = 144;
    private static final int T_RED_GRAPHICS = 145;
    private static final int T_GREEN_GRAPHICS = 146;
    private static final int T_YELLOW_GRAPHICS = 147;
    private static final int T_BLUE_GRAPHICS = 148;
    private static final int T_MAGENTA_GRAPHICS = 149;
    private static final int T_CYAN_GRAPHICS = 150;
    private static final int T_WHITE_GRAPHICS = 151;

    private static final int T_NEW_BACKGROUND = 157;

    private static void paintGraphicsCharacter(final Graphics2D g, final int code, final boolean gap, final int x, final int y, final int width, final int height) {
        if (!isGraphicsCharacter(code)) {
            return;
        }
        final int bits = (code >= 224) ? (code - 192) : (code - 160);
        final int pw = width / 2;
        final int ph = (int) Math.ceil(height / 3.0);
        final int lastPh = (TELETEXT_CHAR_HEIGHT - (ph * 2));

        if ((bits & 1) != 0) {
            g.fillRect(x, y, pw, ph);
        }
        if ((bits & 2) != 0) {
            g.fillRect(x + pw, y, pw, ph);
        }
        if ((bits & 4) != 0) {
            g.fillRect(x, y + ph, pw, ph);
        }
        if ((bits & 8) != 0) {
            g.fillRect(x + pw, y + ph, pw, ph);
        }
        if ((bits & 16) != 0) {
            g.fillRect(x, y + ph + ph, pw, lastPh);
        }
        if ((bits & 32) != 0) {
            g.fillRect(x + pw, y + ph + ph, pw, lastPh);
        }
    }

    public TeletextDisplayRenderer(Memory memory, SystemVIA systemVIA, CRTC6845 crtc6845, VideoULA videoULA) {
        super(memory, systemVIA, crtc6845, videoULA);
    }

    @Override
    public void refreshImage(DisplayMode mode, BufferedImage img) {
        final Graphics2D g = img.createGraphics();

        final int leftMargin = (img.getWidth() - (TELETEXT_CHAR_WIDTH * 40)) / 2;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        final int unadjustedStartAddress = crtc6845.getScreenStartAddress();
        int address = adjustMode7Address(unadjustedStartAddress);
        final int cursorAddress = adjustMode7Address(crtc6845.getCursoeAddress());

        Rectangle cursorRect = null;
        final List<BufferedImage> whiteCharImages = getTeletextCharacterImages(7);
        Color background;
        List<BufferedImage> charImages;
        int foregroundIndex;
        Color graphicsColour;

        for (int y = 0; y < (TELETEXT_CHAR_HEIGHT * 25); y += TELETEXT_CHAR_HEIGHT) {

            background = Color.BLACK;
            foregroundIndex = 7;
            charImages = whiteCharImages;
            graphicsColour = null;

            for (int x = leftMargin; x < (TELETEXT_CHAR_WIDTH * 40 + leftMargin); x += TELETEXT_CHAR_WIDTH) {
                if (crtc6845.isCursorOn() && address == cursorAddress) {
                    cursorRect = new Rectangle(x, y, TELETEXT_CHAR_WIDTH, TELETEXT_CHAR_HEIGHT);
                }

                final int v = memory.readByte(address);

                //
                // Paint background
                //
                g.setColor(background);
                g.fillRect(x, y, TELETEXT_CHAR_WIDTH, TELETEXT_CHAR_HEIGHT);

                if (isTextColourControl(v)) {
                    //
                    // Change foreground colour
                    //
                    foregroundIndex = getTextColourIndex(v);
                    charImages = getTeletextCharacterImages(foregroundIndex);
                    graphicsColour = null;
                } else if (isGraphicsColourControl(v)) {
                    graphicsColour = COLORS[getGraphicsColourIndex(v)];
                } else if (v == T_NEW_BACKGROUND) {
                    //
                    // Set background to current foreground
                    //
                    background = COLORS[foregroundIndex];
                } else {
                    //
                    // Paint a character
                    //
                    if (isGraphicsCharacter(v) && graphicsColour != null) {
                        g.setColor(graphicsColour);
                        paintGraphicsCharacter(g, v, false, x, y, TELETEXT_CHAR_WIDTH, TELETEXT_CHAR_HEIGHT);
                    } else if (isTextCharacter(v)) {
                        final int charIndex = computeCharIndex(v);
                        if (charImages != null && charIndex < charImages.size()) {
                            //g.drawImage(charImages.get(charIndex), x, y, TELETEXT_CHAR_WIDTH, TELETEXT_CHAR_HEIGHT, null);
                            g.drawImage(charImages.get(charIndex), x, y + 2, null);
                        } else {
                            throw new IllegalStateException();
                        }
                    }
                }

                address++;
                if (address >= 0x8000) {
                    address -= 1024;
                }
            }
        }

        paintCursor(g, cursorRect, TELETEXT_CHAR_HEIGHT / 8);
    }

    private List<BufferedImage> getTeletextCharacterImages(final int colorIndex) {
        List<BufferedImage> ret = teletextCharImages.get(colorIndex);
        if (ret == null) {
            ret = createTeletextCharImages(COLORS[colorIndex]);
            teletextCharImages.put(colorIndex, ret);
        }
        return ret;
    }

    private static List<BufferedImage> createTeletextCharImages(final Color color) {
        final List<BufferedImage> ret = new ArrayList<>();
        for (int i = 32; i <= 127; i++) {
            final BufferedImage charImage = TeletextCharacterDefinitions.createImage(i - 32, color);
            ret.add(charImage);
        }
        return ret;
    }

    private static boolean isGraphicsColourControl(final int code) {
        return (code >= 144 && code <= 151);
    }

    private static int getGraphicsColourIndex(final int colourCode) {
        return colourCode - 144;
    }


    private static boolean isTextColourControl(final int code) {
        return (code >= 128 && code <= 135);
    }

    private static int getTextColourIndex(final int colourCode) {
        return colourCode - 128;
    }


    private static boolean isGraphicsCharacter(final int code) {
        return (code >= 160 && code <= 191) || (code >= 224 && code <= 255);
    }

    private static boolean isTextCharacter(final int code) {
        return (code >= 32 && code <= 127) || (code >= 160 && code <= 255);
    }

    private static int computeCharIndex(final int code) {
        if (code >= 32 && code <= 127) {
            return code - 32;
        }
        if (code >= 160 && code <= 255) {
            return code - 160;
        }
        throw new IllegalStateException();
    }

    private static int adjustMode7Address(final int unadjustedAddress) {
        final int addrH = ((((unadjustedAddress >>> 8) & 0xFF) ^ 0x20) + 0x74);
        return (unadjustedAddress & 0xFF) | ((addrH & 0xFF) << 8);
    }
}
