package handin2;

import handin2.Relation.Relation;
import handin2.Relation.RelationLand;
import handin2.UI.*;
import handin2.Way.Way;
import handin2.Way.WayRoad;
import handin2.Way.WayWater;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class View {
    Canvas canvas = new Canvas(640, 480);
    public GraphicsContext gc = canvas.getGraphicsContext2D();
    Affine originalTransform = new Affine();;
    Affine trans = new Affine();
    static double circleSize = 0.05;
    Model model;
    public List<Double> shortestPath;
    public List<Marker> markers;
    Image locationIcon;
    MainPane pane;
    Boolean placeMarkerMode = false;
    public SearchBox searchBox2;
    public SearchBox searchBox;
    public GeoScale scaleBar;

    public double minlat, maxlat, minlon, maxlon;

    public View(Model model, Stage primaryStage) {
        scaleBar = new GeoScale(model.minlat, model.minlon, model.maxlat, model.maxlon);
        this.model = model;
        this.shortestPath = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.locationIcon = new Image("/Icons/locationMarker.png");

        searchBox = new SearchBox(this, model, false);
        searchBox.setId("searchBox");
        searchBox2 = new SearchBox(this, model, true);
        pane = new MainPane(this, model, canvas, primaryStage);
        Scene scene = new Scene(pane);

        primaryStage.setScene(scene);
        primaryStage.show();

        redraw();
        pan(-0.56 * model.minlon, model.maxlat);
        zoom(0, 0, canvas.getHeight() / (model.maxlat - model.minlat));

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double oldWidth = canvas.getWidth();
            double newWidth = scene.getWidth();

            double translateX = (newWidth - oldWidth) / 2;
            pan(translateX, 0);

            canvas.setWidth(newWidth);
            redraw();
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double oldHeight = canvas.getHeight();
            double newHeight = scene.getHeight();

            double translateY = (newHeight - oldHeight) / 2;
            pan(0, translateY);

            canvas.setHeight(newHeight);
            redraw();
        });

        String path = this.getClass().getResource("/main.css").toExternalForm();
        scene.getStylesheets().add(path);
    }

    public void redraw() {
        updateViewingArea();

        List<RelationLand> relevantLand = model.landRelations.search(minlat, maxlat, minlon, maxlon);
        List<WayWater> relevantWaterWays = new ArrayList<>();
        List<WayRoad> relevantRoads;
        Color currentLandColor = Util.currentLandColor;

        List<Way> relevantWaysLight;
        List<Way> relevantWaysMid = new ArrayList<>();
        List<Way> relevantWaysDense = new ArrayList<>();

        List<Relation> relevantRelationsLight;
        List<Relation> relevantRelationsMid = new ArrayList<>();
        List<Relation> relevantRelationsDense = new ArrayList<>();

        double zoomConstant = ((maxlat - minlat) * (maxlon - minlon));
        int zoomLevel = zoomConstant < 0.00008 ? 1 : zoomConstant < 0.01 ? 2 : 3; // Has level 1 to 3 where 1 is most zoomed in and 3 is most zoomed out

        if(zoomLevel == 1) {
            relevantWaysDense = model.waysDense.search(minlat, maxlat, minlon, maxlon);
            relevantWaterWays = model.waterWays.search(minlat, maxlat, minlon, maxlon);
            relevantRelationsDense = model.relationsDense.search(minlat, maxlat, minlon, maxlon);
            relevantRoads = model.roadsDense.search(minlat, maxlat, minlon, maxlon);

            relevantWaysMid = model.waysMid.search(minlat, maxlat, minlon, maxlon);
            relevantRelationsMid = model.relationsMid.search(minlat, maxlat, minlon, maxlon);
        } else if(zoomLevel == 2) {
            relevantWaysMid = model.waysMid.search(minlat, maxlat, minlon, maxlon);
            relevantWaterWays = model.waterWays.search(minlat, maxlat, minlon, maxlon);
            relevantRelationsMid = model.relationsMid.search(minlat, maxlat, minlon, maxlon);
            relevantRoads = model.roadsMid.search(minlat, maxlat, minlon, maxlon);
        } else {
            relevantRoads = model.roadsLight.search(minlat, maxlat, minlon, maxlon);
            currentLandColor = Util.currentNatureColor;
        }
        relevantWaysLight = model.waysLight.search(minlat, maxlat, minlon, maxlon);
        relevantRelationsLight = model.relationsLight.search(minlat, maxlat, minlon, maxlon);

        gc.setTransform(new Affine());

        gc.setFill(Util.currentWaterColor);

        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
        gc.setStroke(Util.currentBaseColor);

        for(RelationLand land : relevantLand) {
            land.draw(gc, currentLandColor);
        }

        drawRelations(relevantRelationsDense);
        drawRelations(relevantRelationsMid);
        drawRelations(relevantRelationsLight);

        drawWays(relevantWaysDense);
        drawWays(relevantWaysMid);
        drawWays(relevantWaysLight);

        for(WayWater waterWay : relevantWaterWays) {
            waterWay.draw(gc, trans);
        }

        gc.setStroke(Util.currentBaseColor);
        for(WayRoad road : relevantRoads) {
            road.draw(gc, trans, zoomLevel);
        }

        if(!this.shortestPath.isEmpty()) {
            drawDirectionPath(this.shortestPath);
        }

        if(!this.markers.isEmpty()) {
            drawMarkers();
        }

        searchBox.draw(gc, circleSize);
        searchBox2.draw(gc, circleSize);

        double diagonalScreen = Math.sqrt(Math.pow(canvas.getWidth(), 2) + Math.pow(canvas.getHeight(), 2));
        this.scaleBar.updateScaleBar(minlon, maxlon, minlat, maxlat, diagonalScreen);
    }

    private void drawRelations(List<Relation> relevantRelations) {
        for(Relation land : relevantRelations) {
            land.draw(gc);
        }
    }

    private void drawWays(List<Way> relevantWays) {
        for (var way : relevantWays) {
            way.draw(gc, trans);
        }
    }

    private void drawMarkers() {
        Marker firstMarker = this.markers.get(0);
        gc.beginPath();
        gc.setStroke(Util.currentPathColor);
        gc.setLineWidth(3/Math.sqrt(trans.determinant()));
        gc.moveTo(firstMarker.lon, firstMarker.lat);

        double distance = 0;
        if(this.markers.size() > 1) {
            for (int i = 1; i < markers.size(); i++) {
                Marker lastMarker = markers.get(i-1);
                Marker currentMarker = markers.get(i);
                gc.lineTo(currentMarker.lon, currentMarker.lat);
                distance += Util.haversine(lastMarker.lat, lastMarker.lon, currentMarker.lat, currentMarker.lon);
            }
            gc.stroke();

            String distanceOutput = distance > 1 ? String.format("%.2f km", distance) : Util.kmToMeter(distance);
            pane.setTextPathDistanceBox("Distance: " + distanceOutput);

        }

        for(Marker marker : markers) {
            marker.draw(locationIcon, this.maxlat, this.minlat, this.maxlon, this.minlon, this);
        }

        gc.setStroke(Util.currentBaseColor);
    }

    void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        redraw();
    }

    public void removeMarkers() {
        markers.clear();
        redraw();
    }

    public void toggleMarkerMode() {
        placeMarkerMode = !placeMarkerMode;
    }

    public void stepBack() {
        if(!markers.isEmpty()) {
            markers.remove(markers.size() - 1);
            redraw();
        }
    }

    void zoom(double dx, double dy, double factor) {
        pan(-dx, -dy);
        trans.prependScale(factor, factor);
        circleSize /= Math.sqrt(factor); // Update the circle size
        pan(dx, dy);
        redraw();
    }

    public void resetZoom() {
        trans = new Affine(originalTransform);
        pan(-0.56 * model.minlon, model.maxlat);
        zoom(0, 0, canvas.getHeight() / (model.maxlat - model.minlat));
    }

    public void changeZoomToPath(double newMinlon, double newMaxlon, double newMinlat, double newMaxlat) {
        trans = new Affine(originalTransform);

        pan(-0.56 * newMinlon, newMaxlat);

        double offset = 1.2;
        double zoomFactor = Math.min(canvas.getWidth() / (newMaxlon * offset - newMinlon * offset), canvas.getHeight() / (newMaxlat * offset - newMinlat * offset));
        zoom(0, 0, zoomFactor);

        Point2D topLeft = mousetoModel(0, 0);
        Point2D bottomRight = mousetoModel(canvas.getWidth(), canvas.getHeight());

        double fullWidth = bottomRight.getX() - topLeft.getX();
        double lineWidth = (newMaxlon * 0.56) - (newMinlon * 0.56);

        double lineWidthPercentage = lineWidth / fullWidth;
        double lineWidthPx = canvas.getWidth() * lineWidthPercentage;

        double fullHeight = bottomRight.getY() - topLeft.getY();
        double lineHeight = newMaxlat - newMinlat;

        double lineHeightPercentage = lineHeight / fullHeight;
        double lineHeightPx = canvas.getHeight() * lineHeightPercentage;

        pan((canvas.getWidth() - lineWidthPx) / 2, (canvas.getHeight() - lineHeightPx) / 2);
    }

    public void changeZoomToPoint(double lon, double lat) {
        trans = new Affine(originalTransform);

        pan(-0.56 * lon, lat);
        double zoomFactor = 200000;
        zoom(0, 0, zoomFactor);
        pan(canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    public Point2D mousetoModel(double lastX, double lastY) {
        try {
            return trans.inverseTransform(lastX, lastY);
        } catch (NonInvertibleTransformException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    void updateViewingArea() {
        Point2D topLeft = mousetoModel(0, 0);
        Point2D bottomRight = mousetoModel(canvas.getWidth(), canvas.getHeight());

//        double offset = 0.002;
        double offset = 0;

        minlon = topLeft.getX() + offset;
        maxlon = bottomRight.getX() - offset;
        minlat = topLeft.getY() - offset;
        maxlat = bottomRight.getY() + offset;
    }

    public void drawDirectionPath(List<Double> coords){
        gc.setStroke(Util.currentPathColor);

        gc.setLineWidth(3/Math.sqrt(trans.determinant()));

        gc.beginPath();
        gc.moveTo(coords.get(0), coords.get(1));
        for (int i = 2; i < coords.size(); i += 2) {
            gc.lineTo(coords.get(i), coords.get(i+1));
        }
        gc.stroke();

        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
    }
}