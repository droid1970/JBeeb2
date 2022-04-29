package com.jbeeb.screen;

import com.jbeeb.device.CRTC6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.main.BBCMicro;
import com.jbeeb.memory.Memory;
import com.jbeeb.teletext.TeletextScreenRenderer;
import com.jbeeb.util.ClockListener;
import com.jbeeb.util.SystemStatus;
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

public final class Screen implements ClockListener {

    private static final int IMAGE_BORDER_SIZE = 32;
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 512;

    private final SystemStatus systemStatus;
    private final BBCMicro bbc;
    private final VideoULA videoULA;
    private final CRTC6845 crtc6845;
    private final SystemVIA systemVIA;
    private final List<IntConsumer> keyUpListeners = new ArrayList<>();
    private final List<BiConsumer<Integer, Boolean>> keyDownListeners = new ArrayList<>();

    private final ScreenRenderer graphicsRenderer;
    private final TeletextScreenRenderer teletextRenderer;

    private final BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    private long cycleCount = 0L;
    private long totalRefreshTimeNanos = 0L;

    private final ImageComponent imageComponent;

    public Screen(
            final SystemStatus systemStatus,
            final BBCMicro bbc,
            final Memory memory,
            final VideoULA videoULA,
            final CRTC6845 crtc6845,
            final SystemVIA systemVIA
    ) {
        this.systemStatus = Objects.requireNonNull(systemStatus);
        this.bbc = Objects.requireNonNull(bbc);
        this.videoULA = Objects.requireNonNull(videoULA);
        this.crtc6845 = Objects.requireNonNull(crtc6845);
        this.systemVIA = Objects.requireNonNull(systemVIA);
        this.graphicsRenderer = new GraphicsModeScreenRenderer(this, memory, systemVIA, crtc6845, videoULA);
        this.teletextRenderer = new TeletextScreenRenderer(memory, systemVIA, crtc6845, videoULA);
        SwingUtilities.invokeLater(this::createAndShowUI);
        this.imageComponent = new ImageComponent();
    }

    @Override
    public void tick() {
        if (currentMode != null && renderer != null && renderer.isClockBased()) {
            renderer.tick(currentMode, image);
        }
    }

    public void imageReady() {
        imageComponent.repaint();
    }

    public void addKeyUpListener(IntConsumer l) {
        keyUpListeners.add(l);
    }

    public void addKeyDownListener(BiConsumer<Integer, Boolean> l) {
        keyDownListeners.add(l);
    }

    public void vsync() {
        final DisplayMode mode = Util.inferDisplayMode(videoULA, crtc6845);
        if (mode != currentMode) {
            currentMode = mode;
            if (mode == null) {
                renderer = null;
            } else {
                if (mode == DisplayMode.MODE7) {
                    renderer = teletextRenderer;
                } else {
                    renderer = graphicsRenderer;
                }
            }
        }
        if (renderer != null && renderer.isClockBased()) {
            renderer.vsync(currentMode);
        } else {
            SwingUtilities.invokeLater(imageComponent::repaint);
        }
    }

    private void createAndShowUI() {
        final JFrame frame = new JFrame("JavaBeeb");

        frame.getContentPane().setBackground(new Color(32, 32, 32));
        frame.getContentPane().add(BorderLayout.CENTER, imageComponent);

        final StatusBar statusBar = new StatusBar();
        frame.getContentPane().add(BorderLayout.SOUTH, statusBar);

        final Timer refreshTimer = new Timer(1000, e -> statusBar.refresh());
        refreshTimer.start();

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        imageComponent.setFocusable(true);
        SwingUtilities.invokeLater(imageComponent::requestFocus);
    }

    private final class StatusBar extends JComponent {

        final JLabel mhzLabel;
        final JLabel screenLabel;

        StatusBar() {
            setOpaque(true);
            setBackground(Color.DARK_GRAY);
            setBorder(new EmptyBorder(4, 8, 8, 4));
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

            final JButton saveStateButton = createButton("SAVE");
            saveStateButton.addActionListener(e -> {
                bbc.saveState();
            });
            add(saveStateButton);

            add(Box.createRigidArea(new Dimension(4, 0)));

            final JButton restoreStateButton = createButton("RESTORE");
            restoreStateButton.addActionListener(e -> {
                bbc.restoreState();
            });
            add(restoreStateButton);

            add(Box.createRigidArea(new Dimension(4, 0)));

            mhzLabel = createLabel();
            add(mhzLabel);

            screenLabel = createLabel();
            add(Box.createRigidArea(new Dimension(4, 0)));
            add(screenLabel);



            add(Box.createGlue());
        }

        JButton createButton(final String text) {
            final JButton button = new JButton(text);
            button.setFocusable(false);
            button.setForeground(Color.WHITE);
            button.setContentAreaFilled(false);
            return button;
        }

        JLabel createLabel() {
            final JLabel label = new JLabel();
            label.setOpaque(false);
            label.setForeground(Color.LIGHT_GRAY);
            return label;
        }

        void refresh() {
            final String mhzString = systemStatus.getString(SystemStatus.KEY_MILLION_CYCLES_PER_SECOND, "?");
            mhzLabel.setText("clockrate (mhz) = " + mhzString);
            final String displayRefreshString = systemStatus.getString(SystemStatus.KEY_AVG_DISPLAY_REFRESH_TIME_MILLIS, "?");
            screenLabel.setText("display (ms) = " + displayRefreshString);
        }

        @Override
        public void paintComponent(final Graphics g) {
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private ScreenRenderer renderer;
    private DisplayMode currentMode;

    private final class ImageComponent extends JComponent {
        public ImageComponent() {
            setOpaque(false);
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(IMAGE_WIDTH + IMAGE_BORDER_SIZE * 2, IMAGE_HEIGHT + IMAGE_BORDER_SIZE * 2));
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

            if (currentMode == null || renderer == null) {
                return;
            }

            if (!renderer.isClockBased()) {
                // Renderer can produce the whole image in one go
                renderer.refreshWholeImage(currentMode, image);
            }

            if (renderer.isImageReady()) {
                final int iw = image.getWidth() + IMAGE_BORDER_SIZE * 2;
                final int ih = image.getHeight() + IMAGE_BORDER_SIZE * 2;

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
                final Rectangle ir = new Rectangle(px + IMAGE_BORDER_SIZE, py + IMAGE_BORDER_SIZE, pw - IMAGE_BORDER_SIZE * 2, ph - IMAGE_BORDER_SIZE * 2);
                g.setColor(Color.BLACK);
                g.fillRect(ir.x - IMAGE_BORDER_SIZE / 2, ir.y - IMAGE_BORDER_SIZE / 2, ir.width + IMAGE_BORDER_SIZE, ir.height + IMAGE_BORDER_SIZE);
                g.setColor(this.hasFocus() ? Color.GREEN : Color.GRAY);
                g.drawRect(ir.x - IMAGE_BORDER_SIZE / 2, ir.y - IMAGE_BORDER_SIZE / 2, ir.width + IMAGE_BORDER_SIZE - 1, ir.height + IMAGE_BORDER_SIZE - 1);
                g.drawImage(image, ir.x, ir.y, ir.width, ir.height, null);
            }

            totalRefreshTimeNanos += System.nanoTime() - startTime;
            cycleCount++;

            if ((cycleCount % 100) == 0) {
                final long avgNanos = totalRefreshTimeNanos / cycleCount;
                final String fmt = Util.formatDurationNanosAsMillis(avgNanos);
                systemStatus.putString(SystemStatus.KEY_AVG_DISPLAY_REFRESH_TIME_MILLIS, fmt);

                cycleCount = 0L;
                totalRefreshTimeNanos = 0L;
            }
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
