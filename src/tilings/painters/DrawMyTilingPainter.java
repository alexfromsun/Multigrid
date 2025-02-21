package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;

public class DrawMyTilingPainter extends PenrosePainter {

    @Override
    void paintThinRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        GridPoint cb_i = c.getPointInDirection(b, 0.38197);
        g2.draw(new Line(a, cb_i));
    }

    @Override
    void paintThickRhombus(Graphics2D g2, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        GridPoint cd_i = c.getPointInDirection(d, 0.61803);
        GridPoint i = c.getPointInDirection(a, 1);

        g2.draw(new Line(c, b));
        g2.draw(new Line(c, d));
        g2.draw(new Line(b, i));
        g2.draw(new Line(cd_i, i));
        g2.draw(new Line(i, a));
    }

    /*
        // the initial version of my tiling, bigger tiles
        private void drawMyTiling(Graphics2D g2,
                                  GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                  double area) {
            g2.setColor(Color.BLACK);
            if (area == 0.587785) {
                Line diagonal =
                        new Line(a, c);
                g2.draw(diagonal);
            } else if (area == 0.951057) {
                Line ab = new Line(a, b);
                g2.draw(ab);
                Line bc = new Line(b, c);
                g2.draw(bc);
            }
        }*/
}

