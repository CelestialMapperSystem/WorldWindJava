/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Creates a date time picker that allows users to enter a start date & time, end date & time
 * and animation speed to simulate the moon's shading over a period of time. 
 * @see gov.nasa.cms.MoonShadingPanel
 * @author kjdickin
 */
public class DateTimePickerDialog extends JDialog
{

    private JSlider animationSpeedSlider;
    private JButton applyChangesButton;
    private JSpinner endDateTime;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JSpinner startDateTime;
    private Calendar calendar;
    private Date startDate;
    private Date endDate;
    private SpinnerDateModel model;
    
    private LatLon position;
    private Date startTime;
    private Date endTime;
    private int animationDuration;

    public DateTimePickerDialog(WorldWindow wwdObject, Component component)
    {
        this.setSize(400, 180);
        this.setTitle("Date/Time Picker");
        this.setAlwaysOnTop(true);
        GridBagConstraints gridBagConstraints;

        jLabel2 = new JLabel();
        jLabel1 = new JLabel();
        endDateTime = new JSpinner();
        jLabel3 = new JLabel();
        animationSpeedSlider = new JSlider();
        applyChangesButton = new JButton();
        animationDuration = 2; // Default 

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(new java.awt.Point(645, 180));
        getContentPane().setLayout(new GridBagLayout());

        //======== Start Date Time ========  
        jLabel2.setText("Start date/time: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(6, 6, 0, 0);
        getContentPane().add(jLabel2, gridBagConstraints);
        
        model = new SpinnerDateModel();
        calendar = new GregorianCalendar();
        startDate = calendar.getTime();
        model.setValue(startDate);
        startDateTime = new JSpinner(model);
        startDateTime.setToolTipText("Select a start date/time");
        startDateTime.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                startDate = (Date) ((JSpinner) e.getSource()).getValue(); // Set date from the JSpinner user input
                calendar.setTime(startDate); // Set calendar time from the date
                startTime=startDate;
            }
        });
   
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 21;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(8, 6, 0, 0);
        getContentPane().add(startDateTime, gridBagConstraints);

        //======== End Date Time ========  
        jLabel1.setText("End date/time:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(6, 6, 0, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

        endDateTime.setModel(new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        endDateTime.setToolTipText("Select a end date/time");
        endDateTime.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                endDate = (Date) ((JSpinner) e.getSource()).getValue(); // Set date from the JSpinner user input
                endTime = endDate;
            }
        });
              
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 21;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(8, 6, 0, 0);
        getContentPane().add(endDateTime, gridBagConstraints);

        //======== Animation Speed ========  
        jLabel3.setText("Animation Speed: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 0, 0);
        getContentPane().add(jLabel3, gridBagConstraints);

        animationSpeedSlider = new JSlider(0,5,2);
        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.setPaintLabels(true);
        animationSpeedSlider.setMajorTickSpacing(1);
        // Action listener for the animation speed
       animationSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                 animationDuration = animationSpeedSlider.getValue();
            }
       });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 164;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 0, 6);
        getContentPane().add(animationSpeedSlider, gridBagConstraints);

    }
    
    public Date getStartDate()
    {
        return startDate;
    }
    
    public Date getEndDate()
    {
        return endDate;
    }
    
    public Calendar getCalendar()
    {
        return calendar;
    }
    
    public synchronized void updatePosition()
    {
        position = SunCalculator.subsolarPoint(calendar);
    }
    
    public synchronized LatLon getPosition()
    {
        calendar.setTime(startDate);
        return position;
    }
    
    public int getDuration(){
        return this.animationDuration;
    }
}


