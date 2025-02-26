package tilings;

import com.jsevy.jdxf.DXFDocument;
import com.jsevy.jdxf.DXFGraphics;
import tilings.multigrid.*;
import tilings.painters.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

public class MultigridFrame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultigridFrame().setVisible(true));
    }

    private final JSpinner symmetrySpinner;
    private final JSpinner radiusSpinner;
    private final JSpinner offsetSpinner;
    private final JSpinner insetSpinner;

    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    private final JToolBar mainToolBar = new JToolBar();
    private Multigrid multigrid = new Multigrid(5, 3, new ArrayList<>(Collections.nCopies(5, .2)), 0);

    private final JButton dxfButton = new JButton("DXF");
    private final JButton zoomButton = new JButton("100%");
    private final JLabel statusBar = new JLabel();
    private JToolBar verticalToolBar = new JToolBar(JToolBar.VERTICAL);
    private HashMap<RhombusPainter, AbstractButton> verticalToolBarComponents = new HashMap<>();

    private FillRhombusByAreaPainter colorByAreaPainter = new FillRhombusByAreaPainter(multigrid.getTileAreaList());
    private FillRhombusByIndicesPainter colorByIndicesPainter = new FillRhombusByIndicesPainter(multigrid.getVertexIndexSet());

    private List<RhombusPainter> beforePainterList = new ArrayList<>();
    private List<RhombusPainter> mainPainterList = new ArrayList<>();
    private List<RhombusPainter> afterPainterList = new ArrayList<>();

    private boolean reverseRhombi;

    public MultigridFrame() {
        setTitle("Multigrid playground");
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        tabbedPane.addChangeListener(e -> revalidateTabbedPane());
        add(tabbedPane);

        mainToolBar.add(new JLabel("Symmetry "));
        SpinnerNumberModel symmetryModel = new SpinnerNumberModel(multigrid.getSymmetry(), 3, 15, 1);
        symmetrySpinner = new JSpinner(symmetryModel);
        symmetrySpinner.setMaximumSize(symmetrySpinner.getPreferredSize());
        mainToolBar.add(symmetrySpinner);
        mainToolBar.addSeparator();

        mainToolBar.add(new JLabel("Radius "));

        SpinnerNumberModel radiusModel = new SpinnerNumberModel(multigrid.getGridRadius(), 0, 10, 1);
        radiusSpinner = new JSpinner(radiusModel);
        radiusSpinner.setMaximumSize(radiusSpinner.getPreferredSize());
        mainToolBar.add(radiusSpinner);
        mainToolBar.addSeparator();

        mainToolBar.add(new JLabel("Offset "));

        SpinnerNumberModel offsetModel = new SpinnerNumberModel((double) multigrid.getOffsetList().getFirst(), -3, 3.0, 0.01);
        offsetSpinner = new JSpinner(offsetModel);
        offsetSpinner.setEditor(new JSpinner.NumberEditor(offsetSpinner, "#.##"));
        JFormattedTextField tf = ((JSpinner.DefaultEditor) offsetSpinner.getEditor()).getTextField();
        tf.setColumns(3);

        offsetSpinner.setMaximumSize(offsetSpinner.getPreferredSize());

        mainToolBar.add(offsetSpinner);
        mainToolBar.addSeparator();

        JButton insetButton = new JButton("Inset ");
        insetButton.setToolTipText("Set inset to 0");
        insetButton.setFocusable(false);
        mainToolBar.add(insetButton);

        SpinnerNumberModel insetModel = new SpinnerNumberModel(multigrid.getGridInset(), 0, .99, 0.02);
        insetSpinner = new JSpinner(insetModel);
        insetSpinner.setEditor(new JSpinner.NumberEditor(insetSpinner, "#.##"));

        JFormattedTextField tfInset = ((JSpinner.DefaultEditor) insetSpinner.getEditor()).getTextField();
        tfInset.setColumns(3);
        insetSpinner.setMaximumSize(insetSpinner.getPreferredSize());

        insetButton.addActionListener(e -> {
            insetSpinner.setValue(0.0);
        });

        mainToolBar.add(insetSpinner);

        mainToolBar.addSeparator();

        JCheckBox reverseCheckbox = new JCheckBox("Reverse rhombi");
        reverseCheckbox.addChangeListener(e -> {
            reverseRhombi = reverseCheckbox.isSelected();
            tabbedPane.getSelectedComponent().repaint();
        });
        mainToolBar.add(reverseCheckbox);
        mainToolBar.addSeparator();

        symmetrySpinner.addChangeListener(e -> {
            updateMultigrid();
            updateStatusBar();
            updateSettings();
            revalidateTabbedPane();
        });

        ChangeListener changeListener = e -> {
            updateMultigrid();
            updateStatusBar();
            revalidateTabbedPane();
        };

        radiusSpinner.addChangeListener(changeListener);
        offsetSpinner.addChangeListener(changeListener);
        insetSpinner.addChangeListener(changeListener);

        mainToolBar.add(Box.createHorizontalGlue());
        mainToolBar.add(dxfButton);
        dxfButton.addActionListener(e -> writeDxf());

        mainToolBar.addSeparator();

        add(mainToolBar, BorderLayout.PAGE_START);
        add(statusBar, BorderLayout.PAGE_END);

        createPainterLists();

        verticalToolBar.addSeparator();

        ArrayList<RhombusPainter> combinedList = new ArrayList<>(beforePainterList);
        combinedList.addAll(afterPainterList);
        for (RhombusPainter painter : combinedList) {
            JCheckBox checkbox = new JCheckBox(painter.getName());
            verticalToolBar.add(checkbox);
            checkbox.addChangeListener(e -> {
                painter.setEnabled(checkbox.isSelected());
                tabbedPane.getSelectedComponent().repaint();
            });
            painter.setEnabled(false);
            verticalToolBarComponents.put(painter, checkbox);
        }
        verticalToolBar.addSeparator();
        add(verticalToolBar, BorderLayout.WEST);

        updateStatusBar();
        updateSettings();

        SwingUtilities.invokeLater(() ->
        {
            setMinimumSize(new Dimension(800, 600));
            offsetSpinner.requestFocus();
        });
    }

    private void updateMultigrid() {
        int symmetry = (int) symmetrySpinner.getValue();
        int radius = (int) radiusSpinner.getValue();
        double offset = (double) offsetSpinner.getValue();
        double gridInset = (double) insetSpinner.getValue();
        multigrid = new Multigrid(symmetry, radius, Collections.nCopies(symmetry, offset), gridInset);
        colorByAreaPainter.setTileAreaList(multigrid.getTileAreaList());
        colorByIndicesPainter.setVertexIndexSet(multigrid.getVertexIndexSet());
//        System.out.println("multigrid.getVertexIndexSet() = " + multigrid.getVertexIndexSet());
    }

    private void createPainterLists() {
        beforePainterList.add(colorByAreaPainter);
        beforePainterList.add(colorByIndicesPainter);
        beforePainterList.add(new DrawRhombusPainter(Color.ORANGE, "Initial tiling"));
        afterPainterList.add(new DrawPenroseArrowsPainter());
        mainPainterList.add(new DrawRhombusPainter(Color.BLACK));
        mainPainterList.add(new DrawKitesAndDartsPainter());
        mainPainterList.add(new DrawCromwellTrapeziumPainter());
        mainPainterList.add(new DrawRibbonsPainter());
        mainPainterList.add(new DrawMyTilingPainter());
        mainPainterList.add(new DrawEquilateralAmmanPainter());
        mainPainterList.add(new TwoLayersPainter());
    }

    private void updateSettings() {
        for (RhombusPainter beforePainter : beforePainterList) {
            boolean supported = beforePainter.isSymmetrySupported(multigrid.getSymmetry());
            AbstractButton button = verticalToolBarComponents.get(beforePainter);
            beforePainter.setEnabled(supported && button.isSelected());
            button.setVisible(supported);
        }
        for (RhombusPainter afterPainter : afterPainterList) {
            boolean supported = afterPainter.isSymmetrySupported(multigrid.getSymmetry());
            AbstractButton button = verticalToolBarComponents.get(afterPainter);
            afterPainter.setEnabled(supported && button.isSelected());
            button.setVisible(supported);
        }
        tabbedPane.removeAll();
        for (RhombusPainter painter : mainPainterList) {
            if (painter.isSymmetrySupported(multigrid.getSymmetry())) {
                tabbedPane.add(painter.getName(), new PainterScrollPane(new TilingPanel(painter)));
            }
        }
        verticalToolBar.revalidate();
        verticalToolBar.repaint();
    }

    private void revalidateTabbedPane() {
        TilingPanel selectedPanel = getSelectedPanel();
        if (selectedPanel != null) {
            selectedPanel.revalidate();
            selectedPanel.repaint();
        }
    }

    private TilingPanel getSelectedPanel() {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        if (scrollPane != null) {
            return (TilingPanel) scrollPane.getViewport().getView();
        }
        return null;
    }

    private void updateStatusBar() {
        statusBar.setText("Number of tiles - " + multigrid.getTileList().size());
    }

    class PainterScrollPane extends JScrollPane {

        public PainterScrollPane(Component view) {
            super(view);
            getVerticalScrollBar().setUnitIncrement(30);
            getHorizontalScrollBar().setUnitIncrement(30);
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent e) {
            if ((e.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK) {
                TilingPanel selectedPanel = getSelectedPanel();
                selectedPanel.updateZoom(e.getWheelRotation());
                zoomButton.setText((int) (selectedPanel.getZoom() * 100) + "%");
            } else {
                super.processMouseWheelEvent(e);
            }
        }

        @Override
        public String toString() {
            return "ScrollPane(tilingPanel)";
        }
    }

    private class TilingPanel extends JPanel {
        private final AffineTransform transform = new AffineTransform();
        private double zoom = 1;
        private final List<Color> colorList = new ArrayList<>();

        private final RhombusPainter mainPainter;

        public TilingPanel(RhombusPainter mainPainter) {
            this.mainPainter = mainPainter;
            //ToolTipManager.sharedInstance().registerComponent(this);
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
            revalidate();
            repaint();
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

            if (beforePainterList != null && !beforePainterList.isEmpty()) {
                for (GridTile tile : multigrid.getTileList()) {
                    for (RhombusPainter beforePainter : beforePainterList) {
                        if (beforePainter.isEnabled()) {
                            beforePainter.paint(g2, tile, reverseRhombi);
                        }
                    }
                }
            }

            for (GridTile tile : multigrid.getTileList()) {
                mainPainter.paint(g2, tile, reverseRhombi);
            }

            if (afterPainterList != null && !afterPainterList.isEmpty()) {
                for (GridTile tile : multigrid.getTileList()) {
                    for (RhombusPainter afterPainter : afterPainterList) {
                        if (afterPainter.isEnabled()) {
                            afterPainter.paint(g2, tile, reverseRhombi);
                        }
                    }
                }
            }

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
            if (multigrid.getSymmetry() % 2 == 1) {
                transform.rotate(-Math.PI / (2 * multigrid.getSymmetry()));
            }
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
            int lineLength = 10;
            if (clipBounds != null) {
                lineLength = clipBounds.width + clipBounds.height;
            }
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

    private void writeDxf() {
        DXFDocument dxfDocument = new DXFDocument("Example DXF output from Java DXFGraphics");
        // set units to mm
        dxfDocument.setUnits(4);
        // set precision digits to 8
        dxfDocument.setPrecisionDigits(8);
        // set to generate zero-length lines; default is to generate DXF point when a zero-length line is drawn
        dxfDocument.generatePoints(false);

        // set viewport params
        dxfDocument.setViewportScale(70);
        dxfDocument.setViewportCenter(120, 120);

        // get the DXFGraphics object to draw into
        DXFGraphics dxfGraphics = dxfDocument.getGraphics();

        // scaling to 1/4 square meter
        dxfGraphics.scale(250, 250);
        //moving the origin to the lower-left corner, as expected for dxf files
//        dxfGraphics.translate(1, -1);

        if (multigrid.getSymmetry() % 2 == 1) {
            dxfGraphics.rotate(-Math.PI / (2 * multigrid.getSymmetry()));
        }

        // supply this as a Graphics subclass object to our main Draw method
        dxfDocument.setLayer("Cut");

        for (GridTile tile : multigrid.getTileList()) {
            getSelectedPanel().mainPainter.paint(dxfGraphics, tile, reverseRhombi);
        }

        dxfDocument.setLayer("Construction");
        double r = multigrid.getTilingRadius();
        dxfGraphics.drawOval(-r, -r, 2 * r, 2 * r);

        // get the DXF output (just text)
        String stringOutput = dxfDocument.toDXFString();

        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setSelectedFile(new File("Puzzle.dxf"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("DXF", "dxf");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            if (selectedFile.exists()) {
                int result = JOptionPane.showConfirmDialog(chooser, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == JOptionPane.CANCEL_OPTION) {
                    chooser.cancelSelection();
                    return;
                }
            }
            try {
                FileWriter fileWriter = new FileWriter(selectedFile);
                fileWriter.write(stringOutput);
                fileWriter.flush();
                fileWriter.close();
            } catch (Exception e) {
                System.out.println("Exception while saving DXF file: " + e);
            }
            chooser.approveSelection();
        }
    }
}
