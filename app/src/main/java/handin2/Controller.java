package handin2;

import handin2.UI.Marker;
import javafx.geometry.Point2D;

public class Controller {
    double lastX;
    double lastY;

    public Controller(Model model, View view) {
        view.pane.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();

            if(e.isSecondaryButtonDown() && view.placeMarkerMode) {
                Point2D mouseToCoordinates = view.mousetoModel(lastX, lastY);

                Marker marker = new Marker(mouseToCoordinates.getY(), mouseToCoordinates.getX());
                view.markers.add(marker);
                view.redraw();
            }
        });
        view.pane.setOnMouseDragged(e -> {
            if(e.isPrimaryButtonDown()) {
                double dx = e.getX() - lastX;
                double dy = e.getY() - lastY;
                view.pan(dx, dy);

                lastX = e.getX();
                lastY = e.getY();
            }
        });
        view.pane.setOnScroll(e -> {
            double factor = e.getDeltaY();
            double zoomFactor = Math.pow(1.01, factor);

            double zoomConstant = (view.maxlat - view.minlat) + (view.maxlon - view.minlon);
            if(zoomConstant < 0.001 && factor > 0) return;

            view.zoom(e.getX(), e.getY(), zoomFactor);
        });
    }

}
