package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;

public class DrawRhombusPainter extends RhombusPainter {
    private final Color color;

    public DrawRhombusPainter(Color color) {
        this.color = color;
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        g2.setColor(color);
        g2.draw(getPath(a, b, c, d));
    }
}
