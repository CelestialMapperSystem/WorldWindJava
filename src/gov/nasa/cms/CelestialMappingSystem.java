package gov.nasa.cms;

import gov.nasa.worldwind.Configuration;
import javax.swing.JFrame;
import java.nio.file.*;

/**
 *
 * @author kjdickin
 */
public class CelestialMappingSystem 
{   
    public static final String APP_NAME = "Celestial Mapping System";

    public static void main(String[] args) 
    {  
        // Set the WorldWind Configuration document to be overriden by CMS properties

        // This is using a local cacerts file that incorporates the rsa cert
        // that kaitlyn provided
        // use this option to the JVM to see verbose output on whether we're
        // able to connect or not:
        // -Djavax.net.debug=ssl,keymanager
        System.setProperty("gov.nasa.worldwind.app.config.document", "gov/nasa/cms/config/cmsConfiguration.xml");
        Path path = Paths.get("C:\\Users\\gknorman\\cms-main\\WorldWindJava"
            + "-cms\\security\\cacerts");
        System.setProperty ("javax.net.ssl.trustStore", String.valueOf(path));
        System.setProperty ("javax.net.ssl.trustStorePassword", "changeit");


        if (Configuration.isMacOS()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
        }

        try {
            CelestialMapper cms = new CelestialMapper();
            cms.initialize();
            cms.setTitle(APP_NAME);
            cms.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(() -> {
                cms.setVisible(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
