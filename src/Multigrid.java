import java.util.*;

public class Multigrid {
    private final int symmetry;
    private final int radius;
    private final double offset;
    private final List<GridLine> lineList = new ArrayList<>();
    private final HashMap<IntersectionPoint, Set<GridLine>> intersectionMap = new HashMap<>();
    private final HashMap<GridLine, List<IntersectionPoint>> lineMap = new HashMap<>();

    private List<Double> sinTable = new ArrayList<>();
    private List<Double> cosTable = new ArrayList<>();

    private Map<IntersectionPoint, List<IntersectionPoint>> dualMap = new HashMap<>();

    public Multigrid(int symmetry, int radius, double offset) {
        this.radius = radius;
        this.symmetry = symmetry;
        this.offset = offset;

        for (int i = 0; i < symmetry; i++) {
            double angle = 2 * i * Math.PI / symmetry;
            for (int j = -radius; j <= radius; j++) {
                GridLine line = new GridLine(angle, offset + j);
                lineList.add(line);
                lineMap.put(line, new ArrayList<>());
            }
            sinTable.add(Math.sin(i * getMultiplier()));
            cosTable.add(Math.cos(i * getMultiplier()));
        }
        calculateIntersections();
    }

    private void calculateIntersections() {
        Map<IntersectionPoint, IntersectionPoint> epsilonIntersectionMap = new HashMap<>();

        for (int i = 0; i < lineList.size(); i++) {
            GridLine lineOne = lineList.get(i);
            for (int j = i + 1; j < lineList.size(); j++) {
                GridLine lineTwo = lineList.get(j);
                IntersectionPoint point = lineOne.getIntersectionPoint(lineTwo);
                Set<GridLine> lineSet;
                if (point != null) {
                    IntersectionPoint roundedPoint = new IntersectionPoint(roundWithEpsilon(point.x()), roundWithEpsilon(point.y()));
                    IntersectionPoint existingPoint = epsilonIntersectionMap.get(roundedPoint);
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

        sortIntersectionPoints();

        for (IntersectionPoint intersection : getIntersections()) {
            List<Double> angles = new ArrayList<>();
            Set<GridLine> lineSet = getIntersectedLineSet(intersection);

            for (GridLine line : lineSet) {
                angles.add(line.getAngle());
                angles.add((line.getAngle() + Math.PI) % (2 * Math.PI));
            }

            angles.sort(Comparator.naturalOrder());

            List<IntersectionPoint> offsetList = new ArrayList<>();
            for (Double angle : angles) {
                double x = intersection.x() + EPSILON * -Math.sin(angle);
                double y = intersection.y() + EPSILON * Math.cos(angle);
                IntersectionPoint offset = new IntersectionPoint(x, y);
                offsetList.add(offset);
            }

            List<IntersectionPoint> medianList = new ArrayList<>();
            int iMax = offsetList.size();

            for (int i = 0; i < iMax; i++) {
                IntersectionPoint offset = offsetList.get(i);
                double x0 = offset.x();
                double y0 = offset.y();

                double x1 = offsetList.get((i + 1) % iMax).x();
                double y1 = offsetList.get((i + 1) % iMax).y();

                double xm = (x0 + x1) / 2;
                double ym = (y0 + y1) / 2;

                IntersectionPoint median = new IntersectionPoint(xm, ym);
                medianList.add(median);

            }

            List<IntersectionPoint> dualList = new ArrayList<>();
            double meanX = 0, meanY = 0;

            for (IntersectionPoint median : medianList) {
                double xd = 0, yd = 0;

                for (int i = 0; i < this.symmetry; i++) {
                    double ci = cosTable.get(i);
                    double si = sinTable.get(i);

                    // todo: sort out the non symmetrical offsets
                    double k = Math.floor(median.x() * ci + median.y() * si - offset);

                    xd += k * ci;
                    yd += k * si;
                }

                IntersectionPoint dual = new IntersectionPoint(xd, yd);
                dualList.add(dual);

                meanX += xd;
                meanY += yd;
            }

            dualMap.put(intersection, dualList);
//            System.out.println("dualList = " + dualList.size());
        }
//        System.out.println("offsetList = " + offsetList);

    }

    private void sortIntersectionPoints() {
        for (GridLine line : lineMap.keySet()) {
            List<IntersectionPoint> points = lineMap.get(line);
            points.sort(line);
        }
    }

    public Set<IntersectionPoint> getIntersections() {
        return intersectionMap.keySet();
    }

    public Map<IntersectionPoint, List<IntersectionPoint>> getDualMap() {
        return dualMap;
    }

    public Set<GridLine> getIntersectedLineSet(IntersectionPoint point) {
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

    public List<IntersectionPoint> getIntersectionList(GridLine line) {
        return lineMap.get(line);
    }

    public static void main(String[] args) {

        GridLine line = new GridLine(0, 0);

        IntersectionPoint p1 = new IntersectionPoint(1, 0);
        IntersectionPoint p2 = new IntersectionPoint(5, 0);
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

    private double getMultiplier() { // dependencies: symmetry
        return 2 * Math.PI / this.symmetry;
    }
}

class GridLine implements Comparator<IntersectionPoint> {
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

    public IntersectionPoint getIntersectionPoint(GridLine line) {
        if (angle == line.getAngle()) {
            return null;
        }
        double determinant = angleCos * line.angleSin - angleSin * line.angleCos;
        if (Math.abs(determinant) < 1e-10) {
            return null;
        }
        double offsetOne = offset;
        double offsetTwo = line.offset;
//        double x = (offsetOne * line.angleCos - offsetTwo * angleCos) / determinant;
//        double y = (offsetOne * line.angleSin - offsetTwo * angleSin) / determinant;

        double y = -(offsetOne * line.angleCos - offsetTwo * angleCos) / determinant;
        double x = (offsetOne * line.angleSin - offsetTwo * angleSin) / determinant;

        // round up the coordinates

//        System.out.println("x = " + x);
//        System.out.println("Multigrid.roundWithEpsilon(x) = " + Multigrid.roundWithEpsilon(x));
//        System.out.println();

//        System.out.print("x = " + x);
//        System.out.println(" Multigrid.roundWithEpsilon(x) = " + Multigrid.roundWithEpsilon(x));
//        return new IntersectionPoint(Multigrid.roundWithEpsilon(x), y);
//        return new IntersectionPoint(Multigrid.roundWithEpsilon(x), Multigrid.roundWithEpsilon(y));
        return new IntersectionPoint(x, y);
    }

    public boolean isPointOnLine(IntersectionPoint p) {
        double lineValue = -p.x() * angleSin + p.y() * angleCos - offset;
        return Multigrid.equalWithEpsilon(lineValue, 0);
    }

    public double reversePoint(IntersectionPoint point) {
//        double xOriginal = point.x() * angleCos - point.y() * angleSin;
//        double yOriginal = point.x() * angleSin - point.y() * angleCos + offset;

        double yOriginal = point.x() * angleCos + point.y() * angleSin - offset;
        double xOriginal = point.x() * angleSin - point.y() * angleCos;

        if (Multigrid.equalWithEpsilon(xOriginal, 0)) {
            xOriginal = 0;
        }
        if (!Multigrid.equalWithEpsilon(yOriginal, 0)) {
//            throw new RuntimeException("y is not close to zero!");
        }
        return xOriginal;
    }

    @Override
    public int compare(IntersectionPoint o1, IntersectionPoint o2) {
        return Double.compare(reversePoint(o1), reversePoint(o2));
    }

    @Override
    public String toString() {
        return "GridLine[angle=" + angle + ", offset=" + offset + "]";
    }
}

record IntersectionPoint(double x, double y) {
    IntersectionPoint {
        if (x == -0.0) {
            x = 0.0;
        }
        if (y == -0.0) {
            y = 0.0;
        }
    }

    public double getDistance() {
        return Math.sqrt((x * x) + (y * y));
    }

    public double getDistance(IntersectionPoint point) {
        double distance = Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
        return Multigrid.roundWithEpsilon(distance);
    }

    public IntersectionPoint rotateAndShift(double angle, double offset) {
        // Rotate the point
        double rotatedX = x * Math.cos(angle) - y * Math.sin(angle);
        double rotatedY = x * Math.sin(angle) + y * Math.cos(angle);

        // Shift the point parallel to the rotated line
        double shiftedX = rotatedX - offset * Math.sin(angle);
        double shiftedY = rotatedY + offset * Math.cos(angle);

        return new IntersectionPoint(shiftedX, shiftedY);
    }
}
