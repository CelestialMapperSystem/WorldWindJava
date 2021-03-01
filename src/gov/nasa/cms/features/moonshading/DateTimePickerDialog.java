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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
    private Date date;
    private SpinnerDateModel model;
    private MoonShadingPanel moonShading;
    private BasicSunPositionProvider spp;
   // Date date2 = new GregorianCalendar(2014, Calendar.JUNE, 12, 2, 1).getTime();

    public DateTimePickerDialog(WorldWindow wwdObject, Component component)
    {
        this.setSize(400, 240);
        this.setTitle("Date/Time Picker");
        GridBagConstraints gridBagConstraints;

        jLabel2 = new JLabel();
        //startDateTime = new JSpinner();
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
        
        model = new SpinnerDateModel();
       // calendar = new GregorianCalendar();
        calendar = new GregorianCalendar(2017, 6, 3, 24, 1);
        date = calendar.getTime();
        model.setValue(date);
        startDateTime = new JSpinner(model);
        startDateTime.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                date = (Date) ((JSpinner) e.getSource()).getValue();
                calendar.setTime(date);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm");
                System.out.println(df.format(date)); 
            }
        });

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
            {
               // spp.updateDateTime();
               // moonShading.update();
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
//                System.out.println(df.format(startDateTime.getValue()));
            }

        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(35, 6, 6, 0);
        getContentPane().add(applyChangesButton, gridBagConstraints);

    }
    
    public Date getDate()
    {
        return date;
    }
    
    public Calendar getCalendar()
    {
        return calendar;
    }

}
