package com.jbeeb.display;

import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.memory.Memory;
import com.jbeeb.util.Util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

public final class Display {

    private static final boolean VERBOSE = false;

    private static final int IMAGE_BORDER_SIZE = 16;

    private final VideoULA videoULA;
    private final CRTC6845 crtc6845;
    private final SystemVIA systemVIA;
    private List<IntConsumer> keyUpListeners = new ArrayList<>();
    private List<BiConsumer<Integer, Boolean>> keyDownListeners = new ArrayList<>();

    private final DisplayRenderer graphicsRenderer;
    private final TeletextDisplayRenderer teletextRenderer;

    private final BufferedImage image = new BufferedImage(640, 512, BufferedImage.TYPE_INT_RGB);
    private long cycle = 0L;

    private final ImageComponent imageComponent;

    public Display(final Memory memory, final VideoULA videoULA, final CRTC6845 crtc6845, final SystemVIA systemVIA) {
        this.videoULA = Objects.requireNonNull(videoULA);
        this.crtc6845 = Objects.requireNonNull(crtc6845);
        this.systemVIA = Objects.requireNonNull(systemVIA);
        this.graphicsRenderer = new GraphicsModeDisplayRenderer(memory, systemVIA, crtc6845, videoULA);
        this.teletextRenderer = new TeletextDisplayRenderer(memory, systemVIA, crtc6845, videoULA);
        SwingUtilities.invokeLater(() -> {
            createAndShowUI();
        });
        this.imageComponent = new ImageComponent();
    }

    public void addKeyUpListener(IntConsumer l) {
        keyUpListeners.add(l);
    }

    public void addKeyDownListener(BiConsumer<Integer, Boolean> l) {
        keyDownListeners.add(l);
    }

    public void vsync() {
        SwingUtilities.invokeLater(() -> imageComponent.repaint());
    }

    private void createAndShowUI() {
        final JFrame frame = new JFrame("JavaBeeb");
        ((JComponent) frame.getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));
        frame.getContentPane().setBackground(new Color(32, 32, 32));
        frame.getContentPane().add(BorderLayout.CENTER, imageComponent);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> imageComponent.requestFocus());
    }

    private final class ImageComponent extends JComponent {
        public ImageComponent() {
            setOpaque(false);
            setBorder(new EmptyBorder(IMAGE_BORDER_SIZE, IMAGE_BORDER_SIZE, IMAGE_BORDER_SIZE, IMAGE_BORDER_SIZE));
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(640,512));
            addKeyListener(new KeyHandler());
        }

        @Override
        public void paintComponent(Graphics g) {
            final long startTime = System.nanoTime();
            final Rectangle r = SwingUtilities.calculateInnerArea(this, null);
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            final DisplayMode mode = Util.inferDisplayMode(videoULA, crtc6845);
            if (mode == null) {
                return;
            }

            final DisplayRenderer renderer = (mode == DisplayMode.MODE7) ? teletextRenderer : graphicsRenderer;
            renderer.refreshImage(mode, image);
            if (image != null) {
                if (false) {
                    g.drawImage(image, 0, 0, null);
                    return;
                }
                final int iw = image.getWidth();
                final int ih = image.getHeight();

                final int rw = r.width;
                final int rh = r.height;
                final double raspect = (double) rw / rh;
                final double iaspect = (double) iw / ih;
                final int px;
                final int py;
                final int pw;
                final int ph;
                if (raspect < iaspect) {
                    pw = rw;
                    ph = (int) (pw / iaspect);
                    px = r.x;
                    py = r.y + (rh - ph) / 2;
                } else {
                    ph = rh;
                    pw = (int) (iaspect * ph);
                    px = r.x + (rw - pw) / 2;
                    py = r.y;
                }
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (!isOpaque()) {
                    g.setColor(getBackground());
                    g.fillRect(px - IMAGE_BORDER_SIZE, py - IMAGE_BORDER_SIZE, pw + IMAGE_BORDER_SIZE * 2, ph + IMAGE_BORDER_SIZE * 2);
                    g.setColor(Color.GRAY);
                    g.drawRect(px - IMAGE_BORDER_SIZE, py - IMAGE_BORDER_SIZE, pw + IMAGE_BORDER_SIZE * 2 - 1, ph + IMAGE_BORDER_SIZE * 2 - 1);
                }
                g.drawImage(image, px, py, pw, ph, null);
            }

            if (VERBOSE && (cycle % 100) == 0) {
                System.err.println("Updated display in " + Util.formatDurationNanosAsMillis(System.nanoTime() - startTime) + "ms");
            }
            cycle++;
        }
    }

    private final class KeyHandler extends KeyAdapter {

        private final HashSet<Integer> pressedKeys = new HashSet<>();

        @Override
        public void keyPressed(KeyEvent e) {
            final int code = e.getKeyCode();
            if (!pressedKeys.contains(code)) {
                pressedKeys.add(code);
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
                    final String text = getCopiedText();
                    if (text != null && !text.isEmpty()) {
                        typeText(text);
                    }
                } else {
                    keyDownListeners.forEach(l -> l.accept(code, e.isShiftDown()));
                }
            }
        }

        private void typeText(final String text) {
            systemVIA.keyUp(KeyEvent.VK_CONTROL);
            final Queue<Runnable> runnables = new LinkedList<>();
            for (char c : text.toCharArray()) {
                runnables.add(() -> systemVIA.characterDown(c));
                runnables.add(() -> systemVIA.characterUp(c));
            }
            final AtomicReference<Timer> timerRef = new AtomicReference<>();
            final Timer timer = new Timer(50, e -> {
                final Runnable r = runnables.poll();
                if (r != null) {
                    r.run();
                } else {
                    timerRef.get().stop();
                }
            });
            timerRef.set(timer);
            timer.start();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            final int code = e.getKeyCode();
            pressedKeys.remove(code);
            keyUpListeners.forEach(l -> l.accept(code));
        }
    }

    private static String getCopiedText() {
        String ret = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        boolean hasStringText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasStringText) {
            try {
                ret = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                ret = "";
            }
        }
        return ret;
    }
}
