/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.cms.features.coordinates;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.*;
import java.util.Iterator;

/**
 * @author tag, rewritten for CMS by gknorman 03/2021
 * @version $Id: CoordinatesDisplay.java 1172 2021-04-21 21:45:02Z gknorman $
 */
public class CoordinatesDisplay
{
    private double previousHeight;
    private final CelestialMapper cms;
    private Layer coordinatesLayer;
    protected static final Offset DEFAULT_OFFSET = new Offset(20d, 140d, AVKey.PIXELS, AVKey.INSET_PIXELS);
    private AnnotationAttributes attrs;
    private CMSCoordAnnotationLayer layer;
    private ScreenAnnotation anno;
    private double previousWidth;
    private int width;
    private int height;

    public CoordinatesDisplay(CelestialMapper cms){
        this.cms = cms;
        Dimension wwSize = cms.getSize();
        this.previousWidth = wwSize.getWidth();
        this.previousHeight = wwSize.getHeight();

//        System.out.println("getSize()  .width: " + wwSize.getWidth());
//        System.out.println("getSize()  .height: " + wwSize.getHeight());

        this.initialize(cms);
    }

    public void initialize(CelestialMapper cms){

        this.coordinatesLayer = this.createLayer();
        cms.getWwd().getModel().getLayers().add(coordinatesLayer);

        this.coordinatesLayer.setEnabled(true);

    }

    public Layer getCoordinatesLayer()
    {
        return coordinatesLayer;
    }

    public void setCoordinatesLayer(Layer coordinatesLayer)
    {
        this.coordinatesLayer = coordinatesLayer;
    }


    protected Layer createLayer()
    {
        Layer layer = this.doCreateLayer();

        layer.setPickEnabled(false);

        return layer;
    }


    protected Layer doCreateLayer()
    {
        this.anno = new ScreenAnnotation("Dummy Text", new Point(200, 200));
        anno.setAlwaysOnTop(true);

        this.attrs = anno.getAttributes();
        attrs.setTextColor(Color.WHITE);
        attrs.setFont(Font.decode("Consolas-Bold-15"));
        attrs.setEffect(AVKey.TEXT_EFFECT_OUTLINE);

        attrs.setFrameShape(AVKey.SHAPE_NONE);
        attrs.setLeader(AVKey.SHAPE_NONE);
        attrs.setBackgroundColor(Color.BLACK);
        attrs.setBorderColor(new Color(0.1f, 0.1f, 0.1f, 0f));
        attrs.setBorderWidth(1d);
        attrs.setCornerRadius(5);
        attrs.setInsets(new Insets(10, 0, 0, 10));


        this.width = 340; 
        this.height = 200;
        attrs.setSize(new Dimension(width, height));
//        attrs.setTextAlign(AVKey.RIGHT);
        attrs.setTextAlign(AVKey.LEFT);
        attrs.setAdjustWidthToText(AVKey.SIZE_FIXED);

        this.layer = new CMSCoordAnnotationLayer();
        layer.setValue(WorldWindowConstants.SCREEN_LAYER, true);
        layer.setPickEnabled(false);
        layer.addAnnotation(anno);
        layer.setName("Coordinates Display");

//        System.out.println(attrs.getDrawOffset().toString());


        Dimension wwSize = cms.getSize();
        if(wwSize.getWidth() != previousWidth)
        {
            this.previousWidth = wwSize.getWidth();
//            System.out.println("getSize()  .width: " + wwSize.getWidth());
        }

        if(wwSize.getHeight() != previousHeight)
        {
            this.previousHeight = wwSize.getHeight();
//            System.out.println("getSize()  .height: " + wwSize.getHeight());
        }


        return layer;
    }

    private class CMSCoordAnnotationLayer extends AnnotationLayer
    {
        DrawContext dc;

        @Override
        public void render(DrawContext dc)
        {
            Iterator<Annotation> iter = this.getAnnotations().iterator();
            Annotation anno = iter.next();
            if (anno != null && anno instanceof ScreenAnnotation)
            {

                this.dc = dc;
                anno.setText(formatText(dc));

                /*
                 Need to update the minimap's dimensions regardless of
                 whether the mouse is over the map and "picking" a point.
                 Otherwise the coordinates display won't update it's
                 position to fit under the minimap until the user mouses
                 over the map!
                */
                cms.getWML().drawIcon(dc);

                Dimension wwSize = cms.getWwjPanel().getSize();
                ((ScreenAnnotation) anno).setScreenPoint(new Point(
                    /*
                     Added a manual offset of 168px to better line up the
                     left aligned display to the edge of the minimap
                    */
                    (int) cms.getWML().getLocationSW().getX() + 168,

                    /*
                     Added a manual offset of the previously set annotation
                     height minus 15 px to create some more spacing between
                     the minimap and the coordinates display
                    */
                    (int) cms.getWML().getLocationSW().getY() - height - 15
                ));
/*

                 We don't need the offset anymore since the draw point is
                 being created relative to the minimap instead of the window!!
                    attrs.setDrawOffset(new Point(
                        0,0
                    ));
                */

                if(wwSize.getWidth() != previousWidth)
                {
                    previousWidth = wwSize.width;
                }

                if(wwSize.getHeight() != previousHeight)
                {

                    previousHeight = wwSize.height;
                }
            }

            super.render(dc);
        }

        @Override
        protected void doRender(DrawContext dc)
        {
            super.doRender(dc);
        }

        public DrawContext getDc()
        {
            return dc;
        }
    }

    private Position getCurrentPosition(DrawContext dc)
    {
        if (dc.getPickedObjects() == null)
            return null;

        PickedObject po = dc.getPickedObjects().getTerrainObject();
        return po != null ? po.getPosition() : null;
    }

    private String formatText(DrawContext dc)
    {
        StringBuilder sb = new StringBuilder();

        Position eyePosition = dc.getView().getEyePosition();
        if (eyePosition != null)
        {
            CMSWWOUnitsFormat units = this.cms.getUnits();
            String origFormat = units.getFormat(CMSUnitsFormat.FORMAT_EYE_ALTITUDE);
            String tempFormat = origFormat;

            if (Math.abs(eyePosition.getElevation() * units.getLengthUnitsMultiplier()) < 10)
            {
                tempFormat = " %,6.3f %s";
                units.setFormat(CMSUnitsFormat.FORMAT_EYE_ALTITUDE, tempFormat);
            }

            sb.append(this.cms.getUnits().eyeAltitudeNL(eyePosition.getElevation()));

            if (!tempFormat.equals(origFormat))
                units.setFormat(CMSUnitsFormat.FORMAT_EYE_ALTITUDE, origFormat);
        }
        else
        {
            sb.append("Altitude\n");
        }

        Position currentPosition = getCurrentPosition(dc);
        if (currentPosition != null)
        {
            sb.append(this.cms.getUnits().latitudeNL(currentPosition.getLatitude()));
            sb.append(this.cms.getUnits().longitudeNL(currentPosition.getLongitude()));
            sb.append(this.cms.getUnits().terrainHeightNL(currentPosition.getElevation(),
                this.cms.getWwd().getSceneController().getVerticalExaggeration()));
        }
        else
        {
            sb.append(this.cms.getUnits().getStringValue(CMSUnitsFormat.LABEL_LATITUDE)).append("\n");
            sb.append(this.cms.getUnits().getStringValue(CMSUnitsFormat.LABEL_LONGITUDE)).append("\n");
            sb.append(this.cms.getUnits().getStringValue(CMSUnitsFormat.LABEL_TERRAIN_HEIGHT)).append("\n");
        }

        sb.append(this.cms.getUnits().pitchNL(computePitch(dc.getView())));
        sb.append(this.cms.getUnits().headingNL(computeHeading(dc.getView())));

        String datum = this.cms.getUnits().datumNL();

        if (cms.getUnits().isShowUTM())
        {
//            sb.append(datum);
            if (currentPosition != null)
            {
                try
                {
                    UTMCoord utm = UTMCoord.fromLatLon(currentPosition.getLatitude(), currentPosition.getLongitude(),
                        this.cms.getUnits().isShowWGS84() ? null : "NAD27");

                    sb.append(this.cms.getUnits().utmZoneNL(utm.getZone()));
                    sb.append(this.cms.getUnits().utmEastingNL(utm.getEasting()));
                    sb.append(this.cms.getUnits().utmNorthingNL(utm.getNorthing()));
                }
                catch (Exception e)
                {
                    sb.append(this.cms.getUnits().getStringValue(
                        CMSUnitsFormat.LABEL_UTM_ZONE)).append("\n");
                    sb.append(this.cms.getUnits().getStringValue(
                        CMSUnitsFormat.LABEL_UTM_EASTING)).append("\n");
                    sb.append(this.cms.getUnits().getStringValue(
                        CMSUnitsFormat.LABEL_UTM_NORTHING)).append("\n");
                }
            }
            else
            {
                sb.append(this.cms.getUnits().getStringValue(
                    CMSUnitsFormat.LABEL_UTM_ZONE)).append("\n");
                sb.append(this.cms.getUnits().getStringValue(
                    CMSUnitsFormat.LABEL_UTM_EASTING)).append("\n");
                sb.append(this.cms.getUnits().getStringValue(
                    CMSUnitsFormat.LABEL_UTM_NORTHING)).append("\n");
            }
        }

        return sb.toString();
    }

    private double computeHeading(View view)
    {
        if (view == null)
            return 0.0;

        if (!(view instanceof OrbitView))
            return 0.0;

        OrbitView orbitView = (OrbitView) view;

        return orbitView.getHeading().getDegrees();
    }

    private double computePitch(View view)
    {
        if (view == null)
            return 0.0;

        if (!(view instanceof OrbitView))
            return 0.0;

        OrbitView orbitView = (OrbitView) view;

        return orbitView.getPitch().getDegrees();
    }
}
