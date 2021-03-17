/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features;

import gov.nasa.cms.*;
import gov.nasa.cms.features.coordinates.CoordinatesDialog;
import gov.nasa.cms.features.layermanager.LayerManagerDialog;
import gov.nasa.cms.features.LineOfSightController;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class CMSToolBar
{
    private final CelestialMapper frame;
    
    private boolean isLayerManagerOpen = false;
    private boolean isMeasureDialogOpen = false;
    private boolean isCoordinatesDialogOpen = false;
    private boolean isProfilerOpen = false;
    private boolean isLineOfSightOpen = false;
    private boolean isLandingSitesOpen;

//    static enum BUTTON {
//        LAYER_MANAGER("Layer Manager"),
//        MEASUREMENTS("Measurements"),
//        COORDINATES("Coordinates"),
//        PROFILER("Profiler"),
//        SIGHT_LINES("Sight Lines"),
//        LANDING_SITES("Landing Sites");
//
//        private JButton jButton;
//        private final String name;
//
//        BUTTON(String s) {
//            this.name = s;
//            this.jButton = new JButton(s);
//        }
//    }

    public CMSToolBar(CelestialMapper frame)
    {
        this.frame = frame;
    }

    public void createToolbar() {
        // The original constructor in worldwindow.ToolBarImpl relies completely
        // on an XML based configuration and initialization.
        // Will attempt to create a new GradientToolBar() object without requiring the
        // same process
        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new GridLayout(1, 5));
        toolBar.setRollover(false);
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);

//        toolBar.addSeparator(new Dimension(150, 0));

        ArrayList<JButton> buttons = new ArrayList<>(5);
        buttons.add(new JButton("Layer Manager"));
        buttons.add(new JButton("Measurements"));
        buttons.add(new JButton("Coordinates"));
        buttons.add(new JButton("Profiler"));
        buttons.add(new JButton("Sight Lines"));
        buttons.add(new JButton("Landing Sites"));

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
//            System.out.println(buttonText);
            // Strange, why does .getName() return null even though
            // ...oh it's calling the getName method from Jbutton not from the Enum.
//            System.out.println(button.getName());

            // Due to weird issues with the original Switch/Case code block here
            // Where the button was set according to the string value of it's name
            // I had to encapsulate everything in an Enum and resort to using this
            // convoluted If / Else tree to make sure that this first button
            // wasn't being given multiple ActionListeners AND that each button was
            //
            if (button.getText().equals("Layer Manager"))
            {
//                System.out.println(buttonText + " = LAYER_MANAGER: " + buttonText.equals(BUTTON.LAYER_MANAGER.name));
                setButtonIcon("cms-data/icons/icons8-layers-48.png", button);
                button.addActionListener((ActionEvent ev) -> {
                    showLayerManager();
                });
            }
            else if (button.getText().equals("Measurements"))
            {
//                System.out.println(buttonText + " = MEASUREMENTS: " + buttonText.equals(BUTTON.MEASUREMENTS.name));
                     setButtonIcon("cms-data/icons/icons8-measurement-tool-48.png",button);
                     button.addActionListener((ActionEvent e) -> {
                         showMeasureTool();
                     });
            }
            else if (button.getText().equals("Coordinates"))
            {
                setButtonIcon("cms-data/icons/icons8-grid-48.png", button);
                button.addActionListener((ActionEvent e) -> showCoordinatesDialog());

            }
            else if (button.getText().equals("Profiler"))
            {
                setButtonIcon("cms-data/icons/icons8-bell-curve-48.png", button);
                button.addActionListener(e -> showProfiler());

            }
            else if (button.getText().equals("Sight Lines"))
            {
                setButtonIcon("cms-data/icons/icons8-head-profile-48.png", button);
                button.addActionListener(e -> showLineOfSight());
            } else if (button.getText().equals("Landing Sites"))
            {
                setButtonIcon("cms-data/icons/icons8-launchpad-48.png", button);
                button.addActionListener(e -> showLandingSites());
            }
        }
    }

    private void showLandingSites()
    {
        this.isLandingSitesOpen = !isLandingSitesOpen;
        if(isLandingSitesOpen){
            if(frame.getLandingSites() == null){
                frame.setLandingSites(new ApolloDialog(frame.getWwd(),frame));
            }
            frame.getLandingSites().setVisible(true);
        } else {
            frame.getLandingSites().setVisible(false);
        }
    }

    private void showLineOfSight()
    {
        this.isLineOfSightOpen = !isLineOfSightOpen;
        if(isLineOfSightOpen){
            if(frame.getLineOfSight() == null){
                frame.setLineOfSight(new LineOfSightController(frame, frame.getWwd()));
            }
            frame.getLineOfSight().setVisible(true);
        } else {
            frame.getLineOfSight().setVisible(false);
        }
    }

    private void showProfiler()
    {
        this.isProfilerOpen = !isProfilerOpen;
        if(isProfilerOpen){
            if(frame.getProfile() == null){
                frame.setProfile(new CMSProfile(frame.getWwd(), frame));
            }
            frame.getProfile().setVisible(true);
        } else {
            frame.getProfile().setVisible(false);
        }
    }

    private void setButtonIcon(String path, AbstractButton button) throws IOException
    {
        button.setIcon(new ImageIcon(ImageIO.read(new File(path))));
    }

    private void showCoordinatesDialog()
    {
        this.isCoordinatesDialogOpen = !isCoordinatesDialogOpen;
        if(isCoordinatesDialogOpen){
            if(frame.getCoordinatesDialog() == null)
            {
                frame.setCoordinatesDialog(new CoordinatesDialog(frame.getWwd(), frame));
            }
            frame.getCoordinatesDialog().setVisible(true);
        }
        else
        {
            frame.getCoordinatesDialog().setVisible(false);
        }
    }

    public void showLayerManager(){
        {
            this.isLayerManagerOpen = !isLayerManagerOpen;
            if (isLayerManagerOpen)
            {
                if (frame.getLayerManager() == null)
                {
                    frame.setLayerManager(new LayerManagerDialog(frame.getWwd(), frame));
                }
                frame.getLayerManager().setVisible(true);
//                frame.setLayerManagerisOpen(true);
            }
            else
            {
                frame.getLayerManager().setVisible(false);
//                frame.setLayerManagerisOpen(false);
            }
        };
    }

    public void showMeasureTool(){
        {

            this.isMeasureDialogOpen = !isMeasureDialogOpen;
            if (isMeasureDialogOpen)
            {
                // Only open if the MeasureDialog has never been opened
                if (frame.getMeasureDialog() == null)
                {
                    // Create the dialog from the WorldWindow, MeasureTool and AppFrame
                    frame.setMeasureDialog(new MeasureDialog(frame.getWwd(), frame.getMeasureTool(), frame));
                }
                // Display on screen
                frame.getMeasureDialog().setVisible(true);
//                frame.setMeasureDialogOpen(true);
            }
            else // Hide the dialog
            {
                frame.getMeasureDialog().setVisible(false);
//                frame.setMeasureDialogOpen(false);
            }
        }
    }

//    public MouseListener createToolbarButtonMouseListener(){
//        MouseListener mouseListener = new MouseListener() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                showLayerManager();
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//
//            }
//        };
//        return mouseListener;
//    }

}
