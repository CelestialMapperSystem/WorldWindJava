/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.cms.util.PanelTitle;
import gov.nasa.cms.util.ShadedPanel;
import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.LayerPath;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;

/**
 * Panel containing functionality for importing imagery & elevations.
 * Utilized in <code>{@link gov.nasa.cms.features.ImportedDataDialog}</code>
 * @author kjdickin
 */
public class ImportedDataPanel extends ShadedPanel
{

    private WorldWindow wwd;
    protected JPanel dataConfigPanel;
    private JPanel buttonPanel ;

    public ImportedDataPanel(WorldWindow wwd)
    {
        super(new BorderLayout());

        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.wwd = wwd;
        this.makePanel();
    }

    protected void makePanel()
    {
        this.add(new PanelTitle("Imported Data", SwingConstants.CENTER), BorderLayout.NORTH);

        this.dataConfigPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        this.dataConfigPanel.setOpaque(false);
        this.dataConfigPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right

        // Put the grid in a container to prevent scroll panel from stretching its vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.setOpaque(false);
        dummyPanel.add(this.dataConfigPanel, BorderLayout.NORTH);

        // Add the dummy panel to a scroll pane.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // top, left, bottom, right

        // Add the scroll pane to a titled panel that will resize with the main window.
        JPanel bodyPanel = new JPanel(new GridLayout(0, 1, 0, 10)); // rows, cols, hgap, vgap
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("")));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        this.add(buttonPanel, BorderLayout.SOUTH);
        this.add(bodyPanel, BorderLayout.CENTER);
    }

    /**
     * Adds the UI components for the specified imported data to this panel, and
     * adds the WorldWind component created from the data to the WorldWindow
     * passed to this panel during construction.
     *
     * @param domElement the document which describes a WorldWind data
     * configuration.
     * @param params the parameter list which overrides or extends information
     * contained in the document.
     *
     * @throws IllegalArgumentException if the Element is null.
     */
    public void addImportedData(final Element domElement, final AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addToWorldWindow(domElement, params);

        String description = this.getDescription(domElement);
        Sector sector = this.getSector(domElement);

        Box box = Box.createHorizontalBox();
        box.setOpaque(false);
        box.add(new JButton(new GoToSectorAction(sector)));
        box.add(Box.createHorizontalStrut(10));
        JLabel descLabel = new JLabel(description);
        descLabel.setOpaque(false);
        box.add(descLabel);

        this.dataConfigPanel.add(box);
        this.revalidate();
    }

    protected String getDescription(Element domElement)
    {
        String displayName = DataConfigurationUtils.getDataConfigDisplayName(domElement);
        String type = DataConfigurationUtils.getDataConfigType(domElement);

        StringBuilder sb = new StringBuilder(displayName);

        if (type.equalsIgnoreCase("Layer"))
        {
            sb.append(" (Layer)");
        } else if (type.equalsIgnoreCase("ElevationModel"))
        {
            sb.append(" (Elevations)");
        }

        return sb.toString();
    }

    protected Sector getSector(Element domElement)
    {
        return WWXML.getSector(domElement, "Sector", null);
    }

    protected void addToWorldWindow(Element domElement, AVList params)
    {
        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type == null)
        {
            return;
        }

        if (type.equalsIgnoreCase("Layer"))
        {
            this.addLayerToWorldWindow(domElement, params);
        } else if (type.equalsIgnoreCase("ElevationModel"))
        {
            this.addElevationModelToWorldWindow(domElement, params);
        }
    }

    protected void addLayerToWorldWindow(Element domElement, AVList params)
    {
        try
        {
            Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
            Layer layer = (Layer) factory.createFromConfigSource(domElement, params);
            if (layer != null)
            {
                layer.setEnabled(true);
                this.addLayer(layer, new LayerPath("Imported"));
            }
        } catch (Exception e)
        {
            String message = Logging.getMessage("generic.CreationFromConfigurationFailed",
                    DataConfigurationUtils.getDataConfigDisplayName(domElement));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected void addLayer(final Layer layer, final LayerPath pathToParent)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                LayerPath path = new LayerPath(pathToParent, layer.getName());
                doAddLayer(layer, path);
            }
        });
    }

    protected void doAddLayer(final Layer layer, final LayerPath path)
    {
        wwd.getModel().getLayers().add(layer);
    }

    protected void addElevationModelToWorldWindow(Element domElement, AVList params)
    {
        ElevationModel em = null;
        try
        {
            Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
            em = (ElevationModel) factory.createFromConfigSource(domElement, params);
        } catch (Exception e)
        {
            String message = Logging.getMessage("generic.CreationFromConfigurationFailed",
                    DataConfigurationUtils.getDataConfigDisplayName(domElement));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        if (em == null)
        {
            return;
        }

        ElevationModel defaultElevationModel = wwd.getModel().getGlobe().getElevationModel();
        if (defaultElevationModel instanceof CompoundElevationModel)
        {
            if (!((CompoundElevationModel) defaultElevationModel).containsElevationModel(em))
            {
                ((CompoundElevationModel) defaultElevationModel).addElevationModel(em);
            }
        } else
        {
            CompoundElevationModel cm = new CompoundElevationModel();
            cm.addElevationModel(defaultElevationModel);
            cm.addElevationModel(em);
            wwd.getModel().getGlobe().setElevationModel(cm);
        }
    }

    protected class GoToSectorAction extends AbstractAction
    {

        protected Sector sector;

        public GoToSectorAction(Sector sector)
        {
            super("Go To");
            this.sector = sector;
            this.setEnabled(this.sector != null);
        }

        public void actionPerformed(ActionEvent e)
        {
            Extent extent = Sector.computeBoundingCylinder(wwd.getModel().getGlobe(),
                    wwd.getSceneController().getVerticalExaggeration(), this.sector);

            Angle fov = wwd.getView().getFieldOfView();
            Position centerPos = new Position(this.sector.getCentroid(), 0d);
            double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

            wwd.getView().goTo(centerPos, zoom);
        }
    }

    public JPanel getButtonPanel()
    {
        return buttonPanel;
    }
}
