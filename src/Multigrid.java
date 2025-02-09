import java.util.*;

public class Multigrid {
    private final int symmetry;
    private final int gridRadius;
    private final double offset;
    private final List<Grid> gridList = new ArrayList<>();

    private final List<GridLine> lineList = new ArrayList<>();
    private final HashMap<GridPoint, Set<GridLine>> intersectionMap = new HashMap<>();
    private final HashMap<GridLine, List<GridPoint>> lineMap = new HashMap<>();

    private final List<Double> sinTable = new ArrayList<>();
    private final List<Double> cosTable = new ArrayList<>();

    private Map<GridPoint, List<GridPoint>> dualMap = new HashMap<>();

    private List<GridTile> tileList;
    private List<Double> tileAreaList;

    private double tilingRadius;
    private double gridInset;

    public Multigrid(int symmetry, int gridRadius, double offset, double gridInset) {
        this.gridRadius = gridRadius;
        this.symmetry = symmetry;
        this.offset = offset;
        this.gridInset = gridInset;
        double[] offsetArray = new double[symmetry];
        Arrays.fill(offsetArray, offset);

//        offsetArray = new double[]{0, -.2, .3, -.4, .5};

        double multiplier = 2 * Math.PI / symmetry;
        for (int i = 0; i < symmetry; i++) {
            double angle = 2 * i * Math.PI / symmetry;
            Grid grid = new Grid(angle, offsetArray[i], gridRadius, gridInset);
            gridList.add(grid);
            lineList.addAll(grid.getLineList());

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
                    GridPoint roundedPoint = new GridPoint(roundWithSmallEpsilon(point.x()), roundWithSmallEpsilon(point.y()));
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

                    if (lineMap.get(lineOne) == null) {
                        lineMap.put(lineOne, new ArrayList<>());
                    }

                    if (lineMap.get(lineTwo) == null) {
                        lineMap.put(lineTwo, new ArrayList<>());
                    }

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
        Set<Double> tileAreaSet = new TreeSet<>();

        for (GridPoint intersection : getIntersections()) {

            // Skip the hanging tiles
            if (!contains(intersection)) {
                continue;
            }

            List<Double> angles = new ArrayList<>();
            Set<GridLine> lineSet = getIntersectedLineSet(intersection);

            for (GridLine line : lineSet) {
                angles.add(line.getAngle());
                angles.add((line.getAngle() + Math.PI) % (2 * Math.PI));
            }

            angles.sort(Comparator.naturalOrder());

            List<GridPoint> offsetList = new ArrayList<>();
            for (Double angle : angles) {
                double x = intersection.x() + SMALL_EPSILON * -Math.sin(angle);
                double y = intersection.y() + SMALL_EPSILON * Math.cos(angle);
                GridPoint offset = new GridPoint(x, y);
                offsetList.add(offset);
            }
            List<GridPoint> medianList = new ArrayList<>();
            int offsetListSize = offsetList.size();

            for (int i = 0; i < offsetListSize; i++) {
                GridPoint offset = offsetList.get(i);
                double x0 = offset.x();
                double y0 = offset.y();

                double x1 = offsetList.get((i + 1) % offsetListSize).x();
                double y1 = offsetList.get((i + 1) % offsetListSize).y();

                double xm = (x0 + x1) / 2;
                double ym = (y0 + y1) / 2;

                GridPoint median = new GridPoint(xm, ym);
                medianList.add(median);
            }

            List<GridPoint> dualList = new ArrayList<>();
            List<Integer> dualIndexList = new ArrayList<>();
            double meanX = 0, meanY = 0;

//          x=-0.24721359549995792, y=2.918393927182259E-17
            if (intersection.x() == -0.24721359549995792 && intersection.y() == 2.918393927182259E-17) {
//                System.out.println();
            }

            for (GridPoint median : medianList) {
                double xd = 0, yd = 0;

                double vertexIndex = 0;


                for (int i = 0; i < this.symmetry; i++) {
                    double ci = cosTable.get(i);
                    double si = sinTable.get(i);

                    double temp = median.x() * ci + median.y() * si - gridList.get(i).getOffset();
                    double k = Math.floor(temp);

                    vertexIndex += k;
                    xd += k * ci;
                    yd += k * si;
                }
                GridPoint dual =
                        new GridPoint(roundWithSmallEpsilon(xd), roundWithSmallEpsilon(yd));
                dualList.add(dual);

                int sum = (int) vertexIndex % this.symmetry;
                dualIndexList.add(Math.abs(sum));
                meanX += xd;
                meanY += yd;
            }

            dualMap.put(intersection, dualList);

            if (isRhombus(dualList)) {

                GridTile tile = new GridTile(intersection, dualList);
                tileList.add(tile);
                tileAreaSet.add(tile.getArea());

                if (getSymmetry() == 5) {
                    tile.setVertexIndices(dualIndexList);
                }

                for (GridPoint point : dualList) {
                    if (point.x() > tilingRadius) {
                        tilingRadius = point.x();
                    }
                    if (point.y() > tilingRadius) {
                        tilingRadius = point.y();
                    }
                }
            }
        }
        tileList = Collections.unmodifiableList(tileList);
        tileAreaList = Collections.unmodifiableList(new ArrayList<>(tileAreaSet));
    }

    public boolean contains(GridPoint point) {
        for (Grid grid : gridList) {
            if (!grid.contains(point)) {
                return false;
            }
        }
        return true;
    }

    public boolean isRhombus(List<GridPoint> pointList) {
        if (pointList.size() != 4) {
            return false;
        }
        GridPoint p1 = pointList.get(0);
        GridPoint p2 = pointList.get(1);
        GridPoint p3 = pointList.get(2);
        GridPoint p4 = pointList.get(3);

        double side1 = p1.getDistance(p2);
        double side2 = p2.getDistance(p3);
        double side3 = p3.getDistance(p4);
        double side4 = p4.getDistance(p1);

        return equalWithBigEpsilon(side1, 1) && equalWithBigEpsilon(side2, 1)
                && equalWithBigEpsilon(side3, 1) && equalWithBigEpsilon(side4, 1);
    }

    public double getGridInset() {
        return gridInset;
    }

    public List<GridTile> getTileList() {
        return tileList;
    }

    public List<Double> getTileAreaList() {
        return tileAreaList;
    }

    public double getTilingRadius() {
        return tilingRadius;
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

    public int getGridRadius() {
        return gridRadius;
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

    private static final double SMALL_EPSILON = 1e-10;
    private static final double BIG_EPSILON = 1e-6;

    static boolean equalWithSmallEpsilon(double a, double b) {
        return equalWithEpsilon(a, b, SMALL_EPSILON);
    }

    static boolean equalWithBigEpsilon(double a, double b) {
        return equalWithEpsilon(a, b, BIG_EPSILON);
    }

    private static boolean equalWithEpsilon(double a, double b, double epsilon) {
        return (Math.abs(a - b) <= epsilon);
    }

    static double roundWithSmallEpsilon(double d) {
        return roundWithEpsilon(d, SMALL_EPSILON);
    }

    static double roundWithBigEpsilon(double d) {
        return roundWithEpsilon(d, BIG_EPSILON);
    }

    private static double roundWithEpsilon(double d, double epsilon) {
        return Math.round(d * (1.0 / epsilon)) / (1.0 / epsilon);
    }
}

class Grid {
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

record GridPoint(double x, double y) {

    public double getDistance() {
        return Math.sqrt((x * x) + (y * y));
    }

    public double getDistance(GridPoint point) {
        return Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
    }

    public GridPoint getCenter(GridPoint point) {
        return new GridPoint((x + point.x)/2 , (y + point.y)/2);
    }

    public GridPoint getPointInDirection(GridPoint p, double distance) {
        double length = getDistance(p);
        double fx = (p.x - x) / length;
        double fy = (p.y - y) / length;
        return new GridPoint(x + fx * distance, y + fy * distance);
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
    private final List<GridPoint> vertexList;
    private final double area;
    private final Map<GridPoint, Integer> vertexIndexMap = new HashMap<>();

    public GridTile(GridPoint intersectionPoint, List<GridPoint> vertexList) {
        this.vertexList = vertexList;
        this.intersection = intersectionPoint;

        GridPoint p1 = vertexList.get(0);
        GridPoint p2 = vertexList.get(1);
        GridPoint p3 = vertexList.get(2);
        GridPoint p4 = vertexList.get(3);

        double d1 = p1.getDistance(p3);
        double d2 = p2.getDistance(p4);
        area = Multigrid.roundWithBigEpsilon(.5 * d1 * d2);
    }

    void setVertexIndices(List<Integer> indices) {
        if (indices.size() != vertexList.size()) {
            throw new RuntimeException("Index count does not match");
        }
        GridPoint mainVertex = null;
        List<Integer> noDuplicates = new ArrayList<>();
        for (Integer index : indices) {
            int i = noDuplicates.indexOf(index);
            if (i == -1) {
                noDuplicates.add(index);
            } else {
                noDuplicates.remove(i);
            }
        }

        if (noDuplicates.size() != 2) {
            throw new RuntimeException("Expected two unique indexes");
        }

        int min = Collections.min(noDuplicates);
        int max = Collections.max(noDuplicates);

// 0.2 - 0/3 (0) 2/4 (2) type 1
// 0.4 - 0/3 (3) 1/4 (1) type 2 (reversed)
// 0.6 - 0/2 (2) 1/4 (4) type 2
// 0.8 - 0/2 (0) 1/3 (3) type 1 (reversed)

        if (min == 0 && max == 2) {
            mainVertex = vertexList.get(indices.indexOf(2));
        } else if (min == 0 && max == 3) {
            mainVertex = vertexList.get(indices.indexOf(0));
        } else if (min == 1 && max == 3) {
            mainVertex = vertexList.get(indices.indexOf(1));
        } else if (min == 2 && max == 4) {
            mainVertex = vertexList.get(indices.indexOf(2));
        } else if (min == 1 && max == 4) {
            mainVertex = vertexList.get(indices.indexOf(4));
        } else {
            System.out.println("min = " + min);
            System.out.println("max = " + max);
            throw new RuntimeException("Unexpected pair of indices");
        }

        while (mainVertex != vertexList.get(0)) {
            vertexList.add(vertexList.remove(0));
        }
    }

    // 0.587785 0.951057
    public double getArea() {
        return area;
    }

    public GridPoint getIntersection() {
        return intersection;
    }

    public List<GridPoint> getVertexList() {
        return vertexList;
    }
}

/*
private boolean isRightTriangle(Triangle t) {
    Complex AB = t.B().subtract(t.A());
    Complex AC = t.C().subtract(t.A());
    return (AB.getReal() * AC.getImaginary() - AB.getImaginary() * AC.getReal() < 0);
}

private Complex convertPoint(Complex point, Complex A, Complex B) {
    Complex AB = A.subtract(B);
    Complex normalizedPoint = point.multiply(AB.abs());
    double angle = Math.atan2(B.getImaginary() - A.getImaginary(), B.getReal() - A.getReal());
    double rotatedX = normalizedPoint.getReal() * Math.cos(angle) + normalizedPoint.getImaginary() * Math.sin(angle);
    double rotatedY = normalizedPoint.getReal() * Math.sin(angle) - normalizedPoint.getImaginary() * Math.cos(angle);
    Complex rotatedPoint = new Complex(rotatedX, rotatedY);
    return rotatedPoint.add(A);
}
*/

