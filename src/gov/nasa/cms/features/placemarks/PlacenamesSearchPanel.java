/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
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

        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        this.makePanel(mainPanel);
    }

    private void makePanel(JPanel mainPanel)
    {
        jtf = new JTextField(15);
        searchLbl = new JLabel("Search");

        String[] columnNames = placemarkSearchData.getHeaders();

        // The 0 argument is number rows.
        model = new DefaultTableModel(columnNames,0);
        sorter = new TableRowSorter<>(model);
        table = new JTable(model);
        table.setRowSorter(sorter);
        this.setLayout(new FlowLayout(FlowLayout.CENTER));


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
        this.add(searchLbl);
        this.add(jtf);
        this.add(jsp);

        setSize(475, 300);
        setVisible(true);
    }
}
