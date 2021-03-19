/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.util.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import java.util.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author dcollins
 * @version $Id: SurfaceEllipse.java 2406 2014-10-29 23:39:29Z dcollins $
 */
public class SurfaceEllipse extends AbstractSurfaceShape implements Exportable
{

    protected static final int MIN_NUM_INTERVALS = 8;
    protected static final int DEFAULT_NUM_INTERVALS = 32;

    protected LatLon center = LatLon.ZERO;
    protected double majorRadius;
    protected double minorRadius;
    protected Angle heading = Angle.ZERO;
    private int intervals = DEFAULT_NUM_INTERVALS;

    protected boolean extrude;
    protected boolean followTerrain; // true if altitude mode indicates terrain following
    protected int altitudeMode = WorldWind.ABSOLUTE;

    /**
     * Constructs a new surface ellipse with the default attributes, default
     * center location, default radii, and default heading.
     */
    public SurfaceEllipse()
    {
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public SurfaceEllipse(SurfaceEllipse source)
    {
        super(source);

        this.center = source.center;
        this.majorRadius = source.majorRadius;
        this.minorRadius = source.minorRadius;
        this.heading = source.heading;
        this.intervals = source.intervals;
    }

    /**
     * Constructs a new surface ellipse with the specified normal (as opposed to
     * highlight) attributes, default center location, default radii, and
     * default heading. Modifying the attribute reference after calling this
     * constructor causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case
     * default attributes are used.
     */
    public SurfaceEllipse(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    /**
     * Constructs a new surface ellipse with the default attributes, the
     * specified center location and radii (in meters).
     *
     * @param center the ellipse's center location.
     * @param majorRadius the ellipse's major radius, in meters.
     * @param minorRadius the ellipse's minor radius, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if either
     * radii is negative.
     */
    public SurfaceEllipse(LatLon center, double majorRadius, double minorRadius)
    {
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (majorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", majorRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (minorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", majorRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Constructs a new surface ellipse with the default attributes, the
     * specified center location, radii (in meters), and heading clockwise from
     * North.
     *
     * @param center the ellipse's center location.
     * @param majorRadius the ellipse's major radius, in meters.
     * @param minorRadius the ellipse's minor radius, in meters.
     * @param heading the ellipse's heading, clockwise from North.
     *
     * @throws IllegalArgumentException if the center or heading are null, or if
     * either radii is negative.
     */
    public SurfaceEllipse(LatLon center, double majorRadius, double minorRadius, Angle heading)
    {
        this(center, majorRadius, minorRadius);

        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heading = heading;
    }

    /**
     * Constructs a new surface ellipse with the default attributes, the
     * specified center location, radii (in meters), heading clockwise from
     * North, and initial number of geometry intervals.
     *
     * @param center the ellipse's center location.
     * @param majorRadius the ellipse's major radius, in meters.
     * @param minorRadius the ellipse's minor radius, in meters.
     * @param heading the ellipse's heading, clockwise from North.
     * @param intervals the initial number of intervals (or slices) defining the
     * ellipse's geometry.
     *
     * @throws IllegalArgumentException if the center or heading are null, if
     * either radii is negative, or if the number of intervals is less than 8.
     */
    public SurfaceEllipse(LatLon center, double majorRadius, double minorRadius, Angle heading, int intervals)
    {
        this(center, majorRadius, minorRadius, heading);

        if (intervals < MIN_NUM_INTERVALS)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", intervals);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.intervals = intervals;
    }

    /**
     * Constructs a new surface ellipse with the specified normal (as opposed to
     * highlight) attributes, the specified center location, and radii (in
     * meters). Modifying the attribute reference after calling this constructor
     * causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case
     * default attributes are used.
     * @param center the ellipse's center location.
     * @param majorRadius the ellipse's major radius, in meters.
     * @param minorRadius the ellipse's minor radius, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if either
     * radii is negative.
     */
    public SurfaceEllipse(ShapeAttributes normalAttrs, LatLon center, double majorRadius, double minorRadius)
    {
        super(normalAttrs);

        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (majorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", majorRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (minorRadius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", majorRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Constructs a new surface ellipse with the specified normal (as opposed to
     * highlight) attributes, the specified center location, radii (in meters),
     * and heading clockwise from North. Modifying the attribute reference after
     * calling this constructor causes this shape's appearance to change
     * accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case
     * default attributes are used.
     * @param center the ellipse's center location.
     * @param majorRadius the ellipse's major radius, in meters.
     * @param minorRadius the ellipse's minor radius, in meters.
     * @param heading the ellipse's heading, clockwise from North.
     *
     * @throws IllegalArgumentException if the center or heading are null, or if
     * either radii is negative.
     */
    public SurfaceEllipse(ShapeAttributes normalAttrs, LatLon center, double majorRadius, double minorRadius,
            Angle heading)
    {
        this(normalAttrs, center, majorRadius, minorRadius);

        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heading = heading;
    }

    /**
     * Constructs a new surface ellipse with the specified normal (as opposed to
     * highlight) attributes, the specified center location, radii (in meters),
     * heading clockwise from North, and initial number of geometry intervals.
     * Modifying the attribute reference after calling this constructor causes
     * this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case
     * default attributes are used.
     * @param center the ellipse's center location.
     * @param majorRadius the ellipse's major radius, in meters.
     * @param minorRadius the ellipse's minor radius, in meters.
     * @param heading the ellipse's heading, clockwise from North.
     * @param intervals the initial number of intervals (or slices) defining the
     * ellipse's geometry.
     *
     * @throws IllegalArgumentException if the center or heading are null, if
     * either radii is negative, or if the number of intervals is less than 8.
     */
    public SurfaceEllipse(ShapeAttributes normalAttrs, LatLon center, double majorRadius, double minorRadius,
            Angle heading, int intervals)
    {
        this(normalAttrs, center, majorRadius, minorRadius, heading);

        if (intervals < MIN_NUM_INTERVALS)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", intervals);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.intervals = intervals;
    }

    public LatLon getCenter()
    {
        return this.center;
    }

    public void setCenter(LatLon center)
    {
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.onShapeChanged();
    }

    public double getMajorRadius()
    {
        return this.majorRadius;
    }

    public double getMinorRadius()
    {
        return this.minorRadius;
    }

    public void setMajorRadius(double radius)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.majorRadius = radius;
        this.onShapeChanged();
    }

    public void setMinorRadius(double radius)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minorRadius = radius;
        this.onShapeChanged();
    }

    public void setRadii(double majorRadius, double minorRadius)
    {
        this.setMajorRadius(majorRadius);
        this.setMinorRadius(minorRadius);
    }

    public Angle getHeading()
    {
        return this.heading;
    }

    public void setHeading(Angle heading)
    {
        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heading = heading;
        this.onShapeChanged();
    }

    public int getIntervals()
    {
        return this.intervals;
    }

    public void setIntervals(int intervals)
    {
        if (intervals < MIN_NUM_INTERVALS)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", intervals);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.intervals = intervals;
        this.onShapeChanged();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to include the globe's state key in the returned state key.
     *
     * @see gov.nasa.worldwind.globes.Globe#getStateKey(DrawContext)
     */
    @Override
    public Object getStateKey(DrawContext dc)
    {
        // Store a copy of the active attributes to insulate the key from changes made to the shape's active attributes.
        return new SurfaceShapeStateKey(this.getUniqueId(), this.lastModifiedTime, this.getActiveAttributes().copy(),
                dc.getGlobe().getStateKey(dc));
    }

    public Iterable<? extends LatLon> getLocations(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.computeLocations(globe, this.intervals);
    }

    public Position getReferencePosition()
    {
        return new Position(this.center, 0);
    }

    protected void doMoveTo(Position oldReferencePosition, Position newReferencePosition)
    {
        Angle heading = LatLon.greatCircleAzimuth(oldReferencePosition, this.center);
        Angle pathLength = LatLon.greatCircleDistance(oldReferencePosition, this.center);
        this.setCenter(LatLon.greatCircleEndPosition(newReferencePosition, heading, pathLength));
    }

    protected void doMoveTo(Globe globe, Position oldReferencePosition, Position newReferencePosition)
    {
        List<LatLon> locations = new ArrayList<LatLon>(1);
        locations.add(this.getCenter());
        List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldReferencePosition, newReferencePosition,
                locations);
        this.setCenter(newLocations.get(0));
    }

    protected List<LatLon> computeLocations(Globe globe, int intervals)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.majorRadius == 0 && this.minorRadius == 0)
        {
            return null;
        }

        int numLocations = 1 + Math.max(MIN_NUM_INTERVALS, intervals);
        double da = (2 * Math.PI) / (numLocations - 1);
        double globeRadius = globe.getRadiusAt(this.center.getLatitude(), this.center.getLongitude());

        LatLon[] locations = new LatLon[numLocations];

        for (int i = 0; i < numLocations; i++)
        {
            double angle = (i != numLocations - 1) ? i * da : 0;
            double xLength = this.majorRadius * Math.cos(angle);
            double yLength = this.minorRadius * Math.sin(angle);
            double distance = Math.sqrt(xLength * xLength + yLength * yLength);
            // azimuth runs positive clockwise from north and through 360 degrees.
            double azimuth = (Math.PI / 2.0) - (Math.acos(xLength / distance) * Math.signum(yLength)
                    - this.heading.radians);

            locations[i] = LatLon.greatCircleEndPosition(this.center, azimuth, distance / globeRadius);
        }

        return Arrays.asList(locations);
    }

    protected List<List<LatLon>> createGeometry(Globe globe, double edgeIntervalsPerDegree)
    {
        int intervals = this.computeNumIntervals(globe, edgeIntervalsPerDegree);

        List<LatLon> drawLocations = this.computeLocations(globe, intervals);
        if (drawLocations == null)
        {
            return null;
        }

        ArrayList<List<LatLon>> geom = new ArrayList<List<LatLon>>();
        geom.add(drawLocations);

        return geom;
    }

    protected int computeNumIntervals(Globe globe, double edgeIntervalsPerDegree)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numEdgeIntervals = this.computeNumEdgeIntervals(globe, edgeIntervalsPerDegree);
        return numEdgeIntervals * this.intervals;
    }

    protected int computeNumEdgeIntervals(Globe globe, double edgeIntervalsPerDegree)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numPositions = 1 + Math.max(MIN_NUM_INTERVALS, intervals);
        double radius = Math.max(this.majorRadius, this.minorRadius);
        double da = (2 * Math.PI) / (numPositions - 1);
        Angle edgePathLength = Angle.fromRadians(da * radius / globe.getRadiusAt(this.center));

        double edgeIntervals = WWMath.clamp(edgeIntervalsPerDegree * edgePathLength.degrees,
                this.minEdgeIntervals, this.maxEdgeIntervals);

        return (int) Math.ceil(edgeIntervals);
    }

    //**************************************************************//
    //******************** Restorable State  ***********************//
    //**************************************************************//
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsLatLon(context, "center", this.getCenter());
        rs.addStateValueAsDouble(context, "majorRadius", this.getMajorRadius());
        rs.addStateValueAsDouble(context, "minorRadius", this.getMinorRadius());
        rs.addStateValueAsDouble(context, "headingDegrees", this.getHeading().degrees);
        rs.addStateValueAsInteger(context, "intervals", this.getIntervals());
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        LatLon ll = rs.getStateValueAsLatLon(context, "center");
        if (ll != null)
        {
            this.setCenter(ll);
        }

        Double d = rs.getStateValueAsDouble(context, "majorRadius");
        if (d != null)
        {
            this.setMajorRadius(d);
        }

        d = rs.getStateValueAsDouble(context, "minorRadius");
        if (d != null)
        {
            this.setMinorRadius(d);
        }

        d = rs.getStateValueAsDouble(context, "headingDegrees");
        if (d != null)
        {
            this.setHeading(Angle.fromDegrees(d));
        }

        Integer i = rs.getStateValueAsInteger(context, "intervals");
        if (d != null)
        {
            this.setIntervals(i);
        }
    }

    protected void legacyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.legacyRestoreState(rs, context);

        // These properties has not changed since the last version, but they're shown here for reference.
        //Double major = rs.getStateValueAsDouble(context, "majorRadius");
        //Double minor = rs.getStateValueAsDouble(context, "minorRadius");
        //if (major != null && minor != null)
        //    this.setAxisLengths(major, minor);
        // This property has not changed since the last version, but it's shown here for reference.
        //LatLon center = rs.getStateValueAsLatLon(context, "center");
        //if (center != null)
        //    this.setCenter(center);
        // This property has not changed since the last version, but it's shown here for reference.
        //Integer intervals = rs.getStateValueAsInteger(context, "intervals");
        //if (intervals != null)
        //    this.setIntervals(intervals);
        Double od = rs.getStateValueAsDouble(context, "orientationDegrees");
        if (od != null)
        {
            this.setHeading(Angle.fromDegrees(od));
        }
    }

    /**
     * Indicates whether to extrude this path. Extruding the path extends a
     * filled interior from the path to the terrain.
     *
     * @return true to extrude this path, otherwise false.
     *
     * @see #setExtrude(boolean)
     */
    public boolean isExtrude()
    {
        return extrude;
    }

    /**
     * Specifies whether to extrude this path. Extruding the path extends a
     * filled interior from the path to the terrain.
     *
     * @param extrude true to extrude this path, otherwise false. The default
     * value is false.
     */
    public void setExtrude(final boolean extrude)
    {
        this.extrude = extrude;
    }

    /**
     * Indicates whether this path is terrain following.
     *
     * @return true if terrain following, otherwise false.
     *
     * @see #setFollowTerrain(boolean)
     */
    public boolean isFollowTerrain()
    {
        return this.followTerrain;
    }

    /**
     * Specifies whether this path is terrain following.
     *
     * @param followTerrain true if terrain following, otherwise false. The
     * default value is false.
     */
    public void setFollowTerrain(final boolean followTerrain)
    {
        this.followTerrain = followTerrain;
    }

    /**
     * Returns this shape's altitude mode.
     *
     * @return this shape's altitude mode.
     *
     * @see #setAltitudeMode(int)
     */
    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    /**
     * Export the polygon to KML as a {@code <Placemark>} element. The
     * {@code output} object will receive the data. This object must be one of:
     * java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws javax.xml.stream.XMLStreamException If an exception occurs while
     * writing the KML
     * @throws java.io.IOException if an exception occurs while exporting the
     * data.
     * @see #export(String, Object)
     */
    @Override
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        } else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        } else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }

        if (xmlWriter == null)
        {
            final String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Placemark");

        final String property = (String) getValue(AVKey.DISPLAY_NAME);
        if (property != null)
        {
            xmlWriter.writeStartElement("name");
            xmlWriter.writeCharacters(property);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeStartElement("visibility");
        xmlWriter.writeCharacters(KMLExportUtil.kmlBoolean(this.isVisible()));
        xmlWriter.writeEndElement();

        final String shortDescription = (String) getValue(AVKey.SHORT_DESCRIPTION);
        if (shortDescription != null)
        {
            xmlWriter.writeStartElement("Snippet");
            xmlWriter.writeCharacters(shortDescription);
            xmlWriter.writeEndElement();
        }

        final String description = (String) getValue(AVKey.BALLOON_TEXT);
        if (description != null)
        {
            xmlWriter.writeStartElement("description");
            xmlWriter.writeCharacters(description);
            xmlWriter.writeEndElement();
        }

        // KML does not allow separate attributes for cap and side, so just use the side attributes.
        final ShapeAttributes normalAttributes = getAttributes();
        final ShapeAttributes highlightAttributes = getHighlightAttributes();

        // Write style map
        if (normalAttributes != null || highlightAttributes != null)
        {
            xmlWriter.writeStartElement("StyleMap");
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.NORMAL, normalAttributes);
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.HIGHLIGHT, highlightAttributes);
            xmlWriter.writeEndElement(); // StyleMap
        }

        // Write geometry
        xmlWriter.writeStartElement("Polygon");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters("0");
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters("clampToGround");
        xmlWriter.writeEndElement();


        // Outer boundary
        String globeName = Configuration.getStringValue(AVKey.GLOBE_CLASS_NAME, "gov.nasa.worldwind.globes.Earth");
        Globe globe = (Globe) WorldWind.createComponent(globeName);

        Iterable<? extends LatLon> outerBoundary = this.getLocations(globe);
        if (outerBoundary != null)
        {
            xmlWriter.writeStartElement("outerBoundaryIs");
            KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, outerBoundary, null);
            xmlWriter.writeEndElement(); // outerBoundaryIs
        }
        exportCoordinatesAsKML(xmlWriter);

        xmlWriter.writeEndElement(); 

        // Write geometry
        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
        if (closeWriterWhenFinished)
        {
            xmlWriter.close();
        }
    }

    /**
     * Exports the coordinates to KML.
     *
     * @param xmlWriter The XML Writer
     * @throws XMLStreamException If an exception occurs
     */
    private void exportCoordinatesAsKML(final XMLStreamWriter xmlWriter) throws XMLStreamException
    {
        final String globeName = Configuration
                .getStringValue(AVKey.GLOBE_CLASS_NAME, "gov.nasa.worldwind.globes.Earth");
        final Globe globe = (Globe) WorldWind.createComponent(globeName);
        final Iterable<? extends LatLon> it = this.computeLocations(globe, 360);
        if (it != null)
        {
            xmlWriter.writeStartElement("coordinates");
            for (final LatLon ll : it)
            {
                final double lat = ll.getLatitude().degrees;
                final double lon = ll.getLongitude().degrees;
                xmlWriter.writeCharacters(String.format(Locale.US, "%f,%f,%f ", lon, lat, 0.0));
            }
            xmlWriter.writeEndElement();
        }
    }

}
