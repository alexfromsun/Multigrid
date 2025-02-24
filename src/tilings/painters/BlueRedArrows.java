package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;
import java.util.List;

public class BlueRedArrows extends DrawPenroseArrowsPainter {

    @Override
    public void paint(Graphics2D g2, GridTile rhombus, boolean isReversed) {
        List<GridPoint> vertexList = rhombus.getVertexList();
        GridPoint a = vertexList.get(isReversed? 2 : 0);
        GridPoint b = vertexList.get(isReversed? 1 : 3);
        GridPoint c = vertexList.get(isReversed? 0 : 2);
        GridPoint d = vertexList.get(isReversed? 3 : 1);
        Graphics2D temp = (Graphics2D) g2.create();
        paintRhombus(temp, rhombus, a, b, c, d);
        temp.dispose();
    }

    @Override
    void paintThinRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        if (isBlue(rhombus)) {
            g2.setColor(Color.RED);
            drawDoubleArrow(g2, a, b);
            g2.draw(new Line(a, b));
            drawDoubleArrow(g2, a, d);
            g2.draw(new Line(a, d));
            g2.setColor(Color.BLUE);
            drawArrow(g2, b, c);
            g2.draw(new Line(b, c));
            drawArrow(g2, d, c);
            g2.draw(new Line(c, d));
        } else {
            g2.setColor(Color.BLUE);
            drawDoubleArrow(g2, b, a);
            g2.draw(new Line(b, a));
            drawDoubleArrow(g2, d, a);
            g2.draw(new Line(d, a));
            g2.setColor(Color.RED);
            drawArrow(g2, c, b);
            g2.draw(new Line(c, b));
            drawArrow(g2, c, d);
            g2.draw(new Line(c, d));
        }
    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        if (isBlue(rhombus)) {
            g2.setColor(Color.BLUE);
            drawDoubleArrow(g2, a, b);
            drawDoubleArrow(g2, a, d);
            drawArrow(g2, b, c);
            drawArrow(g2, d, c);
        } else {
            g2.setColor(Color.RED);
            drawDoubleArrow(g2, b, a);
            drawDoubleArrow(g2, d, a);
            drawArrow(g2, c, b);
            drawArrow(g2, c, d);
        }
        g2.draw(getPath(a, b, c, d));
    }

    protected boolean isBlue(GridTile rhombus) {
        List<GridPoint> vertexList = rhombus.getVertexList();
        int max = rhombus.getVertexIndex(vertexList.get(0));
        int min = rhombus.getVertexIndex(vertexList.get(2));
        return max %2 == 1 || min == 0;
    }

    @Override
    public String getName() {
        return "Blue Red Arrows";
    }
}
