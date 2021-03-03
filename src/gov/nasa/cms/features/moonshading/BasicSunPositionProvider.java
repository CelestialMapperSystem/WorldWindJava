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
    private WorldWindow wwd;
    private CelestialMapper frame;
    private DateTimePickerDialog dateTimePicker;

    
    public BasicSunPositionProvider()
    {
        
        
        

        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        
                        
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

    //updates the current date and time and the position based on current date time
    public synchronized void updateCurrentDateTime()
    {
        calendar=new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        position = SunCalculator.subsolarPoint(calendar);
    }

    //updates date and time and position based on dateTimePicker
    public synchronized void updateDateTime()
            
    {
        calendar = dateTimePicker.getCalendar();
        calendar.setTime(dateTimePicker.getDate());
        System.out.println(dateTimePicker.getDate());
        position = SunCalculator.subsolarPoint(calendar);
    }

    //returns position
    public synchronized LatLon getPosition()
    {
        return position;
    }
}
