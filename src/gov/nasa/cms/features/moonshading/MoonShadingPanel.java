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
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author kjdickin
 */
// Creates the JPanel to be added to the dialog
public class MoonShadingPanel extends JPanel 
{  
    private WorldWindow wwd;
    private JCheckBox enableCheckBox;
    private JButton colorButton;
    private JButton ambientButton;
    private JButton dateTimePickerButton;
    private JRadioButton relativeRadioButton;
    private JRadioButton absoluteRadioButton;
    private JSlider azimuthSlider;
    private JSlider elevationSlider;

    private RectangularNormalTessellator tessellator;

    private LensFlareLayer lensFlareLayer;
    private AtmosphereLayer atmosphereLayer;
    private SunPositionProvider spp = new BasicSunPositionProvider();
    private Vec4 sun, light;
    private DateTimePickerDialog dateTimeDialog;
    private CelestialMapper cms;
    
    public MoonShadingPanel(WorldWindow wwdObject) {
        super(new BorderLayout()); // Create the border layerout
        this.wwd = wwdObject; // Set up the WorldWindow
        JPanel mainPanel = new JPanel(); // Create the panel
        mainPanel.setOpaque(false);
        this.makeControlPanel(mainPanel); // Create the moon shading panels and add to mainPanel

    }
   
    // Reset moon shading in progress
    public void resetMoonShadingProperties()
    {
        this.colorButton.setEnabled(false);
        this.ambientButton.setEnabled(false);
        this.absoluteRadioButton.setEnabled(false);
        this.relativeRadioButton.setEnabled(false);
        this.azimuthSlider.setEnabled(false);
        this.elevationSlider.setEnabled(false);
        // Turn off lighting
        this.tessellator.setLightDirection(null);
        this.lensFlareLayer.setSunDirection(null);
        this.atmosphereLayer.setSunDirection(null);
        this.lensFlareLayer.setEnabled(false);
        this.getWwd().getModel().getLayers().remove(lensFlareLayer);
        
        this.getWwd().redraw();
    }

    private void makeControlPanel(JPanel panel) 
    {

        // Replace sky gradient with atmosphere layer
        this.atmosphereLayer = new AtmosphereLayer();
        for (int i = 0; i < this.getWwd().getModel().getLayers().size(); i++) {
            Layer l = this.getWwd().getModel().getLayers().get(i);
            if (l instanceof SkyGradientLayer) {
                this.getWwd().getModel().getLayers().set(i, this.atmosphereLayer);
            }
        }

        // Add lens flare layer
        this.lensFlareLayer = LensFlareLayer.getPresetInstance(LensFlareLayer.PRESET_BOLD);
        this.getWwd().getModel().getLayers().add(this.lensFlareLayer);

        // Set tessellator
        this.tessellator = new RectangularNormalTessellator();
        this.getWwd().getModel().getGlobe().setTessellator(tessellator);
        System.out.println(this.getWwd().getModel().getGlobe().getTessellator());
        System.out.println(this.getWwd().getModel().getGlobe().getTessellator().getClass());
                
        // Add position listener to update light direction relative to the eye
        getWwd().addPositionListener(new PositionListener() {
            Vec4 eyePoint;

            public void moved(PositionEvent event) {
                if (eyePoint == null || eyePoint.distanceTo3(getWwd().getView().getEyePoint()) > 1000) {
                    update();
                    eyePoint = getWwd().getView().getEyePoint();
                }
            }
        });
        
        // Enable and Color
        final JPanel colorPanel = new JPanel(new GridLayout(0, 3, 0, 0));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        enableCheckBox = new JCheckBox("Enable");
        enableCheckBox.setSelected(true);
        enableCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });  

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Sun Light")));
        controlPanel.setToolTipText("Set the Sun light direction and color");
         colorPanel.add(enableCheckBox);
         
        // Add one minute update timer
        Timer updateTimer = new Timer(60000, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                update();
            }
        });
        updateTimer.start();      

        colorButton = new JButton("Light");
        colorButton.setBackground(this.tessellator.getLightColor());       
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Color c = JColorChooser.showDialog(colorPanel, "Choose a color...",
                        ((JButton) event.getSource()).getBackground());
                if (c != null) {
                    ((JButton) event.getSource()).setBackground(c);
                    update();
                }
            }
        });
        colorPanel.add(colorButton);

        ambientButton = new JButton("Shade");
        ambientButton.setBackground(this.tessellator.getAmbientColor());
        ambientButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Color c = JColorChooser.showDialog(colorPanel, "Choose a color...",
                        ((JButton) event.getSource()).getBackground());
                if (c != null) {
                    ((JButton) event.getSource()).setBackground(c);
                    update();
                }
            }
        });
        colorPanel.add(ambientButton);

        // Relative vs absolute Sun position
        final JPanel positionTypePanel = new JPanel(new GridLayout(0, 2, 0, 0));
        positionTypePanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        relativeRadioButton = new JRadioButton("Relative");
        relativeRadioButton.setSelected(false);
        relativeRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                update();
            }
        });
        positionTypePanel.add(relativeRadioButton);
        absoluteRadioButton = new JRadioButton("Absolute");
        absoluteRadioButton.setSelected(true);
        absoluteRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                update();
            }
        });
        positionTypePanel.add(absoluteRadioButton);
        ButtonGroup group = new ButtonGroup();
        group.add(relativeRadioButton);
        group.add(absoluteRadioButton);

        // Azimuth slider
        JPanel azimuthPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        azimuthPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        azimuthPanel.add(new JLabel("Azimuth:"));
        azimuthSlider = new JSlider(0, 360, 125);
        azimuthSlider.setPaintTicks(true);
        azimuthSlider.setPaintLabels(true);
        azimuthSlider.setMajorTickSpacing(90);
        azimuthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
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
        elevationSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                update();
            }
        });
        elevationPanel.add(elevationSlider);
        
        dateTimePickerButton = new JButton("Date/Time Picker");    
        dateTimePickerButton.setToolTipText("Select a date and time for simulation");
        dateTimePickerButton.addActionListener(new ActionListener() 
         {
             
            public void actionPerformed(ActionEvent event) 
            {
                 if(dateTimeDialog == null)
                 {
                      dateTimeDialog = new DateTimePickerDialog(wwd, cms);
                      //spp.updateDateTime();
                 }      
                 dateTimeDialog.setVisible(true);
            }
        });

        

        // Control panel assembly
        controlPanel.add(colorPanel);
        controlPanel.add(positionTypePanel);
        controlPanel.add(azimuthPanel);
        controlPanel.add(elevationPanel);
        controlPanel.add(dateTimePickerButton);
        this.add(controlPanel, BorderLayout.NORTH);
        
        update();
    }
    
    // Update worldwind
    public void update() {
        if (this.enableCheckBox.isSelected()) {
            // Enable UI controls
            if(lensFlareLayer == null)
            {
                this.getWwd().getModel().getLayers().add(lensFlareLayer);
            }
            lensFlareLayer.setEnabled(true);

            this.colorButton.setEnabled(true);
            this.ambientButton.setEnabled(true);
            this.absoluteRadioButton.setEnabled(true);
            this.relativeRadioButton.setEnabled(true);
            this.azimuthSlider.setEnabled(true);
            this.elevationSlider.setEnabled(true);
            
            // Update colors
               this.tessellator.setLightColor(this.colorButton.getBackground());
               this.tessellator.setAmbientColor(this.ambientButton.getBackground());
 
            if (this.relativeRadioButton.isSelected()) {
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
            } else if (this.absoluteRadioButton.isSelected()) { // Absolute is selected
                // Disable UI controls
                this.azimuthSlider.setEnabled(false);
                this.elevationSlider.setEnabled(false);
                // TO-DO: Move to a separate function not linked to Absolute
                spp.updateCurrentDateTime(); // Update the elevation & azmith based on current date/time
                LatLon sunPos = spp.getPosition();
                sun = getWwd().getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
            }
          
            
            light = sun.getNegative3();
             
            this.tessellator.setLightDirection(light);
            this.lensFlareLayer.setSunDirection(sun);
            this.atmosphereLayer.setSunDirection(sun);
        } else {
            // Disable UI controls
            this.colorButton.setEnabled(false);
            this.ambientButton.setEnabled(false);
            this.absoluteRadioButton.setEnabled(false);
            this.relativeRadioButton.setEnabled(false);
            this.azimuthSlider.setEnabled(false);
            this.elevationSlider.setEnabled(false);
            // Turn off lighting
            this.tessellator.setLightDirection(null);
            this.lensFlareLayer.setSunDirection(null);
            this.atmosphereLayer.setSunDirection(null);
            this.lensFlareLayer.setEnabled(false);
            
        }
        // Redraw
        this.getWwd().redraw();
    }
    
    protected WorldWindow getWwd() {
        return this.wwd;
    }

    protected void setWwd(WorldWindow Wwd) {
        this.wwd = Wwd;
    }
    protected SunPositionProvider getSpp(){
        return this.spp;
    }
    protected RectangularNormalTessellator getTessellator(){
        return this.tessellator;
    }
    protected LensFlareLayer getLensFlareLayer(){
        return this.lensFlareLayer;
    }
    protected AtmosphereLayer getAtmosphereLayer(){
        return this.atmosphereLayer;
    }
    
}


