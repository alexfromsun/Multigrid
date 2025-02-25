package tilings.painters;

import tilings.multigrid.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public abstract class RhombusPainter {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSymmetrySupported(int symmetry) {
        return true;
    }

    public void paint(Graphics2D g2, GridTile rhombus, boolean isReversed) {
        List<GridPoint> vertexList = rhombus.getVertexList();
        GridPoint a = vertexList.get(isReversed ? 2 : 0);
        GridPoint b = vertexList.get(isReversed ? 1 : 3);
        GridPoint c = vertexList.get(isReversed ? 0 : 2);
        GridPoint d = vertexList.get(isReversed ? 3 : 1);
        Graphics2D temp = (Graphics2D) g2.create();
        paintRhombus(temp, rhombus, a, b, c, d);
        temp.dispose();
    }

    public Path2D getPath(GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(a.x(), a.y());
        path.lineTo(b.x(), b.y());
        path.lineTo(c.x(), c.y());
        path.lineTo(d.x(), d.y());
        path.closePath();
        return path;
    }

    public Path2D getPath(GridPoint a, GridPoint b, GridPoint c) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(a.x(), a.y());
        path.lineTo(b.x(), b.y());
        path.lineTo(c.x(), c.y());
        path.closePath();
        return path;
    }

    abstract public void paintRhombus(Graphics2D g2, GridTile rhombus,
                                      GridPoint a, GridPoint b, GridPoint c, GridPoint d);

    public static class Line extends Line2D.Double {
        public Line(GridPoint a, GridPoint b) {
            super(a.x(), a.y(), b.x(), b.y());
        }
    }

    protected List<Color> createColorList(int size) {
        List<Color> colorList = new ArrayList<>();
        // Distribute the hue values evenly around the color wheel
        for (int i = 0; i < size; i++) {
            float hue = (float) i / size;     // 0.0 to <1.0
            float saturation = 0.8f;      // set between 0.0 and 1.0
            float brightness = 0.9f;      // set between 0.0 and 1.0
            // Create the Color using HSB -> RGB conversion
            Color color = Color.getHSBColor(hue, saturation, brightness);
            colorList.add(color);
        }
        return colorList;
    }

    abstract public String getName();
}