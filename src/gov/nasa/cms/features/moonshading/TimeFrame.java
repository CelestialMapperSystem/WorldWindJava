/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;

/**
 *
 * @author kjdickin
 */
public class TimeFrame extends JDialog
{
    private JSlider timeFrameSlider;
    private JButton settingsButton;
    private JButton playPauseButton;
    private JLabel startDateTime;
    private JLabel endDateTime;
    private JLabel currentDateTime;
    
    private DateTimePickerDialog dateTimeDialog;
    private boolean isPlaySelected;
    private Vec4 sun;
    private Vec4 light;
    private RectangularNormalTessellator tessellator;
    private LensFlareLayer lensFlareLayer;
    private WorldWindow wwd;
    
    private Date startDate;
    private Date endDate;
   
    
    public TimeFrame(WorldWindow wwd, Component component, MoonShadingPanel panel)
    {
        this.wwd = wwd;
        sun = panel.getSun();
        light = panel.getLight();
        tessellator = panel.getTessellator();
        lensFlareLayer = panel.getLensFlareLayer();
                    
        GridBagConstraints gridBagConstraints;

        timeFrameSlider = new JSlider();
        settingsButton = new JButton();
        playPauseButton = new JButton();
        startDateTime = new JLabel();
        endDateTime = new JLabel();
        currentDateTime = new JLabel();
        
        //======== Dialog ========  
        this.setBackground(new Color(51, 51, 51));
        Rectangle bounds = component.getBounds();
        this.setLocation(bounds.x + 140, bounds.y + 720);
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        this.setOpacity((float) .70);
        this.getContentPane().setBackground(new Color(51, 51, 51));
        this.getContentPane().setLayout(new GridBagLayout());

        //======== Time Frame Slider ========  
        timeFrameSlider.setBackground(new Color(51, 51, 51));
        timeFrameSlider.setForeground(new Color(255, 255, 255));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 611;
        gridBagConstraints.ipady = 16;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(6, 1, 22, 0);
        this.getContentPane().add(timeFrameSlider, gridBagConstraints);

        //======== Settings ========  
        settingsButton.setForeground(new Color(255, 255, 255));
        settingsButton.setBorderPainted(false); 
        settingsButton.setContentAreaFilled(false); 
        settingsButton.setFocusPainted(false); 
        settingsButton.setOpaque(false);
        settingsButton.setIcon(new ImageIcon("cms-data/icons/settings-icon.png"));
        settingsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                if(dateTimeDialog == null)
                {
                    dateTimeDialog = new DateTimePickerDialog(wwd, component);             
                }
                dateTimeDialog.setVisible(true);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        this.getContentPane().add(settingsButton, gridBagConstraints);

        //======== Play/Pause ========  
        playPauseButton.setForeground(new Color(255, 255, 255));
        playPauseButton.setIcon(new ImageIcon("cms-data/icons/play-icon.png"));
        playPauseButton.setBorderPainted(false); 
        playPauseButton.setContentAreaFilled(false); 
        playPauseButton.setFocusPainted(false); 
        playPauseButton.setOpaque(false);
        isPlaySelected = false;
        playPauseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                isPlaySelected = !isPlaySelected;
                if(isPlaySelected) // Start animating
                {
                    playPauseButton.setIcon(new ImageIcon("cms-data/icons/pause-icon.png"));
                    dateTimeDialog.updatePosition();
                    LatLon sunPos = dateTimeDialog.getPosition();
                    
                    sun = wwd.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3(); 
                    light = sun.getNegative3();
                    
                    tessellator.setLightDirection(light);
                    lensFlareLayer.setSunDirection(sun);
                    
                    startDynamicShading();
                }
                else // Pause animation
                {
                    playPauseButton.setIcon(new ImageIcon("cms-data/icons/play-icon.png"));
                }             
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        this.getContentPane().add(playPauseButton, gridBagConstraints);

        //======== Start Date/Time ========  
        startDateTime.setForeground(new Color(255, 255, 255));
        startDateTime.setText("Start Time");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(22, 0, 22, 0);
        this.getContentPane().add(startDateTime, gridBagConstraints);

        //======== End Date/Time ========  
        endDateTime.setForeground(new Color(255, 255, 255));
        endDateTime.setText("End Time");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(22, 6, 22, 6);
        this.getContentPane().add(endDateTime, gridBagConstraints);

        //======== Current Date/Time ========  
        currentDateTime.setForeground(new Color(255, 255, 255));
        currentDateTime.setText("Current Time");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(16, 370, 0, 0);
        this.getContentPane().add(currentDateTime, gridBagConstraints);

        this.pack();
    }
    
    protected void startDynamicShading()
    {
        startDate = dateTimeDialog.getStartDate(); // Get the start date
        endDate = dateTimeDialog.getEndDate(); // Get the end date
        Calendar cal = dateTimeDialog.getCalendar(); // Get the calendar from DateTimePickerDialog
        dateTimeDialog.getCalendar().setTime(startDate); // Set the calendar time to the start date time
        int value = dateTimeDialog.getDuration(); //speed of animation 

        // Start a new thread to display dynamic shading
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // While the end date/time is after the calendar date/time
                while (endDate.after(dateTimeDialog.getCalendar().getTime()))
                {
                    try
                    {
                        //dateTimeDialog.getCalendar().add(Calendar.SECOND, (int)(totalTime/duration)); // Increment calendar for animation duration
                        dateTimeDialog.getCalendar().add(Calendar.HOUR, 1); // Increment calendar
                        startDate.setTime(cal.getTimeInMillis()); // Set the start time to the new calendar time
                        dateTimeDialog.updatePosition(); // Update the position from DateTimePickerDialog
                        LatLon sunPos = dateTimeDialog.getPosition();  // Get the new LatLon sun position
                        sun = wwd.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3(); // Set the sun position from the LatLon                    
                        light = sun.getNegative3();

                        // Change the tesselator and lensFalreLayer according to new light and sun direction
                        tessellator.setLightDirection(light);
                        lensFlareLayer.setSunDirection(sun);
                        
                        Thread.sleep(value*1000); //animation speed
                        wwd.redraw();
                    } catch (InterruptedException ignore)
                    {
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
