/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.util.TableColumnManager;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.poi.BasicPointOfInterest;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwindx.examples.AirspaceBuilder;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.RoundingMode;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * @author : gknorman
 * @created : 3/31/2021, Wednesday
 **/
public class PlacenamesSearchPanel extends JPanel
{
    private final PlacemarkSearchData placemarkSearchData;
    private WorldWindow wwd;
    private CelestialMapper cms;
    private JTextField jtf;
    private JLabel searchLbl;
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter sorter;
    private JScrollPane jsp;
    protected boolean ignoreSelectEvents = false;

    protected static final String SELECTION_CHANGED = "PlaceNamesPanel.SelectionChanged";
    private ListSelectionModel listSelectionModel;
    protected ArrayList<Position> positions = new ArrayList<>();
    protected ArrayList<Renderable> controlPoints = new ArrayList<>();
    protected RenderableLayer applicationLayer;
    protected CustomRenderableLayer layer;
    protected CustomRenderableLayer controlPointsLayer;
    protected CustomRenderableLayer shapeLayer;
    protected Path line;
    protected SurfaceShape surfaceShape;
    protected ScreenAnnotation annotation;

    protected Color lineColor = Color.YELLOW;
    protected Color fillColor = new Color(.6f, .6f, .4f, .5f);
    protected double lineWidth = 2;

    protected AnnotationAttributes controlPointsAttributes;
    protected AnnotationAttributes controlPointWithLeaderAttributes;
    protected ShapeAttributes leaderAttributes;
    protected AnnotationAttributes annotationAttributes;

    protected boolean followTerrain = false;
    protected boolean showControlPoints = true;
    protected boolean showAnnotation = true;
    protected UnitsFormat unitsFormat = new UnitsFormat();

    // Rectangle enclosed regular shapes attributes
    protected Rectangle2D.Double shapeRectangle = null;
    protected Position shapeCenterPosition = null;
    protected Angle shapeOrientation = null;
    protected int shapeIntervals = 64;
    private CustomRenderableLayer placemarksLayer;
    private CustomRenderableLayer basicPointOfInterestLayer;

    public PlacenamesSearchPanel(WorldWindow wwd, CelestialMapper celestialMapper)
    {
        super(new BorderLayout());
        this.wwd = wwd;
        this.cms = celestialMapper;
        this.placemarkSearchData = new PlacemarkSearchData(wwd,cms);
        this.placemarksLayer = createCustomRenderableLayer();
        this.placemarksLayer.setName("Found Placemarks Layer");

        this.makePanel();
    }

    private void makePanel()
    {
        jtf = new JTextField(15);
        searchLbl = new JLabel("Search");

        JPanel searchField = new JPanel(new GridLayout(1, 2, 5, 5));
        searchField.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        searchField.add(searchLbl);
        searchField.add(jtf);

        String[] columnNames = placemarkSearchData.getHeaders();

        // The 0 argument is number rows.
        model = new DefaultTableModel(columnNames,0){
            @Override
            public void setValueAt(Object inValue, int inRow, int inCol) {
//                System.out.println("Gets called ");
                fireTableCellUpdated(inRow, inCol);
            }
        };
        sorter = new TableRowSorter<>(model);
        table = new JTable(model){
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            };

        };

        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);



        listSelectionModel = table.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());

//        this.table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
//            if (!ignoreSelectEvents) {
//                actionPerformed(new ActionEvent(e.getSource(), -1, SELECTION_CHANGED));
//            }
//        });
        this.table.setToolTipText("<html>Click to select<br>Double-Click to rename</html>");

        int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2);
        int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2);
        table.setPreferredScrollableViewportSize(new Dimension(width,height));
        TableColumnManager tcm = new TableColumnManager(table);
        this.setLayout(new BorderLayout());


        placemarkSearchData.getRowList().forEach( o -> {
            Object [] data = ((ArrayList) o).toArray();
            model.addRow(data);
        });

        // https://stackoverflow.com/questions/4151850/how-to-keep-track-of-row-index-when-jtable-has-been-sorted-by-the-user
        // After populating the table, we can set the modelRowIndex to 0
        table.convertRowIndexToView(0);

        jtf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search(jtf.getText());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                search(jtf.getText());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                search(jtf.getText());
            }
            public void search(String str) {
                if (str.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter(str));
                }
            }
        });

        jsp = new JScrollPane(table);

        JButton newShapeButton = new JButton("New shape");

//        newShapeButton.addActionListener(controller);
        newShapeButton.setToolTipText("Create a new shape centered in the viewport");

        this.add(searchField, BorderLayout.NORTH);
        this.add(jsp, BorderLayout.CENTER);

//        setSize(475, 300);
        setVisible(true);
    }

//    public int[] getSelectedIndices() {
//        return this.table.getSelectedRows();
//    }
//
//    public void setSelectedIndices(int[] indices) {
//        this.ignoreSelectEvents = true;
//
//        if (indices != null && indices.length != 0) {
//            for (int index : indices) {
//                this.table.setRowSelectionInterval(index, index);
//            }
//        } else {
//            this.table.clearSelection();
//        }
//
//        this.ignoreSelectEvents = false;
//    }

//    public void actionPerformed(ActionEvent e) {
//        if (!this.isEnabled()) {
//            return;
//        }
//
//        if (null != e.getActionCommand()) {
//            switch (e.getActionCommand()) {
////                case NEW_AIRSPACE:
////                    this.createNewEntry(this.getView().getSelectedFactory());
////                    break;
////                case CLEAR_SELECTION:
////                    this.selectEntry(null, true);
////                    break;
////                case SIZE_NEW_SHAPES_TO_VIEWPORT:
////                    if (e.getSource() instanceof AbstractButton) {
////                        boolean selected = ((AbstractButton) e.getSource()).isSelected();
////                        this.setResizeNewShapesToViewport(selected);
////                    }
////                    break;
////                case ENABLE_EDIT:
////                    if (e.getSource() instanceof AbstractButton) {
////                        boolean selected = ((AbstractButton) e.getSource()).isSelected();
////                        this.setEnableEdit(selected);
////                    }
////                    break;
////                case OPEN:
////                    this.openFromFile();
////                    break;
////                case OPEN_URL:
////                    this.openFromURL();
////                    break;
////                case OPEN_DEMO_AIRSPACES:
////                    this.openFromPath(DEMO_AIRSPACES_PATH);
////                    this.zoomTo(LatLon.fromDegrees(47.6584074779224, -122.3059199579634),
////                        Angle.fromDegrees(-152), Angle.fromDegrees(75), 750);
////                    break;
////                case REMOVE_SELECTED:
////                    this.removeEntries(Arrays.asList(this.getSelectedEntries()));
////                    break;
////                case SAVE:
////                    this.saveToFile();
////                    break;
//                case SELECTION_CHANGED:
//                    this.viewSelectionChanged();
//                    break;
//                default:
//                    break;
//            }
//        }
//    }

//    protected void viewSelectionChanged() {
//        int[] indices = this.getSelectedIndices();
//        if (indices != null){
//            Arrays.stream(indices).forEach(System.out::println);
//        }
////        if (indices != null) {
////            for (AirspaceBuilder.AirspaceEntry entry : this.getEntriesFor(indices)) {
////                this.selectEntry(entry, false);
////            }
////        }
////        this.getWwd().redraw();
//    }

    private WorldWindow getWwd()
    {
        return this.wwd;
    }

    public JPanel getView() {
        return this;
    }

    // From Oracle example:
    // https://docs.oracle.com/javase/tutorial/uiswing/examples/events/TableListSelectionDemoProject/src/events/TableListSelectionDemo.java
    class SharedListSelectionHandler implements ListSelectionListener {


        public void valueChanged(ListSelectionEvent e) {

            if(!e.getValueIsAdjusting()){

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();

                StringBuilder sb = new StringBuilder();
                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();
                TableColumnModel tcm = table.getColumnModel();

                List rows = new ArrayList();
                List specificRowData = new ArrayList();
//                ArrayList<Position> positions = new ArrayList();
                Map<String, Position> positions = new HashMap<>();

                sb.append("Event for indexes "
                    + firstIndex + " - " + lastIndex
                    + "; isAdjusting is " + isAdjusting
                    + "; selected indexes:");

                if (lsm.isSelectionEmpty()) {
                    sb.append(" <none>");
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    if(getWwd().getModel().getLayers().getLayerByName(placemarksLayer.getName()) != null){
                        placemarksLayer.clearList();
                        placemarksLayer.setEnabled(false);
                    }
                    for (int i = minIndex; i <= maxIndex; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            sb.append(" " + i);
                            Vector vector = model.getDataVector().elementAt(table.getSelectedRow());
                            rows.add(vector);
                            var a = model.getValueAt(i,tcm.getColumnIndex("clean_name"));

                            Double b = Double.valueOf(
                                String.valueOf(model.getValueAt(i,tcm.getColumnIndex("center_lon"))));
                            Double c = Double.valueOf(
                                String.valueOf(model.getValueAt(i,tcm.getColumnIndex("center_lat"))));
                            specificRowData.add(new Vector(Arrays.asList(a, b, c)));
                            Angle lat, lon;
                            if(b > 180.00){
                                lon = Angle.POS360.subtract(Angle.fromDegrees(b)).multiply(-1.0);
                            } else {
                                lon = Angle.fromDegrees(b);
                            }

                            if(c > 90.00) {
                                lat = Angle.POS180.subtract(Angle.fromDegrees(c)).multiply(-1.0);
                            } else {
                                lat = Angle.fromDegrees(c);
                            }
                            Position position = new Position(lat,lon,0);
                            positions.put(String.valueOf(a), position);

                            PointPlacemark pp = new PointPlacemark(position);
                            var basicPointOfInterest = new BasicPointOfInterest(position);
                            var attrs = new PointPlacemarkAttributes();
                            attrs.setLabelColor("ffffffff");
                            attrs.setImageAddress("images/pushpins/plain-red.png");
                            attrs.setLineMaterial(Material.RED);
                            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
                            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
                            attrs.setLineWidth(2d);
                            attrs.setScale(1.0);
                            pp.setLabelText((String) a);

                            pp.setAttributes(attrs);
                            placemarksLayer.addRenderable(pp);
                        }
                    }
                }
                var attrs = new BasicShapeAttributes();
                attrs.setOutlineWidth(2);
                attrs.setOutlineMaterial(new Material(Color.YELLOW));

//                createAnnotations(positions);

                if(positions.size() > 0){
                    wwd.getView().goTo(positions.entrySet().iterator().next().getValue(),wwd.getView().getCurrentEyePosition().getElevation());
                }


                sb.append("\n");
                System.out.println(sb);
                rows.forEach(System.out::println);

                placemarksLayer.setEnabled(true);
                getWwd().getModel().getLayers().add(placemarksLayer);
            }
        }
    }

    private void createAnnotations(Map<String,Position> positionMap)
    {
        this.layer = createCustomRenderableLayer();
        this.controlPointsLayer = createCustomRenderableLayer();
        this.shapeLayer = createCustomRenderableLayer();


        this.layer.setName("Found Placenames");
        this.layer.addRenderable(this.controlPointsLayer);  // add control points layer to render layer
        this.controlPointsLayer.setEnabled(true);
        if (this.applicationLayer != null) {
            this.applicationLayer.addRenderable(this.layer);    // add render layer to the application provided layer
        } else {
            this.wwd.getModel().getLayers().add(this.layer);    // add render layer to the globe model
        }
        // Init control points rendering attributes
        this.controlPointsAttributes = new AnnotationAttributes();
        // Define an 8x8 square centered on the screen point
        this.controlPointsAttributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        this.controlPointsAttributes.setLeader(AVKey.SHAPE_NONE);
        this.controlPointsAttributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        this.controlPointsAttributes.setSize(new Dimension(8, 8));
        this.controlPointsAttributes.setDrawOffset(new Point(0, -4));
        this.controlPointsAttributes.setInsets(new Insets(0, 0, 0, 0));
        this.controlPointsAttributes.setBorderWidth(0);
        this.controlPointsAttributes.setCornerRadius(0);
        this.controlPointsAttributes.setBackgroundColor(Color.BLUE);    // Normal color
        this.controlPointsAttributes.setTextColor(Color.GREEN);         // Highlighted color
        this.controlPointsAttributes.setHighlightScale(1.2);
        this.controlPointsAttributes.setDistanceMaxScale(1);            // No distance scaling
        this.controlPointsAttributes.setDistanceMinScale(1);
        this.controlPointsAttributes.setDistanceMinOpacity(1);

        // Init control point with leader rendering attributes.
        this.controlPointWithLeaderAttributes = new AnnotationAttributes();
        this.controlPointWithLeaderAttributes.setDefaults(this.controlPointsAttributes);
        this.controlPointWithLeaderAttributes.setFrameShape(AVKey.SHAPE_ELLIPSE);
        this.controlPointWithLeaderAttributes.setSize(new Dimension(10, 10));
        this.controlPointWithLeaderAttributes.setDrawOffset(new Point(0, -5));
        this.controlPointWithLeaderAttributes.setBackgroundColor(Color.LIGHT_GRAY);

        this.leaderAttributes = new BasicShapeAttributes();
        this.leaderAttributes.setOutlineMaterial(Material.WHITE);
        this.leaderAttributes.setOutlineOpacity(0.7);
        this.leaderAttributes.setOutlineWidth(3);

        // Annotation attributes

        PlacemarkLabels placemarkLabels = new PlacemarkLabels();
        placemarkLabels.setInitialLabels();

        this.annotationAttributes = new AnnotationAttributes();
        this.annotationAttributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        this.annotationAttributes.setInsets(new Insets(10, 10, 10, 10));
        this.annotationAttributes.setDrawOffset(new Point(0, 10));
        this.annotationAttributes.setTextAlign(AVKey.CENTER);
        this.annotationAttributes.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        this.annotationAttributes.setFont(Font.decode("Arial-Bold-14"));
        this.annotationAttributes.setTextColor(Color.WHITE);
        this.annotationAttributes.setBackgroundColor(new Color(0, 0, 0, 180));
        this.annotationAttributes.setSize(new Dimension(220, 0));
        this.annotation = new ScreenAnnotation("", new Point(0, 0), this.annotationAttributes);
        this.annotation.getAttributes().setVisible(true);
        this.annotation.getAttributes().setDrawOffset(null); // use defaults
        this.shapeLayer.addRenderable(this.annotation);

        this.annotation.getAttributes().setHighlighted(true);
        // Highlite using text color
        this.annotation.getAttributes().setBackgroundColor(
            this.annotation.getAttributes().getTextColor());

        positionMap.forEach((o,o1) -> {

            if(o1 instanceof Position){
                Position position = (Position) o1;
                updateAnnotation(o,position);
            }
        });
        getWwd().getModel().getLayers().add(layer);
        getWwd().getModel().getLayers().add(shapeLayer);
        wwd.redraw();

    }

    public void updateAnnotation(String name, Position pos) {
        if (pos == null) {
            this.annotation.getAttributes().setVisible(false);
            return;
        }

        StringBuilder displayString = new StringBuilder();
        displayString.append(name + "\n");
        displayString.append(this.getDisplayString(pos));

        if (displayString == null) {
            this.annotation.getAttributes().setVisible(false);
            return;
        }

        this.annotation.setText(String.valueOf(displayString));

        this.annotation.setPosition(pos);
        this.annotation.getAttributes().setVisible(true);
        wwd.redraw();
    }

    private String getDisplayString(Position pos)
    {
        StringBuilder sb = new StringBuilder();
            sb.append(this.unitsFormat.angleNL("Latitude: ", pos.getLatitude()));
            sb.append(this.unitsFormat.angleNL("Longitude: ", pos.getLongitude()));
        return sb.toString();
    }

    /**
     * @return Instance of the custom renderable layer to use of our internal layers
     * @see MeasureTool
     */
    protected CustomRenderableLayer createCustomRenderableLayer() {
        return new CustomRenderableLayer();
    }

    protected static class PlacemarkLabels extends AVListImpl
    {
        public static final String SHAPE_LINE = "MeasureTool.ShapeLine";
        public static final String SHAPE_PATH = "MeasureTool.ShapePath";
        public static final String SHAPE_POLYGON = "MeasureTool.ShapePolygon";
        public static final String SHAPE_CIRCLE = "MeasureTool.ShapeCircle";
        public static final String SHAPE_ELLIPSE = "MeasureTool.ShapeEllipse";
        public static final String SHAPE_QUAD = "MeasureTool.ShapeQuad";
        public static final String SHAPE_SQUARE = "MeasureTool.ShapeSquare";

        public static final String EVENT_POSITION_ADD = "MeasureTool.AddPosition";
        public static final String EVENT_POSITION_REMOVE = "MeasureTool.RemovePosition";
        public static final String EVENT_POSITION_REPLACE = "MeasureTool.ReplacePosition";
        public static final String EVENT_METRIC_CHANGED = "MeasureTool.MetricChanged";
        public static final String EVENT_ARMED = "MeasureTool.Armed";
        public static final String EVENT_RUBBERBAND_START = "MeasureTool.RubberBandStart";
        public static final String EVENT_RUBBERBAND_STOP = "MeasureTool.RubberBandStop";

        public static final String ANGLE_LABEL = "MeasureTool.AngleLabel";
        public static final String AREA_LABEL = "MeasureTool.AreaLabel";
        public static final String LENGTH_LABEL = "MeasureTool.LengthLabel";
        public static final String PERIMETER_LABEL = "MeasureTool.PerimeterLabel";
        public static final String RADIUS_LABEL = "MeasureTool.RadiusLabel";
        public static final String HEIGHT_LABEL = "MeasureTool.HeightLabel";
        public static final String WIDTH_LABEL = "MeasureTool.WidthLabel";
        public static final String HEADING_LABEL = "MeasureTool.HeadingLabel";
        public static final String CENTER_LATITUDE_LABEL = "MeasureTool.CenterLatitudeLabel";
        public static final String CENTER_LONGITUDE_LABEL = "MeasureTool.CenterLongitudeLabel";
        public static final String LATITUDE_LABEL = "MeasureTool.LatitudeLabel";
        public static final String LONGITUDE_LABEL = "MeasureTool.LongitudeLabel";
        public static final String ACCUMULATED_LABEL = "MeasureTool.AccumulatedLabel";
        public static final String MAJOR_AXIS_LABEL = "MeasureTool.MajorAxisLabel";
        public static final String MINOR_AXIS_LABEL = "MeasureTool.MinorAxisLabel";

        public static final String CONTROL_TYPE_LOCATION_INDEX = "MeasureTool.ControlTypeLocationIndex";
        public static final String CONTROL_TYPE_REGULAR_SHAPE = "MeasureTool.ControlTypeRegularShape";
        public static final String CONTROL_TYPE_LEADER_ORIGIN = "MeasureTool.ControlTypeLeaderOrigin";

        private static final String CENTER = "Center";
        private static final String NORTH = "North";
        private static final String EAST = "East";
        private static final String SOUTH = "South";
        private static final String WEST = "West";
        private static final String NORTHEAST = "NE";
        private static final String SOUTHEAST = "SE";
        private static final String SOUTHWEST = "SW";
        private static final String NORTHWEST = "NW";
        private static final String NORTH_LEADER = "NorthLeader";

        protected static final double SHAPE_MIN_WIDTH_METERS = 0.1;
        protected static final double SHAPE_MIN_HEIGHT_METERS = 0.1;
        protected static final int MAX_SHAPE_MOVE_ITERATIONS = 10;
        protected static final double SHAPE_CONTROL_EPSILON_METERS = 0.01;

        protected void setInitialLabels() {
            this.setLabel(ACCUMULATED_LABEL, Logging.getMessage(ACCUMULATED_LABEL));
            this.setLabel(ANGLE_LABEL, Logging.getMessage(ANGLE_LABEL));
            this.setLabel(AREA_LABEL, Logging.getMessage(AREA_LABEL));
            this.setLabel(CENTER_LATITUDE_LABEL, Logging.getMessage(CENTER_LATITUDE_LABEL));
            this.setLabel(CENTER_LONGITUDE_LABEL, Logging.getMessage(CENTER_LONGITUDE_LABEL));
            this.setLabel(HEADING_LABEL, Logging.getMessage(HEADING_LABEL));
            this.setLabel(HEIGHT_LABEL, Logging.getMessage(HEIGHT_LABEL));
            this.setLabel(LATITUDE_LABEL, Logging.getMessage(LATITUDE_LABEL));
            this.setLabel(LONGITUDE_LABEL, Logging.getMessage(LONGITUDE_LABEL));
            this.setLabel(LENGTH_LABEL, Logging.getMessage(LENGTH_LABEL));
            this.setLabel(MAJOR_AXIS_LABEL, Logging.getMessage(MAJOR_AXIS_LABEL));
            this.setLabel(MINOR_AXIS_LABEL, Logging.getMessage(MINOR_AXIS_LABEL));
            this.setLabel(PERIMETER_LABEL, Logging.getMessage(PERIMETER_LABEL));
            this.setLabel(RADIUS_LABEL, Logging.getMessage(RADIUS_LABEL));
            this.setLabel(WIDTH_LABEL, Logging.getMessage(WIDTH_LABEL));
        }
        public void setLabel(String labelName, String label) {
            if (labelName != null && labelName.length() > 0) {
                this.setValue(labelName, label);
            }
        }
    }




    protected static class CustomRenderableLayer extends RenderableLayer implements PreRenderable, Renderable {

        @Override
        public void render(DrawContext dc) {
            if (dc.isPickingMode() && !this.isPickEnabled()) {
                return;
            }
            if (!this.isEnabled()) {
                return;
            }

            super.render(dc);
        }
    }

}
