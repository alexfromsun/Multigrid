package tilings.multigrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Grid {
    private final double angle;
    private final double offset;
    private final int radius;
    private final List<GridLine> lineList = new ArrayList<>();
    private final GridLine leftBorder;
    private final GridLine rightBorder;

    public Grid(double angle, double offset, int gridRadius, double inset) {
        this.angle = angle;
        this.offset = offset;
        this.radius = gridRadius;

        for (int j = -gridRadius; j <= gridRadius; j++) {
            GridLine line = new GridLine(angle, offset + j);
            lineList.add(line);
        }

        leftBorder = new GridLine(angle, lineList.getFirst().getOffset() - (1 - inset));
        rightBorder = new GridLine(angle, lineList.getLast().getOffset() + (1 - inset));
    }

    public boolean contains(GridPoint point) {
        double v = point.x() * leftBorder.angleCos + point.y() * leftBorder.angleSin;
        double minD = Math.min(leftBorder.getOffset(), rightBorder.getOffset());
        double maxD = Math.max(leftBorder.getOffset(), rightBorder.getOffset());
        return (v >= minD && v <= maxD);
    }

    public double getAngle() {
        return angle;
    }

    public double getOffset() {
        return offset;
    }

    public int getRadius() {
        return radius;
    }

    public List<GridLine> getLineList() {
        return Collections.unmodifiableList(lineList);
    }
}
