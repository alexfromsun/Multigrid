package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;

public class DrawRibbonsPainter extends PenrosePainter {

    @Override
    void paintThinRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.draw(new Line(a, c));
        g2.draw(new Line(c, d));
    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        GridPoint i = a.getPointInDirection(c, 1);
        g2.draw(new Line(a, c));
        g2.draw(new Line(i, d));
    }

    @Override
    public String getName() {
        return "Ribbons";
    }
}