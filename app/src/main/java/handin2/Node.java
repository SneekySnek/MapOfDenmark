package handin2;

import handin2.Interfaces.Geometry;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;

public class Node implements Serializable, Geometry {
    public double lat, lon;
    Address address;
    boolean hasAppeared;

    public Node(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
    // getter and setter for the address
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void draw(GraphicsContext gc) {
        gc.strokeOval(lon-0.000005 , lat-0.000005, 0.00001, 0.00001);
        gc.fillOval(lon-0.000005, lat-0.000005, 0.00001, 0.00001);
    }

    @Override
    public double distanceTo(double lon, double lat) {
        return Math.sqrt(Math.pow((this.lon - lon),2) + Math.pow((this.lat - lat),2));
    }
}