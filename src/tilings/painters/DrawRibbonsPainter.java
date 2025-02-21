package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;

public class DrawRibbonsPainter extends RhombusPainter {

    public DrawRibbonsPainter() {
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(Color.BLACK);
        double area = rhombus.getArea();
        if (area == 0.587785) {
            g2.draw(new Line(a, c));
            g2.draw(new Line(c, d));
        } else if (area == 0.951057) {
            GridPoint i = a.getPointInDirection(c, 1);
            g2.draw(new Line(a, c));
            g2.draw(new Line(i, d));
        }
    }
}