package tilings.painters;

import tilings.multigrid.*;

import java.awt.*;

public class DrawCromwellTrapeziumPainter extends PenrosePainter {

    @Override
    void paintThinRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        // diagonal
        g2.draw(new Line(a, c));

        g2.draw(new Line(c, d));
        g2.draw(new Line(c, b));
    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        GridPoint i = c.getPointInDirection(a, 1);
        g2.draw(new Line(c, b));
        g2.draw(new Line(c, d));
        g2.draw(new Line(b, i));
        g2.draw(new Line(d, i));
        g2.draw(new Line(a, i));
    }

    @Override
    public String getName() {
        return "Cromwell";
    }
}