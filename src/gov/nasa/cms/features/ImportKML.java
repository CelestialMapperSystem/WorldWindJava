/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.AppFrame;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.KMLLayerTreeNode;
import gov.nasa.worldwind.util.layertree.KMLNetworkLinkTreeNode;
import gov.nasa.worldwind.util.layertree.LayerTree;
import gov.nasa.worldwindx.examples.kml.KMLApplicationController;
import gov.nasa.worldwindx.examples.util.BalloonController;
import gov.nasa.worldwindx.examples.util.HotSpotController;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author twchoi
 */
public class ImportKML extends JMenuItem
{

    private WorldWindow wwd;
    protected LayerTree layerTree;
    protected RenderableLayer hiddenLayer;

    protected HotSpotController hotSpotController;
    protected KMLApplicationController kmlAppController;
    protected BalloonController balloonController;

    // Initializes an Import KML JMenuItem feature which is added to the passed in JMenu
    public ImportKML(AppFrame cms, WorldWindow wwd, JMenu menu)
    {
        super("KML");
        this.wwd = wwd;
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KML/KMZ File", "kml", "kmz"));

        JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Import KML File")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    int status = fileChooser.showOpenDialog(cms);
                    if (status == JFileChooser.APPROVE_OPTION)
                    {
                        for (File file : fileChooser.getSelectedFiles())
                        {
                            new WorkerThread(file, cms).start();
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        });
        menu.add(openFileMenuItem);

        // Add a controller to handle input events on the layer selector and on browser balloons.
        hotSpotController = new HotSpotController(wwd);

        // Add a controller to handle common KML application events.
        kmlAppController = new KMLApplicationController(wwd);

        // Add a controller to display balloons when placemarks are clicked. We override the method addDocumentLayer
        // so that loading a KML document by clicking a KML balloon link displays an entry in the on-screen layer
        // tree.
        balloonController = new BalloonController(wwd)
        {
            @Override
            protected void addDocumentLayer(KMLRoot document)
            {
                addKMLLayer(document);
            }
        };

        // Give the KML app controller a reference to the BalloonController so that the app controller can open
        // KML feature balloons when feature's are selected in the on-screen layer tree.
        kmlAppController.setBalloonController(balloonController);

        //makeMenu(this);  twchoi
        // Set up to receive SSLHandshakeExceptions that occur during resource retrieval.
        WorldWind.getRetrievalService().setSSLExceptionListener(new RetrievalService.SSLExceptionListener()
        {
            public void onException(Throwable e, String path)
            {
                System.out.println(path);
                System.out.println(e);
            }
        });
    }

    /**
     * Adds the specified <code>kmlRoot</code> to this app frame's
     * <code>WorldWindow</code> as a new <code>Layer</code>, and adds a new
     * <code>KMLLayerTreeNode</code> for the <code>kmlRoot</code> to this app
     * frame's on-screen layer tree.
     * <p>
     * This expects the <code>kmlRoot</code>'s <code>AVKey.DISPLAY_NAME</code>
     * field to contain a display name suitable for use as a layer name.
     *
     * @param kmlRoot the KMLRoot to add a new layer for.
     */
    protected void addKMLLayer(KMLRoot kmlRoot)
    {
        // Create a KMLController to adapt the KMLRoot to the WorldWind renderable interface.
        KMLController kmlController = new KMLController(kmlRoot);

        // Adds a new layer containing the KMLRoot to the end of the WorldWindow's layer list. This
        // retrieves the layer name from the KMLRoot's DISPLAY_NAME field.
        RenderableLayer layer = new RenderableLayer();
        layer.setName((String) kmlRoot.getField(AVKey.DISPLAY_NAME));
        layer.addRenderable(kmlController);
        this.getWwd().getModel().getLayers().add(layer);

        // Adds a new layer tree node for the KMLRoot to the on-screen layer tree, and makes the new node visible
        // in the tree. This also expands any tree paths that represent open KML containers or open KML network
        // links.
        KMLLayerTreeNode layerNode = new KMLLayerTreeNode(layer, kmlRoot);
        layerTree.getModel().addLayer(layerNode);
        layerTree.makeVisible(layerNode.getPath());
        layerNode.expandOpenContainers(layerTree);

        // Listens to refresh property change events from KML network link nodes. Upon receiving such an event this
        // expands any tree paths that represent open KML containers. When a KML network link refreshes, its tree
        // node replaces its children with new nodes created from the refreshed content, then sends a refresh
        // property change event through the layer tree. By expanding open containers after a network link refresh,
        // we ensure that the network link tree view appearance is consistent with the KML specification.
        layerNode.addPropertyChangeListener(AVKey.RETRIEVAL_STATE_SUCCESSFUL, new PropertyChangeListener()
        {
            public void propertyChange(final PropertyChangeEvent event)
            {
                if (event.getSource() instanceof KMLNetworkLinkTreeNode)
                {
                    // Manipulate the tree on the EDT.
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            ((KMLNetworkLinkTreeNode) event.getSource()).expandOpenContainers(layerTree);
                            wwd.redraw();
                        }
                    });
                }
            }
        });
    }

    /**
     * A <code>Thread</code> that loads a KML file and displays it in an
     * <code>AppFrame</code>.
     */
    public class WorkerThread extends Thread
    {

        /**
         * Indicates the source of the KML file loaded by this thread.
         * Initialized during construction.
         */
        protected Object kmlSource;
        /**
         * Indicates the <code>AppFrame</code> the KML file content is displayed
         * in. Initialized during construction.
         */
        protected AppFrame cms;

        /**
         * Creates a new worker thread from a specified <code>kmlSource</code>
         * and <code>appFrame</code>.
         *
         * @param kmlSource the source of the KML file to load. May be a
         * {@link File}, a {@link URL}, or an {@link
         *                  java.io.InputStream}, or a {@link String} identifying a file path or
         * URL.
         * @param cms the <code>AppFrame</code> in which to display the KML
         * source.
         */
        public WorkerThread(Object kmlSource, AppFrame cms)
        {
            this.kmlSource = kmlSource;
            this.cms = cms;
        }

        /**
         * Loads this worker thread's KML source into a new
         * <code>{@link gov.nasa.worldwind.ogc.kml.KMLRoot}</code>, then adds
         * the new <code>KMLRoot</code> to this worker thread's
         * <code>AppFrame</code>. The <code>KMLRoot</code>'s
         * <code>AVKey.DISPLAY_NAME</code> field contains a display name created
         * from either the KML source or the KML root feature name.
         * <p>
         * If loading the KML source fails, this prints the exception and its
         * stack trace to the standard error stream, but otherwise does nothing.
         */
        public void run()
        {
            try
            {
                KMLRoot kmlRoot = this.parse();

                // Set the document's display name
                kmlRoot.setField(AVKey.DISPLAY_NAME, formName(kmlSource, kmlRoot));

                // Schedule a task on the EDT to add the parsed document to a layer
                final KMLRoot finalKMLRoot = kmlRoot;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        addKMLLayer(finalKMLRoot);
                    }
                });
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        /**
         * Parse the KML document.
         *
         * @return The parsed document.
         *
         * @throws IOException if the document cannot be read.
         * @throws XMLStreamException if document cannot be parsed.
         */
        protected KMLRoot parse() throws IOException, XMLStreamException
        {
            // KMLRoot.createAndParse will attempt to parse the document using a namespace aware parser, but if that
            // fails due to a parsing error it will try again using a namespace unaware parser. Note that this second
            // step may require the document to be read from the network again if the kmlSource is a stream.
            return KMLRoot.createAndParse(this.kmlSource);
        }
    }

    protected static String formName(Object kmlSource, KMLRoot kmlRoot)
    {
        KMLAbstractFeature rootFeature = kmlRoot.getFeature();

        if (rootFeature != null && !WWUtil.isEmpty(rootFeature.getName()))
        {
            return rootFeature.getName();
        }

        if (kmlSource instanceof File)
        {
            return ((File) kmlSource).getName();
        }

        if (kmlSource instanceof URL)
        {
            return ((URL) kmlSource).getPath();
        }

        if (kmlSource instanceof String && WWIO.makeURL((String) kmlSource) != null)
        {
            return WWIO.makeURL((String) kmlSource).getPath();
        }

        return "KML Layer";
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
