package tilings.multigrid;

public record GridPoint(double x, double y) {

    public double getDistance() {
        return Math.sqrt((x * x) + (y * y));
    }

    public double getDistance(GridPoint point) {
        return Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
    }

    public GridPoint getCenter(GridPoint point) {
        return new GridPoint((x + point.x) / 2, (y + point.y) / 2);
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