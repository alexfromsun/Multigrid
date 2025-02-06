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
    private Multigrid multigrid = new Multigrid(5, 2, .2, 0);
    private JButton zoomButton = new JButton("100%");
    private JLabel statusBar = new JLabel();
    private boolean drawRhombi = true;
    private boolean showTrapezium;
    private boolean fillRhombi;
    private boolean showKitesAndDarts;
    private boolean showArrows;
    private boolean reverseRhombi;

    private static final double PHI = (1 + Math.sqrt(5)) / 2.0;

    public MultigridFrame() {
        setTitle("Collider frame");
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        final JScrollPane scrollPane = new JScrollPane(colliderPanel) {
            @Override
            protected void processMouseWheelEvent(MouseWheelEvent e) {
                if ((e.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK) {
                    colliderPanel.updateZoom(e.getWheelRotation());
                    zoomButton.setText((int) (colliderPanel.getZoom() * 100) + "%");
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

        toolBar.add(new JLabel("Radius "));

        SpinnerNumberModel radiusModel = new SpinnerNumberModel(multigrid.getGridRadius(), 0, 10, 1);
        JSpinner radiusSpinner = new JSpinner(radiusModel);
        radiusSpinner.setMaximumSize(radiusSpinner.getPreferredSize());
        toolBar.add(radiusSpinner);
        toolBar.addSeparator();

        toolBar.add(new JLabel("Offset "));

        SpinnerNumberModel offsetModel = new SpinnerNumberModel(multigrid.getOffset(), -3, 3, 0.01);
        JSpinner offsetSpinner = new JSpinner(offsetModel);
        offsetSpinner.setEditor(new JSpinner.NumberEditor(offsetSpinner, "#.##"));
        JFormattedTextField tf = ((JSpinner.DefaultEditor) offsetSpinner.getEditor()).getTextField();
        tf.setColumns(3);

        offsetSpinner.setMaximumSize(offsetSpinner.getPreferredSize());

        toolBar.add(offsetSpinner);
        toolBar.addSeparator();

        JButton insetButton = new JButton("Inset ");
        insetButton.setToolTipText("Set inset to 0");
        insetButton.setFocusable(false);
        toolBar.add(insetButton);

        SpinnerNumberModel insetModel = new SpinnerNumberModel(multigrid.getGridInset(), 0, .99, 0.02);
        JSpinner insetSpinner = new JSpinner(insetModel);
        insetSpinner.setEditor(new JSpinner.NumberEditor(insetSpinner, "#.##"));

        JFormattedTextField tfInset = ((JSpinner.DefaultEditor) insetSpinner.getEditor()).getTextField();
        tfInset.setColumns(3);
        insetSpinner.setMaximumSize(insetSpinner.getPreferredSize());

        insetButton.addActionListener(e -> {
            insetSpinner.setValue(0.0);
        });

        toolBar.add(insetSpinner);
        toolBar.addSeparator();

        JCheckBox drawRhombiCheckbox = new JCheckBox("Draw rhombi", drawRhombi);
        toolBar.add(drawRhombiCheckbox);
        drawRhombiCheckbox.addActionListener(e -> {
            drawRhombi = drawRhombiCheckbox.isSelected();
            repaint();
        });

        JCheckBox trapeziumHbsCheckbox = new JCheckBox("HBS", showTrapezium);
        toolBar.add(trapeziumHbsCheckbox);
        trapeziumHbsCheckbox.addActionListener(e -> {
            showTrapezium = trapeziumHbsCheckbox.isSelected();
            repaint();
        });

        JCheckBox fillRhombiCheckbox = new JCheckBox("Color");
        toolBar.add(fillRhombiCheckbox);
        fillRhombiCheckbox.addActionListener(e -> {
            fillRhombi = fillRhombiCheckbox.isSelected();
            repaint();
        });

        JCheckBox kitesAndDartsCheckbox = new JCheckBox("Kites and Darts");
        toolBar.add(kitesAndDartsCheckbox);
        kitesAndDartsCheckbox.addActionListener(e -> {
            showKitesAndDarts = kitesAndDartsCheckbox.isSelected();
            repaint();
        });

        JCheckBox arrowsCheckbox = new JCheckBox("Arrows");
        JCheckBox reverseArrowsCheckbox = new JCheckBox("Reverse arrows", reverseRhombi);

        toolBar.add(arrowsCheckbox);
        arrowsCheckbox.addActionListener(e -> {
            showArrows = arrowsCheckbox.isSelected();
            repaint();
        });

        toolBar.add(reverseArrowsCheckbox);
        reverseArrowsCheckbox.addChangeListener(e -> {
            reverseRhombi = reverseArrowsCheckbox.isSelected();
            repaint();
        });

        arrowsCheckbox.setEnabled(multigrid.getSymmetry() == 5);

        ChangeListener changeListener = e -> {
            int symmetry = (int) symmetrySpinner.getValue();
            arrowsCheckbox.setEnabled(symmetry == 5);
            reverseArrowsCheckbox.setEnabled(symmetry == 5);
            if (symmetry != 5) {
                arrowsCheckbox.setSelected(false);
                reverseArrowsCheckbox.setSelected(false);
            }
            int radius = (int) radiusSpinner.getValue();
            double offset = (double) offsetSpinner.getValue();
            double gridInset = (double) insetSpinner.getValue();
            multigrid = new Multigrid(symmetry, radius, offset, gridInset);
            updateStatusBar();
            colliderPanel.revalidate();
            colliderPanel.repaint();
        };

        symmetrySpinner.addChangeListener(changeListener);
        radiusSpinner.addChangeListener(changeListener);
        offsetSpinner.addChangeListener(changeListener);
        insetSpinner.addChangeListener(changeListener);

        toolBar.add(Box.createHorizontalGlue());

        toolBar.add(zoomButton);

        // todo: fix the zoom button
        zoomButton.addActionListener(e ->
        {
            colliderPanel.setZoom(1);
            zoomButton.setText((int) (colliderPanel.getZoom() * 100) + "%");
        });
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
            zoomButton.setText((int) (colliderPanel.getZoom() * 100) + "%");
        };

        minusButton.addActionListener(zoomAction);
        plusButton.addActionListener(zoomAction);

        add(toolBar, BorderLayout.PAGE_START);

        add(statusBar, BorderLayout.PAGE_END);

        updateStatusBar();

        SwingUtilities.invokeLater(() ->

        {
            setMinimumSize(new Dimension(800, 600));
            offsetSpinner.requestFocus();
        });

    }

    private void updateStatusBar() {
        statusBar.setText("Number of tiles - " + multigrid.getTileList().size());
    }

    private class ColliderPanel extends JPanel {
        private final AffineTransform transform = new AffineTransform();
        private double zoom = 1;
        private final List<Color> colorList = new ArrayList<>();

        public ColliderPanel() {
//            ToolTipManager.sharedInstance().registerComponent(this);
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

//            drawLines(g2);

            g2.setColor(Color.RED);

            if (fillRhombi) {
                fillRhombi(g2);
            }

            if (drawRhombi) {
                drawRhombi(g2);
            }

            if (showTrapezium) {
                drawTrapezium(g2);
            }

            if (showKitesAndDarts) {
                drawKitesAndDarts(g2);
            }

            if (showArrows) {
                drawArrows(g2);
            }

            g2.dispose();
        }

        private void drawTrapezium(Graphics2D g2) {
            g2.setColor(Color.BLACK);
            boolean isHbs = false;

            List<GridTile> tileList = multigrid.getTileList();
            for (int i = 0; i < tileList.size(); i++) {
                GridTile tile = tileList.get(i);
                List<GridPoint> vertextList = tile.getVertexList();

                GridPoint a = vertextList.get(reverseRhombi ? 2 : 0);
                GridPoint b = vertextList.get(reverseRhombi ? 1 : 3);
                GridPoint c = vertextList.get(reverseRhombi ? 0 : 2);
                GridPoint d = vertextList.get(reverseRhombi ? 3 : 1);

                if (tile.getArea() == 0.587785) {
                    Line2D diagonal =
                            new Line2D.Double(a.x(), a.y(), c.x(), c.y());
                    g2.draw(diagonal);

//                    Line2D lineAB =
//                            new Line2D.Double(a.x(), a.y(), b.x(), b.y());
//                    g2.draw(lineAB);

//                    Line2D lineAD =
//                            new Line2D.Double(a.x(), a.y(), d.x(), d.y());
//                    g2.draw(lineAD);
//
                    Line2D lineCD =
                            new Line2D.Double(c.x(), c.y(), d.x(), d.y());
                    g2.draw(lineCD);
                   Line2D lineDB =
                            new Line2D.Double(c.x(), c.y(), b.x(), b.y());
                    g2.draw(lineDB);

                } else if (tile.getArea() == 0.951057) {
                    double dx = a.x() - c.x();
                    double dy = a.y() - c.y();

                    double t = 1.0 / PHI;

                    double innerX = c.x() + t * dx;
                    double innerY = c.y() + t * dy;

                    Line2D cb =
                            new Line2D.Double(c.x(), c.y(), b.x(), b.y());
                    g2.draw(cb);

                    Line2D cd =
                            new Line2D.Double(c.x(), c.y(), d.x(), d.y());
                    g2.draw(cd);

                    Line2D lineIB =
                            new Line2D.Double(b.x(), b.y(), innerX, innerY);
                    g2.draw(lineIB);

                    Line2D lineID =
                            new Line2D.Double(d.x(), d.y(), innerX, innerY);
                    g2.draw(lineID);

                    Line2D lineIA =
                            new Line2D.Double(a.x(), a.y(), innerX, innerY);
                    g2.draw(lineIA);

                }
            }
        }

        private void fillRhombi(Graphics2D g2) {
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
            }
        }

        private void drawRhombi(Graphics2D g2) {
            g2.setColor(Color.BLACK);
            boolean isHbs = false;

            List<GridTile> tileList = multigrid.getTileList();
            for (int i = 0; i < tileList.size(); i++) {
                GridTile tile = tileList.get(i);
                List<GridPoint> vertextList = tile.getVertexList();

                GridPoint a = vertextList.get(reverseRhombi ? 2 : 0);
                GridPoint b = vertextList.get(1);
                GridPoint c = vertextList.get(reverseRhombi ? 0 : 2);
                GridPoint d = vertextList.get(3);
                if (!isHbs) {
                    Line2D ab = new Line2D.Double(a.x(), a.y(), b.x(), b.y());
                    g2.draw(ab);
                    Line2D ad = new Line2D.Double(a.x(), a.y(), d.x(), d.y());
                    g2.draw(ad);
                }
                Line2D cb = new Line2D.Double(c.x(), c.y(), b.x(), b.y());
                g2.draw(cb);
                Line2D cd = new Line2D.Double(c.x(), c.y(), d.x(), d.y());
                g2.draw(cd);
            }
        }

        private void drawKitesAndDarts(Graphics2D g2) {

            g2.setColor(Color.BLACK);

            List<GridTile> tileList = multigrid.getTileList();
            for (int i = 0; i < tileList.size(); i++) {
                GridTile tile = tileList.get(i);
                List<GridPoint> vertextList = tile.getVertexList();
                GridPoint a = vertextList.get(reverseRhombi ? 2 : 0);
                GridPoint b = vertextList.get(1);
                GridPoint c = vertextList.get(reverseRhombi ? 0 : 2);
                GridPoint d = vertextList.get(3);

                if (tile.getArea() == 0.587785) {
                    Line2D diagonal =
                            new Line2D.Double(a.x(), a.y(), c.x(), c.y());
                    g2.draw(diagonal);

                    Line2D lineAB =
                            new Line2D.Double(a.x(), a.y(), b.x(), b.y());
                    g2.draw(lineAB);

                    Line2D lineAD =
                            new Line2D.Double(a.x(), a.y(), d.x(), d.y());
                    g2.draw(lineAD);

                } else if (tile.getArea() == 0.951057) {
                    double dx = a.x() - c.x();
                    double dy = a.y() - c.y();

                    double t = 1.0 / PHI;

                    double innerX = c.x() + t * dx;
                    double innerY = c.y() + t * dy;

                    Line2D innerLine =
                            new Line2D.Double(c.x(), c.y(), innerX, innerY);
                    g2.draw(innerLine);

                    Line2D lineIB =
                            new Line2D.Double(b.x(), b.y(), innerX, innerY);
                    g2.draw(lineIB);

                    Line2D lineID =
                            new Line2D.Double(d.x(), d.y(), innerX, innerY);
                    g2.draw(lineID);

                    Line2D lineAB =
                            new Line2D.Double(a.x(), a.y(), b.x(), b.y());
                    g2.draw(lineAB);

                    Line2D lineAD =
                            new Line2D.Double(a.x(), a.y(), d.x(), d.y());
                    g2.draw(lineAD);
                } else {
                    throw new AssertionError("Unexpected tile's area: " + tile.getArea());
                }
            }
        }

        private void drawArrows(Graphics2D g2) {
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
                g2.setColor(Color.BLACK);

                Shape clip = g2.getClip();
                g2.clip(path);

                GridPoint a = vertextList.get(reverseRhombi ? 2 : 0);
                GridPoint b = vertextList.get(1);
                GridPoint c = vertextList.get(reverseRhombi ? 0 : 2);
                GridPoint d = vertextList.get(3);

                if (tile.getArea() == 0.951057) {
                    drawDoubleArrow(g2, b, a);
                    drawDoubleArrow(g2, d, a);
                    drawArrow(g2, b, c);
                    drawArrow(g2, d, c);
                } else {
                    drawDoubleArrow(g2, b, a);
                    drawDoubleArrow(g2, d, a);
                    drawArrow(g2, c, b);
                    drawArrow(g2, c, d);
                }
                g2.setClip(clip);
            }
        }

        private void drawArrow(Graphics2D g2, GridPoint p1, GridPoint p2) {
            fillTriangle(g2, p1, p2, .65);
        }

        private void drawDoubleArrow(Graphics2D g2, GridPoint p1, GridPoint p2) {
            fillTriangle(g2, p1, p2, .5);
            fillTriangle(g2, p1, p2, .8);
        }

        private void drawAxis(Graphics2D g2) {
            g2.setColor(Color.BLACK);
            fillCircle(g2, 0, 0, .05);
            g2.setStroke(new BasicStroke((float) .05));
            drawLine(g2, new GridLine(0, 0));
            drawLine(g2, new GridLine(-Math.PI / 2, 0));
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
//            repaint();
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
//            repaint();
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

        public static void fillTriangle(Graphics2D g2,
                                        GridPoint a,
                                        GridPoint b,
                                        double t) {

            double x1 = a.x(), x2 = b.x(), y1 = a.y(), y2 = b.y();

            // Safety check: if t < 0 or t > 1, we can clamp or just continue
            // but we assume it's in [0..1].

            // 1) Compute the point P = apex at fraction t of the segment AB
            double Px = x1 + t * (x2 - x1);
            double Py = y1 + t * (y2 - y1);

            // 2) Direction vector AB
            double ABx = (x2 - x1);
            double ABy = (y2 - y1);
            double length = Math.hypot(ABx, ABy);

            // If the two points are the same, avoid division by zero
            if (length == 0.0) {
                return;
            }

            // 3) Unit direction from A to B
            double dx = ABx / length;
            double dy = ABy / length;

            // 4) For an equilateral triangle with side s=0.2:
            double side = 0.3;
            double height = (Math.sqrt(3) / 2.0) * side; // ~ 0.1732 for s=0.2
            double halfBase = side / 2.0;                // 0.1 for s=0.2

            // 5) We want the apex at (Px,Py) pointing "forward" (toward B),
            //    so the base is behind the apex along -direction.
            //    -> The midpoint of the base is M = P - height*d
            double Mx = Px - height * dx;
            double My = Py - height * dy;

            // 6) Get a perpendicular to (dx,dy). That is ( -dy, dx ) or ( dy, -dx )
            double px = -dy;
            double py = dx;

            // 7) Base endpoints = M +/- (halfBase)*p
            double B1x = Mx + halfBase * px;
            double B1y = My + halfBase * py;

            double B2x = Mx - halfBase * px;
            double B2y = My - halfBase * py;

            // Create a path for the equilateral triangle
            // Apex -> B1 -> B2 -> back to Apex
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(Px, Py);       // apex
            triangle.lineTo(B1x, B1y);     // base endpoint 1
            triangle.lineTo(B2x, B2y);     // base endpoint 2
            triangle.closePath();

            g2.fill(triangle);
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

