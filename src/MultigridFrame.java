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
    private boolean showCromwell;
    private boolean showMyTiling;
    private boolean showRibbons;
    private boolean showEquilateralAmman;
    private boolean fillRhombi;
    private boolean showKitesAndDarts;
    private boolean showArrows;
    private boolean reverseRhombi;
    private boolean showSourceTiling;

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

        JRadioButton drawRhombiRadioButton = new JRadioButton("Rhombi", drawRhombi);
        toolBar.add(drawRhombiRadioButton);
        JRadioButton kitesAndDartsRadioButton = new JRadioButton("Kites", showKitesAndDarts);
        toolBar.add(kitesAndDartsRadioButton);
        JRadioButton cromwellTrapeziumRadioButton = new JRadioButton("Cromwell", showCromwell);
        toolBar.add(cromwellTrapeziumRadioButton);
        JRadioButton ribbonsRadioButton = new JRadioButton("Ribbons", showRibbons);
        toolBar.add(ribbonsRadioButton);
        JRadioButton myTilingRadioButton = new JRadioButton("My tiling", showMyTiling);
        toolBar.add(myTilingRadioButton);
        JRadioButton equilateralAmmanRadioButton = new JRadioButton("Equilateral Amman", showEquilateralAmman);
        toolBar.add(equilateralAmmanRadioButton);

        ActionListener radioButtonListener = e -> {
            drawRhombi = drawRhombiRadioButton.isSelected();
            showMyTiling = myTilingRadioButton.isSelected();
            showCromwell = cromwellTrapeziumRadioButton.isSelected();
            showKitesAndDarts = kitesAndDartsRadioButton.isSelected();
            showRibbons = ribbonsRadioButton.isSelected();
            showEquilateralAmman = equilateralAmmanRadioButton.isSelected();
            repaint();
        };

        drawRhombiRadioButton.addActionListener(radioButtonListener);
        kitesAndDartsRadioButton.addActionListener(radioButtonListener);
        cromwellTrapeziumRadioButton.addActionListener(radioButtonListener);
        myTilingRadioButton.addActionListener(radioButtonListener);
        ribbonsRadioButton.addActionListener(radioButtonListener);
        equilateralAmmanRadioButton.addActionListener(radioButtonListener);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(drawRhombiRadioButton);
        buttonGroup.add(kitesAndDartsRadioButton);
        buttonGroup.add(cromwellTrapeziumRadioButton);
        buttonGroup.add(myTilingRadioButton);
        buttonGroup.add(ribbonsRadioButton);
        buttonGroup.add(equilateralAmmanRadioButton);

        toolBar.addSeparator();

        JCheckBox reverseArrowsCheckbox = new JCheckBox("Reverse", reverseRhombi);
        toolBar.add(reverseArrowsCheckbox);
        reverseArrowsCheckbox.addChangeListener(e -> {
            reverseRhombi = reverseArrowsCheckbox.isSelected();
            repaint();
        });

        JCheckBox sourceTilingCheckbox = new JCheckBox("Source tiling", showSourceTiling);
        toolBar.add(sourceTilingCheckbox);
        sourceTilingCheckbox.addChangeListener(e -> {
            showSourceTiling = sourceTilingCheckbox.isSelected();
            repaint();
        });

        JCheckBox fillRhombiCheckbox = new JCheckBox("Color", fillRhombi);
        toolBar.add(fillRhombiCheckbox);
        fillRhombiCheckbox.addActionListener(e -> {
            fillRhombi = fillRhombiCheckbox.isSelected();
            repaint();
        });

        JCheckBox arrowsCheckbox = new JCheckBox("Arrows");

        toolBar.add(arrowsCheckbox);
        arrowsCheckbox.addActionListener(e -> {
            showArrows = arrowsCheckbox.isSelected();
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

            List<GridTile> tileList = multigrid.getTileList();

            if (showSourceTiling || fillRhombi) {
                for (GridTile tile : tileList) {
                    List<GridPoint> vertextList = tile.getVertexList();

                    GridPoint a = vertextList.get(reverseRhombi ? 2 : 0);
                    GridPoint b = vertextList.get(reverseRhombi ? 1 : 3);
                    GridPoint c = vertextList.get(reverseRhombi ? 0 : 2);
                    GridPoint d = vertextList.get(reverseRhombi ? 3 : 1);
                    if (fillRhombi) {
                        fillRhombi(g2, a, b, c, d, tile.getArea());
                    }
                    if (showSourceTiling) {
                        drawRhombi(g2, a, b, c, d, Color.ORANGE);
                    }
                }
            }
            for (GridTile tile : tileList) {
                List<GridPoint> vertextList = tile.getVertexList();

                GridPoint a = vertextList.get(reverseRhombi ? 2 : 0);
                GridPoint b = vertextList.get(reverseRhombi ? 1 : 3);
                GridPoint c = vertextList.get(reverseRhombi ? 0 : 2);
                GridPoint d = vertextList.get(reverseRhombi ? 3 : 1);

                double area = tile.getArea();

                if (drawRhombi) {
                    drawRhombi(g2, a, b, c, d, Color.BLACK);
                }

                if (showKitesAndDarts) {
                    drawKitesAndDarts(g2, a, b, c, d, area);
                }

                if (showCromwell) {
                    drawCromwellTrapezium(g2, a, b, c, d, area);
                }

                if (showMyTiling) {
                    drawMyTiling(g2, a, b, c, d, area);
                }

                if (showRibbons) {
                    drawRibbonTiling(g2, a, b, c, d, area);
                }

                if (showEquilateralAmman) {
                    drawEquilateralAmmanTiling(g2, a, b, c, d, area);
                }

                if (showArrows) {
                    drawArrows(g2, a, b, c, d, area);
                }
            }
            g2.dispose();
        }


        // Amman tiling variation, point J is on the point D of the thin rhombus
        private void drawRibbonTiling(Graphics2D g2,
                                      GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                      double area) {
            g2.setColor(Color.BLACK);
            if (area == 0.587785) {
                g2.draw(new Line(a, c));
                g2.draw(new Line(c, d));
            } else if (area == 0.951057) {
                GridPoint i = a.getPointInDirection(c, 1);
                g2.draw(new Line(a, c));
                g2.draw(new Line(i, d));
            }
        }

        // Amman, all sides equal
        private void drawEquilateralAmmanTiling(Graphics2D g2,
                                                GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                                double area) {
            g2.setColor(Color.BLACK);
            if (area == 0.587785) {

                GridPoint bc_i = b.getPointInDirection(c, 0.38197);

                GridPoint i = a.getPointInDirection(bc_i, 0.52573);

                g2.draw(new Line(a, i));
                g2.draw(new Line(b, i));
                g2.draw(new Line(c, i));

            } else if (area == 0.951057) {

                GridPoint ab = a.getPointInDirection(b, 0.417);
                GridPoint cd = c.getPointInDirection(d, 0.417);

                GridPoint ab_i = ab.getPointInDirection(cd, 0.182);
                GridPoint cd_i = cd.getPointInDirection(ab, 0.182);

                g2.draw(new Line(a, ab_i));
                g2.draw(new Line(ab_i, b));
                g2.draw(new Line(ab_i, cd_i));
                g2.draw(new Line(cd_i, c));
                g2.draw(new Line(cd_i, d));
            }
        }
/*
    // inner point is on the center of the side of the thin rhombus
        private void drawA1Tiling(Graphics2D g2,
                                  GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                  double area) {
            g2.setColor(Color.BLACK);
            if (area == 0.587785) {

                g2.draw(new Line(a, b));
                // extra line for the reversed tiling
                g2.draw(new Line(a, d));

                GridPoint abCenter = a.getCenter(b);
                g2.draw(new Line(c, abCenter));

            } else if (area == 0.951057) {

                GridPoint abCenter = a.getCenter(b);
                GridPoint cdCenter = c.getCenter(d);

                GridPoint i = abCenter.getPointInDirection(cdCenter, 0.69098);

                g2.draw(new Line(abCenter.x(), i));
                g2.draw(new Line(i, c));
                g2.draw(new Line(i, d));

                g2.draw(new Line(a, b));
            }
        }
*/

        private void drawMyTiling(Graphics2D g2,
                                  GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                  double area) {
            g2.setColor(Color.BLACK);
            if (area == 0.587785) {
                GridPoint cb_i = c.getPointInDirection(b, 0.38197);
                g2.draw(new Line(a, cb_i));

            } else if (area == 0.951057) {

                GridPoint cd_i = c.getPointInDirection(d, 0.61803);
                GridPoint i = c.getPointInDirection(a, 1);

                g2.draw(new Line(c, b));
                g2.draw(new Line(c, d));
                g2.draw(new Line(b, i));
                g2.draw(new Line(cd_i, i));
                g2.draw(new Line(i, a));
            }
        }


/*
        // the initial version of my tiling, bigger tiles
        private void drawMyTiling(Graphics2D g2,
                                  GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                  double area) {
            g2.setColor(Color.BLACK);
            if (area == 0.587785) {
                Line diagonal =
                        new Line(a, c);
                g2.draw(diagonal);
            } else if (area == 0.951057) {
                Line ab = new Line(a, b);
                g2.draw(ab);
                Line bc = new Line(b, c);
                g2.draw(bc);
            }
        }
*/

        private void drawCromwellTrapezium(Graphics2D g2,
                                           GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                           double area) {
            g2.setColor(Color.BLACK);
            if (area == 0.587785) {
                // diagonal
                g2.draw(new Line(a, c));

                g2.draw(new Line(c, d));
                g2.draw(new Line(c, b));
            } else if (area == 0.951057) {
                GridPoint i = c.getPointInDirection(a, 1);
                g2.draw(new Line(c, b));
                g2.draw(new Line(c, d));
                g2.draw(new Line(b, i));
                g2.draw(new Line(d, i));
                g2.draw(new Line(a, i));
            }
        }

        private Path2D getPath(GridPoint a, GridPoint b, GridPoint c, GridPoint d) {
            Path2D.Double path = new Path2D.Double();
            path.moveTo(a.x(), a.y());
            path.lineTo(b.x(), b.y());
            path.lineTo(c.x(), c.y());
            path.lineTo(d.x(), d.y());
            path.closePath();
            return path;
        }

        private void drawRhombi(Graphics2D g2,
                                GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                Color color) {
            g2.setColor(color);
            g2.draw(getPath(a, b, c, d));
        }

        private void fillRhombi(Graphics2D g2,
                                GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                double area) {
            int colorIndex = multigrid.getTileAreaList().indexOf(area);
            g2.setColor(getColorList().get(colorIndex));
            Path2D path = getPath(a, b, c, d);
            g2.fill(path);
        }

        private void drawKitesAndDarts(Graphics2D g2,
                                       GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                       double area) {
            g2.setColor(Color.BLACK);

            if (area == 0.587785) {
                // diagonal
                g2.draw(new Line(a, c));

                g2.draw(new Line(a, b));
                g2.draw(new Line(a, d));
            } else if (area == 0.951057) {
                GridPoint i = c.getPointInDirection(a, 1);

                // inner lines
                g2.draw(new Line(c, i));
                g2.draw(new Line(b, i));
                g2.draw(new Line(d, i));

                g2.draw(new Line(a, b));
                g2.draw(new Line(a, d));
            } else {
                throw new AssertionError("Unexpected tile's area: " + area);
            }
        }

        private void drawArrows(Graphics2D g2,
                                GridPoint a, GridPoint b, GridPoint c, GridPoint d,
                                double area) {
            g2.setColor(Color.BLACK);

            Shape clip = g2.getClip();
            g2.clip(getPath(a, b, c, d));

            if (area == 0.587785) {
                drawDoubleArrow(g2, b, a);
                drawDoubleArrow(g2, d, a);
                drawArrow(g2, c, b);
                drawArrow(g2, c, d);
            } else if (area == 0.951057) {
                drawDoubleArrow(g2, b, a);
                drawDoubleArrow(g2, d, a);
                drawArrow(g2, b, c);
                drawArrow(g2, d, c);
            }
            g2.setClip(clip);
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

            double fx = a.x() + t * (b.x() - a.x());
            double fy = a.y() + t * (b.y() - a.y());

            double length = a.getDistance(b);
            double dx = (b.x() - a.x()) / length;
            double dy = (b.y() - a.y()) / length;

            double side = 0.2;
            double height = (Math.sqrt(3) / 2.0) * side;
            double halfBase = side / 2.0;

            double mx = fx - height * dx;
            double my = fy - height * dy;

            // get a perpendicular to (dx,dy). That is ( -dy, dx ) or ( dy, -dx )
            double px = -dy;
            double py = dx;

            // Base endpoints = M +/- (halfBase)*p
            double b1x = mx + halfBase * px;
            double b1y = my + halfBase * py;

            double b2x = mx - halfBase * px;
            double b2y = my - halfBase * py;

            // Create a path for the equilateral triangle
            // Apex -> B1 -> B2 -> back to Apex
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(fx, fy);       // apex
            triangle.lineTo(b1x, b1y);     // base endpoint 1
            triangle.lineTo(b2x, b2y);     // base endpoint 2
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

    static class Line extends Line2D.Double {
        public Line(GridPoint a, GridPoint b) {
            super(a.x(), a.y(), b.x(), b.y());
        }
    }
}

