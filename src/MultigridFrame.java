import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.List;
import java.util.Set;

public class MultigridFrame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultigridFrame().setVisible(true));
    }

    private final ColliderPanel colliderPanel = new ColliderPanel();
    private final JPanel toolPanel = new JPanel();
    private Multigrid multigrid = new Multigrid(5, 1, 0.22);

    public MultigridFrame() {
        setTitle("Collider frame");
        setMinimumSize(new Dimension(800, 600));
//        setExtendedState(JFrame.MAXIMIZED_BOTH);

        add(colliderPanel);

        toolPanel.setBorder(BorderFactory.createEtchedBorder());
        toolPanel.add(new JLabel("Symmetry"));
        SpinnerNumberModel symmetryModel = new SpinnerNumberModel(multigrid.getSymmetry(), 1, 10, 1);
        final JSpinner symmetrySpinner = new JSpinner(symmetryModel);
        toolPanel.add(symmetrySpinner);

        toolPanel.add(new JLabel("Offset"));
        SpinnerNumberModel offsetModel = new SpinnerNumberModel(multigrid.getOffset(), 0, 2, 0.01);
        JSpinner offsetSpinner = new JSpinner(offsetModel);
        toolPanel.add(offsetSpinner);
        JFormattedTextField tf = ((JSpinner.DefaultEditor) offsetSpinner.getEditor()).getTextField();
        tf.setColumns(3);
        add(toolPanel, BorderLayout.SOUTH);

        toolPanel.add(new JLabel("Radius"));
        SpinnerNumberModel radiusModel = new SpinnerNumberModel(multigrid.getRadius(), 0, 10, 1);
        JSpinner radiusSpinner = new JSpinner(radiusModel);
        toolPanel.add(radiusSpinner);

        ChangeListener changeListener = _ -> {
            int symmetry = (int) symmetrySpinner.getValue();
            int radius = (int) radiusSpinner.getValue();
            double offset = (double) offsetSpinner.getValue();
            multigrid = new Multigrid(symmetry, radius, offset);
            colliderPanel.revalidate();
            colliderPanel.repaint();
        };
        symmetrySpinner.addChangeListener(changeListener);
        radiusSpinner.addChangeListener(changeListener);
        offsetSpinner.addChangeListener(changeListener);

        add(toolPanel, BorderLayout.SOUTH);
        SwingUtilities.invokeLater(() -> offsetSpinner.requestFocus());
    }

    private class ColliderPanel extends JPanel {
        private final AffineTransform transform = new AffineTransform();

        public ColliderPanel() {
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform newTransform = g2.getTransform();
            newTransform.concatenate(getTransform());
            g2.setTransform(newTransform);

//            drawAxis(g2);
//            debug(g2);

            g2.setStroke(new BasicStroke((float) .05, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.LIGHT_GRAY);

            drawLines(g2);

            g2.setColor(Color.RED);

            drawTiles(g2);

            g2.dispose();
        }

        private void drawTiles(Graphics2D g2) {
            g2.setColor(Color.BLUE);

            List<GridTile> tileList = multigrid.getTileList();
            for (int i = 0; i < tileList.size(); i++)
            {
                GridTile tile = tileList.get(i);
                Path2D.Double path = new Path2D.Double();
                List<GridPoint> vertextList = tile.getVertexList();

                path.moveTo(vertextList.get(0).x(), vertextList.get(0).y());
                for (int j = 1; j < vertextList.size(); j++) {
                    GridPoint dual = vertextList.get(j);
                    path.lineTo(dual.x(), dual.y());
                }
                path.closePath();
                g2.draw(path);

               /* GridPoint intersection = tile.getIntersection();
                Set<GridLine> lineSet = multigrid.getIntersectedLineSet(intersection);

                Graphics2D temp = (Graphics2D) g2.create();
                temp.setColor(Color.MAGENTA);
                temp.translate(-intersection.x(), -intersection.y());
                temp.draw(path);
                temp.dispose();
                drawPoint(g2, intersection);*/
            }
        }

        private void drawAxis(Graphics2D g2) {
            g2.setColor(Color.BLUE);
            fillCircle(g2, 0, 0, .05);
            g2.setStroke(new BasicStroke((float) .05));
            drawLine(g2, new GridLine(0,0));
            drawLine(g2, new GridLine(-Math.PI/2,0));
        }

        private void debug(Graphics2D g2) {
            g2.setStroke(new BasicStroke((float) .05));
            fillCircle(g2, 0, 0, .5);

            GridLine lineY = new GridLine(0, 0);
            drawLine(g2, lineY);

            GridLine lineX = new GridLine(Math.PI / 2, 0);
            drawLine(g2, lineX);

            GridLine line2 = new GridLine(1, 0);
            drawLine(g2, line2);

            GridLine line3 = new GridLine(0, 1);
            drawLine(g2, line3);

            GridPoint p = lineX.getIntersectionPoint(line3);
            g2.setColor(Color.RED);
            drawPoint(g2, p);

            GridPoint pp = line2.getIntersectionPoint(line3);
            g2.setColor(Color.MAGENTA);
            drawPoint(g2, pp);
        }

        @Override
        public void doLayout() {
            super.doLayout();
            transform.setToIdentity();
            transform.translate(getWidth() / 2.0, getHeight() / 2.0);

            int size = Math.max(getWidth(), getHeight());
//            double scale =  (size/(20.0 / 1.2 *multigrid.getRadius()));
            double scale = 40;
            transform.scale(scale, scale);
        }

        public AffineTransform getTransform() {
            return transform;
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e) {
            super.processMouseMotionEvent(e);
            repaint();
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
            repaint();
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            Point2D.Double p = new Point2D.Double(event.getPoint().getX(), event.getPoint().getY());
            Point2D point;
            try {
                point = getTransform().inverseTransform(p, new Point2D.Double());
            } catch (NoninvertibleTransformException e) {
                // this should never happen
                throw new AssertionError(e);
            }
            return "x = " + point.getX() + " y = " + point.getY();
        }

        private void fillCircle(Graphics2D g2, double x, double y, double radius) {
            g2.fill(new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius));
        }

        private void drawLines(Graphics2D g2) {
            for (GridLine line : multigrid.getLineList()) {
                drawLine(g2, line);
            }
        }

        private static void drawLine(Graphics2D g2, GridLine line) {
            AffineTransform t = g2.getTransform();
            Rectangle clipBounds = g2.getClipBounds();
            int lineLength = clipBounds.width + clipBounds.height;
            g2.rotate(line.getAngle() + Math.PI / 2);
            g2.translate(0, -line.getOffset());
            g2.drawLine(-lineLength / 2, 0, lineLength, 0);
            g2.setTransform(t);
        }

        private void drawIntersections(Graphics2D g2) {
            Set<GridPoint> intersectionSet = multigrid.getIntersections();
            for (GridPoint point : intersectionSet) {
                drawPoint(g2, point);
            }
        }

        private void drawPoint(Graphics2D g2, GridPoint point) {
            fillCircle(g2, point.x(), point.y(), .05);
        }
    }
}

