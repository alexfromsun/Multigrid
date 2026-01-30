package tilings.multigrid;

import java.util.*;

public class Multigrid {
    private final int symmetry;
    private final int gridRadius;
    private final List<Grid> gridList = new ArrayList<>();
    private final List<Double> offsetList;

    private final List<GridLine> lineList = new ArrayList<>();
    private final HashMap<GridPoint, Set<GridLine>> intersectionMap = new HashMap<>();
    private final HashMap<GridLine, List<GridPoint>> lineMap = new HashMap<>();

    private final List<Double> sinTable = new ArrayList<>();
    private final List<Double> cosTable = new ArrayList<>();

    private Map<GridPoint, List<GridPoint>> dualMap = new HashMap<>();

    private List<GridTile> tileList;
    private List<Double> tileAreaList;
    private Set<List<Integer>> vertexIndexSet;

    private double tilingRadius;
    private double gridInset;

    public Multigrid(int symmetry, int gridRadius, List<Double> offsetList, double gridInset) {
        this.gridRadius = gridRadius;
        this.symmetry = symmetry;
        this.offsetList = Collections.unmodifiableList(offsetList);
        this.gridInset = gridInset;

        double multiplier = 2 * Math.PI / symmetry;
        for (int i = 0; i < symmetry; i++) {
            double angle = 2 * i * Math.PI / symmetry;
            Grid grid = new Grid(angle, offsetList.get(i), gridRadius, gridInset);
            gridList.add(grid);
            lineList.addAll(grid.getLineList());

            sinTable.add(Math.sin(i * multiplier));
            cosTable.add(Math.cos(i * multiplier));
        }
        calculateIntersections();
    }

    public List<Double> getOffsetList() {
        return offsetList;
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

        calculateTiles();
    }

    private void calculateTiles() {
        tileList = new ArrayList<>();
        Set<Double> tileAreaSet = new TreeSet<>();
        vertexIndexSet = new HashSet<>();

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

                GridTile tile = new GridTile(dualList, dualIndexList);
                tileList.add(tile);
                tileAreaSet.add(tile.getArea());

                dualIndexList.sort(null);
                vertexIndexSet.add(dualIndexList);

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

    public Set<List<Integer>> getVertexIndexSet() {
        return vertexIndexSet;
    }

    public double getTilingRadius() {
        return tilingRadius;
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

