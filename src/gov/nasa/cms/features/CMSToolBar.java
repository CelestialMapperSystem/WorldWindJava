/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features;

import gov.nasa.cms.*;
import gov.nasa.cms.features.layermanager.LayerManagerDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class CMSToolBar
{
    private final CelestialMapper frame;

    // TODO - Create ENUM to hold various button names and any properties
    private final String LAYER_MANAGER = "Layer Manager";
    private final String MEASUREMENTS = "Measurements";
    private final String COORDINATES = "Coordinates";
    private final String PROFILER = "Profiler";
    private final String SIGHT_LINES = "Sight Lines";
    private boolean isLayerManagerOpen;

    public CMSToolBar(CelestialMapper frame)
    {
        this.frame = frame;
    }

    public void createToolbar() {
        // The original constructor in worldwindow.ToolBarImpl relies completely
        // on an XML based configuration and initialization.
        // Will attempt to create a new GradientToolBar() object without requiring the
        // same process
//        this.createMouseListener();
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new GridLayout(1, 5));
        toolBar.setRollover(false);
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);

//        toolBar.addSeparator(new Dimension(150, 0));

        ArrayList<JButton> buttons = new ArrayList<>(5);
        buttons.add(new JButton(LAYER_MANAGER));
        buttons.add(new JButton(MEASUREMENTS));
        buttons.add(new JButton(COORDINATES));
        buttons.add(new JButton(PROFILER));
        buttons.add(new JButton(SIGHT_LINES));

        try
        {
            initializeButtons(buttons);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        buttons.forEach(toolBar::add);
        this.frame.getContentPane().add(toolBar,BorderLayout.PAGE_START);

    }

    private void initializeButtons(ArrayList<JButton> buttons) throws IOException
    {
        for (JButton button: buttons)
        {
            button.setPreferredSize(new Dimension(50,80));
            button.setFocusPainted(false);

            button.setHorizontalTextPosition(AbstractButton.CENTER);
            button.setVerticalTextPosition(AbstractButton.BOTTOM);

            String buttonText = button.getText();

             switch (buttonText){
                 case LAYER_MANAGER:
                     setButtonIcon("cms-data/icons/icons8-layers-48.png",button);
                     button.addMouseListener(createToolbarButtonMouseListener());
                 case MEASUREMENTS:
                     setButtonIcon("cms-data/icons/icons8-layers-48.png",button);
                 case COORDINATES:
                     setButtonIcon("cms-data/icons/icons8-layers-48.png",button);
                 case PROFILER:
                     setButtonIcon("cms-data/icons/icons8-layers-48.png",button);
                 case SIGHT_LINES:
                     setButtonIcon("cms-data/icons/icons8-layers-48.png",button);
             }
        }
    }

    private void setButtonIcon(String path, AbstractButton button) throws IOException
    {
        button.setIcon(new ImageIcon(ImageIO.read(new File(path))));
    }

    public void showLayerManager(){
        {
            this.isLayerManagerOpen = !frame.isLayerManagerOpen();
            if (isLayerManagerOpen)
            {
                if (frame.getLayerManager() == null)
                {
                    frame.setLayerManager(new LayerManagerDialog(frame.getWwd(), frame));
                }
                frame.getLayerManager().setVisible(true);
            }
            else
            {
                frame.getLayerManager().setVisible(false);
            }
        };
    }

    public MouseListener createToolbarButtonMouseListener(){
        MouseListener mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLayerManager();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
        return mouseListener;
    }

}
