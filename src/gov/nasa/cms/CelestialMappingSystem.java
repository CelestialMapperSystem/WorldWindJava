package gov.nasa.cms;

import gov.nasa.cms.features.MoonElevationModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import javax.swing.JFrame;

/**
 * Main class to run the Celestial Mapping System Application.
 * @author kjdickin
 */
public class CelestialMappingSystem
{
    public static final String APP_NAME = "Celestial Mapping System";
    private static MoonElevationModel elevationModel;
    private static WorldWindow wwd;

    public static void main(String[] args)
    {
        // Set the WorldWind Configuration document to be overriden by CMS properties
        System.setProperty("gov.nasa.worldwind.app.config.document", "gov/nasa/cms/config/cmsConfiguration.xml");
        
        try
        {
            // Initialize CelestialMapper
            CelestialMapper cms = new CelestialMapper();
            cms.initialize();

            // Skip the initial animation if the user supplies a command line argument
            if (args.length > 0 && args[0].equals("-skipInitialAnimation"))
            {
                wwd = cms.getWwd();
                elevationModel = new MoonElevationModel(wwd, false);
                                           
            } else
            {
                elevationModel = new MoonElevationModel(wwd, true);
            }
            
            if (Configuration.isMacOS())
            {
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
            }

            cms.setTitle(APP_NAME);
            cms.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(() ->
            {
                cms.setVisible(true);
            });
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
