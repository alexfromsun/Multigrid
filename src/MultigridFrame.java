import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

public class MultigridFrame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultigridFrame().setVisible(true));
    }

    private final ColliderPanel colliderPanel = new ColliderPanel();
    private final JToolBar toolBar = new JToolBar();
    private Multigrid multigrid = new Multigrid(5, 3, 0.43);
    private JLabel zoomLabel = new JLabel("100%");

    public MultigridFrame() {
        setTitle("Collider frame");
        setMinimumSize(new Dimension(800, 600));
//        setExtendedState(JFrame.MAXIMIZED_BOTH);

        final JScrollPane scrollPane = new JScrollPane(colliderPanel) {
            @Override
            protected void processMouseWheelEvent(MouseWheelEvent e) {
                if ((e.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK) {
                    colliderPanel.updateZoom(e.getWheelRotation());
                    zoomLabel.setText((int) (colliderPanel.getZoom() * 100) + "%");
                } else {
                    super.processMouseWheelEvent(e);
                }
            }

            @Override
            public String toString() {
                return "ScrollPane(colliderPanel)";
            }
        };

        scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(30);
        add(scrollPane);

        toolBar.add(new JLabel("Symmetry "));
        SpinnerNumberModel symmetryModel = new SpinnerNumberModel(multigrid.getSymmetry(), 3, 15, 1);
        final JSpinner symmetrySpinner = new JSpinner(symmetryModel);
        symmetrySpinner.setMaximumSize(symmetrySpinner.getPreferredSize());
        toolBar.add(symmetrySpinner);
        toolBar.addSeparator();

        toolBar.add(new JLabel("Offset "));

        SpinnerNumberModel offsetModel = new SpinnerNumberModel(multigrid.getOffset(), 0, 3, 0.01);
        JSpinner offsetSpinner = new JSpinner(offsetModel);
        offsetSpinner.setEditor(new JSpinner.NumberEditor(offsetSpinner, "#.##"));
        Dimension preferredSize = offsetSpinner.getPreferredSize();
        preferredSize.width += 25;
        offsetSpinner.setMaximumSize(preferredSize);

        toolBar.add(offsetSpinner);
        toolBar.addSeparator();

        JFormattedTextField tf = ((JSpinner.DefaultEditor) offsetSpinner.getEditor()).getTextField();
        tf.setColumns(3);

        toolBar.add(new JLabel("Radius "));

        SpinnerNumberModel radiusModel = new SpinnerNumberModel(multigrid.getGridRadius(), 1, 10, 1);
        JSpinner radiusSpinner = new JSpinner(radiusModel);
        radiusSpinner.setMaximumSize(radiusSpinner.getPreferredSize());
        toolBar.add(radiusSpinner);
        toolBar.addSeparator();

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

        toolBar.add(Box.createHorizontalGlue());

        toolBar.add(zoomLabel);
        toolBar.addSeparator();
        JButton plusButton = new JButton("+");
        plusButton.setToolTipText("Ctrl +");

        toolBar.add(plusButton);
        JButton minusButton = new JButton("-");
        minusButton.setToolTipText("Ctrl -");
        toolBar.add(minusButton);

        ActionListener zoomAction = e -> {
            int direction = e.getSource() == plusButton ? -1 : 1;
            colliderPanel.updateZoom(direction);
            zoomLabel.setText((int) (colliderPanel.getZoom() * 100) + "%");
        };

        minusButton.addActionListener(zoomAction);
        plusButton.addActionListener(zoomAction);


        add(toolBar, BorderLayout.PAGE_START);
        SwingUtilities.invokeLater(() -> offsetSpinner.requestFocus());
    }

    private class ColliderPanel extends JPanel {
        private final AffineTransform transform = new AffineTransform();
        private double zoom = 1;
        private final List<Color> colorList = new ArrayList<>();

        public ColliderPanel() {
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        public double getZoom() {
            return zoom;
        }

        public void updateZoom(int direction) {
            if (zoom <= 1 && direction == 1) {
                return;
            }
            zoom += (-.1 * direction);
            JViewport viewport = (JViewport) getParent();
            int viewWidth = (int) (viewport.getWidth() * zoom);
            int viewHeight = (int) (viewport.getHeight() * zoom);

            Dimension preferredSize = getPreferredSize();

            Point viewPosition = viewport.getViewPosition();

            int newViewX = viewPosition.x + (viewWidth - preferredSize.width) / 2;
            int newViewY = viewPosition.y + (viewHeight - preferredSize.height) / 2;

            setPreferredSize(new Dimension(viewWidth, viewHeight));
            viewport.setViewPosition(new Point(newViewX, newViewY));
            revalidate();
            repaint();
        }

        public void setZoom(double zoom) {
            this.zoom = zoom;
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

//            debug(g2);

            g2.setStroke(new BasicStroke((float) .05, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.LIGHT_GRAY);

            drawLines(g2);

            g2.setColor(Color.RED);

            drawTiles(g2);
//            drawAxis(g2);

            g2.dispose();
        }

        public List<Color> getColorList() {
            if (colorList.size() != multigrid.getTileAreaList().size()) {
                colorList.clear();
                // Distribute the hue values evenly around the color wheel
                int colorListSize = multigrid.getTileAreaList().size();
                for (int i = 0; i < colorListSize; i++) {
                    float hue = (float) i / colorListSize;     // 0.0 to <1.0
                    float saturation = 0.8f;      // set between 0.0 and 1.0
                    float brightness = 0.9f;      // set between 0.0 and 1.0

                    // Create the Color using HSB -> RGB conversion
                    Color color = Color.getHSBColor(hue, saturation, brightness);
                    colorList.add(color);
                }
            }
            return colorList;
        }

        private void drawTiles(Graphics2D g2) {
            List<GridTile> tileList = multigrid.getTileList();

            for (int i = 0; i < tileList.size(); i++) {
                GridTile tile = tileList.get(i);
                Path2D.Double path = new Path2D.Double();
                List<GridPoint> vertextList = tile.getVertexList();

                path.moveTo(vertextList.getFirst().x(), vertextList.getFirst().y());
                for (int j = 1; j < vertextList.size(); j++) {
                    GridPoint dual = vertextList.get(j);
                    path.lineTo(dual.x(), dual.y());
                }
                path.closePath();

                int colorIndex = multigrid.getTileAreaList().indexOf(tile.getArea());
                g2.setColor(getColorList().get(colorIndex));
                g2.fill(path);
                g2.setColor(Color.BLACK);
                g2.draw(path);
            }
        }

        private void fillTriangle(Graphics2D g2, GridPoint a, GridPoint b, double t) {
            double sideLength = .3;

            double mx = a.x() + t * (b.x() - a.x());
            double my = a.y() + t * (b.y() - a.y());

            double dx = b.x() - a.x();
            double dy = b.y() - a.y();
            double l = Math.sqrt(dx * dx + dy * dy);

            if (l == 0) {
                throw new IllegalArgumentException("Points are too close");
            }

            double dHatX = dx / l;
            double dHatY = dy / l;

            // 4. Unit normal vector (nHatX, nHatY) - perpendicular to (dx, dy)
            double nHatX = -dy / l;
            double nHatY = dx / l;

            // 5. Triangle dimensions
            double halfBase = sideLength / 2.0;
            double h = Math.sqrt(sideLength * sideLength - halfBase * halfBase);

            Path2D.Double triangle = new Path2D.Double();
            triangle.moveTo(mx - halfBase * nHatX,
                    my - halfBase * nHatY);

            triangle.lineTo(mx + halfBase * nHatX,
                    my + halfBase * nHatY);
            triangle.lineTo(mx + h * dHatX,
                    my + h * dHatY);
            triangle.closePath();
            g2.fill(triangle);
        }

        private void drawAxis(Graphics2D g2) {
            g2.setColor(Color.BLACK);
            fillCircle(g2, 0, 0, .05);
            g2.setStroke(new BasicStroke((float) .05));
            drawLine(g2, new GridLine(0, 0));
            drawLine(g2, new GridLine(-Math.PI / 2, 0));
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

            JViewport viewport = (JViewport) getParent();
            int viewWidth = (int) (viewport.getWidth() * zoom);
            int viewHeight = (int) (viewport.getHeight() * zoom);

            double scale = (double) Math.min(viewWidth, viewHeight) / (2 * (multigrid.getTilingRadius() + 1));
            scale *= zoom;
            transform.scale(scale, scale);
            transform.rotate(-Math.PI / (2 * multigrid.getSymmetry()));
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

        private void drawCircle(Graphics2D g2, double x, double y, double radius) {
            g2.draw(new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius));
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

