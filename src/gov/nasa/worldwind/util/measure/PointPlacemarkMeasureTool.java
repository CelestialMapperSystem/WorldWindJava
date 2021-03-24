/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.measure;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;

public class PointPlacemarkMeasureTool extends MeasureTool
{
    public static final String SHAPE_POINT = "MeasureTool.ShapePoint";

    public PointPlacemarkMeasureTool(WorldWindow wwd)
    {
        super(wwd);
        this.measureShapeType = SHAPE_POINT;
        this.setShowAnnotation(true);
    }

    public PointPlacemarkMeasureTool(WorldWindow wwd, RenderableLayer applicationLayer)
    {
        super(wwd, applicationLayer);
        this.measureShapeType = SHAPE_POINT;
        this.setShowAnnotation(true);
    }

    /**
     * Set the measure shape to an arbitrary list of positions. If the provided list contains two positions, the measure
     * shape will be set to {@link #SHAPE_LINE}. If more then two positions are provided, the measure shape will be set
     * to {@link #SHAPE_PATH} if the last position differs from the first (open path), or {@link #SHAPE_POLYGON} if the
     * path is closed.
     *
     * @param newPositions the shape position list.
     */
    @Override
    public void setPositions(ArrayList<? extends Position> newPositions)
    {
        if (newPositions == null) {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (newPositions.size() < 2) {
            setMeasureShapeType(SHAPE_POINT);
        }

        this.clear();

        // Setup the proper measure shape
        boolean closedShape = newPositions.get(0).equals(newPositions.get(newPositions.size() - 1));
        if (newPositions.size() > 2 && closedShape) {
            setMeasureShapeType(SHAPE_POLYGON);
        } else {
            setMeasureShapeType(getPathType(newPositions));
        }

        // Import positions and create control points
        for (int i = 0; i < newPositions.size(); i++) {
            Position pos = newPositions.get(i);
            this.positions.add(pos);
            if (i < newPositions.size() - 1 || !closedShape) {
                addControlPoint(pos, CONTROL_TYPE_LOCATION_INDEX, this.positions.size() - 1);
            }
         }

        // Update line heading if needed
        if (this.measureShapeType.equals(SHAPE_LINE)) {
            this.shapeOrientation = LatLon.greatCircleAzimuth(this.positions.get(0), this.positions.get(1));
        }

        // Update screen shapes
        updateMeasureShape();
        this.firePropertyChange(EVENT_POSITION_REPLACE, null, null);
        this.wwd.redraw();
    }



    /**
     * Remove the last control point from the current measure shape.
     */
    @Override
    public void removeControlPoint() {
        Position currentLastPosition = null;
        if (this.isRegularShape()) {
            if (this.shapeRectangle != null) {
                this.shapeRectangle = null;
                this.shapeOrientation = null;
                this.positions.clear();
                // remove all control points except center which is first
                while (this.controlPoints.size() > 1) {
                    this.controlPoints.remove(1);
                }
            } else if (this.shapeCenterPosition != null) {
                this.shapeCenterPosition = null;
                this.controlPoints.clear();
            }
        } else {
            if (this.positions.isEmpty()) {
                return;
            }

            if (!this.measureShapeType.equals(SHAPE_POLYGON) || this.positions.size() == 1) {
                currentLastPosition = this.positions.get(this.positions.size() - 1);
                this.positions.remove(this.positions.size() - 1);
            } else {
                // For polygons with more then 2 points, the last position is the same as the first, so remove before it
                currentLastPosition = this.positions.get(this.positions.size() - 2);
                this.positions.remove(this.positions.size() - 2);
                if (positions.size() == 2) {
                    positions.remove(1); // remove last loop position when a polygon shrank to only two (same) positions
                }
            }
            if (this.controlPoints.size() > 0) {
                this.controlPoints.remove(this.controlPoints.size() - 1);
            }
        }
        this.controlPointsLayer.setRenderables(this.controlPoints);
        // Update screen shapes
        updateMeasureShape();
        this.firePropertyChange(EVENT_POSITION_REMOVE, currentLastPosition, null);
        this.wwd.redraw();
    }

    /**
     * Update the current measure shape according to a given control point position.
     *
     * @param point one of the shape control points.
     */
    @Override
    public void moveControlPoint(ControlPoint point) {
        moveControlPoint(point, null); // use the default mode.
    }

    /**
     * Update the current measure shape according to a given control point position and shape edition mode.
     *
     * @param point one of the shape control points.
     * @param mode the shape edition mode.
     */
    public void moveControlPoint(ControlPoint point, String mode) {
        boolean isSinglePoint = false;
        if (point == null) {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (point.getValue(CONTROL_TYPE_REGULAR_SHAPE) != null) {
            // Update shape properties
            updateShapeProperties((String) point.getValue(CONTROL_TYPE_REGULAR_SHAPE), point.getPosition(), mode);
            updateShapeControlPoints();
            //positions = makeShapePositions();
        }

        if (point.getValue(CONTROL_TYPE_LOCATION_INDEX) != null) {
            int positionIndex = (Integer) point.getValue(CONTROL_TYPE_LOCATION_INDEX);
            // Update positions
            Position surfacePosition = computeSurfacePosition(point.getPosition());
            surfacePosition = new Position(point.getPosition(), surfacePosition.getAltitude());
            positions.set(positionIndex, surfacePosition);
            // Update last pos too if polygon and first pos changed
            if (measureShapeType.equals(SHAPE_POLYGON) && positions.size() > 2 && positionIndex == 0) {
                positions.set(positions.size() - 1, surfacePosition);
            }
            // Update heading for simple line
            if (measureShapeType.equals(SHAPE_LINE) && positions.size() > 1) {
                shapeOrientation = LatLon.greatCircleAzimuth(positions.get(0), positions.get(1));
            }
            if (measureShapeType.equals(SHAPE_POINT) && positions.size() == 1) {
                positions.set(positions.size() - 1, surfacePosition);
                isSinglePoint = true;
            }
        }

        // Update rendered shapes
        updateMeasureShape();
    }


}
