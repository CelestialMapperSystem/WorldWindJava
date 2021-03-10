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
public class MoonFlat extends FlatGlobe
{
    public static final double WGS84_EQUATORIAL_RADIUS = 1737400; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 1737400; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.0; // eccentricity squared, semi-major axis

    public MoonFlat()
    {
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES,
            EllipsoidalGlobe.makeElevationModel(AVKey.MOON_ELEVATION_MODEL_CONFIG_FILE,
                "cms-data/layers/MoonElevationModel.xml"));
    }

    public String toString()
    {
        return "Flat Moon";
    }
}
