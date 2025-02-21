package tilings.multigrid;

import java.util.*;

public class GridTile {
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