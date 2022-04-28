package com.jbeeb.teletext;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

final class PathCommand {

    private final BiConsumer<Path2D.Double, Point2D.Double> consumer;
    private final Point2D.Double point;

    public PathCommand(final BiConsumer<Path2D.Double, Point2D.Double> consumer, final Point2D.Double point) {
        this.consumer = consumer;
        this.point = point;
    }

    public static PathCommand moveTo(final double x, final double y) {
        return new PathCommand((path,p) -> path.moveTo(p.getX(), p.getY()), new Point2D.Double(x, y));
    }

    public static PathCommand lineTo(final double x, final double y) {
        return new PathCommand((path,p) -> path.lineTo(p.getX(), p.getY()), new Point2D.Double(x, y));
    }
    public void apply(Path2D.Double path) {
        apply(path, x -> x, y -> y);
    }
    public void apply(Path2D.Double path, UnaryOperator<Double> xtransform, UnaryOperator<Double> ytransform) {
        consumer.accept(path, new Point2D.Double(xtransform.apply(point.getX()), ytransform.apply(point.getY())));
    }
}
