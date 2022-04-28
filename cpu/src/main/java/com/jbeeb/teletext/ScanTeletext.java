package com.jbeeb.teletext;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanTeletext {

    private static final File IMG = new File(System.getProperty("user.home"), "Teletext_Screen.png");

    public static void main(final String[] args) throws Exception {
        final BufferedImage img = ImageIO.read(IMG);
        System.out.println("img = " + img.getWidth() + ", " + img.getHeight());
        SwingUtilities.invokeLater(() -> showUI(img));
    }

    private static void showUI(final BufferedImage img) {
        final JFrame frame = new JFrame("Scan");
        final CharComponent charComponent = new CharComponent();
        final ImageComponent imageComponent = new ImageComponent(img, charComponent);
        final JScrollPane sp = new JScrollPane(imageComponent);
        frame.getContentPane().add(BorderLayout.CENTER, sp);
        frame.getContentPane().add(BorderLayout.NORTH, charComponent);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static final double CW = 5.0;
    private static final double CH = 9.0;
    private static final double IW = 16.0;
    private static final double IH = 20.0;
    private static final double LEFT_MARGIN = 2.5;
    private static final double RIGHT_MARGIN = 1.25;
    private static final double TOP_MARGIN = 1.25;
    private static final double BOTTOM_MARGIN = 2.5;
    private static final double X_SCALE = (IW - LEFT_MARGIN - RIGHT_MARGIN) / CW;
    private static final double Y_SCALE = (IH - TOP_MARGIN - BOTTOM_MARGIN) / CH;

    private static double tX(final double x) {
        return LEFT_MARGIN + x * X_SCALE;
    }

    private static double tY(final double y) {
        return TOP_MARGIN + y * Y_SCALE;
    }

    private static final class CharComponent extends JComponent {

        static int CELL_SIZE = 32;

        private Char ch= new Char();
        private AlphaDefinition alphaDefinition = new AlphaDefinition();
        private Path2D.Double path = new Path2D.Double();
        private int charCode = 102;
        private StringBuilder pathCode = newPathCodeBuilder();

        private StringBuilder newPathCodeBuilder() {
            return new StringBuilder("PATH_MAP.put(" + (charCode++) + ", new PathSpec()");
        }

        CharComponent() {
            setOpaque(true);
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(CELL_SIZE * 8, CELL_SIZE * 12));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(final MouseEvent e) {
                    final int x = e.getX();
                    final int y = e.getY();
                    final int ipx = (x - CELL_SIZE) / CELL_SIZE - 1;
                    final int ipy = (y - CELL_SIZE) / CELL_SIZE;
                    final double px = ipx + 0.5;
                    final double py = ipy + 0.5;
                    if (x >= 0 && x < CELL_SIZE * 7 && y >= 0 && y < CELL_SIZE * 10) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            alphaDefinition.moveTo(px, py);
                            pathCode.append(".moveTo(").append(px).append(", ").append(py).append(")");
                            path.moveTo(tX(px), tY(py));
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            alphaDefinition.lineTo(px, py);
                            pathCode.append(".lineTo(").append(px).append(", ").append(py).append(")");
                            path.lineTo(LEFT_MARGIN + px * X_SCALE, TOP_MARGIN + py * Y_SCALE);
                        }
                    } else {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (path != null) {
                                savedAlphaDefinitions.add(alphaDefinition);
                            }
                        }
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            // Complete the current path
                            pathCode.append(");");
                            System.out.println(pathCode);
                            path = new Path2D.Double();
                            alphaDefinition = new AlphaDefinition();
                            pathCode = newPathCodeBuilder();
//                            pathCode = new StringBuilder();
//                            pathCode.append("new PathSpec(");
//                            System.out.println("\nnew PathSpec();");
                        }
                    }
                    repaint();
                }
            });
        }

        void setChar(Char ch) {
            this.ch = ch;
            repaint();
        }

        List<AlphaDefinition> savedAlphaDefinitions = new ArrayList<>();

        @Override
        public void paintComponent(final Graphics g1) {
            final Graphics2D g = (Graphics2D) g1;
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < 10; y++) {
                    g.setColor(ch != null && ch.cells[x][y] ? Color.WHITE : Color.BLACK);
                    g.fillRect(CELL_SIZE + x * CELL_SIZE, CELL_SIZE + y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }

            if (path != null) {
                final BufferedImage charImage = createPathImage(path, CELL_SIZE / 2);
                paintImage(g, charImage, CELL_SIZE * 9, CELL_SIZE, 1, false);
                g.setColor(Color.WHITE);
                g.drawRect(CELL_SIZE * 9, CELL_SIZE, charImage.getWidth() - 1, charImage.getHeight() - 1);
                int x = CELL_SIZE * 18;
                //for (Path2D.Double p : savedPaths) {
                for (AlphaDefinition p: savedAlphaDefinitions) {
                    final Path2D.Double path = p.toPath(px -> tX(px), py -> tY(py));
                    final BufferedImage ci = createPathImage(path, 1);
                    paintImage(g, ci, x, CELL_SIZE, 1, true);
                    x += ci.getWidth();
                }
            }
        }
    }

    private static int avg(int[] vs) {
        int tot = 0;
        for (int n : vs) {
            tot += n;
        }
        return (int) (tot / (double) vs.length);
    }

    private static void paintImage(final Graphics2D g, final BufferedImage image, final int x, final int y, final double scale, final boolean interpolate) {
        final int iw = (int) (image.getWidth() * scale);
        final int ih = (int) (image.getHeight() * scale);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, (interpolate) ? RenderingHints.VALUE_INTERPOLATION_BICUBIC : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, x, y, iw, ih, null);
    }

    private static BufferedImage createPathImage(final Path2D.Double path, final int scale) {
        final int iw = scale * 16;
        final int ih = scale * 20;
        final BufferedImage charImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = charImage.createGraphics();
        final Path2D.Double scaledPath = new Path2D.Double(path);
        final AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        scaledPath.transform(transform);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(scale * 2.0f * 1.125f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.draw(scaledPath);
        return charImage;
    }

    private static BufferedImage createPathImage_OLD(final Path2D.Double path, final int scale) {
        final double xadj = 7.0 / 6.0;
        final double yadj = 9.0 / 10.0;
        final int iw = scale * 8;
        final int ih = scale * 10;
        final BufferedImage charImage = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = charImage.createGraphics();
        final Path2D.Double scaledPath = new Path2D.Double(path);
        final AffineTransform transform = new AffineTransform();
        transform.scale(scale * xadj, scale * yadj);
        transform.translate(-0.5 * xadj, 0.5 * yadj);
        scaledPath.transform(transform);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(scale * 1.125f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.draw(scaledPath);
        return charImage;
    }

    private static final class Char {
        private final boolean[][] cells = new boolean[6][10];

        void setCell(final int x, final int y) {
            cells[x][y] = true;
        }

        public void print() {
            for (int y = 0; y < 10; y++) {
                final StringBuilder sb = new StringBuilder();
                for (int x = 0; x < 6; x++) {
                    sb.append(cells[x][y] ? "# " : ". ");
                }
                System.out.println(sb);
            }
        }
    }

    private static final class ImageComponent extends JComponent {
        final CharComponent charComponent;
        final BufferedImage img;

        ImageComponent(final BufferedImage img, final CharComponent charComponent) {
            this.img = Objects.requireNonNull(img);
            this.charComponent = Objects.requireNonNull(charComponent);
            setOpaque(true);
            setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            addMouseListener(new MouseHandler());
        }

        Rectangle charRect;

        @Override
        public void paintComponent(final Graphics g1) {
            final Graphics2D g = (Graphics2D) g1;
            if (isOpaque()) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            g.drawImage(img, 0, 0, null);
            if (charRect != null) {
                g.setColor(Color.RED);
                g.drawRect(charRect.x, charRect.y, charRect.width, charRect.height);
            }
        }

        final class MouseHandler extends MouseAdapter {
            @Override
            public void mousePressed(MouseEvent e) {
                final int x = e.getX();
                final int y = e.getY();
                if (isBlackAt(x, y)) {
                    final Char ch = new Char();
                    // Go left to find first white pixel
                    int lx;
                    for (lx = x; lx >= 0 && isBlackAt(lx, y); lx--) {
                    }
                    lx++;
                    int by;
                    for (by = y; by < img.getHeight() && isBlackAt(lx, by); by++) {

                    }
                    by--;

                    int rx;
                    for (rx = x; rx < img.getWidth() && isBlackAt(rx, y); rx++) {

                    }
                    rx--;
                    int ty;
                    for (ty = by; ty > 0 && isBlackAt(lx, ty); ty--) {

                    }
                    ty++;
                    charRect = new Rectangle(lx, ty, (rx - lx) + 1, (by - ty) + 1);
                    repaint();
                    final double pixelWidth = charRect.width / 6.0;
                    final double pixelHeight = charRect.height / 10.0;
                    final double topLeftCentreX = lx + pixelWidth / 2.0;
                    final double topLeftCentreY = ty + pixelHeight / 2.0;
                    for (int py = 0; py < 10; py++) {
                        for (int px = 0; px < 6; px++) {
                            final int cx = (int) (topLeftCentreX + (px * pixelWidth));
                            final int cy = (int) (topLeftCentreY + (py * pixelHeight));
                            if (isWhiteAt(cx, cy)) {
                                ch.setCell(px, py);
                            }
                        }
                    }
                    charComponent.setChar(ch);
                }
            }
        }

        final boolean isBlackAt(final int x, final int y) {
            return (img.getRGB(x, y) & 0xFF) == 0;
        }

        final boolean isWhiteAt(final int x, final int y) {
            return (img.getRGB(x, y) & 0xFF) == 0xFF;
        }
    }
}
