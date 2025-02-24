package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;
import java.util.List;

public class BlueRedPainter extends DrawPenroseArrowsPainter {

    @Override
    void paintThinRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(isBlue(rhombus)? Color.BLUE: Color.RED);
        g2.fill(getPath(b, a, d));
        g2.setColor(isBlue(rhombus)? Color.RED: Color.BLUE);
        g2.fill(getPath(b, c, d));
        g2.setColor(Color.BLACK);
        g2.draw(getPath(a, b, c, d));
    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(isBlue(rhombus)? Color.BLUE: Color.RED);
        g2.fill(getPath(a, b, c, d));
        g2.setColor(Color.BLACK);
        g2.draw(getPath(a, b, c, d));
    }

    protected boolean isBlue(GridTile rhombus) {
        List<GridPoint> vertexList = rhombus.getVertexList();
        int max = rhombus.getVertexIndex(vertexList.get(0));
        int min = rhombus.getVertexIndex(vertexList.get(2));
        return min == 0;
    }

    @Override
    public String getName() {
        return "Blue Red";
    }
}
