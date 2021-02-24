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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.WindowConstants;

/**
 *
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
    private LatLon position;
    private SunPositionProvider spp = new BasicSunPositionProvider();
    private Vec4 sun, light;
      private RectangularNormalTessellator tessellator;
    private LensFlareLayer lensFlareLayer;
    private AtmosphereLayer atmosphereLayer;
    private MoonShadingPanel wwd;
  
    
    public DateTimePickerDialog(WorldWindow wwdObject, Component component)
    {
        
        this.setSize(400, 240);
        this.setTitle("Date/Time Picker");
        GridBagConstraints gridBagConstraints;

        jLabel2 = new JLabel();
        startDateTime = new JSpinner();
        jLabel1 = new JLabel();
        endDateTime = new JSpinner();
        jLabel3 = new JLabel();
        animationSpeedSlider = new JSlider();
        applyChangesButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(new java.awt.Point(633, 180));
        getContentPane().setLayout(new GridBagLayout());

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

        startDateTime.setModel(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        startDateTime.setToolTipText("Select a start date/time");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 21;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(8, 6, 0, 0);
        getContentPane().add(startDateTime, gridBagConstraints);

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
        endDateTime.setToolTipText("Select a start date/time");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 21;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(8, 6, 0, 0);
        getContentPane().add(endDateTime, gridBagConstraints);

        jLabel3.setText("Animation Speed: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 0, 0);
        getContentPane().add(jLabel3, gridBagConstraints);

        // Action listener for the animation speed
        animationSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
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

        applyChangesButton.setText("OK");
        // Action listener for the OK button (apply changes and start animating)
        
        applyChangesButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {   //converts start date and time to string
                //creates date object with start date and time
                Object start = startDateTime.getValue();
                    if (start instanceof Date) {
                        Date startDateTime = (Date)start;
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
                        String startTime = format.format(startDateTime);
                        System.out.println(startTime);
                        Date newDate=new Date(startTime); 
                        calendar.clear();
                        calendar.setTime(newDate);
                        LatLon sunPos=spp.changePosition();
                        sun = wwd.getWwd().getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
                        light = sun.getNegative3();
                        tessellator.setLightDirection(light);
                        lensFlareLayer.setSunDirection(sun);
                        atmosphereLayer.setSunDirection(sun);
                        
                    }
                  //converts end date and time to string
                  //creates date object with end date and time
                Object end = endDateTime.getValue();
                    if (end instanceof Date) {
                        Date endDateTime = (Date)end;
                        SimpleDateFormat format2 = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
                        String endTime = format2.format(endDateTime);
                        Date newDate2=new Date(endTime); 
                 
                //calls changeTimeAndDate() from BasicSunPositionProvider to update position
                //with position variable, can use same format as done previously for absolute checkbox in MoonShadingPanel
            }
                    
            }
        });
        
       
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(35, 6, 6, 0);
        getContentPane().add(applyChangesButton, gridBagConstraints);

        
       
       
          
    }
    
}
