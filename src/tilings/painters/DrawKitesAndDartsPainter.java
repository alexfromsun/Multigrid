package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;

public class DrawKitesAndDartsPainter extends RhombusPainter {

    public DrawKitesAndDartsPainter() {
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(Color.BLACK);
        double area = rhombus.getArea();
        if (area == 0.587785) {
            // diagonal
            g2.draw(new Line(a, c));

            g2.draw(new Line(a, b));
            g2.draw(new Line(a, d));
        } else if (area == 0.951057) {
            GridPoint i = c.getPointInDirection(a, 1);

            // inner lines
            g2.draw(new Line(c, i));
            g2.draw(new Line(b, i));
            g2.draw(new Line(d, i));

            g2.draw(new Line(a, b));
            g2.draw(new Line(a, d));
        } else {
            throw new AssertionError("Unexpected tile's area: " + area);
        }
    }
}

