package tilings;

import tilings.multigrid.*;
import tilings.painters.*;

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
        private final MultigridPainter multigridPainter = new MultigridPainter();

        private final RhombusPainter drawOrangeRhombus = new DrawRhombusPainter(Color.ORANGE);
        private final FillRhombusPainter fillRhombusPainter = new FillRhombusPainter();
        private final RhombusPainter drawArrowsPainter = new DrawPenroseArrowsPainter();

        private final RhombusPainter drawBlackRhombusPainter = new DrawRhombusPainter(Color.BLACK);
        private final RhombusPainter drawKitesAndDarts = new DrawKitesAndDartsPainter();

        private final RhombusPainter drawCromwellTrapeziumPainter = new DrawCromwellTrapeziumPainter();
        private final RhombusPainter drawMyTilingPainter = new DrawMyTilingPainter();

        private final RhombusPainter drawRibbonsPainter = new DrawRibbonsPainter();
        private final RhombusPainter drawEquilateralAmmanPainter = new DrawEquilateralAmmanPainter();

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

            g2.setStroke(new BasicStroke((float) .05, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));


//            debug(g2);

//            drawLines(g2);

            if (fillRhombi) {
                fillRhombusPainter.setUp(multigrid.getTileAreaList());
                multigridPainter.paint(multigrid, g2, fillRhombusPainter, reverseRhombi);
            }

            if (showSourceTiling) {
                multigridPainter.paint(multigrid, g2, drawOrangeRhombus, reverseRhombi);
            }

            if (showArrows) {
                multigridPainter.paint(multigrid, g2, drawArrowsPainter, reverseRhombi);
            }

            if (drawRhombi) {
                multigridPainter.paint(multigrid, g2, drawBlackRhombusPainter, reverseRhombi);
            }

            if (showKitesAndDarts) {
                multigridPainter.paint(multigrid, g2, drawKitesAndDarts, reverseRhombi);
            }

            if (showCromwell) {
                multigridPainter.paint(multigrid, g2, drawCromwellTrapeziumPainter, reverseRhombi);
            }

            if (showMyTiling) {
                multigridPainter.paint(multigrid, g2, drawMyTilingPainter, reverseRhombi);
            }

            if (showRibbons) {
                multigridPainter.paint(multigrid, g2, drawRibbonsPainter, reverseRhombi);
            }

            if (showEquilateralAmman) {
                multigridPainter.paint(multigrid, g2, drawEquilateralAmmanPainter, reverseRhombi);
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


        /*

         */

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

    static class MultigridPainter {
        public void paint(Multigrid multigrid, Graphics2D g2, RhombusPainter rhombusPainter, boolean isReversed) {
            for (GridTile tile : multigrid.getTileList()) {
                rhombusPainter.paint(g2, tile, isReversed);
            }
        }
    }
}
