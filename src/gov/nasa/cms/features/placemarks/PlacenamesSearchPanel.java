/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.util.TableColumnManager;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;
import gov.nasa.worldwind.util.UnitsFormat;
import gov.nasa.worldwind.util.measure.MeasureTool;

import javax.swing.Box;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;

/**
 * @author : gknorman - 3/31/2021 & kdickinson - 04/07/2021
 */
public class PlacenamesSearchPanel extends JPanel
{

    private final PlacemarkSearchData placemarkSearchData;
    private final WorldWindow wwd;
    private final CelestialMapper cms;
    private JTextField jtf;
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter sorter;
    protected boolean ignoreSelectEvents = false;

    protected Path line;
    protected SurfaceShape surfaceShape;

    protected Color lineColor = Color.YELLOW;
    protected Color fillColor = new Color(.6f, .6f, .4f, .5f);
    protected double lineWidth = 2;
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

    private boolean isSearchOptionsOpen = false;
    private JDialog searchOptionsDialog;
    private TableColumnManager tcm;
    private List<String> allColumns;
    private HashMap<String, Integer> searchColumnMap;
    private List<TableColumn> allColumnModels;
    private ArrayList<JCheckBox> searchOptionsCBList;
    private int tableWidth;
    private int tableHeight;
    //    private List<Integer> selectedRowIndices;
    HashMap<PointPlacemark, AbstractMap.SimpleEntry<ScreenAnnotation, Boolean>> savedPlacemarks;
    //    private HashMap<PointPlacemark, AbstractMap.SimpleEntry<ScreenAnnotation, Boolean>> ppAnnotationMap;
    private HashMap<String, Map> selectedPPMap;
    private CustomRenderableLayer annotationsLayer;
    protected static final Double TARGET_RESOLUTION = 10d;

    protected HighResolutionTerrain terrain;
    private boolean noSearchColumns;
    private long timeSinceLastPick;

    public PlacenamesSearchPanel(WorldWindow wwd, CelestialMapper celestialMapper)
    {
        super(new BorderLayout());
        this.wwd = wwd;
        this.cms = celestialMapper;
        this.placemarkSearchData = new PlacemarkSearchData(wwd, cms);
        this.placemarksLayer = createCustomRenderableLayer();
        this.placemarksLayer.setName("Selected Placemarks Layer");
//        this.ppAnnotationMap = new HashMap<>();
        this.selectedPPMap = new HashMap<>();
        this.annotationsLayer = createCustomRenderableLayer();
        this.annotationsLayer.setName("Annotations Layer");
        this.terrain = new HighResolutionTerrain(getWwd().getModel().getGlobe(),
            TARGET_RESOLUTION);

        this.makePanel();
    }

    private void makePanel()
    {
        this.setLayout(new BorderLayout());
        jtf = new JTextField(15);
        JLabel searchLbl = new JLabel("Search Table Fields");

        // Pops up search options Dialog, but can't add action listeners until Table is created
        JButton searchOptions = new JButton("Show/Hide Search Options");
        searchOptions.setToolTipText("Lets you choose which fields to query in the table");

        // Label Text Button
        JButton labelButton = new JButton("Show/Hide Annotations");
        labelButton.setToolTipText("Toggle the placemark label text visibility");
        labelButton.addActionListener(e ->
        {
            showAnnotation = !showAnnotation;
            annotationsLayer.setEnabled(showAnnotation);
            wwd.redraw();
        });

        JButton resetRowSort = new JButton("Reset Row Order");
        addResetRowSortListener(resetRowSort);

        JPanel searchOptionsButtonRow = new JPanel();
        searchOptionsButtonRow.setLayout(new BoxLayout(searchOptionsButtonRow, BoxLayout.LINE_AXIS));
        searchOptionsButtonRow.add(searchOptions);
        searchOptionsButtonRow.add(Box.createHorizontalStrut(10));
        searchOptionsButtonRow.add(resetRowSort);
        searchOptionsButtonRow.add(Box.createHorizontalStrut(10));
        searchOptionsButtonRow.add(labelButton);

        JPanel searchField = new JPanel(new GridLayout(3, 1, 5, 5));
        searchField.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        searchField.add(searchLbl);
        searchField.add(jtf);
        searchField.add(searchOptionsButtonRow);

        JPanel topPanel = new JPanel(new GridLayout(1, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        topPanel.add(searchField);

        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonRow.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JButton resetButton = new JButton("Reset Row/Column Order");
        resetButton.addActionListener(e ->
            resetTable());

        JButton showALlColumns = new JButton("Show all Columns");
        showALlColumns.addActionListener(e ->
            showColumns());

        JButton hideMostColumns = new JButton("Show only Name");
        hideMostColumns.addActionListener(e ->
            showOnlyNameColumn());

        buttonRow.add(resetButton);
        buttonRow.add(showALlColumns);
        buttonRow.add(hideMostColumns);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        bottomPanel.add(buttonRow);

        // Most important step in the initialization process
        createTable();

        // We can add an actionlistener to the search options button now that the table exists
        addSearchOptionsListener(searchOptions);

        // Create the search options dialog
        createSearchOptionsDialog();

        // Hides the misc columns for the default view
        resetTable();

        addDocumentListenerToTextField();

        // Now that the table and it's listeners are ready to go, we add the table to a new ScrollPane
        JScrollPane jsp = new JScrollPane(table);

        // TODO - Enable pinned rows functionality or move to different feature
//        createPinnedRowsPanel();

        this.add(topPanel, BorderLayout.NORTH);
        this.add(jsp, BorderLayout.CENTER);

        // TODO - Enable functionality of right panel buttons or move to different feature
//        this.add(rightPanel, BorderLayout.EAST);
        this.add(bottomPanel, BorderLayout.SOUTH);

//        setSize(475, 300);
        setVisible(true);
    }

    // TODO - Enable create pinned rows functionality or move to different feature
    private void createPinnedRowsPanel()
    {
        //        pinnedRows = new ArrayList<>();
//
//        JButton saveLocations = new JButton("Pin Selected Locations");
//        saveLocations.addActionListener(e -> {
//            pinSelectedRows = true;
//            if(pinnedRows != selectedRowIndices){
//
//                // Effectively merges without duplicates
//                pinnedRows.removeAll(selectedRowIndices);
//                pinnedRows.addAll(selectedRowIndices);
//            }
//        });
//
//        saveLocations.setAlignmentX(Component.CENTER_ALIGNMENT);
//        JButton removeLocations = new JButton("Remove Pins");
//        removeLocations.addActionListener(e -> {
//            pinSelectedRows = false;
//            pinnedRows = new ArrayList<>();
//        });
//
//
//        removeLocations.setAlignmentX(Component.CENTER_ALIGNMENT);
//        JButton exportLocations = new JButton("Export Pins");
//        exportLocations.addActionListener(e -> {
//            pinnedRows.forEach(integer -> {
//                var row = model.getDataVector().elementAt(table.convertRowIndexToModel(integer));
//
//            });
//        });
//        exportLocations.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//        JPanel rightPanel = new JPanel();
//        JPanel rpInnerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
//
//        rpInnerPanel.add(saveLocations);
////        rightPanel.add(Box.createVerticalStrut(20));
//        rpInnerPanel.add(removeLocations);
////        rightPanel.add(Box.createVerticalStrut(20));
//        rpInnerPanel.add(exportLocations);
////        rightPanel.add(Box.createVerticalStrut(20));
//        rightPanel.add(rpInnerPanel, BorderLayout.CENTER);
//        rightPanel.add(box);
    }

    private void addDocumentListenerToTextField()
    {

        // Needs to be a field to be manually refired when the user changes which columns to search
        // however I couldn't find a way to manually fire the listener after all!
        // The easiest way to fire the listener is to simply call jtf.setText(jtf.getText)
        DocumentListener documentListener = new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                search(jtf.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                search(jtf.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                search(jtf.getText());
            }

            public void search(String str)
            {
                if (str.length() == 0 || noSearchColumns)
                {
                    sorter.setRowFilter(null);
                }
                else
                {

                    sorter.setRowFilter(
                        RowFilter.regexFilter("(?i)" + str,
                            searchColumnMap.values().stream()
                                .filter(integer -> integer > -1)
                                .mapToInt(Integer::intValue)
                                .toArray())
                    );
                }
            }
        };
        jtf.getDocument().addDocumentListener(documentListener);
    }

    private void createTable()
    {
        // Start building the table
        String[] columnNames = placemarkSearchData.getHeaders();

        // The 0 argument is number rows.
        model = new DefaultTableModel(columnNames, 0)
        {
            @Override
            public void setValueAt(Object inValue, int inRow, int inCol)
            {
//                System.out.println("Gets called ");
                fireTableCellUpdated(inRow, inCol);
            }
        };

        // Create Row Sorter, but override the click behavior so the third click on a column header will
        // reset the row order back to normal, without using the "row reset" button functions
        // https://stackoverflow.com/questions/5477965/how-to-restore-the-original-row-order-with-jtables-row-sorter
        sorter = new TableRowSorter<>(model)
        {
            @Override
            public void toggleSortOrder(int column)
            {
                List<? extends SortKey> sortKeys = getSortKeys();
                if (sortKeys.size() > 0)
                {
                    if (sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING)
                    {
                        setSortKeys(null);
                        return;
                    }
                }
                super.toggleSortOrder(column);
            }
        };

        table = new JTable(model)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return true;
            }
        };

        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = table.getTableHeader();

        ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
        for (int c = 0; c < table.getColumnCount(); c++)
        {
            TableColumn col = table.getColumnModel().getColumn(c);
            tips.setToolTip(col,
                "<html>Click to sort table by column<br>Right Click to select which columns to show/hide</html>");
        }
        header.addMouseMotionListener(tips);

        ListSelectionModel listSelectionModel = table.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());

        this.table.setToolTipText("<html>Click to select<br>Double-Click to rename</html>");

        tableHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 3);
        tableWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4);

        table.setPreferredScrollableViewportSize(new Dimension(tableWidth, tableHeight));
        this.tcm = new TableColumnManager(table);

        this.allColumnModels = tcm.getAllColumns();

        this.allColumns = tcm.getAllColumns().stream()
            .map(TableColumn::getHeaderValue)
            .map(Object::toString)
            .collect(Collectors.toList());

        // Store each column labelName and it's position in the model's header
        // before any columns are hidden or moved by the user
        this.searchColumnMap = IntStream.range(0, allColumns.size())
            .boxed()
            .collect(Collectors.toMap(
                i -> allColumns.get(i),
                Integer::valueOf,
                (u, v) ->
                {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                HashMap::new
            ));

        // Last but not least, add each row of data from the main CSV to the Table model
        PlacemarkSearchData.getRowList().forEach(o ->
            model.addRow(o));

        // https://stackoverflow.com/questions/4151850/how-to-keep-track-of-row-index-when-jtable-has-been-sorted-by-the-user
        // After populating the table, we can set the modelRowIndex to 0
        table.convertRowIndexToView(0);
    }

    private void showOnlyNameColumn()
    {

        allColumnModels.forEach(tableColumn ->
        {
            table.removeColumn(tableColumn);
            if (tableColumn.getHeaderValue().toString().equalsIgnoreCase("clean_name"))
            {
                table.addColumn(tableColumn);
            }
        });

        // Set clean_name column to a wider width
        TableColumnManager.setJTableColumnsWidth(table, tableWidth, 50);

    }

    private void showColumns()
    {
        TableColumnModel tableColumnModel = table.getColumnModel();

        Collections.list(tableColumnModel.getColumns()).forEach(tableColumn ->
        {
            table.removeColumn(tableColumn);
        });

        allColumnModels.forEach(tableColumn ->
        {
            var labelName = tableColumn.getHeaderValue().toString();
            if (labelName.equals("labelName"))
            {
                tableColumn.setPreferredWidth(100);
            }
            else if (labelName.equals("clean_name"))
            {
                tableColumn.setPreferredWidth(150);
            }

            table.addColumn(tableColumn);
        });
    }

    private void resetTable()
    {
        TableModel tableModel = table.getModel();
        TableColumnModel tableColumnModel = table.getColumnModel();
        RowSorter rs = table.getRowSorter();
        rs.setSortKeys(null);

        // Not sure if it's a good idea to remove their current search term if all they want is to
        // reset their current view of the table columns and row order
//        jtf.setText("");

        // Hide most of the extraneous columns
        int[] columnsToHide =
            {
                1, 3, 4, 8, 9, 10, 15, 16
            };

        // Loop through all columns, visible or otherwise in the model
        for (int i = 0; i < tableModel.getColumnCount() - 1; i++)
        {
            String labelName = tableModel.getColumnName(i);

            // location will be the column's current position in the table, where as
            // i is the column's position in the table's underlying model
            int location = -1;
            try
            {
                // if the column is in the table already, it will have a value > 0
                location = tableColumnModel.getColumnIndex(labelName);
            }
            catch (IllegalArgumentException e)
            {
                // Don't need to do anything if the column isn't currently visible
//                System.out.println(labelName + " not currently visible in table.");
//                    e.printStackTrace();
            }

            if (location > -1)
            {
                // This means the column is where it's supposed to be already
                if (location != i)
                {
                    tableColumnModel.moveColumn(location, i);
                }
            }
            else
            {
                // If the column doesn't exist in the table currently, add it and
                // then move it to the correct position according to the model
                table.addColumn(allColumnModels.get(searchColumnMap.get(labelName)));
                location = tableColumnModel.getColumnIndex(labelName);
                tableColumnModel.moveColumn(location, i);
            }
        }

        // Hiding has to be done after all of the columns exist in the table, or else
        Arrays.stream(columnsToHide).forEach(value -> tcm.hideColumn(value));


        // Helper method to size the visible columns for the default view
        TableColumnManager.setJTableColumnsWidth(table, tableWidth * 3,
            8, 15, 7.75, 7.75,
            7.5, 7.5, 7.5, 7.5,
            7.5, 9, 7.5, 7.5);
    }

    public static boolean contains(final int[] arr, final int key)
    {
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }


    private void addSearchOptionsListener(JButton searchOptions)
    {
        searchOptions.addActionListener(e ->
        {
            this.isSearchOptionsOpen = !isSearchOptionsOpen;
            if (searchOptionsDialog == null)
            {
                return;
            }

            this.searchOptionsDialog.setVisible(isSearchOptionsOpen);
        });
    }


    private void createSearchOptionsDialog()
    {

        searchOptionsDialog = new JDialog(cms);
        Rectangle bounds = cms.getBounds();
        searchOptionsDialog.getContentPane().setLayout(new BorderLayout());
        searchOptionsDialog.setTitle("Choose Fields to Search");
        searchOptionsDialog.setSize(new Dimension(200, 400));

        // Set the location and resizable
        searchOptionsDialog.setLocation(bounds.x, bounds.y + 200);
        searchOptionsDialog.setResizable(true);
        searchOptionsDialog.setVisible(false);

        int columnCount = tcm.getAllColumns().size();

        JPanel searchOptionsTopLabel = new JPanel(new GridLayout(1, 0, 5, 5));
        searchOptionsTopLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel instructions = new JLabel("Choose which columns to search");
        searchOptionsTopLabel.add(instructions);

        JPanel searchOptions = new JPanel(new GridLayout(columnCount + 1, 1, 5, 5));
        searchOptions.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        searchOptionsCBList = new ArrayList<JCheckBox>();

        tcm.getAllColumns().forEach(tableColumn ->
        {
            //  Create a menu item for all columns managed by the table column
            //  manager, checking to see if the column is shown or hidden.
            Object value = tableColumn.getHeaderValue();
            JCheckBox item = new JCheckBox(value.toString());

            item.addActionListener(e ->
            {
                if (item.isSelected())
                {
                    // Remove / Add column from rowFilter's indices
                    this.searchColumnMap.put(value.toString(), allColumns.indexOf(value.toString()));

                    this.noSearchColumns = false;
//                    System.out.println("Enabling: " + value.toString() + " : " + allColumns.indexOf(value.toString()));
                }
                else
                {

                    this.searchColumnMap.put(value.toString(), -1);
//                    System.out.println("Disabling: " + value.toString() + " : " + allColumns.indexOf(value.toString()));
                }

                // Can't think of any other way to manually fire the DocumentListener
                // to tell it that the Rowfilter should be changed because the columns
                // to search have changed:
                jtf.setText(jtf.getText());
            });

            try
            {
                item.setSelected(true);
            }
            catch (IllegalArgumentException e)
            {
                item.setSelected(false);
            }
            searchOptions.add(item);
            searchOptionsCBList.add(item);
        });

        JPanel searchOptionsButtons = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton selectNone = new JButton("Deselect All");

        selectNone.addActionListener(e ->
        {
            this.noSearchColumns = true;
            searchOptionsCBList.forEach(jCheckBox ->
            {

                if (jCheckBox.isSelected())
                {
                    jCheckBox.doClick(0);
                }
            });
        });

        JButton selectAll = new JButton("Select All");

        selectAll.addActionListener(e ->
        {
            this.noSearchColumns = false;
            searchOptionsCBList.forEach(jCheckBox ->
            {
                if (!jCheckBox.isSelected())
                {
                    jCheckBox.doClick(0);
                    jCheckBox.getModel().setEnabled(true);
                }
            });
        });

        JButton selectCoordinates = new JButton("Coord's & Name Only");

        selectCoordinates.addActionListener(e ->
        {
            this.noSearchColumns = false;
            searchOptionsCBList.forEach(jCheckBox ->
            {
                if (jCheckBox.getText().equals("clean_name")
                    || jCheckBox.getText().equals("center_lon")
                    || jCheckBox.getText().equals("center_lat"))
                {
                    if (!jCheckBox.isSelected())
                    {
                        jCheckBox.doClick(0);
//                        jCheckBox.setSelected(true);
                    }
                }
                else
                {
                    if (jCheckBox.isSelected())
                    {
                        jCheckBox.doClick(0);

//                        jCheckBox.setSelected(false);
                    }
                }
            });
        });
        searchOptionsButtons.add(selectNone);
        searchOptionsButtons.add(selectAll);
        searchOptionsButtons.add(selectCoordinates);

        JPanel sbpOuterPanel = new JPanel();
        sbpOuterPanel.add(searchOptionsButtons);

        JScrollPane soOuterScrollPane = new JScrollPane(searchOptions);
        soOuterScrollPane.setPreferredSize(new Dimension(200, 200));

        // Add JPanels to JDialog
        searchOptionsDialog.getContentPane().add(searchOptionsTopLabel, BorderLayout.NORTH);
        searchOptionsDialog.getContentPane().add(soOuterScrollPane, BorderLayout.CENTER);
        searchOptionsDialog.getContentPane().add(sbpOuterPanel, BorderLayout.EAST);

        // Size all of the elements within the dialog
        searchOptionsDialog.pack();
    }

    private void addResetRowSortListener(JButton resetRowSort)
    {
        resetRowSort.addActionListener(e ->
        {
            RowSorter rs = table.getRowSorter();
            rs.setSortKeys(null);
        });
    }

    private WorldWindow getWwd()
    {
        return this.wwd;
    }

    public JPanel getView()
    {
        return this;
    }

    // From Oracle example:
    // https://docs.oracle.com/javase/tutorial/uiswing/examples/events/TableListSelectionDemoProject/src/events/TableListSelectionDemo.java
    class SharedListSelectionHandler extends DefaultListSelectionModel implements ListSelectionListener
    {
        private boolean selectionEnabled = true;

        public SharedListSelectionHandler()
        {
            super();
        }

        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting())
            {
                // The lsm doesn't get the correct row # after the rows have been filtered
                // by the search text box
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                // Use this to get the clean_name column value from each selected row
                TableColumnModel tcm = table.getColumnModel();

                // Turns out we do need the full list of indices for the selected rows
                // as they could be non-continuous between the first and last after the table
                // has been filtered by the search text field
                List<Integer> selectedRowIndices = Arrays.stream(table.getSelectedRows())
                    .map(i -> table.convertRowIndexToModel(i))
                    .boxed()
                    .collect(Collectors.toList());

                Map<String, Position> positions = new HashMap<>();

//                sb.append("Event for listSelectionEvent indexes "
//                    + firstIndex + " - " + lastIndex
//                    + "; isAdjusting is " + isAdjusting
//                    + "; selected indexes:" + lsm.getSelectedIndices());

                if (!lsm.isSelectionEmpty())
                {
                    // Find out which indexes are selected.
                    int[] selectedRows = table.getSelectedRows();
//                    System.out.println("selectedRows:" + Arrays.stream(selectedRows).toArray());
//                    System.out.println("selectedRowIndices: " + selectedRowIndices);
//                    int minIndex = selectedRowIndices.get(0);
//                    int maxIndex = selectedRowIndices.get(selectedRowIndices.size() - 1);
//                    System.out.println("minIndex: " + minIndex + " maxIndex: " + maxIndex);

                    placemarksLayer.setEnabled(false);
                    annotationsLayer.setEnabled(false);

                    // Reset all of the entries in the selected placemarks map to being invisible and NOT clicked
                    selectedPPMap.entrySet().forEach(entry -> {
                        // Leaving stub here as a way of checking if a placemark is "selected" or not
                        // and should remain persistent on the globe or not until it's "deselected"
//                        boolean isClicked = (boolean) entry.getValue().get("selected");
//                        if(!isSelected){}

                        entry.getValue().put("clicked", false);

                        PointPlacemark pp = (PointPlacemark) entry.getValue().get("pointPlacemark");
                        pp.setVisible(false);
                        ScreenAnnotation screenAnnotation = (ScreenAnnotation) entry.getValue().get("annotation");
                        screenAnnotation.getAttributes().setVisible(false);
                        
                        wwd.redraw();
//                        }
                    });

//
                    for (int i : selectedRowIndices)
                    {
//
                        String labelName = (String) model.getValueAt(i, tcm.getColumnIndex("clean_name"));

                        // If the placemark has not been created previously, then create it
                        if (!selectedPPMap.containsKey(labelName))
                        {

                            double longitude = Double.parseDouble(
                                String.valueOf(model.getValueAt(i, model.findColumn("center_lon"))));

                            double latitude = Double.parseDouble(
                                String.valueOf(model.getValueAt(i, model.findColumn("center_lat"))));

                            Angle lat, lon;

                            if (longitude > 180.00)
                            {
                                lon = Angle.POS360.subtract(Angle.fromDegrees(longitude)).multiply(-1.0);
                            }
                            else
                            {
                                lon = Angle.fromDegrees(longitude);
                            }

                            if (latitude > 90.00)
                            {
                                lat = Angle.POS180.subtract(Angle.fromDegrees(latitude)).multiply(-1.0);
                            }
                            else
                            {
                                lat = Angle.fromDegrees(latitude);
                            }

                            /*
                            // Copied example from line of sight on getting true elevation for a given coordinate
                            // setting the placemark to clamp to ground
                             */

                            Position position = terrain.getGlobe().computePositionFromPoint(
                                terrain.getSurfacePoint(lat, lon, 0));

                            positions.put(labelName, position);

                            PointPlacemark pp = new PointPlacemark(position);
                            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

                            var attrs = new PointPlacemarkAttributes();
                            attrs.setLabelColor("ffffffff");
                            attrs.setImageAddress("images/pushpins/plain-red.png");
                            attrs.setLineMaterial(Material.RED);
                            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
                            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
                            attrs.setLineWidth(2d);
                            attrs.setScale(1.0);

                            pp.setLabelText(labelName);

                            pp.setAttributes(attrs);

                            ScreenAnnotation annotation = makeAnnotation(position, pp);

                            Map entry = new HashMap(Map.of(
                                "pointPlacemark", pp,
                                "annotation", annotation,
                                "clicked", false
                            ));
//                            Vector vector = new Vector( Arrays.asList(pp,annotation,false));

                            selectedPPMap.put(labelName, entry);
                            placemarksLayer.addRenderable(pp);
                            annotationsLayer.addRenderable(annotation);
                        }
                        else
                        {
                            PointPlacemark pointPlacemark = (PointPlacemark) selectedPPMap.get(labelName).get(
                                "pointPlacemark");
                            pointPlacemark.setVisible(true);
                            positions.put(labelName, pointPlacemark.getPosition());
                        }
                    } // end for loop
                    placemarksLayer.setEnabled(true);
                    annotationsLayer.setEnabled(true);
                    wwd.redraw();
                } // end if isSelectionEmpty

                timeSinceLastPick = System.currentTimeMillis() - 1;

                wwd.addSelectListener(event -> {
                    String ea = event.getEventAction();
                    if (!ea.equals(SelectEvent.ROLLOVER))
                    {
//                        System.out.println("Current selectListenerEvent: " + ea);
//                        System.out.println(event.getSource());
                    }
                    if (event.getEventAction().equals(SelectEvent.HOVER))
                    { // there are many of these
                        if (event.hasObjects() && event.getTopObject() instanceof PointPlacemark)
                        {
                            // Have to make sure that the listener event isn't being fired twice by adding
                            // a small delay of 1ms.

                            // Also, this condition needs to be checked apart from whether the event has objects
                            // otherwise when CTRL-CLICK selecting multiple rows, the wwd listener will fire multiple
                            // times and the else condition will always be reached.
                            if(System.currentTimeMillis() - timeSinceLastPick > 1)
                            {
//                                System.out.println("Event hasObjects, invoking placeMarkEvent(HOVER)");
                                placemarkEvent(event, "HOVER");
                                event.consume();
                            }
                        } // end if event.hasObjects()
                        else
                        {
                            if (SwingUtilities.isEventDispatchThread())
                            {
//                                System.out.println("Event does not have Objects, hiding Annotations");
                                selectedPPMap.forEach((key, value) -> {
                                    ScreenAnnotation sc = (ScreenAnnotation) value.get("annotation");
                                    if (sc.getAttributes().isVisible())
                                    {
//                                        System.out.println(key + ": Is no longer Hovered!");
                                        if (!(boolean) value.get("clicked"))
                                        {
//                                            System.out.println(
//                                                key + ": has not been clicked, hiding annotation!");
                                            sc.getAttributes().setVisible(false);
                                            wwd.redraw();
                                        } else {
//                                            System.out.println(key + ": has been CLICKED and Annotation should be visible");
                                        }
                                    }
                                });
                            }
                        }
                    }// end if event.getTopObject() == SelectEvent.HOVER
                    else if ((event.getEventAction().equals(SelectEvent.LEFT_CLICK) || event.getEventAction().equals(
                        SelectEvent.LEFT_DOUBLE_CLICK)))
                    {
                        if (event.hasObjects() && event.getTopObject() instanceof PointPlacemark)
                        {
                            // Have to make sure that the click event action isn't fired twice by adding even
                            // a small delay of 1ms
                            if(System.currentTimeMillis() - timeSinceLastPick > 1){
//                                System.out.println("Event hasObjects, invoking placeMarkEvent(CLICKED)");
                                placemarkEvent(event, "CLICKED");
                                event.consume();
                            }
                        }
                    }

                    timeSinceLastPick = System.currentTimeMillis();
                });

                if (positions.size() > 0)
                {
//                    System.out.println("Current num of positions: " + positions.size());
                    wwd.getView().goTo(positions.entrySet().iterator().next().getValue(),
                        wwd.getView().getCurrentEyePosition().getElevation());
                }

                placemarksLayer.setEnabled(true);
                annotationsLayer.setEnabled(true);
                annotationsLayer.setPickEnabled(false);
                if(getWwd().getModel().getLayers().getLayerByName(placemarksLayer.getName()) == null){
                    getWwd().getModel().getLayers().add(placemarksLayer);
                }
                if(getWwd().getModel().getLayers().getLayerByName(annotationsLayer.getName()) == null){
                    getWwd().getModel().getLayers().add(annotationsLayer);
                }

                wwd.redraw();
            }
        }

        public boolean isSelectionEnabled()
        {
            return selectionEnabled;
        }

        public void setSelectionEnabled(boolean selectionEnabled)
        {
            this.selectionEnabled = selectionEnabled;
        }
    }

    private ScreenAnnotation makeAnnotation(Position pos, PointPlacemark pp)
    {
        String displayString = this.formatStatistics(pp);
        AnnotationAttributes annotationAttributes = new AnnotationAttributes();
        annotationAttributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        annotationAttributes.setInsets(new Insets(10, 10, 10, 10));
        annotationAttributes.setDrawOffset(new Point(0, 30));
        annotationAttributes.setTextAlign(AVKey.CENTER);
        annotationAttributes.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        annotationAttributes.setFont(Font.decode("Arial-Bold-14"));
        annotationAttributes.setTextColor(Color.WHITE);
        annotationAttributes.setBackgroundColor(new Color(0, 0, 0, 180));
        annotationAttributes.setSize(new Dimension(220, 0));
        ScreenAnnotation annotation = new ScreenAnnotation("", new Point(0, 0), annotationAttributes);
        annotation.getAttributes().setVisible(false);
        annotation.getAttributes().setDrawOffset(new Point(0, 70)); // use defaults
        annotation.setPosition(new Position(new LatLon(pos.getLatitude(), pos.getLongitude()), 0d));
//        annotation.getAttributes().setVisible(true);

        annotation.setText(displayString);
        return annotation;
    }

    protected String formatStatistics(PointPlacemark pp)
    {
        StringBuilder sb = new StringBuilder();
        double value;
        String s;

        String str = pp.getLabelText();
        s = String.format("Landmark: %s", str);
        sb.append(s);

        // Latitude
        value = pp.getPosition().latitude.degrees;
        s = String.format("\nLatitude: %7.4f\u00B0", value);
        sb.append(s);

        // Longitude
        value = pp.getPosition().longitude.degrees;
        s = String.format("\nLongitude: %7.4f\u00B0", value);
        sb.append(s);

        // Longitude
        value = pp.getPosition().getElevation();
        s = String.format("\nElevation: %7.4f km\u00B0", value);
        sb.append(s);

        return sb.toString();
    }

    /**
     * @return Instance of the custom renderable layer to use of our internal layers
     * @see MeasureTool
     */
    protected CustomRenderableLayer createCustomRenderableLayer()
    {
        return new CustomRenderableLayer();
    }

    protected static class CustomRenderableLayer extends RenderableLayer implements PreRenderable, Renderable
    {

        @Override
        public void render(DrawContext dc)
        {
            if (dc.isPickingMode() && !this.isPickEnabled())
            {
                return;
            }
            if (!this.isEnabled())
            {
                return;
            }

            super.render(dc);
        }
    }

    private void placemarkEvent(SelectEvent event, String behavior)
    {
//        System.out.println(event.getTopObject().getClass()
//            + " : " + event.getTopObject().toString());

        timeSinceLastPick = System.currentTimeMillis();

        Runnable hoverBehavior = () -> {
            if (event.getTopObject() instanceof PointPlacemark)
            {
                PointPlacemark pp = (PointPlacemark) event.getTopObject();
                var selected = selectedPPMap.get(pp.getLabelText());
                ScreenAnnotation annotation = (ScreenAnnotation) selected.get("annotation");
//                System.out.println("Current Hovered Placemark: " + pp.getLabelText() + " " + pp);

                if (!annotation.getAttributes().isVisible())
                {
                    annotation.getAttributes().setVisible(true);
                }
                wwd.redraw();
            }
        };
        Runnable clickedBehavior = () -> {
            if (event.getTopObject() instanceof PointPlacemark)
            {
                PointPlacemark pp = (PointPlacemark) event.getTopObject();
                var selected = selectedPPMap.get(pp.getLabelText());
                ScreenAnnotation annotation = (ScreenAnnotation) selected.get("annotation");

//                System.out.println("Current clicked Placemark: " + pp.getLabelText() + " " + pp);

                boolean clicked = (boolean) selected.get("clicked");

//                System.out.println("isClicked before update?: " + clicked);

                if (!clicked)
                {
                    annotation.getAttributes().setVisible(true);
                    selected.put("clicked", true);
                    wwd.redraw();
                }
                else
                {
                    annotation.getAttributes().setVisible(false);
                    selected.put("clicked", false);
                    wwd.redraw();
                }
            }
        };

        if (SwingUtilities.isEventDispatchThread())
        {
            if (behavior.equals("HOVER"))
            {
                hoverBehavior.run();
            }
            else if (behavior.equals("CLICKED"))
            {
                clickedBehavior.run();
            }
        }
        else
        {
            if (behavior.equals("HOVER"))
            {
                SwingUtilities.invokeLater(hoverBehavior);
            }
            else if (behavior.equals("CLICKED"))
            {
                SwingUtilities.invokeLater(clickedBehavior);
            }
        }
    }
}
