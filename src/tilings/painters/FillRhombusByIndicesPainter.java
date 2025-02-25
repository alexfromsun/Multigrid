package tilings.painters;

import tilings.multigrid.GridPoint;
import tilings.multigrid.GridTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FillRhombusByIndicesPainter extends RhombusPainter {

    private List<Integer> indexSumList = new ArrayList<>();
    private List<Color> colorList;

    public FillRhombusByIndicesPainter(Set<List<Integer>> vertextIndexSet) {
        setVertexIndexSet(vertextIndexSet);
    }

    public void setVertexIndexSet(Set<List<Integer>> vertextIndexSet) {
        indexSumList.clear();
        for (List<Integer> indices : vertextIndexSet) {
            int sum = 0;
            for (Integer index : indices) {
                sum += index;
            }
            indexSumList.add(sum);
        }
        if (indexSumList.size() != vertextIndexSet.size()) {
            throw new AssertionError("Mismatch in the size of the vertextIndexSet and indexSumList");
        }
        colorList = createColorList(indexSumList.size());
    }

    @Override
    public void paintRhombus(Graphics2D g2, GridTile rhombus, GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
        int sum = rhombus.getVertexIndex(a) + rhombus.getVertexIndex(b)
                + rhombus.getVertexIndex(c) + rhombus.getVertexIndex(d);
        int colorIndex = indexSumList.indexOf(sum);
        g2.setColor(colorList.get(colorIndex));
        g2.fill(getPath(a, b, c, d));
    }

    @Override
    public String getName() {
        return "Color by indices";
    }
}
