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
        dateTimePicker = new DateTimePickerDialog(wwd, frame);
        dateTimePicker.setVisible(true);
        calendar = new GregorianCalendar();
        updatePosition();

        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                while (true)
                {
                    try
                    {
                        Thread.sleep(60000);
                    } catch (InterruptedException ignore)
                    {
                    }
                    calendar.setTime(dateTimePicker.getDate());
                    updatePosition();
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
        setDateTime();
        return position;
    }

    public synchronized void setDateTime()
    {
        calendar.setTime(dateTimePicker.getDate());
    }
}
