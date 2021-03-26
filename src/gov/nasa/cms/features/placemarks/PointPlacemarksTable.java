/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.worldwind.render.PointPlacemark;

import javax.swing.table.*;
import java.util.ArrayList;

public class PointPlacemarksTable extends AbstractTableModel
{

    private ArrayList<PointPlacemark> pointPlacemarkArrayList;
    private final String [] columnNames = new String[] {
        "Id", "Label", "Lat", "Long", "Elev", "UTMZone", "E", "N"
    };

    private final Class[] columnClass = new Class[] {
        Integer.class, String.class, Double.class, Boolean.class
    };

    public PointPlacemarksTable(ArrayList<PointPlacemark> pointPlacemarkArrayList)
    {
        this.pointPlacemarkArrayList = pointPlacemarkArrayList;
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    @Override
    public int getRowCount()
    {
        return pointPlacemarkArrayList.size();
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column)
    {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        return columnClass[columnIndex];
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param rowIndex    the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        PointPlacemark pm = pointPlacemarkArrayList.get(rowIndex);
        switch (columnIndex){
            case 0:
                return rowIndex + 1;
            case 1:
                return pm.getLabelText();
            case 2:
                return pm.getPosition().getLatitude();
            case 3:
                return pm.getPosition().getLongitude();
            case 4:
                return pm.getPosition().getElevation();
        }
        return null;
    }

}
