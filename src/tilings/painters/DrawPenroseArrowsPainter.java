package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;
import java.awt.geom.Path2D;

public class DrawPenroseArrowsPainter extends PenrosePainter {

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        Shape clip = g2.getClip();
        g2.clip(getPath(a, b, c, d));
        super.paintRhombus(g2, rhombus, a, b, c, d);
        g2.setClip(clip);
    }

    @Override
    void paintThinRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        drawDoubleArrow(g2, b, a);
        drawDoubleArrow(g2, d, a);
        drawArrow(g2, b, c);
        drawArrow(g2, d, c);
    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        drawDoubleArrow(g2, b, a);
        drawDoubleArrow(g2, d, a);
        drawArrow(g2, c, b);
        drawArrow(g2, c, d);
    }

    protected void drawArrow(Graphics2D g2, GridPoint p1, GridPoint p2) {
        fillTriangle(g2, p1, p2, .65);
    }

    protected void drawDoubleArrow(Graphics2D g2, GridPoint p1, GridPoint p2) {
        fillTriangle(g2, p1, p2, .5);
        fillTriangle(g2, p1, p2, .8);
    }

    protected void fillTriangle(Graphics2D g2,
                             GridPoint a,
                             GridPoint b,
                             double t) {

        double fx = a.x() + t * (b.x() - a.x());
        double fy = a.y() + t * (b.y() - a.y());

        double length = a.getDistance(b);
        double dx = (b.x() - a.x()) / length;
        double dy = (b.y() - a.y()) / length;

        double side = 0.2;
        double height = (Math.sqrt(3) / 2.0) * side;
        double halfBase = side / 2.0;

        double mx = fx - height * dx;
        double my = fy - height * dy;

        // get a perpendicular to (dx,dy). That is ( -dy, dx ) or ( dy, -dx )
        double px = -dy;
        double py = dx;

        // Base endpoints = M +/- (halfBase)*p
        double b1x = mx + halfBase * px;
        double b1y = my + halfBase * py;

        double b2x = mx - halfBase * px;
        double b2y = my - halfBase * py;

        // Create a path for the equilateral triangle
        // Apex -> B1 -> B2 -> back to Apex
        Path2D triangle = new Path2D.Double();
        triangle.moveTo(fx, fy);       // apex
        triangle.lineTo(b1x, b1y);     // base endpoint 1
        triangle.lineTo(b2x, b2y);     // base endpoint 2
        triangle.closePath();

        g2.fill(triangle);
    }

    @Override
    public String getName() {
        return "Penrose Arrows";
    }
}
