import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.*;
import java.util.List;
import java.util.Set;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

public class MultigridFrame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultigridFrame().setVisible(true));
    }

    private final ColliderPanel colliderPanel = new ColliderPanel();
    private final JPanel toolPanel = new JPanel();
    private Multigrid multigrid = new Multigrid(5, 1, 0);

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
        SpinnerNumberModel offsetModel = new SpinnerNumberModel(multigrid.getOffset(), 0, 0.99, 0.01);
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
//            drawIntersections(g2);

            drawDuals(g2);

            g2.dispose();
        }

        private void drawDuals(Graphics2D g2) {
            g2.setColor(Color.BLUE);

            for (IntersectionPoint intersection : multigrid.getIntersections()) {
                Path2D.Double path = new Path2D.Double();
                List<IntersectionPoint> dualList = multigrid.getDualMap().get(intersection);

//                if (dualList.size() == 4) {
//                if (intersection.x() == 2.0 && intersection.y() == -0.6498393924658127)
                {
//                    System.out.println("dualList = " + dualList);
                    path.moveTo(dualList.get(0).x(), dualList.get(0).y());
                    for (int i = 1; i < dualList.size(); i++) {
                        IntersectionPoint dual = dualList.get(i);
                        path.lineTo(dual.x(), dual.y());
                    }
                    path.closePath();
                    g2.draw(path);
//                    Shape clip = g2.getClip();
//                    g2.setClip(path);
//                    fillCircle(g2, dualList.get(0).x(), dualList.get(0).y(), .2);
//                    g2.setClip(clip);
                }
            }
        }

        private void drawAxis(Graphics2D g2) {
            g2.setColor(Color.BLUE);
            fillCircle(g2, 0, 0, .05);
//            g2.setStroke(new BasicStroke((float) .05));
//            drawLine(g2, new GridLine(0,0));
//            drawLine(g2, new GridLine(-Math.PI/2,0));
        }

        private void debug(Graphics2D g2) {
            g2.setStroke(new BasicStroke((float) .05));

            fillCircle(g2, 0, 0, .5);

//            fillCircle(g2, 1, 1, .2);

            GridLine lineY = new GridLine(0, 0);
            drawLine(g2, lineY);

            GridLine lineX = new GridLine(Math.PI / 2, 0);
            drawLine(g2, lineX);

            GridLine line2 = new GridLine(1, 0);
            drawLine(g2, line2);

            GridLine line3 = new GridLine(0, 1);
            drawLine(g2, line3);

            IntersectionPoint p = lineX.getIntersectionPoint(line3);
            g2.setColor(Color.RED);
            drawPoint(g2, p);

            IntersectionPoint pp = line2.getIntersectionPoint(line3);
            g2.setColor(Color.MAGENTA);
            System.out.println("pp = " + pp);

            drawPoint(g2, pp);
        }

        @Override
        public void doLayout() {
            super.doLayout();
            transform.setToIdentity();
            transform.translate(getWidth() / 2.0, getHeight() / 2.0);

            double scale = 50;
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
            Set<IntersectionPoint> intersectionSet = multigrid.getIntersections();
            for (IntersectionPoint point : intersectionSet) {
                drawPoint(g2, point);
            }
        }

        private void drawPoint(Graphics2D g2, IntersectionPoint point) {
            fillCircle(g2, point.x(), point.y(), .05);
        }
    }
}

