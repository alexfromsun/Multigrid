package tilings.painters;

import tilings.multigrid.*;

import java.awt.*;

public class DrawEquilateralAmmanPainter extends PenrosePainter {

    @Override
    void paintThinRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        GridPoint bc_i = b.getPointInDirection(c, 0.38197);

        GridPoint i = a.getPointInDirection(bc_i, 0.52573);

        g2.draw(new Line(a, i));
        g2.draw(new Line(b, i));
        g2.draw(new Line(c, i));
    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
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

    @Override
    public String getName() {
        return "Equilateral Amman";
    }
}