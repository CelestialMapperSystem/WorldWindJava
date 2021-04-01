/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.util.TableColumnManager;
import gov.nasa.worldwind.WorldWindow;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.RoundingMode;
import java.text.*;
import java.util.ArrayList;

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

    public PlacenamesSearchPanel(WorldWindow wwd, CelestialMapper celestialMapper)
    {
        super(new BorderLayout());
        this.wwd = wwd;
        this.cms = celestialMapper;
        this.placemarkSearchData = new PlacemarkSearchData(wwd,cms);

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

        int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2);
        int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2);
        table.setPreferredScrollableViewportSize(new Dimension(width,height));
        TableColumnManager tcm = new TableColumnManager(table);
        this.setLayout(new BorderLayout());


        placemarkSearchData.getRowList().forEach( o -> {
            Object [] data = ((ArrayList) o).toArray();
            model.addRow(data);
        });

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

        this.add(searchField, BorderLayout.NORTH);
        this.add(jsp, BorderLayout.CENTER);

//        setSize(475, 300);
        setVisible(true);
    }
}
