package handin2;

import handin2.Interfaces.Geometry;
import handin2.Interfaces.Spatial;

import java.io.Serializable;

public class BoundingBox implements Serializable, Geometry {
    public double minlat, maxlat, minlon, maxlon;

    public BoundingBox(){
        minlat = minlon = Double.MAX_VALUE;
        maxlat = maxlon = -Double.MAX_VALUE;
    }

    BoundingBox(double minlat, double maxlat, double minlon, double maxlon){
        this.minlat = minlat;
        this.minlon = minlon;
        this.maxlat = maxlat;
        this.maxlon = maxlon;
    }

    public boolean intersects(BoundingBox w){
        return (minlat <= w.maxlat && maxlat >= w.minlat && minlon <= w.maxlon && maxlon >= w.minlon);
    }

    public boolean intersects(Spatial object){
        return intersects(object.getBoundingBox());
    }

    public boolean contains(BoundingBox w){
        return (minlat <= w.minlat && maxlat >= w.maxlat && minlon <= w.minlon && maxlon >= w.maxlon);
    }

    public boolean contains(Spatial object){
        return contains(object.getBoundingBox());
    }

    public BoundingBox combinePeek(BoundingBox boundingBox){
        BoundingBox b = new BoundingBox();
        b.minlat = Math.min(minlat, boundingBox.minlat);
        b.minlon = Math.min(minlon, boundingBox.minlon);
        b.maxlat = Math.max(maxlat, boundingBox.maxlat);
        b.maxlon = Math.max(maxlon, boundingBox.maxlon);
        return b;
    }

    public BoundingBox combinePeek(Spatial object){
        return combinePeek(object.getBoundingBox());
    }

    public void combine(BoundingBox boundingBox){
        minlat = Math.min(minlat, boundingBox.minlat);
        minlon = Math.min(minlon, boundingBox.minlon);
        maxlat = Math.max(maxlat, boundingBox.maxlat);
        maxlon = Math.max(maxlon, boundingBox.maxlon);
    }

    public void combine(Spatial object){
        combine(object.getBoundingBox());
    }

    public void replace(BoundingBox boundingBox){
        minlat = boundingBox.minlat;
        minlon = boundingBox.minlon;
        maxlat = boundingBox.maxlat;
        maxlon = boundingBox.maxlon;
    }

    public void replace(Spatial object){
        replace(object.getBoundingBox());
    }

    public double area(){
        return (maxlat - minlat) * (maxlon - minlon);
    }

    public double overlap(BoundingBox boundingBox){
        return Math.max(0.0, (Math.min(maxlat, boundingBox.maxlat) - Math.max(minlat, boundingBox.minlat)) * (Math.min(maxlon, boundingBox.maxlon) - Math.max(minlon, boundingBox.minlon)));
    }

    public double overlap(Spatial object){
        return overlap(object.getBoundingBox());
    }

    @Override
    public double distanceTo(double lon, double lat){
        var dlon = Math.max(Math.max(this.minlon - lon, lon - this.maxlon), 0);
        var dlat = Math.max(Math.max(this.minlat - lat, lat - this.maxlat), 0);
        return Math.sqrt(dlon*dlon + dlat*dlat);
    }

    public double distanceTo(Node node){
        return distanceTo(node.lon, node.lat);
    }
}