/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.cms.features;

import gov.nasa.cms.*;
import gov.nasa.cms.containers.GridPointPosition;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Shows how to compute terrain intersections using the highest resolution
 * terrain data available from a globe's elevation model.
 * <p>
 * To generate and show intersections, Shift + LeftClick or + RightClick
 * anywhere on the globe. The program forms a grid of locations around the
 * selected location. The grid points are shown in yellow. It then determines
 * whether a line between the selected location and each grid point intersects
 * the terrain. If it does, the intersection nearest the selected location is
 * shown in cyan and a line is drawn from the selected location to the
 * intersection. If there is no intersection, a line is drawn from the selected
 * location to the grid position.
 * <p>
 * If the highest resolution terrain is not available for the area around the
 * selected location, it is retrieved from the elevation model's source, which
 * is most likely a remote server. Since the high-res data must be retrieved and
 * then loaded from the local disk cache, it will take some time to compute and
 * show the intersections.
 * <p>
 * This example imports functionality from TerrainIntersections.java and uses a
 * {@link gov.nasa.worldwind.terrain.Terrain} object To perform the terrain
 * retrieval, generation and intersection calculations.
 *
 * @author Tyler Choi
 * @version $Id: CMSLineOfSight.java 2020-11-30 14:14:38Z twchoi $
 */
public class CMSLineOfSight extends JCheckBoxMenuItem
{

    /**
     * The width and height in degrees of the grid used to calculate
     * intersections.
     */
    protected static final Angle GRID_RADIUS = Angle.fromDegrees(0.05);

    /**
     * The number of cells along each edge of the grid.
     */
    protected static final int GRID_DIMENSION = 10; // cells per side

    // Create containers to hold the intersection points and the lines emanating from the center.
    protected Vector<Position> firstIntersectionPositions = new Vector<>();
    protected Vector<GridPointPosition> gridPointsPositions = new Vector<>();

    protected Vector<Position> gridSightLines = new Vector<>(
        GRID_DIMENSION * GRID_DIMENSION);
    protected Vector<Position[]> intersectionSightLines = new Vector<>(
        GRID_DIMENSION * GRID_DIMENSION);

    // Make the picked location's position and model-coordinate point available to all methods.
    protected Position referencePosition;
    protected Vec4 referencePoint;
    protected Position exageratedReferencePosition;

    /**
     * The desired terrain resolution to use in the intersection calculations.
     */
    protected static final Double TARGET_RESOLUTION = 10d;
    // meters, or null for globe's highest resolution
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

    protected Vector<Position> grid;
    protected int numGridPoints; // used to monitor percentage progress

    protected long startTime, endTime; // for reporting calculation duration

    protected Position previousCurrentPosition;

    private WorldWindow wwd;
    private AppFrame cms;
    private boolean layersNotNull;
    private final LineOfSightController lineOfSightController;

    private long lastTime = System.currentTimeMillis();
    private AtomicInteger debugCounter;
    private AtomicBoolean isDone;
    private Vector<Future> threadPoolTasks;
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean stopped = new AtomicBoolean(true);

    public CMSLineOfSight(CelestialMapper cms, WorldWindow wwd,
        LineOfSightController aThis)
    {
        setWwd(wwd); //sets Wwd to Wwd parameter from CelestialMapper
        setCms(cms);
        this.layersNotNull = false;
        this.lineOfSightController = aThis;
        this.debugCounter = new AtomicInteger(0);
        this.isDone = new AtomicBoolean(false);
        this.threadPoolTasks = new Vector<>();
    }

    public void activate()
    {
        createThreadPool();
        setTerrainResolution(TARGET_RESOLUTION);
        initializeRenderableLayers();
        setSightLineProperties();
    }

    public void deactivate()
    {
        String[] ActiveLayers = {"Grid", "Origin", "Intersections",
            "Grid Sight Lines", "Intersection Sight Lines",
            "Intersection Points Lines"};

        for (String layer : ActiveLayers)
        {
            RenderableLayer selectedLayer
                = (RenderableLayer) this.wwd.getModel().getLayers().getLayerByName(
                layer);
            selectedLayer.removeAllRenderables();
            selectedLayer.dispose();
            wwd.redraw();
//            this.wwd.getModel().getLayers().remove(selectedLayer);
        }
    }

    public void createThreadPool()
    {
        // Create a thread pool.
        this.threadPool = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS, 200,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());
    }

    public void setTerrainResolution(double TARGET_RESOLUTION)
    {
        // Be sure to re-use the Terrain object to take advantage of its caching.
        this.terrain = new HighResolutionTerrain(getWwd().getModel().getGlobe(),
            TARGET_RESOLUTION);
    }

    public void initializeRenderableLayers()
    {
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
        this.getWwd().getModel().getLayers().add(
            this.intersectionSightLinesLayer);

        this.intersectionPointsLayer = new RenderableLayer();
        this.intersectionPointsLayer.setName("Intersection Points Lines");
        this.getWwd().getModel().getLayers().add(this.intersectionPointsLayer);
    }

    public void setSightLineProperties()
    {
        // Set up a mouse handler to generate a grid and start intersection calculations when the user shift-clicks.
        this.getWwd().getInputHandler().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent mouseEvent)
            {
                // Control-Click cancels any currently running operation.
                if ((mouseEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)
                    != 0)
                {
                    if (calculationDispatchThread != null)
                    {
                        calculationDispatchThread.interrupt();
                    }

                    while (threadPool.getActiveCount() > 0)
                    {
                        threadPoolTasks.forEach(future -> future.cancel(true));
                        threadPool.purge();
                    }
                    deactivate();
                    clearPositionLists();
                    return;
                }

                // Alt-Click repeats the most recent calculations.
                if ((mouseEvent.getModifiersEx() & InputEvent.ALT_DOWN_MASK)
                    != 0)
                {
                    if (previousCurrentPosition == null)
                    {
                        return;
                    }

                    mouseEvent.consume(); // tell the rest of WW that this event has been processed

                    if (calculationDispatchThread != null
                        && calculationDispatchThread.isAlive())
                    {
                        calculationDispatchThread.interrupt();
                    }

                    if (threadPool.getActiveCount() > 0)
                    {
                        threadPoolTasks.forEach(future -> future.cancel(true));
                        threadPool.purge();
                    }
                    deactivate();
                    clearPositionLists();
                    wwd.redraw();

                    computeAndShowIntersections(previousCurrentPosition);
                    return;
                }

                // Perform the intersection tests in response to Shift-Click.
                if ((mouseEvent.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK)
                    == 0)
                {
                    return;
                }
                else
                {
                    mouseEvent.consume(); // tell the rest of WW that this event has been processed

                    if (calculationDispatchThread != null
                        && calculationDispatchThread.isAlive())
                    {
                        calculationDispatchThread.interrupt();
                    }

                    if (threadPool.getActiveCount() > 0)
                    {
                        threadPoolTasks.forEach(future -> future.cancel(true));
                        threadPool.purge();
                    }

                    final Position pos = getWwd().getCurrentPosition();
                    if (pos == null)
                    {
                        return;
                    }

                    clearLayers();
                    clearPositionLists();
                    if (grid != null)
                    {
                        grid.clear();
                    }

                    wwd.redraw();

                    // Start actual calculations and rendering
                    computeAndShowIntersections(pos);
                }
            }
        });
    }

    public void interrupt()
    {
        running.set(false);
        calculationDispatchThread.interrupt();
    }

    boolean isRunning()
    {
        return running.get();
    }

    boolean isStopped()
    {
        return stopped.get();
    }

    // use computeAndShow with my own supplied coordinates > position object

    protected void computeAndShowIntersections(final Position curPos)
    {
        this.previousCurrentPosition = curPos;

        SwingUtilities.invokeLater(() -> setCursor(WaitCursor));

        // Dispatch the calculation threads in a separate thread to avoid locking up the user interface.
        this.calculationDispatchThread = new Thread()
        {
            @Override
            public void start()
            {
                running.set(true);
                stopped.set(false);
                try
                {
                    performIntersectionTests(curPos);
                }
                catch (InterruptedException e)
                {
                    running.set(false);
                    stopped.set(true);
                    Thread.currentThread().interrupt();
                    System.out.println("Operation was interrupted");
                }
            }
        };
//
        this.calculationDispatchThread.start();
        System.out.println(calculationDispatchThread.toString());
    }

    // This is a collection of synchronized accessors to the list updated during the calculations.
    protected void clearPositionLists()
    {
        this.firstIntersectionPositions.clear();
//        this.sightLines.clear();
        this.gridSightLines.clear();
        this.intersectionSightLines.clear();
    }

    protected void addIntersectionPosition(Position position)
    {
        this.firstIntersectionPositions.add(position);
    }

    protected void addGridSightLine(Position gridPosition)
    {
        ShapeAttributes lineAttributes = new BasicShapeAttributes();
        lineAttributes.setDrawOutline(true);
        lineAttributes.setDrawInterior(false);
        lineAttributes.setOutlineMaterial(Material.GREEN);
        lineAttributes.setOutlineOpacity(0.6);
        lineAttributes.setOutlineWidth(2);

        Path path = new Path(exageratedReferencePosition,
            new Position(gridPosition,
                0));
        path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
//                path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        path.setAttributes(lineAttributes);
        this.gridSightLinesLayer.addRenderable(path);

//        this.gridSightLines.add(gridPosition);
    }

    protected void addIntersectionSightLine(Position positionA,
        Position positionB)
    {
        this.intersectionSightLines.add(new Position[] {positionA, positionB});
    }

    /**
     * Keeps the progress meter current. When calculations are complete,
     * displays the results.
     */

    protected void formGrid(final Position curPos, double height)
    {
        double gridRadius = GRID_RADIUS.degrees;
        Sector sector = Sector.fromDegrees(
            curPos.getLatitude().degrees - gridRadius,
            curPos.getLatitude().degrees + gridRadius,
            curPos.getLongitude().degrees - gridRadius,
            curPos.getLongitude().degrees + gridRadius);

        this.grid = buildGrid(sector, height, GRID_DIMENSION, GRID_DIMENSION);
        this.numGridPoints = grid.size();
    }

    protected void performIntersectionTests(final Position curPos)
        throws InterruptedException
    {

        // TODO - Update so that the grid shows the altitude of the selected point
        // instead of showing the arbitrary number "5" as originally shown below.
//        final double height = curPos.getAltitude();
        // Raise the selected location and the grid points a little above ground just to show we can.
//        final double height = 5; // meters
        final double height = 5; // meters
//        final double height = curAltitude + 5; // meters

//        System.out.println("height at curPos is: " + height);

        // Form the grid.
        this.formGrid(curPos, height);
//        this.iterableGrid = new Vector<>(grid);

        // TODO Remove call to showGrid here unless the actionlistener for the checkbox has been enabled

        // Compute the position of the selected location (incorporate its height).
        this.referencePosition = new Position(curPos.getLatitude(),
            curPos.getLongitude(), 0);
        this.referencePoint = terrain.getSurfacePoint(curPos.getLatitude(),
            curPos.getLongitude(), 0);
        this.exageratedReferencePosition = new Position(referencePosition,
            height * 5);

//            // Pre-caching is unnecessary and is useful only when it occurs before the intersection
//            // calculations. It will incur extra overhead otherwise. The normal intersection calculations
//            // cause the same caching, making subsequent calculations on the same area faster.
//            this.preCache(grid, this.referencePosition);
        // On the EDT, show the grid.
        if (SwingUtilities.isEventDispatchThread())
        {
            this.lineOfSightController.updateProgressBar(0);
            this.lineOfSightController.updateProgressBar(null);
            showGrid();
            showCenterPoint();
            getWwd().redraw();
        }
        else
        {
            SwingUtilities.invokeLater(() -> {
                this.lineOfSightController.updateProgressBar(0);
                this.lineOfSightController.updateProgressBar(null);
                showGrid();
                showCenterPoint();
                getWwd().redraw();
            });
        }

        this.startTime = System.currentTimeMillis();
        debugCounter.set(0);
        isDone.set(false);

        if (!stopped.get())
        {
            for (Position gridPos : grid) // for each grid point.
            {

                if (NUM_THREADS > 0)
                {
//                    System.out.println("NUM_THREADS: " + NUM_THREADS);
                    if (running.get())
                    {
                        threadPoolTasks.add(
                            this.threadPool.submit(new Intersector(gridPos)));
                    }
                }
                else
                {
//                System.out.println("Performing Intersection Calculation On Single Thread");
                    try
                    {
                        performIntersection(gridPos);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            running.set(false);
            this.updateProgress();
        }
    }

    /**
     * Performs one line of sight calculation between the reference position and
     * a specified grid position.
     *
     * @param gridPosition the grid position.
     * @throws InterruptedException if the operation is interrupted.
     */
    protected void performIntersection(Position gridPosition)
        throws InterruptedException
    {
        debugCounter.incrementAndGet();
//        System.out.println("Counter inside thread: " + debugCounter.get());
        // Intersect the line between this grid point and the selected position.
        Intersection[] intersections = this.terrain.intersect(
            new Position(this.referencePosition), new Position(gridPosition,
                0));
        if (intersections == null || intersections.length == 0)
        {
            // No intersection, so the line goes from the center to the grid point.
            this.addGridSightLine(gridPosition);
        }
        else
        {
            // Only the first intersection is shown.
            Vec4 iPoint = intersections[0].getIntersectionPoint();
            Vec4 gPoint = getSurfacePoint(gridPosition);

            // Check to see whether the intersection is beyond the grid point.
            if (iPoint.distanceTo3(this.referencePoint) >= gPoint.distanceTo3(
                this.referencePoint))
            {
                // Intersection is beyond the grid point; the line goes from the center to the grid point.
                this.addGridSightLine(
                    new Position(gridPosition));
            }
            else
            {
                // Compute the position corresponding to the intersection.
                Position iPosition
                    = this.terrain.getGlobe().computePositionFromPoint(iPoint);

                // The sight line goes from the user-selected position to the intersection position.
                ShapeAttributes lineAttributes = new BasicShapeAttributes();
                lineAttributes.setDrawOutline(true);
                lineAttributes.setDrawInterior(false);
                lineAttributes.setOutlineMaterial(Material.CYAN);
                lineAttributes.setOutlineOpacity(0.6);
                lineAttributes.setOutlineWidth(2);

                Path path = new Path(exageratedReferencePosition,
                    new Position(iPosition, 0));
                path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
//                path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                path.setAttributes(lineAttributes);
                this.intersectionSightLinesLayer.addRenderable(path);

                ShapeAttributes interToGridlineAttributes =
                    new BasicShapeAttributes();
                interToGridlineAttributes.setDrawOutline(true);
                interToGridlineAttributes.setDrawInterior(true);
                interToGridlineAttributes.setOutlineMaterial(Material.RED);
                interToGridlineAttributes.setOutlineOpacity(0.8);
                interToGridlineAttributes.setOutlineWidth(2);

                Path fromInterToGridPath = new Path(new Position(iPosition,
                    0), gridPosition);
                fromInterToGridPath.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                fromInterToGridPath.setAttributes(interToGridlineAttributes);

                this.intersectionSightLinesLayer.addRenderable(
                    fromInterToGridPath);

                // Keep track of the intersection positions.
                this.addIntersectionPosition(iPosition);

                PointPlacemarkAttributes intersectionPointAttributes =
                    new PointPlacemarkAttributes();
                intersectionPointAttributes.setLineMaterial(Material.CYAN);
                intersectionPointAttributes.setScale(6d);
                intersectionPointAttributes.setUsePointAsDefaultImage(true);

                PointPlacemark pm = new PointPlacemark(iPosition);
                pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                pm.setAttributes(intersectionPointAttributes);
                pm.setValue(AVKey.DISPLAY_NAME, iPosition.toString());
                this.intersectionPointsLayer.addRenderable(pm);

//                System.out.println("Intersection Found for: " + this.referencePosition + " : " + iPosition);
            }
        }

        updateProgress();
    }

    public Vec4 getSurfacePoint(Position gridPosition)
    {
        return terrain.getSurfacePoint(gridPosition.getLatitude(),
            gridPosition.getLongitude(),
            gridPosition.getAltitude());
    }

    protected void updateProgress()
    {
        // Update the progress bar only once every 250 milliseconds to avoid stealing time from calculations.
        if (this.debugCounter.get() >= this.numGridPoints)
        {
            endTime = System.currentTimeMillis();
        }
        else if (System.currentTimeMillis() < this.lastTime + 250)
        {
            return;
        }
        this.lastTime = System.currentTimeMillis();

        // On the EDT, update the progress bar and if calculations are complete, update the WorldWindow.
        SwingUtilities.invokeLater(() -> {
            int progress = (int) (100d * debugCounter.get()
                / (double) numGridPoints);
            this.lineOfSightController.updateProgressBar(progress);

            if (progress >= 100)
            {
                setCursor(Cursor.getDefaultCursor());
                this.lineOfSightController.updateProgressBar(
                    (endTime - startTime) + " ms");
//
                if (!isDone.get())
                {
                    this.postResults();
                    System.out.printf("Calculation time %d milliseconds\n",
                        endTime - startTime);
                }

                isDone.set(true);
            }
        });
    }

    public boolean isLayersNotNull()
    {
        return layersNotNull;
    }

    /**
     * Inner {@link Runnable} to perform a single line/terrain intersection
     * calculation.
     */
    protected class Intersector implements Runnable
    {

        protected final Position gridPosition;

        public Intersector(Position gridPosition)
        {
            this.gridPosition = gridPosition;
        }

        @Override
        public void run()
        {
            try
            {
                performIntersection(this.gridPosition);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected Vector<Position> buildGrid(Sector sector, double height,
        int nLatCells, int nLonCells)
    {
        Vector<Position> newGrid = new Vector<>(
            (nLatCells + 1) * (nLonCells + 1));

        double dLat = sector.getDeltaLatDegrees() / nLatCells;
        double dLon = sector.getDeltaLonDegrees() / nLonCells;

        for (int j = 0; j <= nLatCells; j++)
        {
            double lat = j == nLatCells
                ? sector.getMaxLatitude().degrees
                : sector.getMinLatitude().degrees + j * dLat;

            for (int i = 0; i <= nLonCells; i++)
            {
                double lon = i == nLonCells
                    ? sector.getMaxLongitude().degrees
                    : sector.getMinLongitude().degrees + i * dLon;

                Position p = Position.fromDegrees(lat, lon, height);
                newGrid.add(p);
            }
        }

        return newGrid;
    }

    protected void preCache(Vector<Position> grid, Position centerPosition)
        throws InterruptedException
    {
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

        SwingUtilities.invokeLater(
            () -> this.lineOfSightController.updateProgressBar(100));

        long end = System.currentTimeMillis();
        System.out.printf(
            "Pre-caching time %d milliseconds, cache usage %f, tiles %d\n",
            end - start,
            terrain.getCacheUsage(), terrain.getNumCacheEntries());
    }

    protected void clearLayers()
    {
        this.intersectionsLayer.removeAllRenderables();
        this.intersectionPointsLayer.removeAllRenderables();
        this.intersectionSightLinesLayer.removeAllRenderables();
        this.gridSightLinesLayer.removeAllRenderables();
        this.gridPoints.removeAllRenderables();
        this.gridOrigin.removeAllRenderables();
    }

    /**
     * Updates the WorldWind model with the new intersection locations and sight
     * lines.
     */
    protected void postResults()
    {
        if (firstIntersectionPositions.size() < 1)
        {
            System.out.println("No Intersections!");
        }

        this.layersNotNull = true;
        this.stopped.set(true);

        this.getWwd().redraw();

        // Should do this on a new thread so program doesn't hang
        Runnable writeCSVs = new Runnable()
        {
            @Override
            public void run()
            {
                csvOutput(grid, "Grid Points");
                csvOutput(firstIntersectionPositions, "Intersections");
            }
        };

        writeCSVs.run();

    }

    protected void csvOutput(Vector<Position> positions, String ListName)
    {

        if(positions.size() < 1){
            return;
        }
        File outputDir = new File("csv_output");
        if (!outputDir.exists())
        {
            outputDir.mkdir();
        }

        long timeStamp = System.currentTimeMillis();

        DecimalFormat formatter = new DecimalFormat("###_##");
        String origin =
            formatter.format(referencePosition.getLongitude().getDegrees()) +
                "_" + formatter.format(
                referencePosition.getLatitude().getDegrees());
        File filename =
            new File(
                outputDir + "\\" + ListName + "_" + origin + "_" + timeStamp +
                    ".csv");

        System.out.println("Writing File: " + filename.getPath());
        try (PrintWriter writer = new PrintWriter(filename))
        {
            int rowNum = 0;
            String header =
                ListName + "_Entries," + "Longitude" + "," + "Latitude" + "," +
                    "Altitude" + "," + "Origin";
            writer.println(header);

            // Add the origin to the first row of each CSV
            positions.add(0, referencePosition);

            for (Position p : positions)
            {
                // Get the position with correct elevation for the origin point
                if (rowNum == 0)
                {
                    p =
                        this.terrain.getGlobe().computePositionFromPoint(
                            referencePoint);
                }

                // Only the first row should have the last column, Origin
                // set to true
                String pLabel = rowNum + "," +
                    p.longitude.toString() + "," + p.latitude.toString() + ","
                    + p.elevation + "," + (rowNum == 0);
                writer.println(pLabel.replaceAll("°+", ""));
                rowNum++;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    protected void showIntersections(Vector<Position> intersections)
    {
        this.intersectionsLayer.removeAllRenderables();

        // Display the intersections as CYAN points.
        PointPlacemarkAttributes intersectionPointAttributes;
        intersectionPointAttributes = new PointPlacemarkAttributes();
        intersectionPointAttributes.setLineMaterial(Material.CYAN);
        intersectionPointAttributes.setScale(6d);
        intersectionPointAttributes.setUsePointAsDefaultImage(true);

        for (Position p : intersections)
        {
            PointPlacemark pm = new PointPlacemark(p);
            pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pm.setAttributes(intersectionPointAttributes);
            pm.setValue(AVKey.DISPLAY_NAME, p.toString());
            this.intersectionsLayer.addRenderable(pm);
        }
    }

    protected void showIntersectionSightLines(Vector<Position[]> sightLines)
    {
        this.intersectionSightLinesLayer.removeAllRenderables();

        // Display the sight lines as green lines.
        ShapeAttributes lineAttributes;
        lineAttributes = new BasicShapeAttributes();
        lineAttributes.setDrawOutline(true);
        lineAttributes.setDrawInterior(false);
        lineAttributes.setOutlineMaterial(Material.CYAN);
        lineAttributes.setOutlineOpacity(0.6);

        for (Position[] pp : sightLines)
        {

            Path path = new Path(pp[0], pp[1]);
//            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            path.setAttributes(lineAttributes);
            this.intersectionSightLinesLayer.addRenderable(path);
        }
    }

    protected void showGridSightLines(Vector<Position> grid, Position cPos)
    {
        this.gridSightLinesLayer.removeAllRenderables();

        // Display lines from the center to each grid point.
        ShapeAttributes lineAttributes;
        lineAttributes = new BasicShapeAttributes();
        lineAttributes.setDrawOutline(true);
        lineAttributes.setDrawInterior(false);
        lineAttributes.setOutlineMaterial(Material.GREEN);
        lineAttributes.setOutlineOpacity(0.6);

        for (Position p : grid)
        {

            Path path = new Path(cPos, p);
//            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            path.setAttributes(lineAttributes);
            this.gridSightLinesLayer.addRenderable(path);
        }
    }

    protected void showGrid()
    {
        this.gridPoints.removeAllRenderables();

        // Display the grid points in yellow.
        PointPlacemarkAttributes gridPointAttributes;
        gridPointAttributes = new PointPlacemarkAttributes();
        gridPointAttributes.setLineMaterial(Material.YELLOW);
        gridPointAttributes.setScale(6d);
        gridPointAttributes.setUsePointAsDefaultImage(true);

        for (Position p : grid)
        {
            Position p2 = new Position(p,
                terrain.getGlobe().getElevation(p.getLatitude(),
                    p.getLongitude()));

            PointPlacemark pm = new PointPlacemark(p2);
            pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pm.setAttributes(gridPointAttributes);
            pm.setLineEnabled(true);
//            pm.setValue(AVKey.DISPLAY_NAME, iPosition.toString());
            pm.setValue(AVKey.DISPLAY_NAME, p2.toString());
            this.gridPoints.addRenderable(pm);
        }
    }

    protected void showCenterPoint()
    {
        // Display the center point in red.
        PointPlacemarkAttributes selectedLocationAttributes;
        selectedLocationAttributes = new PointPlacemarkAttributes();
        selectedLocationAttributes.setLineMaterial(Material.RED);
        selectedLocationAttributes.setScale(8d);
        selectedLocationAttributes.setUsePointAsDefaultImage(true);

        // This is also an inefficient call, doubling what was done in the performIntersections method
        // but it is here until a more accessible static data member is created

        Position p = this.terrain.getGlobe().computePositionFromPoint(
            this.referencePoint);
        PointPlacemark pm = new PointPlacemark(exageratedReferencePosition);
        pm.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
//        pm.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        pm.setAttributes(selectedLocationAttributes);
        pm.setValue(AVKey.DISPLAY_NAME, p.toString());
        pm.setLineEnabled(true);
        this.gridOrigin.addRenderable(pm);
//        System.out.println(pm.getAttributes());
    }

    public void toggleGridLines(boolean toggle)
    {
        this.gridSightLinesLayer.setEnabled(toggle);
    }

    public void toggleGridPoints(boolean toggle)
    {
        this.gridPoints.setEnabled(toggle);
    }

    public void toggleGridOrigin(boolean toggle)
    {
        this.gridOrigin.setEnabled(toggle);
    }

    public void togglesIntersections(boolean toggle)
    {
        this.intersectionsLayer.setEnabled(toggle);
    }

    public void toggleIntersectionSightLines(boolean toggle)
    {
        this.intersectionSightLinesLayer.setEnabled(toggle);
    }

    public void toggleIntersectionPoints(boolean toggle)
    {
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

    public AppFrame getCms()
    {
        return cms;
    }

    public final void setCms(AppFrame cms)
    {
        this.cms = cms;
    }
}
