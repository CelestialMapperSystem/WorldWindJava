/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.placemarks;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.*;

/**
 * @author : gknorman - 4/5/2021, Monday
 * Based on this example: http://www.java2s.com/Tutorial/Java/0240__Swing/SettingColumnHeaderToolTipsinaJTableComponents.htm
 *
 **/
public class ColumnHeaderToolTips extends MouseMotionAdapter {
    TableColumn curCol;
    Map tips = new HashMap();
    public void setToolTip(TableColumn col, String tooltip) {
        if (tooltip == null) {
            tips.remove(col);
        } else {
            tips.put(col, tooltip);
        }
    }
    public void mouseMoved(MouseEvent evt) {
        JTableHeader header = (JTableHeader) evt.getSource();
        JTable table = header.getTable();
        TableColumnModel colModel = table.getColumnModel();
        int vColIndex = colModel.getColumnIndexAtX(evt.getX());
        TableColumn col = null;
        if (vColIndex >= 0) {
            col = colModel.getColumn(vColIndex);
        }
        if (col != curCol) {
            header.setToolTipText((String) tips.get(col));
            curCol = col;
        }
    }
}
