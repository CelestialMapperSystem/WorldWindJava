/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import gov.nasa.worldwind.WorldWindow;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
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
    private JLabel startDateTime;
    private JLabel endDateTime;
    private JLabel currentDateTime;
   
    
    public TimeFrame(WorldWindow wwd, Component component)
    {
        //this.setSize(400, 60);
        this.setMaximumSize(new Dimension(400, 100)); // Why won't these change?
        this.setLocation(640, 770);
        this.setUndecorated(true);
        this.getRootPane().setOpaque(false);
        this.getContentPane().setBackground(Color.darkGray);
        this.setOpacity(0.6F);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
  
        GridBagConstraints gridBagConstraints;
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        timeFrameSlider = new JSlider();
        settingsButton = new JButton();
        settingsButton.setToolTipText("Date/Time Settings");
        startDateTime = new JLabel();
        endDateTime = new JLabel();
        currentDateTime = new JLabel();
        
        this.getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 250;
        gridBagConstraints.ipady = 40;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 19, 0);
        timeFrameSlider.setBackground(Color.darkGray);
        this.getContentPane().add(timeFrameSlider, gridBagConstraints);

        settingsButton.setBorderPainted(false); 
        settingsButton.setContentAreaFilled(false); 
        settingsButton.setFocusPainted(false); 
        settingsButton.setOpaque(false);
        settingsButton.setIcon(new ImageIcon("cms-data/icons/settings-icon.png"));
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
        gridBagConstraints.insets = new java.awt.Insets(30, 10, 0, 10);
        this.getContentPane().add(settingsButton, gridBagConstraints);

        startDateTime.setText("Start Time");
        startDateTime.setForeground(Color.WHITE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(49, 19, 19, 0);
        this.getContentPane().add(startDateTime, gridBagConstraints);

        endDateTime.setText("End Time");
        endDateTime.setForeground(Color.WHITE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(49, 20, 19, 10);
        this.getContentPane().add(endDateTime, gridBagConstraints);

        currentDateTime.setText("Current Time");
        currentDateTime.setForeground(Color.WHITE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 200, 0, 0);
        this.getContentPane().add(currentDateTime, gridBagConstraints);

        this.pack();
    }
}
