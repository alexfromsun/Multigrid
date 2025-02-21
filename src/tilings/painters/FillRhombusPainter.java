package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FillRhombusPainter extends RhombusPainter {
    private List<Double> tileAreaList;
    private List<Color> colorList;

    public FillRhombusPainter() {
    }

    public void setUp(List<Double> tileAreaList) {
        this.tileAreaList = tileAreaList;
        if (colorList == null || colorList.size() != tileAreaList.size()) {
            colorList = new ArrayList<>();
            // Distribute the hue values evenly around the color wheel
            int colorListSize = tileAreaList.size();
            for (int i = 0; i < colorListSize; i++) {
                float hue = (float) i / colorListSize;     // 0.0 to <1.0
                float saturation = 0.8f;      // set between 0.0 and 1.0
                float brightness = 0.9f;      // set between 0.0 and 1.0
                // Create the Color using HSB -> RGB conversion
                Color color = Color.getHSBColor(hue, saturation, brightness);
                colorList.add(color);
            }
        }
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        int colorIndex = tileAreaList.indexOf(rhombus.getArea());
        g2.setColor(colorList.get(colorIndex));
        g2.fill(getPath(a, b, c, d));
    }
}
