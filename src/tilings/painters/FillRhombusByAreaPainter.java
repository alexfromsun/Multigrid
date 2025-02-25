package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FillRhombusByAreaPainter extends RhombusPainter {
    private List<Double> tileAreaList;
    private List<Color> colorList;

    public FillRhombusByAreaPainter(List<Double> tileAreaList) {
        setTileAreaList(tileAreaList);
    }

    public void setTileAreaList(List<Double> tileAreaList) {
        this.tileAreaList = tileAreaList;
        colorList = createColorList(tileAreaList.size());
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        int colorIndex = tileAreaList.indexOf(rhombus.getArea());
        g2.setColor(colorList.get(colorIndex));
        g2.fill(getPath(a, b, c, d));
    }

    @Override
    public String getName() {
        return "Color by area";
    }
}
