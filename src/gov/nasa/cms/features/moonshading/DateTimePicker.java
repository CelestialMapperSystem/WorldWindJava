/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import gov.nasa.worldwind.WorldWindow;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

/**
 *
 * @author kjdickin
 */
public class DateTimePicker extends JPanel
{
     private WorldWindow wwd;
     private static JFrame frame;
      public DateTimePicker(boolean cycleMonths) 
      {
        super(new SpringLayout());

        String[] labels = {"Month: ","Day:", "Year: ", "Time: "};
        int numPairs = labels.length;
        Calendar calendar = Calendar.getInstance();
        JFormattedTextField ftf = null;

        //Add the first label-spinner pair.
        String[] monthStrings = getMonthStrings(); //get month names
        SpinnerListModel monthModel = null;
        if (cycleMonths) { //use custom model
            monthModel = new CyclingSpinnerListModel(monthStrings);
        } else { //use standard model
            monthModel = new SpinnerListModel(monthStrings);
        }
        JSpinner spinner = addLabeledSpinner(this,
                                             labels[0],
                                             monthModel);
        //Tweak the spinner's formatted text field.
        ftf = getTextField(spinner);
        if (ftf != null ) {
            ftf.setColumns(8); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
        
        
        //Add second label-spinner pair
        int currentDay= calendar.get(Calendar.DAY_OF_MONTH);
        SpinnerModel dayModel = new SpinnerNumberModel(currentDay, //initial value
                                       1, //min
                                       31, //max
                                       1);                //step
        //If we're cycling, hook this model up to the month model.
        if (monthModel instanceof CyclingSpinnerListModel) {
            ((CyclingSpinnerListModel)monthModel).setLinkedModel(dayModel);
        }
        spinner = addLabeledSpinner(this, labels[1], dayModel);
        //Make the year be formatted without a thousands separator.
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));

        
        
        //Add third label-spinner pair.
        int currentYear = calendar.get(Calendar.YEAR);
        SpinnerModel yearModel = new SpinnerNumberModel(currentYear, //initial value
                                       currentYear - 100, //min
                                       currentYear + 100, //max
                                       1);                //step
        //If we're cycling, hook this model up to the month model.
        if (monthModel instanceof CyclingSpinnerListModel) {
            ((CyclingSpinnerListModel)monthModel).setLinkedModel(yearModel);
        }
        spinner = addLabeledSpinner(this, labels[2], yearModel);
        //Make the year be formatted without a thousands separator.
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        
        
        //Add the fourth label-spinner pair.
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        SpinnerModel dateModel = new SpinnerDateModel(initDate,
                                     earliestDate,
                                     latestDate,
                                     Calendar.YEAR);//ignored for user input
        spinner = addLabeledSpinner(this, labels[3], dateModel);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "hh:mm"));

        //Lay out the panel.
        SpringUtilities.makeCompactGrid(this,
                                        numPairs, 2, //rows, cols
                                        10, 10,        //initX, initY
                                        50, 10);       //xPad, yPad
    }

       /**
     * DateFormatSymbols returns an extra, empty value at the
     * end of the array of months.  Remove it.
     */
    protected static String[] getMonthStrings() {
        String[] months = new java.text.DateFormatSymbols().getMonths();
        int lastIndex = months.length - 1;

        if (months[lastIndex] == null
           || months[lastIndex].length() <= 0) { //last item empty
            String[] monthStrings = new String[lastIndex];
            System.arraycopy(months, 0,
                             monthStrings, 0, lastIndex);
            return monthStrings;
        } else { //last item not empty
            return months;
        }
    }
    
     protected static JSpinner addLabeledSpinner(Container c,
                                                String label,
                                                SpinnerModel model) {
        JLabel l = new JLabel(label);
        c.add(l);

        JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);

        return spinner;
    }
     
     /**
     * Return the formatted text field used by the editor, or
     * null if the editor doesn't descend from JSpinner.DefaultEditor.
     */
    public JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        } else {
            System.err.println("Unexpected editor type: "
                               + spinner.getEditor().getClass()
                               + " isn't a descendant of DefaultEditor");
            return null;
        }
    }
    
    public static void createAndShowGUI() 
    {
        //Create and set up the window.
        if(frame == null)
        {
          frame = new JFrame("Date/Time Picker");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

          //Add content to the window.
          frame.add(new DateTimePicker(false));

          // Offset the bounds to appear underneath Moon Shading dialog
          Rectangle bounds = frame.getBounds();
          frame.setLocation(bounds.x + 352, bounds.y + 555);

          //Display the window.
          frame.pack();
          frame.setVisible(true);
        }
    }
    
    public static JFrame getFrame()
    {
         return frame;
    }

}
