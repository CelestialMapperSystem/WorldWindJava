package gov.nasa.cms.features.moonshading;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Michael de Hoog
 * @version $Id: BasicSunPositionProvider.java 10406 2009-04-22 18:28:45Z
 * patrickmurris $
 */
public class BasicSunPositionProvider implements SunPositionProvider
{

    private LatLon position;
    private Calendar calendar;
    private DateTimePickerDialog dateTimePicker;
    private WorldWindow wwd;
    private CelestialMapper frame;
    
    public BasicSunPositionProvider()
    {
        // Create and open the DateTimePickerDialog
        dateTimePicker = new DateTimePickerDialog(wwd, frame); 
        dateTimePicker.setVisible(true);
        
        // Set the calendar equal to DateTimePickerDialog's calendar
        calendar = dateTimePicker.getCalendar();

        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        updatePosition();
                        Thread.sleep(10); // Sleep for a small period of time to update the globe quickly
                    } catch (InterruptedException ignore)
                    {
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void updateDateTime()
    {
        calendar.setTime(dateTimePicker.getDate());
        updatePosition();
    }

    private synchronized void updatePosition()
    {
        position = SunCalculator.subsolarPoint(calendar);
    }

    public synchronized LatLon getPosition()
    {
        calendar.setTime(dateTimePicker.getDate());
        return position;
    }
}
