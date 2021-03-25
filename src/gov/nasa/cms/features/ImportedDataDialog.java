/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwindx.applications.worldwindow.features.NetworkActivitySignal;
import java.awt.Component;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.DataStoreProducer;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.data.WWDotNetLayerSetConverter;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwindx.applications.worldwindow.features.DataImportUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.*;
import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.w3c.dom.*;


/**
 *
 * @author kjdickin
 */
public class ImportedDataDialog implements NetworkActivitySignal.NetworkUser
{
    protected FileStore fileStore;
    protected ImportedDataPanel dataConfigPanel;
    protected Thread importThread;
    private JDialog dialog;
    private CelestialMapper frame;
    private NetworkActivitySignal networkActivitySignal;
    private JButton importButton = new JButton("Import");

    public ImportedDataDialog(WorldWindow wwd, Component component)
    {
        this.frame = (CelestialMapper) component;
        
        fileStore = WorldWind.getDataFileStore();
        
        dialog = new JDialog((Frame) component);
        Rectangle bounds = component.getBounds();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Import Imagery & Elevations");
        dialog.setPreferredSize(new Dimension(400, 400));
        dialog.setLocation(bounds.x, bounds.y + 60);
        dialog.setResizable(false);
        
        dataConfigPanel = new ImportedDataPanel(wwd);
        
        dataConfigPanel.getButtonPanel().add(importButton, BorderLayout.WEST);
        importButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                importFromFile();
            }
        });
        dialog.getContentPane().add(dataConfigPanel, BorderLayout.CENTER);
        dialog.pack();
    }
    
    private void importFromFile()
    {
        JFileChooser fc = new JFileChooser();

        fc.setDialogTitle("Import File");
        fc.setMultiSelectionEnabled(false);
        ImportableDataFilter filter = new ImportableDataFilter();
        fc.addChoosableFileFilter(filter);

        int retVal = fc.showDialog(frame, "Import");

        if (retVal != JFileChooser.APPROVE_OPTION)
            return;

        final File file = fc.getSelectedFile();
        if (file == null) // This should never happen, but we check anyway.
            return;

        fc.removeChoosableFileFilter(filter);
        fc.setMultiSelectionEnabled(true);
        fc.setDialogTitle("");

        this.importThread = new Thread(new Runnable()
        {
            public void run()
            {
                networkActivitySignal.addNetworkUser(ImportedDataDialog.this);

                try
                {
                    Document dataConfig = null;

                    try
                    {
                        // Import the file into a form usable by WorldWind components.
                        dataConfig = importDataFromFile(ImportedDataDialog.this.dialog, file, fileStore);
                    }
                    catch (Exception e)
                    {
                        final String message = e.getMessage();

                        // Show a message dialog indicating that the import failed, and why.
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                JOptionPane.showMessageDialog(ImportedDataDialog.this.dialog, message, "Import Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }

                    if (dataConfig != null)
                    {
                        addImportedData(dataConfig, null, dataConfigPanel);
                    }
                }
                finally
                {
                    networkActivitySignal.removeNetworkUser(ImportedDataDialog.this);
                }
            }
        });

        this.importThread.start();
    }
    
    protected static void addImportedData(final Document dataConfig, final AVList params,
        final ImportedDataPanel panel)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    addImportedData(dataConfig, params, panel);
                }
            });
        }
        else
        {
            panel.addImportedData(dataConfig.getDocumentElement(), params);
        }
    }
    
    protected static class ImportableDataFilter extends javax.swing.filechooser.FileFilter
    {
        public ImportableDataFilter()
        {
        }

        public boolean accept(File file)
        {
            if (file == null || file.isDirectory())
                return true;

            if (DataImportUtil.isDataRaster(file, null))
                return true;
            else if (DataImportUtil.isWWDotNetLayerSet(file))
                return true;

            return false;
        }

        public String getDescription()
        {
            return "Supported Images/Elevations";
        }
    }
    
    protected static Document importDataFromFile(Component parentComponent, File file, FileStore fileStore)
        throws Exception
    {
        // Create a DataStoreProducer which is capable of processing the file.
        final DataStoreProducer producer = createDataStoreProducerFromFile(file);
        if (producer == null)
        {
            throw new IllegalArgumentException("Unrecognized file type");
        }

        // Create a ProgressMonitor that will provide feedback on how
        final ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent,
            "Importing " + file.getName(), null, 0, 100);

        final AtomicInteger progress = new AtomicInteger(0);

        // Configure the ProgressMonitor to receive progress events from the DataStoreProducer. This stops sending
        // progress events when the user clicks the "Cancel" button, ensuring that the ProgressMonitor does not
        PropertyChangeListener progressListener = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (progressMonitor.isCanceled())
                    return;

                if (evt.getPropertyName().equals(AVKey.PROGRESS))
                    progress.set((int) (100 * (Double) evt.getNewValue()));
            }
        };
        producer.addPropertyChangeListener(progressListener);
        progressMonitor.setProgress(0);

        // Configure a timer to check if the user has clicked the ProgressMonitor's "Cancel" button. If so, stop
        // production as soon as possible. This just stops the production from completing; it doesn't clean up any state
        // changes made during production,
        java.util.Timer progressTimer = new java.util.Timer();
        progressTimer.schedule(new TimerTask()
        {
            public void run()
            {
                progressMonitor.setProgress(progress.get());

                if (progressMonitor.isCanceled())
                {
                    producer.stopProduction();
                    this.cancel();
                }
            }
        }, progressMonitor.getMillisToDecideToPopup(), 100L);

        Document doc = null;
        try
        {
            // Import the file into the specified FileStore.
            doc = createDataStoreFromFile(file, fileStore, producer);

            // The user clicked the ProgressMonitor's "Cancel" button. Revert any change made during production, and
            // discard the returned DataConfiguration reference.
            if (progressMonitor.isCanceled())
            {
                doc = null;
                producer.removeProductionState();
            }
        }
        finally
        {
            // Remove the progress event listener from the DataStoreProducer. stop the progress timer, and signify to the
            // ProgressMonitor that we're done.
            producer.removePropertyChangeListener(progressListener);
            progressMonitor.close();
            progressTimer.cancel();
        }

        return doc;
    }
    
    protected static Document createDataStoreFromFile(File file, FileStore fileStore,
        DataStoreProducer producer) throws Exception
    {
        File importLocation = DataImportUtil.getDefaultImportLocation(fileStore);
        if (importLocation == null)
        {
            String message = Logging.getMessage("generic.NoDefaultImportLocation");
            Logging.logger().severe(message);
            return null;
        }

        // Create the production parameters. These parameters instruct the DataStoreProducer where to import the cached
        // data, and what name to put in the data configuration document.
        AVList params = new AVListImpl();
        params.setValue(AVKey.DATASET_NAME, file.getName());
        params.setValue(AVKey.DATA_CACHE_NAME, file.getName());
        params.setValue(AVKey.FILE_STORE_LOCATION, importLocation.getAbsolutePath());
        producer.setStoreParameters(params);

        // Use the specified file as the the production data source.
        producer.offerDataSource(file, null);

        try
        {
            // Convert the file to a form usable by WorldWind components, according to the specified DataStoreProducer.
            // This throws an exception if production fails for any reason.
            producer.startProduction();
        }
        catch (Exception e)
        {
            // Exception attempting to convert the file. Revert any change made during production.
            producer.removeProductionState();
            throw e;
        }

        // Return the DataConfiguration from the production results. Since production sucessfully completed, the
        // DataStoreProducer should contain a DataConfiguration in the production results. We test the production
        // results anyway.
        Iterable results = producer.getProductionResults();
        if (results != null && results.iterator() != null && results.iterator().hasNext())
        {
            Object o = results.iterator().next();
            if (o != null && o instanceof Document)
            {
                return (Document) o;
            }
        }

        return null;
    }
    
    protected static DataStoreProducer createDataStoreProducerFromFile(File file)
    {
        if (file == null)
            return null;

        DataStoreProducer producer = null;

        AVList params = new AVListImpl();
        if (DataImportUtil.isDataRaster(file, params))
        {
            if (AVKey.ELEVATION.equals(params.getStringValue(AVKey.PIXEL_FORMAT)))
                producer = new TiledElevationProducer();
            else if (AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT)))
                producer = new TiledImageProducer();
        }
        else if (DataImportUtil.isWWDotNetLayerSet(file))
            producer = new WWDotNetLayerSetConverter();

        return producer;
    }

    
    
    public void setVisible(boolean visible)
    {
        dialog.setVisible(visible);
    }
    
    @Override
    public boolean hasNetworkActivity()
    {
        return this.importThread != null && this.importThread.isAlive();
    }
}
