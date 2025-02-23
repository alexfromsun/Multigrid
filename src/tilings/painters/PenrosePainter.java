package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;

abstract public class PenrosePainter extends RhombusPainter {

    @Override
    public boolean isSymmetrySupported(int symmetry) {
        return symmetry == 5;
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(Color.BLACK);
        double area = rhombus.getArea();
        if (area == 0.587785) {
            paintThinRhombus(g2, a, b, c, d);
        } else if (area == 0.951057) {
            paintThickRhombus(g2, a, b, c, d);
        } else {
            throw new AssertionError("Unexpected tile's area: " + area);
        }
    }

    abstract void paintThinRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d);

    abstract void paintThickRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d);
}
