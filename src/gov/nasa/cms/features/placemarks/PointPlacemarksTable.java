/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.features.coordinates.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.PointPlacemark;

import javax.swing.table.*;
import java.util.*;
import java.util.stream.*;

import static gov.nasa.cms.features.coordinates.CMSUnitsFormat.FORMAT_DECIMAL_DEGREES;

public class PointPlacemarksTable extends AbstractTableModel
{

    private ArrayList<PointPlacemark> pointPlacemarkArrayList;
    private ArrayList<Map> mapArrayList;
    private static int idCount = 0;
    private CMSWWOUnitsFormat unitsFormat;

    private final String [] columnNames = new String[] {
        "Id", "Label", "Lat", "Long", "Elev", "UTMZone", "E", "N"
    };

    private final Class[] columnClass = new Class[] {
        Integer.class, String.class, Double.class, Boolean.class
    };

    public PointPlacemarksTable()
    {
        this.pointPlacemarkArrayList = new ArrayList<>();
        this.unitsFormat = new CMSWWOUnitsFormat();
        this.unitsFormat.setShowUTM(true);
        this.unitsFormat.setShowWGS84(false);
        this.mapArrayList = new ArrayList<>();
    }

    public void addEntry(PointPlacemark pm){

        // First add placemark to internal ArrayList
        pointPlacemarkArrayList.add(pm);

        // Then extract the properties of the placemark as strings for display in the table
        Position currentPosition = pm.getPosition();
        UTMCoord utm = UTMCoord.fromLatLon(currentPosition.getLatitude(), currentPosition.getLongitude());

        var id = String.valueOf(++idCount);
        var label = pm.getLabelText();

        var latitude = String.format(unitsFormat.getFormat(FORMAT_DECIMAL_DEGREES), currentPosition.getLatitude()).trim();
        var longitude = String.format(unitsFormat.getFormat(FORMAT_DECIMAL_DEGREES), currentPosition.getLongitude()).trim();
        var elevation = String.format(unitsFormat.terrainHeight(currentPosition.getElevation(),1.0)).trim();

        var zone = String.valueOf(utm.getZone());
        var easting = String.valueOf(utm.getEasting());
        var northing = String.valueOf(utm.getNorthing());

        Map<String, String> map = Stream.of(new String[][] {
            { "id", id },
            { "label", label },
            { "latitude", latitude },
            { "longitude", longitude },
            { "elevation", elevation },
            { "zone", zone },
            { "easting", easting },
            { "northing", northing }
        }).collect(Collectors.toMap(
            data -> data[0],
            data -> data[1]
        ));
        mapArrayList.add(map);
        System.out.println(idCount + " : " + mapArrayList.size());
    }

    public PointPlacemark getPlacemarkAtTrueIndex(int index){
        return pointPlacemarkArrayList.get(index);
    }

    public Map getEntry(int index){
        return mapArrayList.get(index);
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
        return mapArrayList.size();
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
        Map entry = mapArrayList.get(rowIndex);
        switch (columnIndex){
            case 0:
                return rowIndex + 1;
            case 1:
                return entry.get("id");
            case 2:
                return entry.get("latitude");
            case 3:
                return entry.get("longitude");
            case 4:
                return entry.get("elevation");
            case 5:
                return entry.get("zone");
            case 6:
                return entry.get("easting");
            case 7:
                return entry.get("northing");
        }
        return null;
    }

}
