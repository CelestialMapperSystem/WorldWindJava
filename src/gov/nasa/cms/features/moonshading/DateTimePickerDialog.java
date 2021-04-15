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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
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
    private JSpinner endDateTime;
    private JLabel endDateLabel;
    private JLabel startDateLabel;
    private JLabel animationSpeedLabel;
    private JSpinner startDateTime;
    private JRadioButton clock24Hour;
    private JRadioButton clock12Hour;
    private Calendar calendar;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SpinnerModel model;
    
    private LatLon position;
    Date startDateToConvert;
    Date endDateToConvert;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int animationDuration;

    public DateTimePickerDialog(WorldWindow wwdObject, Component component)
    {
        this.setSize(400, 250);
        this.setTitle("Date/Time Picker");
        this.setAlwaysOnTop(true);
        GridBagConstraints gridBagConstraints;

        startDateLabel = new JLabel();
        endDateLabel = new JLabel();
        endDateTime = new JSpinner();
        animationSpeedLabel = new JLabel();
        animationSpeedSlider = new JSlider();
        clock24Hour = new JRadioButton("24-hour clock");
        clock12Hour = new JRadioButton("12-hour clock");
        animationDuration = 2; // Default 
        startDateToConvert = new Date();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(new java.awt.Point(645, 180));
        getContentPane().setLayout(new GridBagLayout());

        //======== Start Date Time ========  
        startDateLabel.setText("Start date/time: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(6, 6, 0, 0);
        getContentPane().add(startDateLabel, gridBagConstraints);
        
        model = new SpinnerDateModel();
        calendar = new GregorianCalendar();
        startDate = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
        startDateToConvert = Date.from(startDate.atZone(java.time.ZoneOffset.systemDefault()).toInstant());
        model.setValue(startDateToConvert);
        startDateTime = new JSpinner(model);
        startDateTime.setToolTipText("Select a start date/time");
        startDateTime.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                startDateToConvert = (Date) ((JSpinner) e.getSource()).getValue(); // Set date from the JSpinner user input
                startDate = startDateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                calendar.set(startDate.getYear(), startDate.getMonthValue()-1, startDate.getDayOfMonth(),
                             startDate.getHour(), startDate.getMinute(), startDate.getSecond());
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
        endDateLabel.setText("End date/time:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 14;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(6, 6, 0, 0);
        getContentPane().add(endDateLabel, gridBagConstraints);

        endDateTime.setModel(new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        endDateTime.setToolTipText("Select a end date/time");
        endDate = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
        endDateToConvert = Date.from(endDate.atZone(java.time.ZoneOffset.systemDefault()).toInstant());
        endDateTime.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                endDateToConvert = (Date) ((JSpinner) e.getSource()).getValue(); // Set date from the JSpinner user input
                endDate = endDateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                calendar.set(endDate.getYear(), endDate.getMonthValue()-1, endDate.getDayOfMonth(),
                             endDate.getHour(), endDate.getMinute(), endDate.getSecond());
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
        animationSpeedLabel.setText("Animation Speed: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 0, 0);
        getContentPane().add(animationSpeedLabel, gridBagConstraints);

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
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 164;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 0, 6);
        getContentPane().add(animationSpeedSlider, gridBagConstraints);
        
         //======== Universal Time ========  
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 0, 0);
        getContentPane().add(clock12Hour, gridBagConstraints);
        
         //======== Military Time ========  
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 0, 0);
        getContentPane().add(clock24Hour, gridBagConstraints);
        
        // Put in a button group so user can only enable one at a time
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(clock12Hour);
        buttonGroup.add(clock24Hour);
        clock24Hour.setSelected(true);

    }
    
    public JRadioButton get24HourClockButton()
    {
        return clock24Hour;
    }
    
    public JRadioButton get12HourClockButton()
    {
        return clock12Hour;
    }
    
    public LocalDateTime getStartDate()
    {
        return startDate;
    }
    
    public LocalDateTime getEndDate()
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
        calendar.set(startDate.getYear(), startDate.getMonthValue()-1, startDate.getDayOfMonth());
        return position;
    }
    
    public int getDuration(){
        return this.animationDuration;
    }
}


