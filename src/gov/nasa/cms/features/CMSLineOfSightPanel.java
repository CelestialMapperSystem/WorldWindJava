/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.RayCastingSupport;
import gov.nasa.worldwind.view.orbit.OrbitView;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * This class currently only provides one panel to interact with the
 * CMSLineOfSight class.
 *
 * @author gknorman
 */
public class CMSLineOfSightPanel extends JPanel
{

     private WorldWindow wwd;
     private JDialog dialog;
     private JPanel mainPanel;
     private JCheckBox gridLines, gridPoints, intersectionPoints, intersectionLines,
             originPoint;
     protected JProgressBar progressBar;
     private final CMSLineOfSight lineOfSight;
     private LineOfSightController lineOfSightController;
     private JPanel buttonPanel;
     private JButton clearButton;
     private double samplingLength = 30; // Ray casting sample length
     private int centerOffset = 100; // meters above ground for center
     private int pointOffset = 10;   // meters above ground for sampled points
     private Vec4 light = new Vec4(1, 1, -1).normalize3();   // Light direction (from South-East)
     private double ambiant = .4;                            // Minimum lighting (0 - 1)

     private RenderableLayer renderableLayer;
     private SurfaceImage surfaceImage;
     private ScreenAnnotation screenAnnotation;
     private JComboBox radiusCombo;
     private JComboBox samplesCombo;
     private JCheckBox shadingCheck;
     private JButton computeButton;
     private JDialog elevationLOSDialog;

     CMSLineOfSightPanel(WorldWindow wwd, CMSLineOfSight lineOfSightObject, LineOfSightController lineOfSightController)
     {
          super(new BorderLayout());
          this.wwd = wwd;
          this.lineOfSight = lineOfSightObject;
          this.lineOfSightController = lineOfSightController;

          mainPanel = new JPanel();
          mainPanel.setOpaque(false);
          this.makePanel(mainPanel);

          this.renderableLayer = new RenderableLayer();
          this.renderableLayer.setName("Line of sight");
          this.renderableLayer.setPickEnabled(false);
          wwd.getModel().getLayers().add(renderableLayer);
          wwd.getModel().getLayers().add(new CrosshairLayer());
     }

     private void makePanel(JPanel mainPanel)
     {
          //======== Inner Panel ======== 
          JPanel shapePanel = new JPanel(new GridLayout(7, 1, 5, 5));
          shapePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
          shapePanel.add(new JLabel("Show / Hide Visual Components"));
          this.originPoint = new JCheckBox("Origin Point");
          this.gridLines = new JCheckBox("Grid Lines");
          this.gridPoints = new JCheckBox("Grid Points");
          this.intersectionPoints = new JCheckBox("Intersection Points");
          this.intersectionLines = new JCheckBox("Lines to Intersections");

          // Display a progress bar.
          this.progressBar = new JProgressBar(0, 100);
          this.progressBar.setBorder(new EmptyBorder(0, 10, 0, 10));
          this.progressBar.setBorderPainted(false);
          this.progressBar.setStringPainted(true);
          // this.layerPanel.add(this.progressBar, BorderLayout.SOUTH);  //this line causes an error due to different CMS layer panel - twchoi

          shapePanel.add(originPoint);
          shapePanel.add(gridLines);
          shapePanel.add(gridPoints);
          shapePanel.add(intersectionPoints);
          shapePanel.add(intersectionLines);
          shapePanel.add(progressBar, BorderLayout.SOUTH); //

          buttonPanel = new JPanel(new GridLayout(1, 1, 5, 5));
          buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

          clearButton = new JButton("Clear Results");

          clearButton.addActionListener(e ->
          {
               lineOfSight.resetAll();
          });
          buttonPanel.add(clearButton);

          // Iterate through all components in shapePanel to set the checkboxes
          // to selected by default
          for (Component checkbox : shapePanel.getComponents())
          {
               if (checkbox instanceof JCheckBox)
               {
                    ((JCheckBox) checkbox).setSelected(true);
               }
          }

          // Scheduling this for "later" so that the actionlisteners don't fire
          // before the grid is actually shown
          setCheckboxListeners();

          //======== Outer Panel ======== 
//        JPanel outerPanel = new JPanel();
          JPanel outerPanel = mainPanel;
          outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
          // Add the border padding in the dialog
          outerPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), new TitledBorder("Line of Sight")));
          outerPanel.setToolTipText("Control Panel for Line of Sight Analysis");
          outerPanel.add(shapePanel, BorderLayout.CENTER);
          outerPanel.add(buttonPanel, BorderLayout.SOUTH);
          this.add(outerPanel, BorderLayout.NORTH);

          JPanel controlPanel = new JPanel(new GridLayout(0, 1, 0, 0));
          controlPanel.setBorder(
                  new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                          new TitledBorder("Elevation LOS Profile")));

          // Radius combo
          JPanel radiusPanel = new JPanel(new GridLayout(0, 2, 0, 0));
          radiusPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
          radiusPanel.add(new JLabel("Max radius:"));
          radiusCombo = new JComboBox(new String[]
          {
               "5km", "10km",
               "20km", "30km", "50km", "100km", "200km"
          });
          radiusCombo.setSelectedItem("10km");
          radiusPanel.add(radiusCombo);

          // Samples combo
          JPanel samplesPanel = new JPanel(new GridLayout(0, 2, 0, 0));
          samplesPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
          samplesPanel.add(new JLabel("Samples:"));
          samplesCombo = new JComboBox(new String[]
          {
               "128", "256", "512"
          });
          samplesCombo.setSelectedItem("128");
          samplesPanel.add(samplesCombo);

          // Shading checkbox
          JPanel shadingPanel = new JPanel(new GridLayout(0, 2, 0, 0));
          shadingPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
          shadingPanel.add(new JLabel("Light:"));
          shadingCheck = new JCheckBox("Add shading");
          shadingCheck.setSelected(false);
          shadingPanel.add(shadingCheck);

          // Compute button
          JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 0));
          buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
          computeButton = new JButton("Compute");
          computeButton.addActionListener(new ActionListener()
          {
               public void actionPerformed(ActionEvent actionEvent)
               {
                    update();
               }
          });
          buttonPanel.add(computeButton);

          // Help text
          JPanel helpPanel = new JPanel(new GridLayout(0, 1, 0, 0));
          buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
          helpPanel.add(new JLabel("Place view center on an elevated"));
          helpPanel.add(new JLabel("location and click \"Compute\""));

          // Panel assembly
          controlPanel.add(radiusPanel);
          controlPanel.add(samplesPanel);
          controlPanel.add(shadingPanel);
          controlPanel.add(buttonPanel);
          controlPanel.add(helpPanel);

          this.add(controlPanel, BorderLayout.CENTER);

     } // end makePanel();

     private void setCheckboxListeners()
     {
          SwingUtilities.invokeLater(() ->
          {
//            if(lineOfSightController.layersNotNull()){ 
//            }
               this.originPoint.addActionListener((e) ->
               {
                    if (lineOfSightController.layersNotNull())
                    {
                         lineOfSightController.toggleOrigin(((JCheckBox) e.getSource()).isSelected());
                    }
               });
               this.gridLines.addActionListener((e) ->
               {
                    if (lineOfSightController.layersNotNull())
                    {
                         lineOfSightController.toggleGridLines(((JCheckBox) e.getSource()).isSelected());
                    }
               });
               this.gridPoints.addActionListener((e) ->
               {
                    if (lineOfSightController.layersNotNull())
                    {
                         lineOfSightController.toggleGridPoints(((JCheckBox) e.getSource()).isSelected());
                    }
               });
               this.intersectionPoints.addActionListener((e) ->
               {
                    if (lineOfSightController.layersNotNull())
                    {
                         lineOfSightController.toggleIntersectionPoints(((JCheckBox) e.getSource()).isSelected());
                    }
               });
               this.intersectionLines.addActionListener((e) ->
               {
                    if (lineOfSightController.layersNotNull())
                    {
                         lineOfSightController.toggleIntersectionSightLines(((JCheckBox) e.getSource()).isSelected());
                    }
               });
          });

     }

     // Update line of sight computation
     private void update()
     {
          new Thread(new Runnable()
          {
               public void run()
               {
                    computeLineOfSight();
               }
          }, "LOS thread").start();
     }

     private void computeLineOfSight()
     {
          computeButton.setEnabled(false);
          computeButton.setText("Computing...");

          try
          {
               Globe globe = getWwd().getModel().getGlobe();
               OrbitView view = (OrbitView) getWwd().getView();
               Position centerPosition = view.getCenterPosition();

               // Compute sector
               String radiusString = ((String) radiusCombo.getSelectedItem());
               double radius = 1000 * Double.parseDouble(radiusString.substring(0, radiusString.length() - 2));
               double deltaLatRadians = radius / globe.getEquatorialRadius();
               double deltaLonRadians = deltaLatRadians / Math.cos(centerPosition.getLatitude().radians);
               Sector sector = new Sector(centerPosition.getLatitude().subtractRadians(deltaLatRadians),
                       centerPosition.getLatitude().addRadians(deltaLatRadians),
                       centerPosition.getLongitude().subtractRadians(deltaLonRadians),
                       centerPosition.getLongitude().addRadians(deltaLonRadians));

               // Compute center point
               double centerElevation = globe.getElevation(centerPosition.getLatitude(),
                       centerPosition.getLongitude());
               Vec4 center = globe.computePointFromPosition(
                       new Position(centerPosition, centerElevation + centerOffset));

               // Compute image
               float hueScaleFactor = .7f;
               int samples = Integer.parseInt((String) samplesCombo.getSelectedItem());
               BufferedImage image = new BufferedImage(samples, samples, BufferedImage.TYPE_4BYTE_ABGR);
               double latStepRadians = sector.getDeltaLatRadians() / image.getHeight();
               double lonStepRadians = sector.getDeltaLonRadians() / image.getWidth();
               for (int x = 0; x < image.getWidth(); x++)
               {
                    Angle lon = sector.getMinLongitude().addRadians(lonStepRadians * x + lonStepRadians / 2);
                    for (int y = 0; y < image.getHeight(); y++)
                    {
                         Angle lat = sector.getMaxLatitude().subtractRadians(latStepRadians * y + latStepRadians / 2);
                         double el = globe.getElevation(lat, lon);
                         // Test line of sight from point to center
                         Vec4 point = globe.computePointFromPosition(lat, lon, el + pointOffset);
                         double distance = point.distanceTo3(center);
                         if (distance <= radius)
                         {
                              if (RayCastingSupport.intersectSegmentWithTerrain(
                                      globe, point, center, samplingLength, samplingLength) == null)
                              {
                                   // Center visible from point: set pixel color and shade
                                   float hue = (float) Math.min(distance / radius, 1) * hueScaleFactor;
                                   float shade = shadingCheck.isSelected()
                                           ? (float) computeShading(globe, lat, lon, light, ambiant) : 0f;
                                   image.setRGB(x, y, Color.HSBtoRGB(hue, 1f, 1f - shade));
                              } else if (shadingCheck.isSelected())
                              {
                                   // Center not visible: apply shading nonetheless if selected
                                   float shade = (float) computeShading(globe, lat, lon, light, ambiant);
                                   image.setRGB(x, y, new Color(0f, 0f, 0f, shade).getRGB());
                              }
                         }
                    }
               }
               // Blur image
               PatternFactory.blur(PatternFactory.blur(PatternFactory.blur(PatternFactory.blur(image))));

               // Update surface image
               if (this.surfaceImage != null)
               {
                    this.renderableLayer.removeRenderable(this.surfaceImage);
               }
               this.surfaceImage = new SurfaceImage(image, sector);
               this.surfaceImage.setOpacity(.5);
               this.renderableLayer.addRenderable(this.surfaceImage);

               // Compute distance scale image
               BufferedImage scaleImage = new BufferedImage(64, 256, BufferedImage.TYPE_4BYTE_ABGR);
               Graphics g2 = scaleImage.getGraphics();
               int divisions = 10;
               int labelStep = scaleImage.getHeight() / divisions;
               for (int y = 0; y < scaleImage.getHeight(); y++)
               {
                    int x1 = scaleImage.getWidth() / 5;
                    if (y % labelStep == 0 && y != 0)
                    {
                         double d = radius / divisions * y / labelStep / 1000;
                         String label = Double.toString(d) + "km";
                         g2.setColor(Color.BLACK);
                         g2.drawString(label, x1 + 6, y + 6);
                         g2.setColor(Color.WHITE);
                         g2.drawLine(x1, y, x1 + 4, y);
                         g2.drawString(label, x1 + 5, y + 5);
                    }
                    float hue = (float) y / (scaleImage.getHeight() - 1) * hueScaleFactor;
                    g2.setColor(Color.getHSBColor(hue, 1f, 1f));
                    g2.drawLine(0, y, x1, y);
               }

               // Update distance scale screen annotation
               if (this.screenAnnotation != null)
               {
                    this.renderableLayer.removeRenderable(this.screenAnnotation);
               }
               this.screenAnnotation = new ScreenAnnotation("", new Point(20, 20));
               this.screenAnnotation.getAttributes().setImageSource(scaleImage);
               this.screenAnnotation.getAttributes().setSize(
                       new Dimension(scaleImage.getWidth(), scaleImage.getHeight()));
               this.screenAnnotation.getAttributes().setAdjustWidthToText(Annotation.SIZE_FIXED);
               this.screenAnnotation.getAttributes().setDrawOffset(new Point(scaleImage.getWidth() / 2, 0));
               this.screenAnnotation.getAttributes().setBorderWidth(0);
               this.screenAnnotation.getAttributes().setCornerRadius(0);
               this.screenAnnotation.getAttributes().setBackgroundColor(new Color(0f, 0f, 0f, 0f));
               this.renderableLayer.addRenderable(this.screenAnnotation);

               // Redraw
               this.getWwd().redraw();
          } finally
          {
               computeButton.setEnabled(true);
               computeButton.setText("Compute");
          }
     }

     /**
      * Compute shadow intensity at a globe position.
      *
      * @param globe the <code>Globe</code>.
      * @param lat the location latitude.
      * @param lon the location longitude.
      * @param light the light direction vector. Expected to be normalized.
      * @param ambiant the minimum ambiant light level (0..1).
      * @return the shadow intensity for the location. No shadow = 0, totaly
      * obscured = 1.
      */
     private static double computeShading(Globe globe, Angle lat, Angle lon, Vec4 light, double ambiant)
     {
          double thirtyMetersRadians = 30 / globe.getEquatorialRadius();
          Vec4 p0 = globe.computePointFromPosition(lat, lon, 0);
          Vec4 px = globe.computePointFromPosition(lat, Angle.fromRadians(lon.radians - thirtyMetersRadians), 0);
          Vec4 py = globe.computePointFromPosition(Angle.fromRadians(lat.radians + thirtyMetersRadians), lon, 0);

          double el0 = globe.getElevation(lat, lon);
          double elx = globe.getElevation(lat, Angle.fromRadians(lon.radians - thirtyMetersRadians));
          double ely = globe.getElevation(Angle.fromRadians(lat.radians + thirtyMetersRadians), lon);

          Vec4 vx = new Vec4(p0.distanceTo3(px), 0, elx - el0).normalize3();
          Vec4 vy = new Vec4(0, p0.distanceTo3(py), ely - el0).normalize3();
          Vec4 normal = vx.cross3(vy).normalize3();

          return 1d - Math.max(-light.dot3(normal), ambiant);
     }

     /**
      * @return the wwd
      */
     public WorldWindow getWwd()
     {
          return wwd;
     }

     /**
      * @param wwd the wwd to set
      */
     public void setWwd(WorldWindow wwd)
     {
          this.wwd = wwd;
     }

     public JProgressBar getProgressBar()
     {
          return this.progressBar;
     }

     public void setProgressBar(JProgressBar progressBar)
     {
          this.progressBar = progressBar;
     }

     public void updateProgressBar(int progress)
     {
          this.progressBar.setValue(progress);
     }

     public void updateProgressBar(String update)
     {
          this.progressBar.setString(update);
     }

     public JDialog getDialog()
     {
          return dialog;
     }

     public void setDialog(JDialog dialog)
     {
          this.dialog = dialog;
     }

     public JPanel getMainPanel()
     {
          return mainPanel;
     }

     public void setMainPanel(JPanel mainPanel)
     {
          this.mainPanel = mainPanel;
     }

     public JCheckBox getGridLines()
     {
          return gridLines;
     }

     public void setGridLines(JCheckBox gridLines)
     {
          this.gridLines = gridLines;
     }

     public JCheckBox getGridPoints()
     {
          return gridPoints;
     }

     public void setGridPoints(JCheckBox gridPoints)
     {
          this.gridPoints = gridPoints;
     }

     public JCheckBox getIntersectionPoints()
     {
          return intersectionPoints;
     }

     public void setIntersectionPoints(JCheckBox intersectionPoints)
     {
          this.intersectionPoints = intersectionPoints;
     }

     public JCheckBox getIntersectionLines()
     {
          return intersectionLines;
     }

     public void setIntersectionLines(JCheckBox intersectionLines)
     {
          this.intersectionLines = intersectionLines;
     }

     public JCheckBox getOriginPoint()
     {
          return originPoint;
     }

     public void setOriginPoint(JCheckBox originPoint)
     {
          this.originPoint = originPoint;
     }

}
