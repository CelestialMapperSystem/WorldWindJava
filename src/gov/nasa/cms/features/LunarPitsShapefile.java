/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.features;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileRenderable;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import javax.swing.SwingUtilities;

/**
 *
 * @author kjdickin
 */
public class LunarPitsShapefile implements ShapefileRenderable.AttributeDelegate
{

    String lunarPitsShapefile = "cms-data/lunarpits/LUNAR_PIT_LOCATIONS_180.SHP";
    private Layer layer;

    public LunarPitsShapefile(WorldWindow wwd)
    {
        ShapefileLayerFactory factory = new ShapefileLayerFactory();

        ShapefileLayerFactory.CompletionCallback callBack = new ShapefileLayerFactory.CompletionCallback()
        {
            @Override
            public void completion(Object result)
            {
                layer = (Layer) result; // the result is the layer the factory created
                String layerName = layer.getName();
                layerName = layerName.substring(0, layerName.length() - 4);
                layer.setName(WWIO.getFilename(layerName));

                // Add the layer to the WorldWindow's layer list.
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        wwd.getModel().getLayers().add(layer);
                        layer.setEnabled(false);

                    }
                });
            }

            @Override
            public void exception(Exception e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
            }
        };
        factory.createFromShapefileSource(lunarPitsShapefile, callBack);
    }

    @Override
    public void assignAttributes(ShapefileRecord shapefileRecord, ShapefileRenderable.Record renderableRecord)
    {
    }

    @Override
    public void assignRenderableAttributes(ShapefileRecord shapefileRecord, Renderable renderable)
    {
         DBaseRecord attrs = shapefileRecord.getAttributes();
            String label = attrs.getValue("name").toString();
    }

}
