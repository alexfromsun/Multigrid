import java.util.*;

public class Multigrid {
    private final int symmetry;
    private final int radius;
    private final double offset;
    private final List<GridLine> lineList = new ArrayList<>();
    private final HashMap<GridPoint, Set<GridLine>> intersectionMap = new HashMap<>();
    private final HashMap<GridLine, List<GridPoint>> lineMap = new HashMap<>();

    private List<Double> sinTable = new ArrayList<>();
    private List<Double> cosTable = new ArrayList<>();

    private Map<GridPoint, List<GridPoint>> dualMap = new HashMap<>();

    private List<GridTile> tileList;

    public Multigrid(int symmetry, int radius, double offset) {
        this.radius = radius;
        this.symmetry = symmetry;
        this.offset = offset;
        double multiplier = 2 * Math.PI / symmetry;
        for (int i = 0; i < symmetry; i++) {
            double angle = 2 * i * Math.PI / symmetry;
            for (int j = -radius; j <= radius; j++) {
                GridLine line = new GridLine(angle, offset + j);
                lineList.add(line);
                lineMap.put(line, new ArrayList<>());
            }
            sinTable.add(Math.sin(i * multiplier));
            cosTable.add(Math.cos(i * multiplier));
        }
        calculateIntersections();
    }

    private void calculateIntersections() {
        Map<GridPoint, GridPoint> epsilonIntersectionMap = new HashMap<>();

        for (int i = 0; i < lineList.size(); i++) {
            GridLine lineOne = lineList.get(i);
            for (int j = i + 1; j < lineList.size(); j++) {
                GridLine lineTwo = lineList.get(j);
                GridPoint point = lineOne.getIntersectionPoint(lineTwo);
                Set<GridLine> lineSet;
                if (point != null) {
                    GridPoint roundedPoint = new GridPoint(roundWithEpsilon(point.x()), roundWithEpsilon(point.y()));
                    GridPoint existingPoint = epsilonIntersectionMap.get(roundedPoint);
                    if (existingPoint != null) {
                        lineSet = intersectionMap.get(existingPoint);
                        point = existingPoint;
                    } else {
                        epsilonIntersectionMap.put(roundedPoint, point);
                        lineSet = intersectionMap.get(point);
                        if (lineSet == null) {
                            lineSet = new HashSet<>();
                            intersectionMap.put(point, lineSet);
                        }
                    }

                    lineSet.add(lineOne);
                    lineSet.add(lineTwo);

                    if (!lineMap.get(lineOne).contains(point)) {
                        lineMap.get(lineOne).add(point);
                    }
                    if (!lineMap.get(lineTwo).contains(point)) {
                        lineMap.get(lineTwo).add(point);
                    }
                }
            }
        }

// todo: does sorting needed at all?
        sortIntersectionPoints();

        calculateTiles();
    }

    private void calculateTiles() {
        tileList = new ArrayList<>();
        for (GridPoint intersection : getIntersections()) {
            GridTile tile = new GridTile(intersection);

            List<Double> angles = new ArrayList<>();
            Set<GridLine> lineSet = getIntersectedLineSet(intersection);

            for (GridLine line : lineSet) {
                angles.add(line.getAngle());
                angles.add((line.getAngle() + Math.PI) % (2 * Math.PI));
            }

            angles.sort(Comparator.naturalOrder());

            List<GridPoint> offsetList = new ArrayList<>();
            for (Double angle : angles) {
                double x = intersection.x() + EPSILON * -Math.sin(angle);
                double y = intersection.y() + EPSILON * Math.cos(angle);
                GridPoint offset = new GridPoint(x, y);
                offsetList.add(offset);
            }

            tile.setOffsetList(offsetList);

            List<GridPoint> medianList = new ArrayList<>();
            int iMax = offsetList.size();

            for (int i = 0; i < iMax; i++) {
                GridPoint offset = offsetList.get(i);
                double x0 = offset.x();
                double y0 = offset.y();

                double x1 = offsetList.get((i + 1) % iMax).x();
                double y1 = offsetList.get((i + 1) % iMax).y();

                double xm = (x0 + x1) / 2;
                double ym = (y0 + y1) / 2;

                GridPoint median = new GridPoint(xm, ym);
                medianList.add(median);
            }

            tile.setMedianList(medianList);
            List<GridPoint> dualList = new ArrayList<>();
            double meanX = 0, meanY = 0;

            for (GridPoint median : medianList) {
                double xd = 0, yd = 0;

                for (int i = 0; i < this.symmetry; i++) {
                    double ci = cosTable.get(i);
                    double si = sinTable.get(i);

                    // todo: sort out the non symmetrical offsets
                    double k = Math.floor(median.x() * ci + median.y() * si - offset);

                    xd += k * ci;
                    yd += k * si;
                }
                GridPoint dual =
                        new GridPoint(roundWithEpsilon(xd), roundWithEpsilon(yd));
                dualList.add(dual);

                meanX += xd;
                meanY += yd;
            }

            dualMap.put(intersection, dualList);
            tile.setVertexList(dualList);
            if (tile.isRhombus()) {
                tileList.add(tile);
            }
        }
        tileList = Collections.unmodifiableList(tileList);
    }

    public List<GridTile> getTileList() {
        return tileList;
    }

    private void sortIntersectionPoints() {
        for (GridLine line : lineMap.keySet()) {
            List<GridPoint> points = lineMap.get(line);
            points.sort(line);
        }
    }

    public Set<GridPoint> getIntersections() {
        return intersectionMap.keySet();
    }

    public Map<GridPoint, List<GridPoint>> getDualMap() {
        return dualMap;
    }

    public Set<GridLine> getIntersectedLineSet(GridPoint point) {
        return intersectionMap.get(point);
    }

    public int getRadius() {
        return radius;
    }

    public int getSymmetry() {
        return symmetry;
    }

    public double getOffset() {
        return offset;
    }

    public List<GridLine> getLineList() {
        return lineList;
    }

    public List<GridPoint> getIntersectionList(GridLine line) {
        return lineMap.get(line);
    }

    public static void main(String[] args) {

        GridLine line = new GridLine(0, 0);

        GridPoint p1 = new GridPoint(1, 0);
        GridPoint p2 = new GridPoint(5, 0);
        System.out.println("line.isPointOnLine(p1) = " + line.isPointOnLine(p1));
        System.out.println("line.isPointOnLine(p2) = " + line.isPointOnLine(p2));

        System.out.println("p1.getDistance(p2) = " + p1.getDistance(p2));

        GridLine line2 = new GridLine(Math.PI / 4, 3);

        p1 = p1.rotateAndShift(line2.getAngle(), line2.getOffset());
        p2 = p2.rotateAndShift(line2.getAngle(), line2.getOffset());

        System.out.println("p1 = " + p1);
        System.out.println("p2 = " + p2);

        System.out.println("p1.getDistance(p2) = " + p1.getDistance(p2));
        System.out.println("line2.isPointOnLine(p1) = " + line2.isPointOnLine(p1));
        System.out.println("line2.isPointOnLine(p2) = " + line2.isPointOnLine(p2));
    }

    private static final double EPSILON = 1e-10;

    static boolean equalWithEpsilon(double a, double b) {
        return (Math.abs(a - b) < EPSILON);
    }

    static double roundWithEpsilon(double d) {
        return Math.round(d * (1.0 / EPSILON)) / (1.0 / EPSILON);
    }
}

class GridLine implements Comparator<GridPoint> {
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
        return Multigrid.equalWithEpsilon(lineValue, 0);
    }

    public double reversePoint(GridPoint point) {
        double yOriginal = point.x() * angleCos + point.y() * angleSin - offset;
        double xOriginal = point.x() * angleSin - point.y() * angleCos;

        if (Multigrid.equalWithEpsilon(xOriginal, 0)) {
            xOriginal = 0;
        }
        if (!Multigrid.equalWithEpsilon(yOriginal, 0)) {
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

record GridPoint(double x, double y) {

    public double getDistance() {
        return Math.sqrt((x * x) + (y * y));
    }

    public double getDistance(GridPoint point) {
        return Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
    }

    public GridPoint rotateAndShift(double angle, double offset) {
        // Rotate the point
        double rotatedX = x * Math.cos(angle) - y * Math.sin(angle);
        double rotatedY = x * Math.sin(angle) + y * Math.cos(angle);

        // Shift the point parallel to the rotated line
        double shiftedX = rotatedX - offset * Math.sin(angle);
        double shiftedY = rotatedY + offset * Math.cos(angle);

        return new GridPoint(shiftedX, shiftedY);
    }
}

class GridTile {
    private final GridPoint intersection;
    private List<GridPoint> vertexList;
    private List<GridPoint> offsetList;
    private List<GridPoint> medianList;

    public GridTile(GridPoint intersectionPoint) {
        this.intersection = intersectionPoint;
    }

    public GridPoint getIntersection() {
        return intersection;
    }

    public void setVertexList(List<GridPoint> vertexList) {
        this.vertexList = Collections.unmodifiableList(vertexList);
    }

    public List<GridPoint> getVertexList() {
        return vertexList;
    }

    public List<GridPoint> getOffsetList() {
        return offsetList;
    }

    public void setOffsetList(List<GridPoint> offsetList) {
        this.offsetList = Collections.unmodifiableList(offsetList);
    }

    public List<GridPoint> getMedianList() {
        return medianList;
    }

    public void setMedianList(List<GridPoint> medianList) {
        this.medianList = Collections.unmodifiableList(medianList);
    }

    public boolean isRhombus() {
        if (vertexList.size() != 4) {
            return false;
        }
        GridPoint p1 = vertexList.get(0);
        GridPoint p2 = vertexList.get(1);
        GridPoint p3 = vertexList.get(2);
        GridPoint p4 = vertexList.get(3);

        double side1 = p1.getDistance(p2);
        double side2 = p2.getDistance(p3);
        double side3 = p3.getDistance(p4);
        double side4 = p4.getDistance(p1);

        return Multigrid.equalWithEpsilon(side1, side2) &&
                Multigrid.equalWithEpsilon(side2, side3) &&
                Multigrid.equalWithEpsilon(side3, side4) &&
                Multigrid.equalWithEpsilon(side4, side1);
    }
}
