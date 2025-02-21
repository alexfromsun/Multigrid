package tilings.multigrid;

import java.util.Comparator;

public class GridLine implements Comparator<GridPoint> {
    private final double angle;
    private final double offset;
    protected final double angleSin;
    protected final double angleCos;

    public GridLine(double angle, double offset) {
        this.angle = angle;
        this.offset = offset;
        angleSin = Math.sin(angle);
        angleCos = Math.cos(angle);
    }

    public double getAngle() {
        return angle;
    }

    public double getOffset() {
        return offset;
    }

    public GridPoint getIntersectionPoint(GridLine line) {
        if (angle == line.getAngle()) {
            return null;
        }
        double determinant = angleCos * line.angleSin - angleSin * line.angleCos;
        if (Math.abs(determinant) < 1e-10) {
            return null;
        }
        double offsetOne = offset;
        double offsetTwo = line.offset;

        double y = -(offsetOne * line.angleCos - offsetTwo * angleCos) / determinant;
        double x = (offsetOne * line.angleSin - offsetTwo * angleSin) / determinant;

        return new GridPoint(x, y);
    }

    public boolean isPointOnLine(GridPoint p) {
        double lineValue = -p.x() * angleSin + p.y() * angleCos - offset;
        return Multigrid.equalWithSmallEpsilon(lineValue, 0);
    }

    public double reversePoint(GridPoint point) {
        double yOriginal = point.x() * angleCos + point.y() * angleSin - offset;
        double xOriginal = point.x() * angleSin - point.y() * angleCos;

        if (Multigrid.equalWithSmallEpsilon(xOriginal, 0)) {
            xOriginal = 0;
        }
        if (!Multigrid.equalWithSmallEpsilon(yOriginal, 0)) {
            throw new RuntimeException("y is not close to zero!");
        }
        return xOriginal;
    }

    @Override
    public int compare(GridPoint o1, GridPoint o2) {
        return Double.compare(reversePoint(o1), reversePoint(o2));
    }

    @Override
    public String toString() {
        return "GridLine[angle=" + angle + ", offset=" + offset + "]";
    }
}
