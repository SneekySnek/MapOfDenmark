package handin2.Way;

import java.io.Serializable;
import java.util.ArrayList;

import handin2.BoundingBox;
import handin2.Interfaces.Geometry;
import handin2.Interfaces.Spatial;
import handin2.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

public class Way implements Serializable, Spatial, Geometry {
    public double[] coords;
    public BoundingBox boundingBox;

    /**
     * Create bounding box and coordinates array based on provided
     * nodes list
     * @param way
     */
    public Way(ArrayList<Node> way) {
        boundingBox = new BoundingBox();

        coords = new double[way.size() * 2];
        boundingBox.minlat = boundingBox.maxlat = way.get(0).lat;
        boundingBox.minlon = boundingBox.maxlon = way.get(0).lon;

        for (int i = 0; i < way.size(); i++) {
            var node = way.get(i);
            coords[2 * i] = node.lon;
            coords[2 * i + 1] = node.lat;

            if      (node.lat < boundingBox.minlat) {boundingBox.minlat = node.lat; }
            else if (node.lat > boundingBox.maxlat) {boundingBox.maxlat = node.lat; }
            if      (node.lon < boundingBox.minlon) {boundingBox.minlon = node.lon; }
            else if (node.lon > boundingBox.maxlon) {boundingBox.maxlon = node.lon; }
        }
    }

    public BoundingBox getBoundingBox(){
        return boundingBox;
    }

    public void outlineArea(GraphicsContext gc, Affine trans) {
        gc.beginPath();

        gc.moveTo(coords[0], coords[1]);
        for (int i = 2 ; i < coords.length ; i += 2) {
            gc.lineTo(coords[i], coords[i+1]);
        }
    }

    public void draw(GraphicsContext gc, Affine trans) {
        outlineArea(gc, trans);
        gc.stroke();
    }

    public void drawFill(GraphicsContext gc, Affine trans, Color color) {
        outlineArea(gc, trans);
        gc.setFill(color);
        gc.fill();
    }

    @Override
    public double distanceTo(double lon, double lat) {
        return boundingBox.distanceTo(lon, lat);
    }

    public double[] findClosestCoordinates(double lon, double lat) {
        double closestLon = 0;
        double closestLat = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < coords.length - 2; i += 2) {
            double lon1 = coords[i];
            double lat1 = coords[i + 1];
            double lon2 = coords[i + 2];
            double lat2 = coords[i + 3];

            // Calculate the closest point on the line segment to (lon, lat)
            double[] closestPoint = closestPointOnSegment(lon1, lat1, lon2, lat2, lon, lat);
            double distance = Math.sqrt(Math.pow(lon - closestPoint[0], 2) + Math.pow(lat - closestPoint[1], 2));

            // Update minimum distance and corresponding coordinates
            if (distance < minDistance) {
                minDistance = distance;
                closestLon = closestPoint[0];
                closestLat = closestPoint[1];
            }
        }

        return new double[]{closestLon, closestLat};
    }

    // Method to find the closest point on a line segment to a given point
    public double[] closestPointOnSegment(double lon1, double lat1, double lon2, double lat2, double lon, double lat) {
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double t = ((lon - lon1) * dlon + (lat - lat1) * dlat) / (dlon * dlon + dlat * dlat);

        if (t < 0) {
            return new double[]{lon1, lat1};
        } else if (t > 1) {
            return new double[]{lon2, lat2};
        } else {
            double closestLon = lon1 + t * dlon;
            double closestLat = lat1 + t * dlat;
            return new double[]{closestLon, closestLat};
        }
    }
}