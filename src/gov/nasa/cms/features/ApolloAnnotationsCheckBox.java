/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.PowerOfTwoPaddedImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.URL;

import static gov.nasa.worldwindx.examples.ApplicationTemplate.insertBeforeCompass;

/**
 * Illustrates how to use a WorldWind <code>{@link Annotations.java}</code> to
 * display on-screen Apollo landing site information to the user in the form of
 * a text label with an optional image. Annotations may be attached to a
 * geographic position or a point on the screen. Annotations.java provide
 * support for multi-line text, simple HTML text markup, and many styling
 * attributes such as font face, size and colors, background shape and
 * background image.
 *
 * @author Tyler Choi
 * @version $Id: ApolloAnnotations.java 2020-08-02 16:44:38Z twchoi $
 */
public class ApolloAnnotationsCheckBox extends JCheckBox
{

    private WorldWindow wwd;

    private AnnotationLayer layer;
    private Annotation currentAnnotation;
    private Color savedBorderColor;
    private BufferedImage savedImage;
    private Annotation lastPickedObject;

    private final static PowerOfTwoPaddedImage APOLLO11
            = PowerOfTwoPaddedImage.fromPath("images/Apollo11.jpg");
    private final static PowerOfTwoPaddedImage APOLLO12
            = PowerOfTwoPaddedImage.fromPath("images/Apollo12.jpg");
    private final static PowerOfTwoPaddedImage APOLLO14
            = PowerOfTwoPaddedImage.fromPath("images/Apollo14.jpg");
    private final static PowerOfTwoPaddedImage APOLLO15
            = PowerOfTwoPaddedImage.fromPath("images/Apollo15.jpg");
    private final static PowerOfTwoPaddedImage APOLLO16
            = PowerOfTwoPaddedImage.fromPath("images/Apollo16.jpg");
    private final static PowerOfTwoPaddedImage APOLLO17
            = PowerOfTwoPaddedImage.fromPath("images/Apollo17.jpg");

    private boolean isItemEnabled;

    public void setupAnnotations()
    {

        /**
         * ********* Image Annotations for Apollo sites ******
         */
        GlobeAnnotation ga;
        this.layer = new AnnotationLayer(); //create a single layer displaying all annotations
        String layerName = "Apollo Logo"; // Set layer name from Annotations to Apollo
        layer.setName(layerName);

        ga = this.makeTopImageBottomTextAnnotation(APOLLO11, "<a href=\"https://www.nasa.gov/mission_pages/apollo/apollo-11.html\">Apollo 11 - July 20, 1969. Mare Tranquillitatis</a>",
                Position.fromDegrees(0.6875, 23.4333, 0));
        //ga.setValue("Apollo 11", Apollo11URL);
        layer.addAnnotation(ga);

        ga = this.makeTopImageBottomTextAnnotation(APOLLO12, "<a href=\"https://www.nasa.gov/mission_pages/apollo/apollo-12\">Apollo 12 - November 19, 1969. Oceanus Procellarum</a>",
                Position.fromDegrees(-3.1975, -23.3856, 0));
        layer.addAnnotation(ga);

        ga = this.makeTopImageBottomTextAnnotation(APOLLO14, "<a href=\"https://www.nasa.gov/mission_pages/apollo/apollo-14\">Apollo 14 - February 5, 1971. Fra Mauro Highlands</a>",
                Position.fromDegrees(-3.6733, -17.4653, 0));
        layer.addAnnotation(ga);

        ga = this.makeTopImageBottomTextAnnotation(APOLLO15, "<a href=\"https://www.nasa.gov/mission_pages/apollo/apollo-15\">Apollo 15 - July 30, 1971. Montes Apenninus</a>",
                Position.fromDegrees(26.1008, 3.6527, 0));
        layer.addAnnotation(ga);

        ga = this.makeTopImageBottomTextAnnotation(APOLLO16, "<a href=\"https://www.nasa.gov/mission_pages/apollo/apollo-16\">Apollo 16 - April 20, 1972. Descartes Highlands</a>",
                Position.fromDegrees(-8.9913, 15.5144, 0));
        layer.addAnnotation(ga);

        ga = this.makeTopImageBottomTextAnnotation(APOLLO17, "<a href=\"https://www.nasa.gov/mission_pages/apollo/apollo-17\">Apollo 17 - December 11, 1972. Taurus-Littrow Valley</a>",
                Position.fromDegrees(20.1653, 30.7658, 0));
        layer.addAnnotation(ga);

        // Add layer to the layer list and update the layer panel
        insertBeforeCompass(this.getWwd(), layer);

        /**
         * ***** Minimal Apollo Sites *********
         */
        // Create default attributes
        AnnotationAttributes defaultAttributes = new AnnotationAttributes();
        defaultAttributes.setCornerRadius(10);
        defaultAttributes.setInsets(new Insets(8, 8, 8, 8));
        defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        defaultAttributes.setTextColor(Color.WHITE);
        defaultAttributes.setDrawOffset(new Point(25, 25));
        defaultAttributes.setDistanceMinScale(.5);
        defaultAttributes.setDistanceMaxScale(2);
        defaultAttributes.setDistanceMinOpacity(.5);
        defaultAttributes.setLeaderGapWidth(14);
        defaultAttributes.setDrawOffset(new Point(20, 40));

        //Create minimal Apollo annotations option
        this.layer = new AnnotationLayer();
        //layerName = layer.getName();
        layerName = "Apollo Minimal";
        layer.setName(layerName);

        AnnotationAttributes spAttr = new AnnotationAttributes();
        spAttr.setDefaults(defaultAttributes);
        spAttr.setFont(Font.decode("Arial-BOLDITALIC-12"));
        spAttr.setTextColor(Color.YELLOW);
        spAttr.setTextAlign(AVKey.CENTER);
        spAttr.setDrawOffset(new Point(0, 5));
        spAttr.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        layer.addAnnotation(new GlobeAnnotation("Apollo 11",
                Position.fromDegrees(0.6875, 23.4333, 0), spAttr));
        layer.addAnnotation(new GlobeAnnotation("Apollo 12",
                Position.fromDegrees(-3.1975, -23.3856, 0), spAttr));
        layer.addAnnotation(new GlobeAnnotation("Apollo 14",
                Position.fromDegrees(-3.6733, -17.4653, 0), spAttr));
        layer.addAnnotation(new GlobeAnnotation("Apollo 15",
                Position.fromDegrees(26.1008, 3.6527, 0), spAttr));
        layer.addAnnotation(new GlobeAnnotation("Apollo 16",
                Position.fromDegrees(-8.9913, 15.5144, 0), spAttr));
        layer.addAnnotation(new GlobeAnnotation("Apollo 17",
                Position.fromDegrees(20.1653, 30.7658, 0), spAttr));

        //Add Apollo Minimal to layer list panel
        insertBeforeCompass(this.getWwd(), layer);
    }

    public GlobeAnnotation makeTopImageBottomTextAnnotation(PowerOfTwoPaddedImage image, String text,
            Position position)
    {
        // Create annotation
        GlobeAnnotation ga = new GlobeAnnotation(text, position);
        int inset = 10; // pixels
        ga.getAttributes().setInsets(new Insets(image.getOriginalHeight() + inset * 2, inset, inset, inset));
        ga.getAttributes().setImageSource(image.getPowerOfTwoImage());
        ga.getAttributes().setImageOffset(new Point(inset, inset));
        ga.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
        ga.getAttributes().setImageOpacity(1);
        ga.getAttributes().setSize(new Dimension(image.getOriginalWidth() + inset * 2, 0));
        ga.getAttributes().setScale(0.8);
        ga.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
        ga.getAttributes().setBackgroundColor(Color.WHITE);
        ga.getAttributes().setFont(Font.decode("Arial-BOLD-13"));
        ga.getAttributes().setDrawOffset(new Point(0, 5));
        ga.getAttributes().setTextColor(Color.BLACK);
        ga.getAttributes().setTextAlign(AVKey.CENTER);
        ga.getAttributes().setBorderColor(Color.BLACK);
        return ga;
    }

    // --- Selection ---------------------------------------
    public void setupSelection()
    {
        // Add a select listener to select or highlight annotations on rollover
        this.getWwd().addSelectListener(new SelectListener()
        {
            private BasicDragger dragger = new BasicDragger(getWwd());

            public void selected(SelectEvent event)
            {
                if (event.hasObjects() && event.getTopObject() instanceof Annotation)
                {
                    // Handle cursor change on hyperlink
                    if (event.getTopPickedObject().getValue(AVKey.URL) != null)
                    {
                        ((Component) getWwd()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else
                    {
                        ((Component) getWwd()).setCursor(Cursor.getDefaultCursor());
                    }
                }

                // Select/unselect on left click on annotations
                if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                {
                    if (event.hasObjects())
                    {
                        if (event.getTopObject() instanceof Annotation)
                        {
                            // Check for text or url
                            PickedObject po = event.getTopPickedObject();
                            if (po.getValue(AVKey.TEXT) != null)
                            {
                                System.out.println("Text: \"" + po.getValue(AVKey.TEXT) + "\" Hyperlink: "
                                        + po.getValue(AVKey.URL));
                                if (po.getValue(AVKey.URL) != null)
                                {
                                    // Try to launch a browser with the clicked URL
                                    try
                                    {
                                        BrowserOpener.browse(new URL((String) po.getValue(AVKey.URL)));
                                    } catch (Exception ignore)
                                    {
                                    }
                                }
                                if (currentAnnotation == event.getTopObject())
                                {
                                    return;
                                }
                            }
                            // Left click on an annotation - select
                            if (currentAnnotation != null)
                            {
                                // Unselect current
                                //AppFrame.this.currentAnnotation.getAttributes().setHighlighted(false);
                                currentAnnotation.getAttributes().setBorderColor(
                                        savedBorderColor);
                            }
                            if (currentAnnotation != event.getTopObject())
                            {
                                // Select new one if not current one already
                                currentAnnotation = (Annotation) event.getTopObject();
                                //AppFrame.this.currentAnnotation.getAttributes().setHighlighted(true);
                                savedBorderColor = currentAnnotation
                                        .getAttributes().getBorderColor();
                                savedImage = currentAnnotation.getAttributes()
                                        .getImageSource() instanceof BufferedImage
                                                ? (BufferedImage) currentAnnotation.getAttributes().getImageSource()
                                                : null;
                                currentAnnotation.getAttributes().setBorderColor(Color.YELLOW);
                            } else
                            {
                                // Clear current annotation
                                currentAnnotation = null; // switch off
                            }

                        } else
                        {
                            System.out.println("Left click on " + event.getTopObject());
                        }
                    }
                } // Highlight on rollover
                else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
                {
                    highlight(event.getTopObject());
                }

            }
        });
    }

    private void launchURL(String linkName)
    {
        // Try to launch a browser with the clicked URL
        try
        {
            BrowserOpener.browse(new URL((String) linkName));
        } catch (Exception ignore)
        {
        }
    }

    public void highlight(Object o)
    {
        // Manage highlighting of Annotations.
        if (this.lastPickedObject == o)
        {
            return; // same thing selected
        }
        // Turn off highlight if on.
        if (this.lastPickedObject != null) // && this.lastPickedObject != this.currentAnnotation)
        {
            this.lastPickedObject.getAttributes().setHighlighted(false);
            this.lastPickedObject = null;
        }

        // Turn on highlight if object selected.
        if (o != null && o instanceof Annotation)
        {
            this.lastPickedObject = (Annotation) o;
            this.lastPickedObject.getAttributes().setHighlighted(true);
        }
    }

    public ApolloAnnotationsCheckBox(WorldWindow Wwd)
    {
        super("Annotations");

        this.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                isItemEnabled = ((JCheckBox) event.getSource()).isSelected();

                if (isItemEnabled)
                {
                    setWwd(Wwd); //sets Wwd to Wwd parameter from CelestialMapper
                    setupAnnotations();
                    setupSelection();
                    
                } else
                {
                    String[] ApolloLayers =
                    {
                        "Apollo Minimal", "Apollo Logo"
                    };
                    for (String layer : ApolloLayers)
                    {
                        Layer selectedLayer = Wwd.getModel().getLayers().getLayerByName(layer);
                        Wwd.getModel().getLayers().remove(selectedLayer); //removes Apollo layer from layer list
                    }                

                }
            }
        });
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public void setWwd(WorldWindow wwd)
    {
        this.wwd = wwd;
    }

}
