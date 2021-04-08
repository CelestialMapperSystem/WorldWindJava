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
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.measure.MeasureTool;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.stream.*;

/**
 * @author : gknorman - 3/31/2021
 *
 */
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
    private PointPlacemark pp;
    protected boolean ignoreSelectEvents = false;

    protected static final String SELECTION_CHANGED = "PlaceNamesPanel.SelectionChanged";
    private ListSelectionModel listSelectionModel;
    protected Path line;
    protected SurfaceShape surfaceShape;
    protected ScreenAnnotation annotation;
    protected String labelName;

    protected Color lineColor = Color.YELLOW;
    protected Color fillColor = new Color(.6f, .6f, .4f, .5f);
    protected double lineWidth = 2;

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

    private boolean isSearchOptionsOpen = false;
    private JDialog searchOptionsDialog;
    private TableColumnManager tcm;
    private List<String> allColumns;
    private HashMap<String, Integer> searchColumnMap;
    private List<TableColumn> allColumnModels;
    private ArrayList<JCheckBox> searchOptionsCBList;
    private int tableWidth;
    private int tableHeight;
    private List<Integer> selectedRowIndices;
    private int[] selectedRows;
    private ArrayList<Integer> pinnedRows;
    private boolean pinSelectedRows;

    public PlacenamesSearchPanel(WorldWindow wwd, CelestialMapper celestialMapper)
    {
        super(new BorderLayout());
        this.wwd = wwd;
        this.cms = celestialMapper;
        this.placemarkSearchData = new PlacemarkSearchData(wwd, cms);
        this.placemarksLayer = createCustomRenderableLayer();
        this.placemarksLayer.setName("Found Placemarks Layer");

        this.makePanel();
    }

    private void makePanel()
    {
        jtf = new JTextField(15);
        searchLbl = new JLabel("Search Table Fields");

        // Pops up search options Dialog, but can't add action listeners until Table is created
        JButton searchOptions = new JButton("Show/Hide Search Options");
        searchOptions.setToolTipText("Lets you choose which fields to query in the table");
        
        // Label Text Button
        JButton labelButton = new JButton("Show/Hide Label");
        labelButton.setToolTipText("Toggle the placemark label text visibility");
        labelButton.addActionListener(e ->
        {
            if(pp.getLabelText() == null)
            {
                pp.setLabelText(labelName);   
                wwd.redraw();
            }
            else if(pp.getLabelText() != null)
            {
                pp.setLabelText(null);
                wwd.redraw();
            }
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
        JButton resetButton = new JButton("Reset Table");
        resetButton.addActionListener(e ->
        {
            resetTable();
        });

        JButton showALlColumns = new JButton("Show all Columns");
        showALlColumns.addActionListener(e ->
        {
            showColumns();
        });

        JButton hideMostColumns = new JButton("Show only Name");
        hideMostColumns.addActionListener(e ->
        {
            showOnlyName();
        });

        buttonRow.add(resetButton);
        buttonRow.add(showALlColumns);
        buttonRow.add(hideMostColumns);

        JPanel bottomPanel = new JPanel(new GridLayout(1, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        bottomPanel.add(buttonRow);

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

            /**
             * Updates the selection models of the table, depending on the state
             * of the two flags: <code>toggle</code> and <code>extend</code>.
             * Most changes to the selection that are the result of keyboard or
             * mouse events received by the UI are channeled through this method
             * so that the behavior may be overridden by a subclass. Some UIs
             * may need more functionality than this method provides, such as
             * when manipulating the lead for discontiguous selection, and may
             * not call into this method for some selection changes.
             * <p>
             * This implementation uses the following conventions:
             * <ul>
             * <li> <code>toggle</code>: <em>false</em>, <code>extend</code>:
             * <em>false</em>. Clear the previous selection and ensure the new
             * cell is selected.
             * <li> <code>toggle</code>: <em>false</em>, <code>extend</code>:
             * <em>true</em>. Extend the previous selection from the anchor to
             * the specified cell, clearing all other selections.
             * <li> <code>toggle</code>: <em>true</em>, <code>extend</code>:
             * <em>false</em>. If the specified cell is selected, deselect it.
             * If it is not selected, select it.
             * <li> <code>toggle</code>: <em>true</em>, <code>extend</code>:
             * <em>true</em>. Apply the selection state of the anchor to all
             * cells between it and the specified cell.
             * </ul>
             *
             * @param rowIndex affects the selection at <code>row</code>
             * @param columnIndex affects the selection at <code>column</code>
             * @param toggle see description above
             * @param extend if true, extend the current selection
             * @since 1.3
             */
//            @Override
//            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
//            {
//                if(!(toggle && jt.getSelectedRowCount() == 1))
//                {
//                    super.changeSelection(rowIndex, columnIndex, toggle, extend);
//                }
//            }
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

        listSelectionModel = table.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SharedListSelectionHandler());

//        this.table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
//            if (!ignoreSelectEvents) {
//                actionPerformed(new ActionEvent(e.getSource(), -1, SELECTION_CHANGED));
//            }
//        });
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
                        i -> Integer.valueOf(i),
                        (u, v) ->
                {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                        HashMap::new
                ));

        // We can add an actionlistener to the search options button now that the table exists
        addSearchOptionsListener(searchOptions);

        // Create the search options dialog
        createSearchOptionsDialog();

        resetTable();

        this.setLayout(new BorderLayout());

        placemarkSearchData.getRowList().forEach(o ->
        {
            Object[] data = ((ArrayList) o).toArray();
            model.addRow(data);
        });

        // https://stackoverflow.com/questions/4151850/how-to-keep-track-of-row-index-when-jtable-has-been-sorted-by-the-user
        // After populating the table, we can set the modelRowIndex to 0
        table.convertRowIndexToView(0);

        jtf.getDocument().addDocumentListener(new DocumentListener()
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
                if (str.length() == 0)
                {
                    sorter.setRowFilter(null);
                } else
                {
                    int[] columnsToSearch;
                    sorter.setRowFilter(
                            RowFilter.regexFilter("(?i)" + str,
                                    searchColumnMap.entrySet().stream()
                                            .filter(a -> a.getValue() > -1)
                                            .map(Map.Entry::getValue)
                                            .mapToInt(Integer::intValue)
                                            .toArray())
                    );
                }
            }
        });

        model.addTableModelListener(new TableModelListener()
        {
            @Override
            public void tableChanged(TableModelEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (selectedRowIndices.size() > 0)
                        {
                            selectedRowIndices.forEach(integer ->
                            {
                                table.addRowSelectionInterval(integer, integer);
                            });
                        }
                    }
                });
            }
        });

        jsp = new JScrollPane(table);

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
        this.add(topPanel, BorderLayout.NORTH);
        this.add(jsp, BorderLayout.CENTER);

        // TODO - Enable functionality of right panel buttons or move to different feature
//        this.add(rightPanel, BorderLayout.EAST);
        this.add(bottomPanel, BorderLayout.SOUTH);

//        setSize(475, 300);
        setVisible(true);
    }

    private void showOnlyName()
    {
        TableModel tableModel = table.getModel();
        TableColumnModel tableColumnModel = table.getColumnModel();

//            System.out.println(tableModel.getColumnCount() + " : " + model.getColumnCount());
        allColumnModels.forEach(tableColumn ->
        {
            table.removeColumn(tableColumn);
            if (tableColumn.getHeaderValue().toString().equalsIgnoreCase("clean_name"))
            {
                table.addColumn(tableColumn);
            }
        });

        // Set
        tcm.setJTableColumnsWidth(table, tableWidth, 100);
//            tcm.showColumn("clean_name");
    }

    private void showColumns()
    {
        TableModel tableModel = table.getModel();
        TableColumnModel tableColumnModel = table.getColumnModel();

        // For debugging
//        var columnCount = tableColumnModel.getColumnCount();
//        System.out.println("Column Count: " + columnCount);
//        var modelColumnCount = tableModel.getColumnCount();
//        System.out.println("Table Column Count: " + modelColumnCount);
//        System.out.println("All Columns Size: " + allColumnModels.size());
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
            } else if (labelName.equals("clean_name"))
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
            } catch (IllegalArgumentException e)
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
            } else
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

        // For debugging
//        var columnCount = tableColumnModel.getColumnCount();
//        System.out.println("Column Count: " + columnCount);
//        var modelColumnCount = tableModel.getColumnCount();
//        System.out.println("Table Column Count: " + modelColumnCount);
//        System.out.println("All Columns Size: " + allColumnModels.size());
        // Helper method to size the visible columns for the default view
        tcm.setJTableColumnsWidth(table, tableWidth * 3,
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

            if (isSearchOptionsOpen)
            {
                this.searchOptionsDialog.setVisible(true);
            } else
            {
                this.searchOptionsDialog.setVisible(false);
            }
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
        searchOptionsDialog.setLocation(bounds.x + 50, bounds.y + 200);
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
//                    System.out.println("Enabling: " + value.toString() + " : " + allColumns.indexOf(value.toString()));
                } else
                {
                    this.searchColumnMap.put(value.toString(), -1);
//                    System.out.println("Disabling: " + value.toString() + " : " + allColumns.indexOf(value.toString()));
                }
            });

            try
            {
                item.setSelected(true);
            } catch (IllegalArgumentException e)
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
            searchOptionsCBList.forEach(jCheckBox ->
            {
                jCheckBox.setSelected(false);
            });
        });

        JButton selectAll = new JButton("Select All");
        selectAll.addActionListener(e ->
        {
            searchOptionsCBList.forEach(jCheckBox ->
            {
                jCheckBox.setSelected(true);
            });
        });

        JButton selectCoordinates = new JButton("Coord's & Name Only");
        selectCoordinates.addActionListener(e ->
        {
            searchOptionsCBList.forEach(jCheckBox ->
            {
                if (jCheckBox.getText().equals("clean_name")
                        || jCheckBox.getText().equals("center_lon")
                        || jCheckBox.getText().equals("center_lat"))
                {
                    jCheckBox.setSelected(true);
                } else
                {
                    jCheckBox.setSelected(false);
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

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                StringBuilder sb = new StringBuilder();

                // The lsm doesn't get the correct row # after the rows have been filtered
                // by the search text box
                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
//                System.out.println("firstIndex: " + firstIndex + " lastIndex: " + lastIndex);
                boolean isAdjusting = e.getValueIsAdjusting();
                TableColumnModel tcm = table.getColumnModel();

                Arrays.stream(table.getSelectedRows()).forEach(i ->
                {
//                    System.out.println(table.convertRowIndexToModel(i));
                });

                // Turns out we do need the full list of indices for the selected rows
                // as they could be non-continuous between the first and last after the table
                // has been filtered by the search text field
                selectedRowIndices = Arrays.stream(table.getSelectedRows())
                        .map(i -> table.convertRowIndexToModel(i))
                        .boxed()
                        .collect(Collectors.toList());

                ArrayList rows = new ArrayList();
                List specificRowData = new ArrayList();
                Map<String, Position> positions = new HashMap<>();

                sb.append("Event for indexes "
                        + firstIndex + " - " + lastIndex
                        + "; isAdjusting is " + isAdjusting
                        + "; selected indexes:");

                if (!lsm.isSelectionEmpty())
                {
                    // Find out which indexes are selected.
                    selectedRows = table.getSelectedRows();
                    int minIndex = selectedRowIndices.get(0);
                    int maxIndex = selectedRowIndices.get(selectedRowIndices.size() - 1);
                    System.out.println("minIndex: " + minIndex + " maxIndex: " + maxIndex);

                    if (getWwd().getModel().getLayers().getLayerByName(placemarksLayer.getName()) != null)
                    {
                        placemarksLayer.removeAllRenderables();
                        placemarksLayer.setEnabled(false);
                    }
                    for (int i : selectedRowIndices)
                    {
                        sb.append(" " + i);
                        Vector vector = model.getDataVector().elementAt(
                                table.convertRowIndexToModel(table.getSelectedRow()));
                        rows.add(vector);

                        labelName = (String) model.getValueAt(i, tcm.getColumnIndex("clean_name"));
                        String category = (String) model.getValueAt(i, tcm.getColumnIndex("Category"));

                        Double longitude = Double.valueOf(
                                String.valueOf(model.getValueAt(i, model.findColumn("center_lon"))));
                        Double latitude = Double.valueOf(
                                String.valueOf(model.getValueAt(i, model.findColumn("center_lat"))));
                        specificRowData.add(new Vector(Arrays.asList(labelName, category, longitude, latitude)));

                        Angle lat, lon;

                        if (longitude > 180.00)
                        {
                            lon = Angle.POS360.subtract(Angle.fromDegrees(longitude)).multiply(-1.0);
                        } else
                        {
                            lon = Angle.fromDegrees(longitude);
                        }

                        if (latitude > 90.00)
                        {
                            lat = Angle.POS180.subtract(Angle.fromDegrees(latitude)).multiply(-1.0);
                        } else
                        {
                            lat = Angle.fromDegrees(latitude);
                        }
                        Position position = new Position(lat, lon, 0);
                        positions.put(labelName, position);

                        pp = new PointPlacemark(position);

                        var attrs = new PointPlacemarkAttributes();
                        attrs.setLabelColor("ffffffff");
                        attrs.setImageAddress("images/pushpins/plain-red.png");
                        attrs.setLineMaterial(Material.RED);
                        attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
                        attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
                        attrs.setLineWidth(2d);
                        attrs.setScale(1.0);
                        String fullLabel = labelName + "\n"
                                + category + "\n"
                                + "lon: " + longitude.toString() + "\n"
                                + "lat: " + latitude.toString();
                        pp.setLabelText(labelName);

                        pp.setAttributes(attrs);
                        placemarksLayer.addRenderable(pp);

                        // Show or hide annotation layer
                        wwd.addSelectListener(new SelectListener()
                        {
                            @Override
                            public void selected(SelectEvent event)
                            {
                                if (event.getEventAction().equals(SelectEvent.HOVER))
                                { // there are many of these
                                    if (event.hasObjects())
                                    {
                                        if (event.getTopObject() instanceof PointPlacemark)
                                        {
                                            if(annotation == null)
                                            {
                                                makeAnnotation(position);
                                                wwd.redraw();
                                            }
                                            else if(annotation.getAttributes().isVisible())
                                            {
                                                annotation.getAttributes().setVisible(false);  
                                                wwd.redraw();
                                            }
                                            else if(!annotation.getAttributes().isVisible())
                                            {
                                                annotation.getAttributes().setVisible(true);  
                                                wwd.redraw();
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }

                var attrs = new BasicShapeAttributes();
                attrs.setOutlineWidth(2);
                attrs.setOutlineMaterial(new Material(Color.YELLOW));

                if (positions.size() > 0)
                {
                    wwd.getView().goTo(positions.entrySet().iterator().next().getValue(),
                            wwd.getView().getCurrentEyePosition().getElevation());
                }

                sb.append("\n");

                // Can uncomment for debugging purposes
//                System.out.println(sb);
//                rows.forEach(System.out::println);
//                specificRowData.forEach(System.out::println);
                placemarksLayer.setEnabled(true);
                getWwd().getModel().getLayers().add(placemarksLayer);
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

        @Override
        public void addSelectionInterval(int index0, int index1)
        {
            if (selectionEnabled)
            {
                super.addSelectionInterval(index0, index1);
            }
        }

    }

    private void makeAnnotation(Position pos)
    {
        String displayString = this.formatStatistics();
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
        this.annotation.getAttributes().setVisible(false);
        this.annotation.getAttributes().setDrawOffset(new Point(0, 30)); // use defaults
        this.annotation.setPosition(pos);
        this.annotation.getAttributes().setVisible(true);
        
        this.annotation.setText(displayString);
        this.placemarksLayer.addRenderable(this.annotation);
        wwd.redraw();
    }



    protected String formatStatistics()
    {    
        StringBuilder sb = new StringBuilder();
        double value;
        String s;
        
        String str = pp.getLabelText();
        s = String.format("Landmark: %s", pp.getLabelText());
        sb.append(s);
        
        // Latitude
        value = pp.getPosition().latitude.degrees;
        s = String.format("\nLatitude: %7.4f\u00B0", value);
        sb.append(s);
        
        // Longitude
        value = pp.getPosition().longitude.degrees;
        s = String.format("\nLongitude: %7.4f\u00B0", value);
        sb.append(s);
        
        return sb.toString();
    }

    /**
     * @return Instance of the custom renderable layer to use of our internal
     * layers
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
}
