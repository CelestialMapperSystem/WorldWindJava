/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.features.wms;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.wms.*;
import gov.nasa.worldwindx.applications.worldwindow.core.Controller;
import gov.nasa.worldwindx.applications.worldwindow.features.*;
import gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import gov.nasa.worldwindx.examples.WMSLayersPanel;
//import gov.nasa.worldwindx.examples.WMSLayersPanel;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public class WMSLegendRetriever implements NetworkActivitySignal.NetworkUser
{
    private final CelestialMapper cms;
    private CmsWmsLayersPanel wmsPanel;
    private String serverAddress;
    private URI serverURI;
    private Thread loadingThread;
    private Controller controller;
    protected static final String FEATURE_TITLE = "WMS Server Panel";
    protected static final String ICON_PATH = "gov/nasa/worldwindx/applications/worldwindow/images/wms-64x64.png";
//    protected static final String[] INITIAL_SERVER_LIST = new String[]
//        {
//            "http://neowms.sci.gsfc.nasa.gov/wms/wms",
//            "http://giifmap.cnr.berkeley.edu/cgi-bin/naip.wms?",
//            "http://wms.jpl.nasa.gov/wms.cgi"};

    protected LayerTree layerTree;
    protected JTextField nameField;
    protected JTextField urlField;
    protected JButton infoButton;
    private WMSCapabilities caps;
    protected final WorldWindow wwd;
    protected final Dimension size;
    protected final TreeSet<CmsWmsLayersPanel.LayerInfo> layerInfos = new TreeSet<CmsWmsLayersPanel.LayerInfo>(new Comparator<CmsWmsLayersPanel.LayerInfo>()
    {
        public int compare(CmsWmsLayersPanel.LayerInfo infoA, CmsWmsLayersPanel.LayerInfo infoB)
        {
            String nameA = infoA.getName();
            String nameB = infoB.getName();
            return nameA.compareTo(nameB);
        }
    });

    public WMSLegendRetriever(CelestialMapper cms)
    {
        this.cms = cms;
        this.wwd = this.cms.getWwd();
        this.size = new Dimension(200,400);

        try
        {
            this.serverAddress = "http://neowms.sci.gsfc.nasa.gov/wms/wms";
            this.wmsPanel = new CmsWmsLayersPanel(this.wwd, serverAddress,size);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    public CmsWmsLayersPanel getWmsPanel()
    {
        return wmsPanel;
    }

    public String getServerAddress()
    {
        return serverAddress;
    }

    public URI getServerURI()
    {
        return serverURI;
    }

    public Thread getLoadingThread()
    {
        return loadingThread;
    }

    public Controller getController()
    {
        return controller;
    }

    public WMSCapabilities getCaps()
    {
        return caps;
    }

    public void contactWMSServer(String URLString) throws URISyntaxException
    {
        this.serverURI = new URI(URLString.trim()); // throws an exception if server name is not a valid uri.

        // Thread off a retrieval of the server's capabilities document and update of this panel.
        this.loadingThread = new Thread(new Runnable()
        {
            private WMSCapabilities capabilities;

            public void run()
            {
//                controller.getNetworkActivitySignal().addNetworkUser(WMSLegendRetriever.this);
                try
                {
                    CapabilitiesRequest request = new CapabilitiesRequest(serverURI, "");
                    this.capabilities = new WMSCapabilities(request);
                    this.capabilities.parse();
//                    if (!Thread.currentThread().isInterrupted())
                        createLayerList(this.capabilities);
                }
                catch (XMLStreamException e)
                {
                    String msg = "Error retrieving servers capabilities " + serverURI;
                    Util.getLogger().log(Level.SEVERE, msg, e);
                    controller.showErrorDialog(e, "Get Capabilities Error", msg);
                }
                catch (Exception e)
                {
                    if (e.getClass().getName().toLowerCase().contains("timeout"))
                    {
                        String msg = "Connection to server timed out\n" + serverURI;
                        controller.showErrorDialog(e, "Connection Timeout", msg);
                        Util.getLogger().log(Level.SEVERE, msg + serverURI, e);
                    }
                    else
                    {
                        String msg = "Attempt to contact server failed\n" + serverURI;
                        controller.showErrorDialog(e, "Server Not Responding", msg);
                        Util.getLogger().log(Level.SEVERE, msg + serverURI, e);
                    }
                }
//                finally // ensure that the cursor is restored to default whether succes or failure
//                {
//                    EventQueue.invokeLater(new Runnable()
//                    {
//                        public void run()
//                        {
//                            controller.getNetworkActivitySignal().removeNetworkUser(WMSLegendRetriever.this);
//                        }
//                    });
//                }
            }
        });

        this.loadingThread.setPriority(Thread.MIN_PRIORITY);
        this.loadingThread.start();
    }

    protected void createLayerList(final WMSCapabilities caps)
    {
        java.util.List<WMSLayerCapabilities> layers = caps.getCapabilityInformation().getLayerCapabilities();
        if (layers.size() == 0)
            return;

        // TODO: Make the list for all top-level layers if more than one.
        WMSLayerCapabilities layer = layers.get(0);

        String docAbstract = caps.getServiceInformation().getServiceAbstract();
        if (docAbstract != null)
            this.infoButton.setToolTipText(Util.makeMultiLineToolTip(docAbstract));
        String infoUrl = caps.getServiceInformation().getOnlineResource().getHref();
        this.infoButton.putClientProperty("CapsURL", infoUrl != null ? infoUrl
            : caps.getRequestURL("GetCapabilities", "HTTP", "Get"));

        EventQueue.invokeLater(new Runnable() // UI changes should be finalized on the EDT
        {
            public void run()
            {
                if (nameField.getText() == null || nameField.getText().length() == 0)
                    nameField.setText(getServerDisplayString(caps));

                urlField.setText(serverURI.toString());//SelectedItem(serverURI.toString());

                layerTree.expandRow(0); // ensure that the top grouping layer is expanded
            }
        });
    }

    @Override
    public boolean hasNetworkActivity()
    {
        return false;
    }

    protected String getServerDisplayString(WMSCapabilities caps)
    {
        String title = caps.getServiceInformation().getServiceTitle();
        return title != null ? title : this.serverURI.getHost();
    }



//        WMSLayerCapabilities capabilities = new WMSLayerCapabilities(this.getNameSpaceURI);

//        String layerNames = Configuration.getStringValue(AVKey.LAYERS_CLASS_NAMES);
//        System.out.println(layerNames);
//        String uris = Configuration.getStringValue(AVKey.GET_MAP_URL);
//        System.out.println(uris);
//        Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
//        factory.createFromConfigSource("cms-data/layers/ClementineBasemapV2.xml", AVKey.);
//        String datastore = Configuration.getStringValue(AVKey.DATA_FILE_STORE_CONFIGURATION_FILE_NAME);
//        System.out.println(datastore);
//        System.out.println(Configuration.getStringValue("gov.nasa.worldwind.avkey.DataFileStoreConfigurationFileName"));

//        this.getWwd().getEntries().forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));

//        StringSetXMLEventParser setXMLEventParser = new StringSetXMLEventParser("./gov/nasa/cms/config/cmsLayers.xml");
//        System.out.println(setXMLEventParser.getStrings());

//    String server = "https://planetarymaps.usgs.gov/cgi-bin/mapserv?map=/maps/earth/moon_simp_cyl.map";
//        try
//    {
//        URI uri = new URI(server.trim());
//        System.out.println(uri);
//        Capabilities cap = Capabilities.retrieve(uri,"wms");
//        System.out.println(cap.getVendorSpecificCapabilities());
//
//    }
//        catch (URISyntaxException e)
//    {
//        e.printStackTrace();
//    }
//        catch (Exception e)
//    {
//        e.printStackTrace();
//    }

//        this.getWwd().getModel().getLayers().forEach(e -> {
//            System.out.println(e.getName() + ": " + e.isNetworkRetrievalEnabled());
//            WMSLayerCapabilities::getDataURLs();
////            System.out.println((Lay);
//        });
}
