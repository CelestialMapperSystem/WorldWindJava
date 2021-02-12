/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features.moonshading;

import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
/**
 *
 * @author hniyer
 */
public class MainWindow extends JFrame implements PropertyChangeListener{
    private static final long serialVersionUID=1L;
    private static MainWindow window;
    JFormattedTextField textField = new JFormattedTextField(DateFormat.getDateInstance(DateFormat.SHORT));

public MainWindow(){
    
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(368,362);
    setTitle("DateTime Picker");
    
    Container cp= getContentPane();
    FlowLayout flowLayout = new FlowLayout();
    
    cp.setLayout(flowLayout);
    cp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
    
    textField.setValue(new Date());
    textField.setPreferredSize(new Dimension(130,30));
    //add UI controls to ContentPane
    cp.add(textField);
    
    JButton calButton= new JButton("Pick a Date");
    cp.add(calButton);
    
    CalendarWindow calendarWindow = new CalendarWindow();
    calendarWindow.setUndecorated(true);
    calendarWindow.addPropertyChangeListener(this);
    
    calButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e)
        {
            calendarWindow.setLocation(textField.getLocationOnScreen().x,(textField.getLocationOnScreen().y+textField.getHeight()));
            
            Date selectedDate=(Date)textField.getValue();
            calendarWindow.resetSelection(selectedDate);
            calendarWindow.setVisible(true);
        }
    });
}

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals("selectedDate"))
        {
            java.util.Calendar cal = (java.util.Calendar)event.getNewValue();
             Date selDate=cal.getTime();
             
             textField.setValue(selDate);
        }
    }
    public static void main(String[] args){
        window = new MainWindow();
        window.setVisible(true);
    }
}
