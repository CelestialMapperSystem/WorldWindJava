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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
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
        this.initSlider(); // Initialize the slider with months in white text
        dateTimeDialog = new DateTimePickerDialog(wwd, component);
        this.wwd = wwd;
        sun = panel.getSun();
        light = panel.getLight();
        tessellator = panel.getTessellator();
        lensFlareLayer = panel.getLensFlareLayer();

        GridBagConstraints gridBagConstraints;

        settingsButton = new JButton();
        playPauseButton = new JButton();
        startDateTime = new JLabel();
        endDateTime = new JLabel();
        currentDateTime = new JLabel();

        //======== Dialog ========  
        this.setBackground(new Color(51, 51, 51));
        Rectangle bounds = component.getBounds();
        this.setLocation(bounds.x + 140, bounds.y + 700);
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
        gridBagConstraints.ipadx = 611;
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
                    String startLabel = startDate.toString();
                    startDateTime.setText(startLabel);

                    String endLabel = endDate.toString();
                    endDateTime.setText(endLabel);

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
        int value = dateTimeDialog.getDuration(); //speed of animation 
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
                int currentMonth=month-1;
                int currentDay=day-1;
                int currentHour=time-1;
                //incrementation of shading
                int shadingInterval;
                int num=0;//humber of days/hours/months to update by
                int value=0;//current day,month,hour 
                
                // While the end date/time is after the calendar date/time
                while (endDate.after(dateTimeDialog.getCalendar().getTime()))
                {
                    
                    try
                    {
                        if (!isPlaySelected)
                        {
                            break;
                        }
                        if(diffInMillies<6.048e+8){
                            shadingInterval=Calendar.HOUR;
                            num=1;//1 hour
                            currentHour++;
                            currentHour=currentHour%24;//wraps hours around
                            value=currentHour;
                            
                        }
                        else if(diffInMillies<2.628e+9){
                            shadingInterval=Calendar.HOUR;
                            num=24;//1 day
                            currentDay++;
                            currentDay=currentDay%31; //tentative, will mod by 30 or 28 or 31 dependent on month
                            value=currentDay;
                        }
                        else{
                            shadingInterval=Calendar.MONTH;
                            num=1;//1 month
                            currentMonth++;
                            currentMonth=currentMonth%12; //wraps the month around
                            value=currentMonth;
                        }
                        timeFrameSlider.setValue(value);//sets value to be month,day, or hour
                        dateTimeDialog.getCalendar().add(shadingInterval, num); // Increment calendar by month,day,or hour
                        startDate.setTime(cal.getTimeInMillis()); // Set the start time to the new calendar time
                        
                        // Update the current date 
                        currentDate = startDate;
                        String currentLabel = currentDate.toString();
                        currentDateTime.setText(currentLabel);

                        dateTimeDialog.updatePosition(); // Update the position from DateTimePickerDialog
                        LatLon sunPos = dateTimeDialog.getPosition();  // Get the new LatLon sun position
                        sun = wwd.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3(); // Set the sun position from the LatLon                    
                        light = sun.getNegative3();

                        // Change the tesselator and lensFalreLayer according to new light and sun direction
                        tessellator.setLightDirection(light);
                        lensFlareLayer.setSunDirection(sun);

                        Thread.sleep(value * 1000); //animation speed
                        wwd.redraw();
//                        currentMonth++;
//                        currentMonth=currentMonth%12;
                    } catch (InterruptedException ignore)
                    {
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    protected void initSlider()
    {
        
        startDate = dateTimeDialog.getStartDate(); // Get the start date
        endDate = dateTimeDialog.getEndDate(); // Get the end date
        long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());//difference between start and end date in milliseconds
        
        
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

        timeFrameSlider = new JSlider(0, 11, 0);
        timeFrameSlider.setPaintTicks(true);
        timeFrameSlider.setPaintLabels(true);
        timeFrameSlider.setMajorTickSpacing(1);
        Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
        table.put(0, jan);
        table.put(1, feb);
        table.put(2, mar);
        table.put(3, apr);
        table.put(4, may);
        table.put(5, jun);
        table.put(6, jul);
        table.put(7, aug);
        table.put(8, sep);
        table.put(9, oct);
        table.put(10, nov);
        table.put(11, dec);
        timeFrameSlider.setLabelTable(table);
        
    }
}
