/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.util.Logging;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author dcollins
 * @version $Id: SurfaceCircle.java 2302 2014-09-08 20:40:47Z tgaskins $
 */
public class SurfaceCircle extends SurfaceEllipse
{
    /** Constructs a new surface circle with the default attributes, default center location and default radius. */
    public SurfaceCircle()
    {
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public SurfaceCircle(SurfaceCircle source)
    {
        super(source);
    }

    /**
     * Constructs a new surface circle with the specified normal (as opposed to highlight) attributes, default center
     * location, and default radius. Modifying the attribute reference after calling this constructor causes this
     * shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public SurfaceCircle(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    /**
     * Constructs a new surface circle with the default attributes, the specified center location and radius (in
     * meters).
     *
     * @param center the circle's center location.
     * @param radius the circle's radius, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the radius is negative.
     */
    public SurfaceCircle(LatLon center, double radius)
    {
        super(center, radius, radius);
    }

    /**
     * Constructs a new surface circle with the default attributes, the specified center location, radius (in meters),
     * and initial number of geometry intervals.
     *
     * @param center    the circle's center location.
     * @param radius    the circle's radius, in meters.
     * @param intervals the initial number of intervals (or slices) defining the circle's geometry.
     *
     * @throws IllegalArgumentException if the center is null, if the radius is negative, or if the number of intervals
     *                                  is less than 8.
     */
    public SurfaceCircle(LatLon center, double radius, int intervals)
    {
        super(center, radius, radius, Angle.ZERO, intervals);
    }

    /**
     * Constructs a new surface circle with the specified normal (as opposed to highlight) attributes, the specified
     * center location, and radius (in meters). Modifying the attribute reference after calling this constructor causes
     * this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the circle's center location.
     * @param radius      the circle's radius, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the radius is negative.
     */
    public SurfaceCircle(ShapeAttributes normalAttrs, LatLon center, double radius)
    {
        super(normalAttrs, center, radius, radius);
    }

    /**
     * Constructs a new surface circle with the specified normal (as opposed to highlight) attributes, the specified
     * center location,  radius (in meters), and initial number of geometry intervals. Modifying the attribute reference
     * after calling this constructor causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the circle's center location.
     * @param radius      the circle's radius, in meters.
     * @param intervals   the initial number of intervals (or slices) defining the circle's geometry.
     *
     * @throws IllegalArgumentException if the center is null, if the radius is negative, or if the number of intervals
     *                                  is less than 8.
     */
    public SurfaceCircle(ShapeAttributes normalAttrs, LatLon center, double radius, int intervals)
    {
        super(normalAttrs, center, radius, radius, Angle.ZERO, intervals);
    }

    public double getRadius()
    {
        return this.getMajorRadius();
    }

    public void setRadius(double radius)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("Geom.RadiusIsNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setRadii(radius, radius);
    }
    
    /**
     * Export the polygon to KML as a {@code <Placemark>} element. The {@code output} object will receive the data. This
     * object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws javax.xml.stream.XMLStreamException If an exception occurs while writing the KML
     * @throws java.io.IOException        if an exception occurs while exporting the data.
     * @see #export(String, Object)
     */
    @Override
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        }
        else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        }
        else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }

        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Placemark");

        String property = getStringValue(AVKey.DISPLAY_NAME);
        if (property != null)
        {
            xmlWriter.writeStartElement("name");
            xmlWriter.writeCharacters(property);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeStartElement("visibility");
        xmlWriter.writeCharacters(KMLExportUtil.kmlBoolean(this.isVisible()));
        xmlWriter.writeEndElement();

        String shortDescription = (String) getValue(AVKey.SHORT_DESCRIPTION);
        if (shortDescription != null)
        {
            xmlWriter.writeStartElement("Snippet");
            xmlWriter.writeCharacters(shortDescription);
            xmlWriter.writeEndElement();
        }

        String description = (String) getValue(AVKey.BALLOON_TEXT);
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
        xmlWriter.writeStartElement("Circle");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters("0");
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters("clampToGround");
        xmlWriter.writeEndElement();

        String globeName = Configuration.getStringValue(AVKey.GLOBE_CLASS_NAME, "gov.nasa.worldwind.globes.Earth");
        Globe globe = (Globe) WorldWind.createComponent(globeName);

        // Outer boundary
        Iterable<? extends LatLon> outerBoundary = this.getLocations(globe);
        if (outerBoundary != null)
        {
            xmlWriter.writeStartElement("outerBoundaryIs");
            KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, outerBoundary, null);
            xmlWriter.writeEndElement(); // outerBoundaryIs
        }

        xmlWriter.writeEndElement(); // Polygon
        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    } 
}
