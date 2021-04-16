package gov.nasa.cms;

import gov.nasa.worldwind.Configuration;

import javax.swing.JFrame;
import java.nio.file.*;

/**
 * @author kjdickin
 */
public class CelestialMappingSystem
{
    public static final String APP_NAME = "Celestial Mapping System";

    public static void main(String[] args)
    {
        // Set the WorldWind Configuration document to be overriden by CMS properties
        System.setProperty("gov.nasa.worldwind.app.config.document",
            "gov/nasa/cms/config/cmsConfiguration.xml");

        // Set the WorldWind Configuration document to be overriden by CMS properties
        if (Configuration.isMacOS())
        {
            System.setProperty(
                "com.apple.mrj.application.apple.menu.about.name", APP_NAME);
        }

        try
        {
            CelestialMapper cms = new CelestialMapper();
            cms.initialize();
            cms.setTitle(APP_NAME);
            cms.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(() -> {
                cms.setVisible(true);
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
