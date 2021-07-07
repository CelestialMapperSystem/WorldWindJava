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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
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

    private JSlider timeFrameSlider = new JSlider();
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
    private Date currentDate;

    private JLabel jan = new JLabel("Jan");
    private JLabel feb = new JLabel("Feb");
    private JLabel mar = new JLabel("Mar");
    private JLabel apr = new JLabel("Apr");
    private JLabel may = new JLabel("May");
    private JLabel jun = new JLabel("Jun");
    private JLabel jul = new JLabel("Jul");
    private JLabel aug = new JLabel("Aug");
    private JLabel sep = new JLabel("Sep");
    private JLabel oct = new JLabel("Oct");
    private JLabel nov = new JLabel("Nov");
    private JLabel dec = new JLabel("Dec");

    public TimeFrame(WorldWindow wwd, Component component, MoonShadingPanel panel)
    {

        dateTimeDialog = new DateTimePickerDialog(wwd, component);
        this.initMonthSlider(); // Initialize the slider with months in white text
        this.wwd = wwd;
        sun = panel.getSun();
        light = panel.getLight();
        tessellator = panel.getTessellator();
        lensFlareLayer = panel.getLensFlareLayer();
        startDate = dateTimeDialog.getStartDate();
        endDate = dateTimeDialog.getEndDate();

        GridBagConstraints gridBagConstraints;

        settingsButton = new JButton();
        playPauseButton = new JButton();
        startDateTime = new JLabel();
        endDateTime = new JLabel();
        currentDateTime = new JLabel();

        //======== Dialog ========  
        this.setBackground(new Color(51, 51, 51));
        Rectangle bounds = component.getBounds();
        this.setLocation(bounds.x + 400, bounds.y + 900);
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        this.setOpacity((float) .70);
        this.getContentPane().setBackground(Color.BLACK);
        this.getContentPane().setLayout(new GridBagLayout());

        //======== Time Frame Slider ========  
        timeFrameSlider.setBackground(Color.BLACK);
        timeFrameSlider.setForeground(new Color(255, 255, 255));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 811;
        gridBagConstraints.ipady = 16;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(6, 1, 22, 0);
        this.getContentPane().add(timeFrameSlider, gridBagConstraints);

        //======== Settings ========  
        settingsButton.setForeground(new Color(255, 255, 255));
        settingsButton.setBorderPainted(false);
        settingsButton.setToolTipText("Select start/end date and time for simulation");
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setOpaque(false);
        settingsButton.setIcon(new ImageIcon("cms-data/icons/settings-icon.png"));
        settingsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
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
        playPauseButton.setToolTipText("Play or pause the simulation");
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
                if (isPlaySelected) // Start animating
                {
                    startDate = dateTimeDialog.getStartDate();
                    endDate = dateTimeDialog.getEndDate();
                    
                    setStartEndTime(); // Update start and end labels
                    changeSlider(); // Update time frame slider

                    dateTimeDialog.setVisible(false);
                    playPauseButton.setIcon(new ImageIcon("cms-data/icons/pause-icon.png"));
                    dateTimeDialog.updatePosition();
                    LatLon sunPos = dateTimeDialog.getPosition();

                    sun = wwd.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
                    light = sun.getNegative3();

                    tessellator.setLightDirection(light);
                    lensFlareLayer.setSunDirection(sun);

                    startDynamicShading();
                } else // Pause animation
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
        int durationTime = dateTimeDialog.getDuration(); //speed of animation 
        LocalDate localDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); //start date
        int month = localDate.getMonthValue(); //month value from start date
        int day = localDate.getDayOfMonth();//day of month from start date
        int time = startDate.toInstant().atZone(ZoneId.systemDefault()).getHour();
        long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());//difference between start and end date in milliseconds

        // Start a new thread to display dynamic shading
        Thread thread;
        thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                int currentMonth = month;
                int currentDay = day;
                int currentHour = time + 1;
                //incrementation of shading
                int shadingInterval;
                int num = 0;//humber of days/hours/months to update by
                int value = 0;//current day,month,hour 
                
                // While the end date/time is after the calendar date/time
                while (endDate.after(dateTimeDialog.getCalendar().getTime()))
                {                   
                    try
                    {
                        //changeSlider();
                        if (!isPlaySelected)
                        {
                            break;
                        }
                        //if difference is less than a week
                        if (diffInMillies < 6.048e+8)
                        {
                            shadingInterval = Calendar.HOUR;
                            num = 1;//1 hour
                            currentHour++;
                            if(currentHour > 23){
                                currentHour = 0;
                            }
                            value = currentHour; // Shading happens once before going into this function
                            timeFrameSlider.setValue(value);
                            wwd.redraw();

                        } //if the difference is less than a month
                        else if (diffInMillies < 2.628e+9)
                        {
                            shadingInterval = Calendar.DAY_OF_MONTH;
                            num = 1;//1 day
                            currentDay++;
                            currentDay = currentDay % 31; //tentative, will mod by 30 or 28 or 31 dependent on month
                            value = currentDay;
                            timeFrameSlider.setValue(value);
                        } else
                        {
                            //if the difference is greater than a month
                            shadingInterval = Calendar.MONTH;
                            num = 1;//1 month
                            currentMonth++;
                            value = currentMonth;
                            timeFrameSlider.setValue(value);
                            if(currentMonth == 12)
                            {
                                currentMonth = 0;
                            }
                        }
                        //changeSlider();
                        
                        dateTimeDialog.getCalendar().add(shadingInterval, num); // Increment calendar by month,day,or hour
                        startDate.setTime(cal.getTimeInMillis()); // Set the start time to the new calendar time

                        changeCurrentTime(); // Make sure to update current time
                        
                        dateTimeDialog.updatePosition(); // Update the position from DateTimePickerDialog
                        LatLon sunPos = dateTimeDialog.getPosition();  // Get the new LatLon sun position
                        sun = wwd.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3(); // Set the sun position from the LatLon                    
                        light = sun.getNegative3();

                        // Change the tesselator and lensFalreLayer according to new light and sun direction
                        tessellator.setLightDirection(light);
                        lensFlareLayer.setSunDirection(sun);

                        Thread.sleep(durationTime * 1000); //animation speed
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

    protected void changeSlider()
    {
        startDate = dateTimeDialog.getStartDate(); // Get the start date
        endDate = dateTimeDialog.getEndDate(); // Get the end date
        long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());//difference between start and end date in milliseconds
        LocalDate localDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); //start date
        int month = localDate.getMonthValue(); //month value from start date
        JLabel[] dayLabels=new JLabel[32];
        JLabel[] hourLabels = new JLabel[25];
        Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();//table for JLabels

                        

        // If start - end is less than a week, change time frame to show hours
        if (diffInMillies < 6.048e+8)
        {
            timeFrameSlider.setPaintTicks(true);
            timeFrameSlider.setPaintLabels(true);
            timeFrameSlider.setMajorTickSpacing(1);
            table = new Hashtable<Integer, JLabel>();

            if (dateTimeDialog.get24HourClockButton().isSelected())
            {        
                timeFrameSlider.setMinimum(0);
                timeFrameSlider.setMaximum(23);                
                for (int x = 0; x < 24; x++) // Display Military time
                {
                    hourLabels[x] = new JLabel();
                    
                    hourLabels[x].setForeground(Color.WHITE);
                    hourLabels[x].setText(x + ":00");
                    table.put(x, hourLabels[x]);
                    timeFrameSlider.setLabelTable(table);
                }
            }
            else // Display Universal time
            {           
                initHourSlider();
            }         
        }
        
        // If start - end is less than a month, represent days
        if (diffInMillies < 2.628e+9 && diffInMillies>=6.048e+8)
        {            
            //time frame from 1-31
            if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12)
            {
                timeFrameSlider.setMinimum(1);
                timeFrameSlider.setMaximum(31); 
                timeFrameSlider.setPaintTicks(true);
                timeFrameSlider.setPaintLabels(true);
                timeFrameSlider.setMajorTickSpacing(1);
                table = new Hashtable<Integer, JLabel>();
                for (int x = 1; x < 32; x++)
                {
                    dayLabels[x]=new JLabel();
                    dayLabels[x].setForeground(Color.WHITE);
                    dayLabels[x].setText(x+"");
                    table.put(x, dayLabels[x]);
                    timeFrameSlider.setLabelTable(table);
                }
                
            }
            //time frame from 1-30
            if (month == 4 || month == 6 || month == 9 || month == 11)
            {
                timeFrameSlider.setMinimum(1);
                timeFrameSlider.setMaximum(30); 
                timeFrameSlider.setPaintTicks(true);
                timeFrameSlider.setPaintLabels(true);
                timeFrameSlider.setMajorTickSpacing(1);
                table = new Hashtable<Integer, JLabel>();
                for (int x = 1; x < 31; x++)
                {
                    dayLabels[x]=new JLabel();
                    dayLabels[x].setForeground(Color.WHITE);
                    dayLabels[x].setText(x+"");
                    table.put(x, dayLabels[x]);
                    timeFrameSlider.setLabelTable(table);
                }
                
            }

            //time frame from 1-28
            if (month == 2)
            {
                timeFrameSlider.setPaintTicks(true);
                timeFrameSlider.setPaintLabels(true);
                timeFrameSlider.setMajorTickSpacing(1);
                table = new Hashtable<Integer, JLabel>();
                timeFrameSlider.setMinimum(1);
                timeFrameSlider.setMaximum(28); 
                for (int x = 1; x < 29; x++)
                {
                    dayLabels[x]=new JLabel();
                    dayLabels[x].setForeground(Color.WHITE);
                    dayLabels[x].setText(x + "");
                    table.put(x, dayLabels[x]);
                    timeFrameSlider.setLabelTable(table);
                }
                
            }
        }

        //if we need time frame to represent months
        if (diffInMillies >= 2.628e+9)
        {
            timeFrameSlider.setPaintTicks(true);
            timeFrameSlider.setPaintLabels(true);
            timeFrameSlider.setLabelTable(null);
            timeFrameSlider.setMajorTickSpacing(1);
            table = new Hashtable<Integer, JLabel>();
            timeFrameSlider.setMinimum(1);
            timeFrameSlider.setMaximum(12);
            table.put(1, jan);
            table.put(2, feb);
            table.put(3, mar);
            table.put(4, apr);
            table.put(5, may);
            table.put(6, jun);
            table.put(7, jul);
            table.put(8, aug);
            table.put(9, sep);
            table.put(10, oct);
            table.put(11, nov);
            table.put(12, dec);
            timeFrameSlider.setLabelTable(table);
        }

    }
    
    // Sets the start and end date/time to either military or universal
    private void setStartEndTime()
    {                  
        if(dateTimeDialog.get12HourClockButton().isSelected())
        {
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm aa");
            
            startDateTime.setText(df.format(startDate));          
            endDateTime.setText(df.format(endDate));
            
        }
        else // Military time
        {
            String startLabel = startDate.toString();                 
            startDateTime.setText(startLabel);

            String endLabel = endDate.toString();
            endDateTime.setText(endLabel);
        }
    }
    
    // Modifies the current date/time to change with the shading animation to either military or universal
    private void changeCurrentTime()
    {
        if(dateTimeDialog.get12HourClockButton().isSelected())
        {
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm aa");
            
            currentDate = startDate;
            currentDateTime.setText(df.format(currentDate));
            
        }
        else // Military time
        {
            currentDate = startDate;
            String currentLabel = currentDate.toString();
            currentDateTime.setText(currentLabel);
        }
    }
    
    private void initHourSlider()
    {
        JLabel[] labels = new JLabel[25];
        labels[1] = new JLabel("12:00");
        labels[2] = new JLabel("1:00");
        labels[3] = new JLabel("2:00");
        labels[4] = new JLabel("3:00");
        labels[5] = new JLabel("4:00");
        labels[6] = new JLabel("5:00");
        labels[7]= new JLabel("6:00");
        labels[8] = new JLabel("7:00");
        labels[9] = new JLabel("8:00");
        labels[10] = new JLabel("9:00");
        labels[11] = new JLabel("10:00");
        labels[12] = new JLabel("11:00");
        labels[13] = new JLabel("12:00");
        labels[14] = new JLabel("1:00");
        labels[15] = new JLabel("2:00");
        labels[16] = new JLabel("3:00");
        labels[17] = new JLabel("4:00");
        labels[18] = new JLabel("5:00");
        labels[19] = new JLabel("6:00");
        labels[20] = new JLabel("7:00");
        labels[21] = new JLabel("8:00");
        labels[22] = new JLabel("9:00");
        labels[23] = new JLabel("10:00");
        labels[24] = new JLabel("11:00");
        
        Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();//table for JLabels
        timeFrameSlider.setPaintTicks(true);
        timeFrameSlider.setPaintLabels(true);
        timeFrameSlider.setMajorTickSpacing(1);
        timeFrameSlider.setMinimum(1);
        timeFrameSlider.setMaximum(24);
        
        for(int i = 1; i <= 24; i++)
        {
            table.put(i, labels[i]);
            labels[i].setForeground(Color.WHITE);
        }
        timeFrameSlider.setLabelTable(table);       
    }
    
    protected void initMonthSlider()
    {
        jan.setForeground(Color.WHITE);
        feb.setForeground(Color.WHITE);
        mar.setForeground(Color.WHITE);
        apr.setForeground(Color.WHITE);
        may.setForeground(Color.WHITE);
        jun.setForeground(Color.WHITE);
        jul.setForeground(Color.WHITE);
        aug.setForeground(Color.WHITE);
        sep.setForeground(Color.WHITE);
        oct.setForeground(Color.WHITE);
        nov.setForeground(Color.WHITE);
        dec.setForeground(Color.WHITE);

        Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();//table for JLabels
        timeFrameSlider.setMinimum(1);
        timeFrameSlider.setMaximum(12);
        timeFrameSlider.setLabelTable(null);
        timeFrameSlider.setPaintTicks(true);
        timeFrameSlider.setPaintLabels(true);
        timeFrameSlider.setMajorTickSpacing(1);
        table = new Hashtable<Integer, JLabel>();
        table.put(1, jan);
        table.put(2, feb);
        table.put(3, mar);
        table.put(4, apr);
        table.put(5, may);
        table.put(6, jun);
        table.put(7, jul);
        table.put(8, aug);
        table.put(9, sep);
        table.put(10, oct);
        table.put(11, nov);
        table.put(12, dec);
        timeFrameSlider.setLabelTable(table);

    }
}
