package gov.nasa.cms.features.moonshading;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Feature for viewing shading and illumination of the moon. Users can either
 * modify the elevation and azimuth manually or use the
 * {@link gov.nasa.cms.features.moonshading.DateTimePickerDialog} to set a start
 * and end date and simulate the shading dynamically.
 *
 * @see gov.nasa.cms.features.moonshading.MoonShadingDialog
 * @author kjdickin
 */
public class MoonShadingPanel extends JPanel
{

    private WorldWindow wwd;
    private JButton colorButton;
    private JButton ambientButton;
    private JButton dateTimePickerButton;
    private JButton coordinatesButton;
    private JSlider azimuthSlider;
    private JSlider elevationSlider;
    private JCheckBox timeFrameCheckBox;

    private RectangularNormalTessellator tessellator;

    private LensFlareLayer lensFlareLayer;
    private Vec4 sun, light;
    private DateTimePickerDialog dateTimeDialog;
    private CelestialMapper cms;
    private CoordinatesDialog coordinatesDialog;

    private Date startDate;
    private Date endDate;

    public MoonShadingPanel(WorldWindow wwdObject)
    {
        super(new BorderLayout()); 
        this.wwd = wwdObject; // Set up the WorldWindow
        JPanel mainPanel = new JPanel(); // Create the panel
        mainPanel.setOpaque(false);
        this.makeControlPanel(mainPanel); // Create the moon shading panels and add to mainPanel

    }

    // Reset moon shading properties (all buttons and layers)
    public void resetMoonShadingProperties()
    {
        this.colorButton.setEnabled(false);
        this.ambientButton.setEnabled(false);
        this.azimuthSlider.setEnabled(false);
        this.elevationSlider.setEnabled(false);
        // Turn off lighting
        this.tessellator.setLightDirection(null);
        this.lensFlareLayer.setSunDirection(null);
        this.lensFlareLayer.setEnabled(false);
        this.getWwd().getModel().getLayers().remove(lensFlareLayer);

        this.getWwd().redraw();
    }

    private void makeControlPanel(JPanel panel)
    {
        // Add lens flare layer
        this.lensFlareLayer = LensFlareLayer.getPresetInstance(LensFlareLayer.PRESET_BOLD);
        this.getWwd().getModel().getLayers().add(this.lensFlareLayer);

        // Set tessellator
        this.tessellator = new RectangularNormalTessellator();
        this.getWwd().getModel().getGlobe().setTessellator(tessellator);
        System.out.println(this.getWwd().getModel().getGlobe().getTessellator());
        System.out.println(this.getWwd().getModel().getGlobe().getTessellator().getClass());

        // Add position listener to update light direction relative to the eye
        getWwd().addPositionListener(new PositionListener()
        {
            Vec4 eyePoint;

            public void moved(PositionEvent event)
            {
                if (eyePoint == null || eyePoint.distanceTo3(getWwd().getView().getEyePoint()) > 1000)
                {
                    // TO-DO: Find way to enable shading upon starting
                    // Including the update() function here changes the shading back to the eye position after user enters new date & time
                    // update();
                    eyePoint = getWwd().getView().getEyePoint();
                }
            }
        });

        // Time Frame
        JPanel timeFramePanel = new JPanel(new GridLayout(0, 1, 0, 0));
        timeFramePanel.setToolTipText("Show or hide the time frame layer");
        timeFrameCheckBox = new JCheckBox("Show / hide time frame");
        timeFrameCheckBox.setSelected(true);
        timeFramePanel.add(timeFrameCheckBox);
        
        // Color
        final JPanel colorPanel = new JPanel(new GridLayout(0, 3, 0, 0));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Sun Light")));
        controlPanel.setToolTipText("Set the Sun light direction and color");

        // Add one minute update timer
        Timer updateTimer = new Timer(60000, new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                update();
            }
        });
        updateTimer.start();

        colorButton = new JButton("Light");
        colorButton.setBackground(this.tessellator.getLightColor());
        colorButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                Color c = JColorChooser.showDialog(colorPanel, "Choose a color...",
                        ((JButton) event.getSource()).getBackground());
                if (c != null)
                {
                    ((JButton) event.getSource()).setBackground(c);
                    update();
                }
            }
        });
        colorPanel.add(colorButton);

        ambientButton = new JButton("Shade");
        ambientButton.setBackground(this.tessellator.getAmbientColor());
        ambientButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                Color c = JColorChooser.showDialog(colorPanel, "Choose a color...",
                        ((JButton) event.getSource()).getBackground());
                if (c != null)
                {
                    ((JButton) event.getSource()).setBackground(c);
                    update();
                }
            }
        });
        colorPanel.add(ambientButton);

        // Azimuth slider
        JPanel azimuthPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        azimuthPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        azimuthPanel.add(new JLabel("Azimuth:"));
        azimuthSlider = new JSlider(0, 360, 125);
        azimuthSlider.setPaintTicks(true);
        azimuthSlider.setPaintLabels(true);
        azimuthSlider.setMajorTickSpacing(90);
        azimuthSlider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                update();
            }
        });
        azimuthPanel.add(azimuthSlider);

        // Elevation slider
        JPanel elevationPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        elevationPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        elevationPanel.add(new JLabel("Elevation:"));
        elevationSlider = new JSlider(-10, 90, 50);
        elevationSlider.setPaintTicks(true);
        elevationSlider.setPaintLabels(true);
        elevationSlider.setMajorTickSpacing(10);
        elevationSlider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                update();
            }
        });
        elevationPanel.add(elevationSlider);

        //Coordinate Zoom-In Feature
        coordinatesButton = new JButton("Coordinates");
        coordinatesButton.setToolTipText("Enter the coordinates");
        coordinatesButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent event)
            {
                if (coordinatesDialog == null)
                {
                    cms = new CelestialMapper();
                    coordinatesDialog = new CoordinatesDialog(wwd, cms);

                    coordinatesDialog.setVisible(true);
                }
            }
        });
        
        // Control panel assembly
        controlPanel.add(timeFramePanel);
        controlPanel.add(colorPanel);
        controlPanel.add(azimuthPanel);
        controlPanel.add(elevationPanel);
        controlPanel.add(coordinatesButton);
        this.add(controlPanel, BorderLayout.NORTH);
    }

    // Update light and sun direction
    public void update()
    {
        // Enable UI controls
        if (lensFlareLayer == null)
        {
            this.getWwd().getModel().getLayers().add(lensFlareLayer);
        }
        lensFlareLayer.setEnabled(true);

        this.colorButton.setEnabled(true);
        this.azimuthSlider.setEnabled(true);
        this.elevationSlider.setEnabled(true);

        // Update colors
        this.tessellator.setLightColor(this.colorButton.getBackground());
        this.tessellator.setAmbientColor(this.ambientButton.getBackground());

        // Enable UI controls
        this.azimuthSlider.setEnabled(true);
        this.elevationSlider.setEnabled(true);
        // Compute Sun position relative to the eye position
        Angle elevation = Angle.fromDegrees(this.elevationSlider.getValue());
        Angle azimuth = Angle.fromDegrees(this.azimuthSlider.getValue());
        Position eyePos = getWwd().getView().getEyePosition();
        sun = Vec4.UNIT_Y;
        sun = sun.transformBy3(Matrix.fromRotationX(elevation));
        sun = sun.transformBy3(Matrix.fromRotationZ(azimuth.multiply(-1)));
        sun = sun.transformBy3(getWwd().getModel().getGlobe().computeModelCoordinateOriginTransform(
                eyePos.getLatitude(), eyePos.getLongitude(), 0));

        light = sun.getNegative3();

        this.tessellator.setLightDirection(light);
        this.lensFlareLayer.setSunDirection(sun);

        // Redraw
        this.getWwd().redraw();
    }
    
    protected WorldWindow getWwd()
    {
        return this.wwd;
    }

    protected void setWwd(WorldWindow Wwd)
    {
        this.wwd = Wwd;
    }

    public Vec4 getSun()
    {
        return sun;
    }
    
    public Vec4 getLight()
    {
        return light;
    }
    
    public RectangularNormalTessellator getTessellator()
    {
        return tessellator;
    }
    
    public LensFlareLayer getLensFlareLayer()
    {
        return lensFlareLayer;
    }
    
    public JCheckBox getTimeFrameCheckBox()
    {
        return timeFrameCheckBox;
    }
}


