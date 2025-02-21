package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;

public class DrawKitesAndDartsPainter extends PenrosePainter {

    @Override
    void paintThinRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        // diagonal
        g2.draw(new Line(a, c));

        g2.draw(new Line(a, b));
        g2.draw(new Line(a, d));

    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        GridPoint i = c.getPointInDirection(a, 1);

        // inner lines
        g2.draw(new Line(c, i));
        g2.draw(new Line(b, i));
        g2.draw(new Line(d, i));

        g2.draw(new Line(a, b));
        g2.draw(new Line(a, d));
    }
}

