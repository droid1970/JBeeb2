package com.jbeeb.teletext;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

final class AlphaDefinition {

    private final List<PathCommand> commands = new ArrayList<>();

    public void addCommand(final PathCommand command) {
        this.commands.add(command);
    }

    public Path2D.Double toPath() {
        return toPath(x -> x, y -> y);
    }

    public Path2D.Double toPath(UnaryOperator<Double> xtransform, UnaryOperator<Double> ytransform) {
        final Path2D.Double path = new Path2D.Double();
        commands.forEach(c -> c.apply(path, xtransform, ytransform));
        return path;
    }

    public AlphaDefinition moveTo(final double x, final double y) {
        addCommand(PathCommand.moveTo(x, y));
        return this;
    }

    public AlphaDefinition lineTo(final double x, final double y) {
        addCommand(PathCommand.lineTo(x, y));
        return this;
    }
}