/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms;

import gov.nasa.cms.features.CMSLineOfSightPanel;
import gov.nasa.cms.features.LineOfSightController;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Shows how to compute terrain intersections using the highest resolution terrain data available from a globe's
 * elevation model.
 * <p>
 * To generate and show intersections, Shift + LeftClick or + RightClick anywhere on the globe. The program forms a grid of locations
 * around the selected location. The grid points are shown in yellow. It then determines whether a line between the
 * selected location and each grid point intersects the terrain. If it does, the intersection nearest the selected
 * location is shown in cyan and a line is drawn from the selected location to the intersection. If there is no
 * intersection, a line is drawn from the selected location to the grid position.
 * <p>
 * If the highest resolution terrain is not available for the area around the selected location, it is retrieved from
 * the elevation model's source, which is most likely a remote server. Since the high-res data must be retrieved and
 * then loaded from the local disk cache, it will take some time to compute and show the intersections.
 * <p>
 * This example imports functionality from TerrainIntersections.java and uses a {@link gov.nasa.worldwind.terrain.Terrain} object 
 * To perform the terrain retrieval, generation and intersection calculations.
 *
 *
 * @author Tyler Choi
 * @version $Id: CMSLineOfSight.java 2020-11-30 14:14:38Z twchoi $
 */
public class CMSLineOfSight extends JCheckBoxMenuItem {
    /**
     * The width and height in degrees of the grid used to calculate intersections.
     */
    protected static final Angle GRID_RADIUS = Angle.fromDegrees(0.05);

    /**
     * The number of cells along each edge of the grid.
     */
    protected static final int GRID_DIMENSION = 10; // cells per side

    /**
     * The desired terrain resolution to use in the intersection calculations.
     */
    protected static final Double TARGET_RESOLUTION = 10d; // meters, or null for globe's highest resolution
    protected static final int NUM_THREADS = 4; // set to 1 to run synchronously
    private static final Cursor WaitCursor = new Cursor(Cursor.WAIT_CURSOR);
    
    protected HighResolutionTerrain terrain;
    protected RenderableLayer gridPoints;
    protected RenderableLayer gridOrigin;
    protected RenderableLayer intersectionsLayer;
    protected RenderableLayer gridSightLinesLayer;
    protected RenderableLayer intersectionSightLinesLayer;
    protected RenderableLayer intersectionPointsLayer;
    protected RenderableLayer tilesLayer;
    protected Thread calculationDispatchThread;
//    protected JProgressBar progressBar;
    protected ThreadPoolExecutor threadPool;
    protected List<Position> grid;
    protected int numGridPoints; // used to monitor percentage progress
    protected long startTime, endTime; // for reporting calculation duration
    protected Position previousCurrentPosition;
    private WorldWindow wwd;
    private boolean isItemEnabled;
    private LayerPanel layerPanel;
    private AppFrame cms;
    private CMSLineOfSightPanel controlPanel;
    private JFrame frame;
    private JDialog dialog;
    private boolean layersNotNull;
    private LineOfSightController lineOfSightController;

    public CMSLineOfSight(CelestialMapper cms, WorldWindow wwd, LineOfSightController aThis) {
        setWwd(wwd); //sets Wwd to Wwd parameter from CelestialMapper
        setCms(cms); 
        this.layersNotNull = false;
        this.lineOfSightController = aThis;
    }
    
    public void activate(){
        setSightLineProperties();
    }
    
    public void deactivate(){
        String[] SightLineLayers = {"Grid", "Origin", "Intersections",
            "Grid Sight Lines", "Intersection Sight Lines", "Intersection Points Lines"};

        for (String layer : SightLineLayers) {
            Layer selectedLayer = this.wwd.getModel().getLayers().getLayerByName(layer);
            this.wwd.getModel().getLayers().remove(selectedLayer);
        }
    }
    
    public void setSightLineProperties()
    {
        // Create a thread pool.
        this.threadPool = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS, 200, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//               createAndShowGui();
//            }
//        });
       
        // Be sure to re-use the Terrain object to take advantage of its caching.
        this.terrain = new HighResolutionTerrain(getWwd().getModel().getGlobe(), TARGET_RESOLUTION);

        this.gridPoints = new RenderableLayer();
        this.gridPoints.setName("Grid");
        this.getWwd().getModel().getLayers().add(this.gridPoints);
        
        this.gridOrigin = new RenderableLayer();
        this.gridOrigin.setName("Origin");
        this.getWwd().getModel().getLayers().add(this.gridOrigin);

        this.intersectionsLayer = new RenderableLayer();
        this.intersectionsLayer.setName("Intersections");
        this.getWwd().getModel().getLayers().add(this.intersectionsLayer);

        this.gridSightLinesLayer = new RenderableLayer();
        this.gridSightLinesLayer.setName("Grid Sight Lines");
        this.getWwd().getModel().getLayers().add(this.gridSightLinesLayer);
        
        this.intersectionSightLinesLayer = new RenderableLayer();
        this.intersectionSightLinesLayer.setName("Intersection Sight Lines");
        this.getWwd().getModel().getLayers().add(this.intersectionSightLinesLayer);
        
        this.intersectionPointsLayer = new RenderableLayer();
        this.intersectionPointsLayer.setName("Intersection Points Lines");
        this.getWwd().getModel().getLayers().add(this.intersectionPointsLayer);

        // Set up a mouse handler to generate a grid and start intersection calculations when the user shift-clicks.
        this.getWwd().getInputHandler().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                // Control-Click cancels any currently running operation.
                if ((mouseEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                    if (calculationDispatchThread != null && calculationDispatchThread.isAlive()) {
                        calculationDispatchThread.interrupt();
                    }
                    return;
                }

                // Alt-Click repeats the most recent calculations.
                if ((mouseEvent.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0) {
                    if (previousCurrentPosition == null) {
                        return;
                    }

                    mouseEvent.consume(); // tell the rest of WW that this event has been processed

                    computeAndShowIntersections(previousCurrentPosition);
                    return;
                }

                // Perform the intersection tests in response to Shift-Click.
                if ((mouseEvent.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
                    return;
                }

                mouseEvent.consume(); // tell the rest of WW that this event has been processed

                final Position pos = getWwd().getCurrentPosition();
                if (pos == null) {
                    return;
                }

                computeAndShowIntersections(pos);
            }
        });
    }

    protected void computeAndShowIntersections(final Position curPos) {
        this.previousCurrentPosition = curPos;

        SwingUtilities.invokeLater(() -> {
            setCursor(WaitCursor);
        });

        // Dispatch the calculation threads in a separate thread to avoid locking up the user interface.
        this.calculationDispatchThread = new Thread(() -> {
            try {
                performIntersectionTests(curPos);
            } catch (InterruptedException e) {
                System.out.println("Operation was interrupted");
            }
        });

        this.calculationDispatchThread.start();
    }

    // Create containers to hold the intersection points and the lines emanating from the center.
    protected List<Position> firstIntersectionPositions = new ArrayList<>();
    protected List<Position> gridPointsPositions = new ArrayList<>();
    protected List<Position[]> sightLines = new ArrayList<>(GRID_DIMENSION * GRID_DIMENSION);
    protected List<Position[]> gridSightLines = new ArrayList<>(GRID_DIMENSION * GRID_DIMENSION);
    protected List<Position[]> intersectionSightLines = new ArrayList<>(GRID_DIMENSION * GRID_DIMENSION);

    // Make the picked location's position and model-coordinate point available to all methods.
    protected Position referencePosition;
    protected Vec4 referencePoint;
    
    // This is a collection of synchronized accessors to the list updated during the calculations.
    
    protected synchronized void clearPositionLists(){
        this.firstIntersectionPositions.clear();
        this.sightLines.clear();
        this.gridPointsPositions.clear();
        this.gridSightLines.clear();
        this.intersectionSightLines.clear();
    }
    
    protected synchronized void addIntersectionPosition(Position position) {
        this.firstIntersectionPositions.add(position);
    }
    
//    protected synchronized void addIntersectionPoints(Position position) {
//        this.firstIntersectionPositions.add(position);
//    }
    
    protected synchronized void addGridPoints(Position position) {
        this.gridPointsPositions.add(position);
    }

    protected synchronized void addSightLine(Position positionA, Position positionB) {
        this.sightLines.add(new Position[]{positionA, positionB});
    }
    
    protected synchronized void addGridSightLine(Position positionA, Position positionB) {
        this.gridSightLines.add(new Position[]{positionA, positionB});
    }
    
    protected synchronized void addIntersectionSightLine(Position positionA, Position positionB) {
        this.intersectionSightLines.add(new Position[]{positionA, positionB});
    }

    protected synchronized int getSightlinesSize() {
        return this.sightLines.size();
    }

    public boolean isLayersNotNull() {
        return layersNotNull;
    }

    public void setLayersNotNull(boolean layersNotNull) {
        this.layersNotNull = layersNotNull;
    }
    
    

    private long lastTime = System.currentTimeMillis();

    /**
     * Keeps the progress meter current. When calculations are complete, displays the results.
     */
    protected synchronized void updateProgress() {
        // Update the progress bar only once every 250 milliseconds to avoid stealing time from calculations.
        if (this.sightLines.size() >= this.numGridPoints) {
            endTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() < this.lastTime + 250) {
            return;
        }
        this.lastTime = System.currentTimeMillis();

        // On the EDT, update the progress bar and if calculations are complete, update the WorldWindow.
        SwingUtilities.invokeLater(() -> {
            int progress = (int) (100d * getSightlinesSize() / (double) numGridPoints);
            this.lineOfSightController.updateProgressBar(progress);

            if (progress >= 100) {
                setCursor(Cursor.getDefaultCursor());
                this.lineOfSightController.updateProgressBar((endTime - startTime) + " ms");
                showResults();
                System.out.printf("Calculation time %d milliseconds\n", endTime - startTime);
            }
        });
    }

    
    
    protected void formGrid(final Position curPos, double height){
        double gridRadius = GRID_RADIUS.degrees;
        Sector sector = Sector.fromDegrees(
                curPos.getLatitude().degrees - gridRadius, curPos.getLatitude().degrees + gridRadius,
                curPos.getLongitude().degrees - gridRadius, curPos.getLongitude().degrees + gridRadius);

        this.grid = buildGrid(sector,  height, GRID_DIMENSION, GRID_DIMENSION);
        this.numGridPoints = grid.size();
        
    }
    
    

    protected void performIntersectionTests(final Position curPos) throws InterruptedException {
        // Clear the results lists when the user selects a new location.
//        this.firstIntersectionPositions.clear();
//        this.sightLines.clear();
          this.clearPositionLists();

        // TODO - Update so that the grid shows the altitude of the selected point
        // instead of showing the arbitrary number "5" as originally shown below.
//        final double height = curPos.getAltitude();

//        double curAltitude = curPos.getAltitude();
        
        // Raise the selected location and the grid points a little above ground just to show we can.
        final double height = 5; // meters
//        final double height = curAltitude + 5; // meters

        System.out.println("height at curPos is: " + height);

        // Form the grid.
        this.formGrid(curPos, height);

        // Compute the position of the selected location (incorporate its height).
        this.referencePosition = new Position(curPos.getLatitude(), curPos.getLongitude(), height);
        this.referencePoint = terrain.getSurfacePoint(curPos.getLatitude(), curPos.getLongitude(), height);

//            // Pre-caching is unnecessary and is useful only when it occurs before the intersection
//            // calculations. It will incur extra overhead otherwise. The normal intersection calculations
//            // cause the same caching, making subsequent calculations on the same area faster.
//            this.preCache(grid, this.referencePosition);
        // On the EDT, show the grid.
        SwingUtilities.invokeLater(() -> {
            this.lineOfSightController.updateProgressBar(0);
            this.lineOfSightController.updateProgressBar(null);
            clearLayers();
            // TODO Remove call to showGrid here unless the actionlistener for the checkbox has been enabled
            showGrid(grid, referencePosition);
            getWwd().redraw();
        });

        // Perform the intersection calculations.
        this.startTime = System.currentTimeMillis();
        for (Position gridPos : this.grid) // for each grid point.
        {
            //noinspection ConstantConditions
            if (NUM_THREADS > 0) {
                this.threadPool.execute(new Intersector(gridPos));
            } else {
                performIntersection(gridPos);
            }
        }
    }

    /**
     * Performs one line of sight calculation between the reference position and a specified grid position.
     *
     * @param gridPosition the grid position.
     *
     * @throws InterruptedException if the operation is interrupted.
     */
    protected void performIntersection(Position gridPosition) throws InterruptedException {
        // Intersect the line between this grid point and the selected position.
        Intersection[] intersections = this.terrain.intersect(this.referencePosition, gridPosition);
        if (intersections == null || intersections.length == 0) {
            // No intersection, so the line goes from the center to the grid point.
            this.sightLines.add(new Position[]{this.referencePosition, gridPosition});
            return;
        }

        // Only the first intersection is shown.
        Vec4 iPoint = intersections[0].getIntersectionPoint();
        Vec4 gPoint = terrain.getSurfacePoint(gridPosition.getLatitude(), gridPosition.getLongitude(),
                gridPosition.getAltitude());

        // Check to see whether the intersection is beyond the grid point.
        if (iPoint.distanceTo3(this.referencePoint) >= gPoint.distanceTo3(this.referencePoint)) {
            // Intersection is beyond the grid point; the line goes from the center to the grid point.
            this.addSightLine(this.referencePosition, gridPosition);
            return;
        }

        // Compute the position corresponding to the intersection.
        Position iPosition = this.terrain.getGlobe().computePositionFromPoint(iPoint);

        // The sight line goes from the user-selected position to the intersection position.
        this.addSightLine(this.referencePosition, new Position(iPosition, 0));

        // Keep track of the intersection positions.
        this.addIntersectionPosition(iPosition);

        this.updateProgress();
    }

    /**
     * Inner {@link Runnable} to perform a single line/terrain intersection calculation.
     */
    protected class Intersector implements Runnable {

        protected final Position gridPosition;

        public Intersector(Position gridPosition) {
            this.gridPosition = gridPosition;
        }

        @Override
        public void run() {
            try {
                performIntersection(this.gridPosition);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected List<Position> buildGrid(Sector sector, double height, int nLatCells, int nLonCells) {
        List<Position> grid = new ArrayList<>((nLatCells + 1) * (nLonCells + 1));

        double dLat = sector.getDeltaLatDegrees() / nLatCells;
        double dLon = sector.getDeltaLonDegrees() / nLonCells;

        for (int j = 0; j <= nLatCells; j++) {
            double lat = j == nLatCells
                    ? sector.getMaxLatitude().degrees : sector.getMinLatitude().degrees + j * dLat;

            for (int i = 0; i <= nLonCells; i++) {
                double lon = i == nLonCells
                        ? sector.getMaxLongitude().degrees : sector.getMinLongitude().degrees + i * dLon;

                grid.add(Position.fromDegrees(lat, lon, height));
            }
        }

        return grid;
    }

    protected void preCache(List<Position> grid, Position centerPosition) throws InterruptedException {
        // Pre-cache the tiles that will be needed for the intersection calculations.
        double n = 0;
        final long start = System.currentTimeMillis();
        for (Position gridPos : grid) // for each grid point.
        {
            final double progress = 100 * (n++ / grid.size());
            terrain.cacheIntersectingTiles(centerPosition, gridPos);

            SwingUtilities.invokeLater(() -> {
                this.lineOfSightController.updateProgressBar((int) progress);
                this.lineOfSightController.updateProgressBar(null);
            });
        }

        SwingUtilities.invokeLater(() -> {
            this.lineOfSightController.updateProgressBar(100);
        });

        long end = System.currentTimeMillis();
        System.out.printf("Pre-caching time %d milliseconds, cache usage %f, tiles %d\n", end - start,
                terrain.getCacheUsage(), terrain.getNumCacheEntries());
    }

    protected void clearLayers() {
        this.intersectionsLayer.removeAllRenderables();
        this.gridSightLinesLayer.removeAllRenderables();
        this.gridPoints.removeAllRenderables();
        this.gridOrigin.removeAllRenderables();
    }
    
    /**
     * Updates the WorldWind model with the new intersection locations and sight lines.
     */
    protected void showResults() {
        this.showGrid(grid, referencePosition);
        this.showIntersections(firstIntersectionPositions);
        this.showSightLines(sightLines);
        this.showCenterPoint(referencePosition);
//            this.showIntersectingTiles(this.grid, this.referencePosition);
        setLayersNotNull(true);
        this.getWwd().redraw();
    }

    protected void showIntersections(List<Position> intersections) {
        this.intersectionsLayer.removeAllRenderables();

        // Display the intersections as CYAN points.
        PointPlacemarkAttributes intersectionPointAttributes;
        intersectionPointAttributes = new PointPlacemarkAttributes();
        intersectionPointAttributes.setLineMaterial(Material.CYAN);
        intersectionPointAttributes.setScale(6d);
        intersectionPointAttributes.setUsePointAsDefaultImage(true);

        for (Position p : intersections) {
            PointPlacemark pm = new PointPlacemark(p);
            pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pm.setAttributes(intersectionPointAttributes);
            pm.setValue(AVKey.DISPLAY_NAME, p.toString());
            this.intersectionsLayer.addRenderable(pm);
        }
    }

    protected void showSightLines(List<Position[]> sightLines) {
        this.gridSightLinesLayer.removeAllRenderables();

        // Display the sight lines as green lines.
        ShapeAttributes lineAttributes;
        lineAttributes = new BasicShapeAttributes();
        lineAttributes.setDrawOutline(true);
        lineAttributes.setDrawInterior(false);
        lineAttributes.setOutlineMaterial(Material.GREEN);
        lineAttributes.setOutlineOpacity(0.6);

        for (Position[] pp : sightLines) {
            List<Position> endPoints = new ArrayList<>();
            endPoints.add(pp[0]);
            endPoints.add(pp[1]);

            Path path = new Path(endPoints);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setAttributes(lineAttributes);
            this.gridSightLinesLayer.addRenderable(path);
        }
    }

    protected void showGridSightLines(List<Position> grid, Position cPos) {
        this.gridSightLinesLayer.removeAllRenderables();

        // Display lines from the center to each grid point.
        ShapeAttributes lineAttributes;
        lineAttributes = new BasicShapeAttributes();
        lineAttributes.setDrawOutline(true);
        lineAttributes.setDrawInterior(false);
        lineAttributes.setOutlineMaterial(Material.GREEN);
        lineAttributes.setOutlineOpacity(0.6);

        for (Position p : grid) {
            List<Position> endPoints = new ArrayList<>();
            endPoints.add(cPos);
            endPoints.add(new Position(p.getLatitude(), p.getLongitude(), 0));

            Path path = new Path(endPoints);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setAttributes(lineAttributes);
            this.gridSightLinesLayer.addRenderable(path);
        }
    }
    
    protected void showGrid(List<Position> grid, Position cPos) {
        this.gridPoints.removeAllRenderables();

        // Display the grid points in yellow.
        PointPlacemarkAttributes gridPointAttributes;
        gridPointAttributes = new PointPlacemarkAttributes();
        gridPointAttributes.setLineMaterial(Material.YELLOW);
        gridPointAttributes.setScale(6d);
        gridPointAttributes.setUsePointAsDefaultImage(true);

        for (Position p : grid) {
            PointPlacemark pm = new PointPlacemark(p);
            pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pm.setAttributes(gridPointAttributes);
            pm.setLineEnabled(true);
            pm.setValue(AVKey.DISPLAY_NAME, p.toString());
            this.gridPoints.addRenderable(pm);
        }

        showCenterPoint(cPos);
    }

    protected void showCenterPoint(Position cPos) {
        // Display the center point in red.
        PointPlacemarkAttributes selectedLocationAttributes;
        selectedLocationAttributes = new PointPlacemarkAttributes();
        selectedLocationAttributes.setLineMaterial(Material.RED);
        selectedLocationAttributes.setScale(8d);
        selectedLocationAttributes.setUsePointAsDefaultImage(true);

        PointPlacemark pm = new PointPlacemark(cPos);
        pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        pm.setAttributes(selectedLocationAttributes);
        pm.setValue(AVKey.DISPLAY_NAME, cPos.toString());
        pm.setLineEnabled(true);
        this.gridOrigin.addRenderable(pm);
        System.out.println(pm.getAttributes());
    }

    public void toggleGridLines(boolean toggle) {
        this.gridSightLinesLayer.setEnabled(toggle);
    }
    
    public void toggleGridPoints(boolean toggle) {
        this.gridPoints.setEnabled(toggle);
    }
    
        public void toggleGridOrigin(boolean toggle) {
        this.gridOrigin.setEnabled(toggle);
    }
    
    public void togglesIntersections(boolean toggle) {
        this.intersectionsLayer.setEnabled(toggle);
    }
    
    public void toggleIntersectionSightLines(boolean toggle) {
        this.intersectionSightLinesLayer.setEnabled(toggle);
    }
    
    public void toggleIntersectionPoints(boolean toggle) {
        this.intersectionPointsLayer.setEnabled(toggle);
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public final void setWwd(WorldWindow wwd)
    {
        this.wwd = wwd;
    }

    public AppFrame getCms() {
        return cms;
    }

    public final void setCms(AppFrame cms) {
        this.cms = cms;
    }
    
    


}
