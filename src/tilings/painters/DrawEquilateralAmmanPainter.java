package tilings.painters;

import tilings.multigrid.*;
import java.awt.*;

public class DrawEquilateralAmmanPainter extends RhombusPainter {
    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(Color.BLACK);
        double area = rhombus.getArea();
        if (area == 0.587785) {

            GridPoint bc_i = b.getPointInDirection(c, 0.38197);

            GridPoint i = a.getPointInDirection(bc_i, 0.52573);

            g2.draw(new Line(a, i));
            g2.draw(new Line(b, i));
            g2.draw(new Line(c, i));

        } else if (area == 0.951057) {

            GridPoint ab = a.getPointInDirection(b, 0.417);
            GridPoint cd = c.getPointInDirection(d, 0.417);

            GridPoint ab_i = ab.getPointInDirection(cd, 0.182);
            GridPoint cd_i = cd.getPointInDirection(ab, 0.182);

            g2.draw(new Line(a, ab_i));
            g2.draw(new Line(ab_i, b));
            g2.draw(new Line(ab_i, cd_i));
            g2.draw(new Line(cd_i, c));
            g2.draw(new Line(cd_i, d));
        }
    }
}