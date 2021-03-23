/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import gov.nasa.worldwind.WorldWindow;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
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
    private JLabel startDateTime;
    private JLabel endDateTime;
    private JLabel currentDateTime;
   
    
    public TimeFrame(WorldWindow wwd, Component component)
    {
        this.setSize(400, 120);
        this.setLocation(580, 800);
        this.setUndecorated(true);
        this.getRootPane ().setOpaque (false);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
  
        GridBagConstraints gridBagConstraints;
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        timeFrameSlider = new JSlider();
        settingsButton = new JButton();
        startDateTime = new JLabel();
        endDateTime = new JLabel();
        currentDateTime = new JLabel();
        
        this.getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 309;
        gridBagConstraints.ipady = 43;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 19, 0);
        this.getContentPane().add(timeFrameSlider, gridBagConstraints);

        settingsButton.setText("Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
               
            }
        });
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 86, 0, 10);
        this.getContentPane().add(settingsButton, gridBagConstraints);

        startDateTime.setText("startTime");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(49, 19, 19, 0);
        this.getContentPane().add(startDateTime, gridBagConstraints);

        endDateTime.setText("endTime");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(49, 97, 19, 10);
        this.getContentPane().add(endDateTime, gridBagConstraints);

        currentDateTime.setText("currentTime");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 281, 0, 0);
        this.getContentPane().add(currentDateTime, gridBagConstraints);

        this.pack();
    }
}
