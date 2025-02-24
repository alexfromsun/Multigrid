package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;
import java.util.List;

abstract public class PenrosePainter extends RhombusPainter {

    @Override
    public boolean isSymmetrySupported(int symmetry) {
        return symmetry == 5;
    }

    @Override
    public void paint(Graphics2D g2, GridTile rhombus, boolean isReversed) {
        List<GridPoint> vertexList = rhombus.getVertexList();
        boolean useMinVertex = isMinIndexDirection(rhombus);

        GridPoint a = vertexList.get(isReversed != useMinVertex ? 2 : 0);
        GridPoint b = vertexList.get(isReversed != useMinVertex ? 1 : 3);
        GridPoint c = vertexList.get(isReversed != useMinVertex ? 0 : 2);
        GridPoint d = vertexList.get(isReversed != useMinVertex ? 3 : 1);
        Graphics2D temp = (Graphics2D) g2.create();
        paintRhombus(temp, rhombus, a, b, c, d);
        temp.dispose();
    }

    protected boolean isMinIndexDirection(GridTile rhombus) {
        List<GridPoint> vertexList = rhombus.getVertexList();
        int min = rhombus.getVertexIndex(vertexList.get(2));
        int max = rhombus.getVertexIndex(vertexList.get(0));
        if (min == 0 && max == 2) {
// max
            return false;
        } else if (min == 0 && max == 3) {
// min
            return true;
        } else if (min == 1 && max == 3) {
// min
            return true;
        } else if (min == 2 && max == 4) {
// min
            return true;
        } else if (min == 1 && max == 4) {
// max
            return false;
        }
        throw new AssertionError("Unexpected pair, min = " + min + ", max = " + max);
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(Color.BLACK);
        double area = rhombus.getArea();
        if (area == 0.587785) {
            paintThinRhombus(g2, rhombus, a, b, c, d);
        } else if (area == 0.951057) {
            paintThickRhombus(g2, rhombus, a, b, c, d);
        } else {
            throw new AssertionError("Unexpected tile's area: " + area);
        }
    }

    abstract void paintThinRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d);

    abstract void paintThickRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d);
}
