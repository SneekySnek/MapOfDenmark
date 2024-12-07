package handin2.UI;

import handin2.View;
import javafx.scene.image.Image;

public class Marker {
    public double lat, lon;

    public Marker(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public void draw(Image locationIcon, double maxlat, double minlat, double maxlon, double minlon, View view) {
        double zoomFactor = (maxlat - minlat) + (maxlon - minlon);
        if(zoomFactor < 0.8) {
            zoomFactor *= 0.7;
        } else {
            zoomFactor *= 0.5;
        }

        double scaledWidth = Math.max(0.02 * zoomFactor, 0.00004);
        double scaledHeight = Math.max(0.03 * zoomFactor, 0.00006);

        double centerImageX = lon - (scaledWidth / 2);
        double bottomImageY = lat - scaledHeight;

        view.gc.drawImage(locationIcon, centerImageX, bottomImageY, scaledWidth, scaledHeight);
    }
}
