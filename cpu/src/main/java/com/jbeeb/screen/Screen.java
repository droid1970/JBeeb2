package com.jbeeb.screen;

import com.jbeeb.clock.ClockSpeed;
import com.jbeeb.device.Crtc6845;
import com.jbeeb.device.SystemVIA;
import com.jbeeb.device.VideoULA;
import com.jbeeb.main.BBCMicro;
import com.jbeeb.memory.Memory;
import com.jbeeb.teletext.TeletextScreenRenderer;
import com.jbeeb.clock.ClockListener;
import com.jbeeb.util.SystemStatus;
import com.jbeeb.util.Util;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

public final class Screen implements ClockListener {

    private static final int IMAGE_BORDER_SIZE = 32;
    private static final int IMAGE_WIDTH = 640;
    private static final int IMAGE_HEIGHT = 512;

    private static final double NORMAL_CLOCK_RATE = 2.0;
    private static final double MIN_NORMAL_CLOCK_RATE = NORMAL_CLOCK_RATE * 0.975;
    private static final double MAX_NORMAL_CLOCK_RATE = NORMAL_CLOCK_RATE * 1.05;

    private static final Color LED_ON_COLOUR = Color.RED;
    private static final Color LED_OFF_COLOUR = Color.GRAY;
    private static final Color NORMAL_CLOCK_RATE_COLOUR = Color.GREEN.darker();
    private static final Color FAST_CLOCK_RATE_COLOUR = Color.ORANGE;
    private static final Color SLOW_CLOCK_RATE_COLOUR = Color.ORANGE;

    private final SystemStatus systemStatus;
    private final BBCMicro bbc;
    private final VideoULA videoULA;
    private final SystemVIA systemVIA;
    private final List<IntConsumer> keyUpListeners = new ArrayList<>();
    private final List<BiConsumer<Integer, Boolean>> keyDownListeners = new ArrayList<>();

    private ScreenRenderer renderer;
    private final ScreenRenderer graphicsRenderer;
    private final TeletextScreenRenderer teletextRenderer;

    private final BufferedImage image0 = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage image1 = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    private long cycleCount = 0L;
    private long totalRefreshTimeNanos = 0L;

    private final ImageComponent imageComponent;

    private Timer pausedTimer;
    private volatile boolean paused;

    public Screen(
            final SystemStatus systemStatus,
            final BBCMicro bbc,
            final Memory memory,
            final VideoULA videoULA,
            final Crtc6845 crtc6845,
            final SystemVIA systemVIA
    ) {
        this.systemStatus = Objects.requireNonNull(systemStatus);
        this.bbc = Objects.requireNonNull(bbc);
        this.videoULA = Objects.requireNonNull(videoULA);
        this.systemVIA = Objects.requireNonNull(systemVIA);
        this.graphicsRenderer = new GraphicsModeScreenRenderer(this, memory, systemVIA, crtc6845, videoULA);
        this.teletextRenderer = new TeletextScreenRenderer(memory, systemVIA, crtc6845, videoULA);
        this.imageComponent = new ImageComponent();

        SwingUtilities.invokeLater(this::createAndShowUI);
    }

    private int imageIndex;
    private SystemPalette systemPalette = SystemPalette.DEFAULT;

    private BufferedImage getImageToPaint() {
        return (imageIndex & 1) == 0 ? image0 : image1;
    }

    private BufferedImage getImageToShow() {
        return (imageIndex & 1) != 0 ? image0 : image1;
    }

    @Override
    public void tick(final ClockSpeed clockSpeed, final long elapsedNanos) {
        if (renderer != null && renderer.isClockBased()) {
            renderer.tick(getImageToPaint(), clockSpeed, elapsedNanos);
        }
    }

    public void imageReady(final long timeNanos) {
        swapImages();
        totalRefreshTimeNanos += timeNanos;
        imageComponent.repaint();
    }

    private void swapImages() {
        imageIndex++;
    }

    public void addKeyUpListener(IntConsumer l) {
        keyUpListeners.add(l);
    }

    public void addKeyDownListener(BiConsumer<Integer, Boolean> l) {
        keyDownListeners.add(l);
    }

    public void verticalSync() {
        if (videoULA.isTeletext()) {
            renderer = teletextRenderer;
        } else {
            renderer = graphicsRenderer;
        }

        if (renderer != null && renderer.isClockBased()) {
            final BufferedImage image = getImageToPaint();
            Util.fillRect(image.getWritableTile(0, 0).getDataBuffer(), systemPalette.getColour(0).getRGB(), 0, 0, image.getWidth(), image.getHeight(), image.getWidth());
            renderer.vsync();
        } else {
            SwingUtilities.invokeLater(imageComponent::repaint);
        }
    }

    private void createAndShowUI() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        final var frame = new JFrame("JavaBeeb");
        //frame.setUndecorated(true);
        frame.getContentPane().setBackground(systemPalette.getColour(0));
        frame.getContentPane().add(BorderLayout.CENTER, imageComponent);

        final StatusBar statusBar = new StatusBar();
        frame.getContentPane().add(BorderLayout.SOUTH, statusBar);

        final Timer refreshTimer = new Timer(1000, e -> statusBar.refresh());
        refreshTimer.start();

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //frame.setSize(screenSize);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //frame.setLocation(0, 0);
        imageComponent.setFocusable(true);
        SwingUtilities.invokeLater(imageComponent::requestFocus);
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            cancelPausedTimer();
            pausedTimer = new Timer(250, e -> {
                imageComponent.repaint();
            });
            pausedTimer.start();
        } else {
            cancelPausedTimer();
        }
    }

    private void cancelPausedTimer() {
        if (pausedTimer != null) {
            pausedTimer.stop();
            pausedTimer = null;
        }
    }

    private final class StatusBar extends JComponent {

        final ClockIcon clockIcon;
        final JLabel clockLabel;

        final JLabel screenLabel;
        final JLabel capsLockLabel;
        final LedIcon capsLockIcon;
        boolean verbose = false;

        StatusBar() {
            setOpaque(true);
            setBackground(Color.DARK_GRAY);
            setBorder(new EmptyBorder(4, 8, 8, 4));
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

            final var saveStateButton = createButton("SAVE");
            saveStateButton.addActionListener(e -> {
                bbc.saveState();
            });
            add(saveStateButton);

            final var restoreStateButton = createButton("RESTORE");
            restoreStateButton.addActionListener(e -> {
                bbc.restoreState();
            });
            add(Box.createRigidArea(new Dimension(4,0)));
            add(restoreStateButton);

            final var verboseCheckbox = createCheckbox("verbose");
            verboseCheckbox.addActionListener(e -> {
                verbose = !verbose;
                bbc.getCpu().setVerboseCondition(verbose ? () -> bbc.getCpu().getPC() < 0x8000 : () -> false);
                bbc.getCpu().setFetchDelayMillis(verbose ? 1 : 0);
                bbc.getCpu().setFetchDelayCondition(verbose ? cpu -> cpu.getPC() < 0x8000 : null);
            });
            add(Box.createRigidArea(new Dimension(4,0)));
            add(verboseCheckbox);

            //
            // Clock speed
            //
            final JComboBox<ClockSpeed> speedCombo = new JComboBox<>(ClockSpeed.getStandardValues());
            speedCombo.setPreferredSize(new Dimension(128, 18));
            speedCombo.setMinimumSize(new Dimension(128, 18));
            speedCombo.setMaximumSize(new Dimension(128, 18));
            speedCombo.setSelectedItem(bbc.getClock().getClockSpeed());
            speedCombo.addActionListener(e -> {
                bbc.getClock().setClockSpeed(speedCombo.getItemAt(speedCombo.getSelectedIndex()));
            });
//            add(Box.createRigidArea(new Dimension(4,0)));
//            add(speedCombo);

            add(Box.createGlue());

            //
            // Labels
            //
            add(Box.createRigidArea(new Dimension(8,0)));
            clockIcon = new ClockIcon(12, 16, 1);
            clockIcon.setColour(Color.BLACK);
            clockLabel = createLabel();
            clockLabel.setHorizontalAlignment(JLabel.RIGHT);
            clockLabel.setIcon(clockIcon);
            clockLabel.setText("00.00 Mhz");
            clockLabel.setPreferredSize(clockLabel.getPreferredSize());
            clockLabel.setText("");

            add(clockLabel);

            capsLockLabel = createLabel();
            capsLockLabel.setText("caps");
            capsLockIcon = new LedIcon(12, 16, 1);
            capsLockIcon.setOn(bbc.getSystemVIA().isCapslockLightOn());
            capsLockLabel.setIcon(capsLockIcon);
            bbc.getSystemVIA().setCapsLockChangedCallback(() -> {
                capsLockIcon.setOn(bbc.getSystemVIA().isCapslockLightOn());
                capsLockLabel.repaint();
            });
            add(Box.createRigidArea(new Dimension(8,0)));
            add(capsLockLabel);

            screenLabel = createLabel();
//            add(Box.createRigidArea(new Dimension(8,0)));
//            add(screenLabel);

            final Dimension currentPreferredSize = getPreferredSize();
            setPreferredSize(new Dimension(currentPreferredSize.width, currentPreferredSize.height - 6));
        }

        JButton createButton(final String text) {
            final JButton button = new JButton(text);
            button.setFocusable(false);
            button.setForeground(Color.BLACK);
            button.setContentAreaFilled(true);
            return button;
        }

        JCheckBox createCheckbox(final String text) {
            final JCheckBox button = new JCheckBox(text);
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
            try {
                clockIcon.setRate(Double.parseDouble(mhzString));
            } catch (Exception ex) {
                // Ignored
            }
            clockLabel.setText(mhzString + " Mhz");
            final String displayRefreshString = systemStatus.getString(SystemStatus.KEY_AVG_DISPLAY_REFRESH_TIME_MILLIS, "?");
            screenLabel.setText("vsync = " + Util.formatDouble(systemStatus.getDouble(SystemStatus.KEY_VSYNCS_PER_SECOND, 0.0)));
        }

        @Override
        public void paintComponent(final Graphics g) {
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private static final class LedIcon extends RoundIcon  {

        LedIcon(final int width, final int height, final int yoffset) {
            super(width, height, yoffset);
        }

        public void setOn(final boolean on) {
            setColour(on ? LED_ON_COLOUR : LED_OFF_COLOUR);
        }
    };

    private static final class ClockIcon extends RoundIcon  {

        ClockIcon(final int width, final int height, final int yoffset) {
            super(width, height, yoffset);
        }

        public void setRate(final double rate) {
            if (rate >= MIN_NORMAL_CLOCK_RATE && rate <= MAX_NORMAL_CLOCK_RATE) {
                setColour(NORMAL_CLOCK_RATE_COLOUR);
            } else {
                if (rate < NORMAL_CLOCK_RATE) {
                    setColour(SLOW_CLOCK_RATE_COLOUR);
                } else {
                    setColour(FAST_CLOCK_RATE_COLOUR);
                }
            }
        }
    };

    private final class ImageComponent extends JComponent {

        private Timer disableCursorTimer;

        public ImageComponent() {
            setOpaque(false);
            setBackground(systemPalette.getColour(0));
            setPreferredSize(new Dimension(IMAGE_WIDTH + IMAGE_BORDER_SIZE * 2, IMAGE_HEIGHT + IMAGE_BORDER_SIZE * 2));
            addKeyListener(new KeyHandler());
            disableCursor();
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    enableCursor(2000);
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    requestFocus();
                }
            });
            addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    bbc.getClock().setPaused(false);
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    bbc.getClock().setPaused(true);
                    repaint();
                }
            });
        }

        void enableCursor(final int enableTime) {
            setCursor(Cursor.getDefaultCursor());
            startDisableCursorTimer(enableTime);
        }

        void stopDisableCursorTimer() {
            if (disableCursorTimer != null) {
                disableCursorTimer.stop();
                disableCursorTimer = null;
            }
        }

        void startDisableCursorTimer(final int delay) {
            stopDisableCursorTimer();
            disableCursorTimer = new Timer(delay, e -> {
                disableCursor();
            });
            disableCursorTimer.setRepeats(false);
            disableCursorTimer.start();
        }

        void disableCursor() {
            setCursor( getToolkit().createCustomCursor(
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                    new Point(),
                    null)
            );
        }

        @Override
        public void paintComponent(Graphics g) {
            final long startTime = System.nanoTime();
            final Rectangle r = SwingUtilities.calculateInnerArea(this, null);
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            if (renderer == null) {
                return;
            }

            if (!renderer.isClockBased()) {
                // Renderer can produce the whole image in one go
                renderer.refreshWholeImage(getImageToPaint());
                swapImages();
            }

            if (paused || renderer.isImageReady()) {
                final BufferedImage image = getImageToShow();
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
                g.setColor(systemPalette.getColour(0));
                g.fillRect(ir.x - IMAGE_BORDER_SIZE / 2, ir.y - IMAGE_BORDER_SIZE / 2, ir.width + IMAGE_BORDER_SIZE, ir.height + IMAGE_BORDER_SIZE);
                g.drawImage(image, ir.x, ir.y, ir.width, ir.height, null);
                if (!hasFocus()) {
                    g.setColor(new Color(255, 255, 255, 128));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
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
