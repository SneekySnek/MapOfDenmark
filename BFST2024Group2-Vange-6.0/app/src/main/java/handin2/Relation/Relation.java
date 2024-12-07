package handin2.Relation;

import handin2.BoundingBox;
import handin2.Interfaces.Spatial;
import handin2.Way.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.List;

public class Relation implements Serializable, Spatial {
    double[] coordsOuter;
    double[] coordsInner;

    BoundingBox boundingBox;

    /**
     * Populate waysOuter and waysInner lists with coordinates from inner and outer ways
     * @param waysOuter
     * @param waysOuterCoordsSize
     * @param waysInner
     * @param waysInnerCoordsSize
     */
    Relation(List<Way> waysOuter, int waysOuterCoordsSize, List<Way> waysInner, int waysInnerCoordsSize) {
        coordsOuter = new double[waysOuterCoordsSize];
        coordsInner = new double[waysInnerCoordsSize];
        addCoords(waysOuter, coordsOuter);
        addCoords(waysInner, coordsInner);
    }

    /**
     * Create a relation for a single way. This constructor is used in RelationLand to handle land
     * such as islands and islets consisting of only 1 way.
     * @param way
     */
    Relation(Way way) {
        coordsOuter = new double[way.coords.length];
        coordsInner = new double[0];
        for(int i = 2; i <= coordsOuter.length; i += 2) {
            coordsOuter[i-2] = way.coords[i-2];
            coordsOuter[i-1] = way.coords[i-1];
        }
        boundingBox = way.boundingBox;
    }

    public BoundingBox getBoundingBox(){
        return boundingBox;
    }

    private void addCoords(List<Way> ways, double[] coordsArray) {
        if(ways.isEmpty()) return;

        int k = 2;
        for(Way way : ways) {
            for(int i = 2; i <= way.coords.length; i += 2) {
                coordsArray[k-2] = way.coords[i-2];
                coordsArray[k-1] = way.coords[i-1];

                k += 2;
            }
            updateBoundingBox(way.boundingBox);
        }
    }

    private void updateBoundingBox(BoundingBox newWayBoundingBox) {
        if(boundingBox == null) {
            boundingBox = new BoundingBox();
            boundingBox.minlat = newWayBoundingBox.minlat;
            boundingBox.maxlat = newWayBoundingBox.maxlat;
            boundingBox.minlon = newWayBoundingBox.minlon;
            boundingBox.maxlon = newWayBoundingBox.maxlon;
        } else {
            if(newWayBoundingBox.minlat < boundingBox.minlat) boundingBox.minlat = newWayBoundingBox.minlat;
            else if (newWayBoundingBox.maxlat > boundingBox.maxlat) boundingBox.maxlat = newWayBoundingBox.maxlat;
            if(newWayBoundingBox.minlon < boundingBox.minlon) boundingBox.minlon = newWayBoundingBox.minlon;
            else if (newWayBoundingBox.maxlon > boundingBox.maxlon) boundingBox.maxlon = newWayBoundingBox.maxlon;
        }
    }

    public void draw(GraphicsContext gc) {
        draw(gc, Color.TRANSPARENT);
    }

    public void draw(GraphicsContext gc, Color color) {
        draw(gc, color, color);
    }

    public void draw(GraphicsContext gc, Color innerColor, Color outerColor) {
        draw(coordsOuter, gc, outerColor);
        draw(coordsInner, gc, innerColor);
    }

    public void draw(double[] coords, GraphicsContext gc, Color color) {
        if(coords.length < 1) return;

        gc.beginPath();
        gc.moveTo(coords[0], coords[1]);

        boolean beginNewPath = false;
        double wayBeginPathLon = coords[0];
        double wayBeginPathLat = coords[1];

        for (int i = 2; i < coords.length; i += 2) {
            if(beginNewPath) {
                gc.beginPath();
                gc.moveTo(coords[i], coords[i+1]);
                wayBeginPathLon = coords[i];
                wayBeginPathLat = coords[i+1];
                beginNewPath = false;
                continue;
            }

            gc.lineTo(coords[i], coords[i+1]);

            if(coords[i] == wayBeginPathLon && coords[i+1] == wayBeginPathLat) {
                gc.setFill(color);
                gc.fill();
                beginNewPath = true;
            }
        }
    }

    protected void drawOuterWithStroke(GraphicsContext gc, Color strokeColor, Color fillColor) {
        gc.beginPath();
        gc.moveTo(coordsOuter[0], coordsOuter[1]);

        boolean beginNewPath = false;
        double wayBeginPathLon = coordsOuter[0];
        double wayBeginPathLat = coordsOuter[1];

        for (int i = 2 ; i < coordsOuter.length; i += 2) {
            if(beginNewPath) {
                gc.beginPath();
                gc.moveTo(coordsOuter[i], coordsOuter[i+1]);
                wayBeginPathLon = coordsOuter[i];
                wayBeginPathLat = coordsOuter[i+1];
                beginNewPath = false;
                continue;
            }

            gc.lineTo(coordsOuter[i], coordsOuter[i+1]);

            if(coordsOuter[i] == wayBeginPathLon && coordsOuter[i+1] == wayBeginPathLat) {
                gc.setStroke(strokeColor);
                gc.stroke();
                gc.setFill(fillColor);
                gc.fill();
                beginNewPath = true;
            }
        }

        gc.setStroke(Color.rgb(189, 205, 217));
    }
}
