/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.avlist.AVKey;

/**
 *
 * @author kjdickin
 */
public class Moon extends EllipsoidalGlobe
{
    public static final double WGS84_EQUATORIAL_RADIUS = 1737400; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 1737400; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.0; // eccentricity squared, semi-major axis


    public static final double ELEVATION_MIN = -9000; // Depth of Antoniadi Crater
    public static final double ELEVATION_MAX = 10700; // Height of Selean Summit.

    public Moon()
    {
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES,
            EllipsoidalGlobe.makeElevationModel(AVKey.MOON_ELEVATION_MODEL_CONFIG_FILE,
                "cms-data/layers/LROElevation.xml"));
    }

    public String toString()
    {
        return "Moon";
    }
}
