package handin2.Way;

import handin2.Node;
import handin2.Pathfinding.Graph;
import handin2.Util;
import handin2.Way.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

import java.util.*;

public class WayRoad extends Way {
    String tag;
    public int maxSpeed;
    public boolean vehicle;
    public boolean bike;
    public boolean pedestrian;

    public WayRoad(ArrayList<Node> way, String tag, int maxSpeed, boolean vehicle, boolean bike, boolean pedestrian) {
        super(way);

        this.tag = tag;
        this.maxSpeed = maxSpeed;
        this.vehicle = vehicle;
        this.bike = bike;
        this.pedestrian = pedestrian;
    }

    public void draw(GraphicsContext gc, Affine trans, int zoomLevel) {
        int motorWayStrokeWidth = 10;
        int trunkStrokeWidth = 9;
        int primaryStrokeWidth = 8;
        int secondaryStrokeWidth = 7;
        int tertiaryStrokeWidth = 6;

        if(zoomLevel == 3) {
            motorWayStrokeWidth = 1;
            trunkStrokeWidth = 1;
        } else if(zoomLevel == 2) {
            motorWayStrokeWidth = 7;
            trunkStrokeWidth = 6;
            primaryStrokeWidth = 5;
            secondaryStrokeWidth = 4;
            tertiaryStrokeWidth = 3;
        }

        switch (tag) {
            case "motorway", "motorway_link":
                gc.setStroke(Util.currentMotorwayColor);
                gc.setLineWidth(motorWayStrokeWidth / Math.sqrt(trans.determinant()));
                break;
            case "trunk", "trunk_link":
                gc.setStroke(Util.currentTrunkColor);
                gc.setLineWidth(trunkStrokeWidth / Math.sqrt(trans.determinant()));
                break;
            case "primary", "primary_link":
                gc.setLineWidth(primaryStrokeWidth / Math.sqrt(trans.determinant()));
                gc.setStroke(Util.currentPrimaryColor);
                break;
            case "secondary", "secondary_link":
                gc.setLineWidth(secondaryStrokeWidth / Math.sqrt(trans.determinant()));
                gc.setStroke(Util.currentSecondaryColor);
                break;
            case "tertiary", "tertiary_link":
                gc.setLineWidth(tertiaryStrokeWidth / Math.sqrt(trans.determinant()));
                gc.setStroke(Util.currentTertiaryColor);
                break;
            default:
                break;
        }

        super.draw(gc, trans);
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
        gc.setStroke(Util.currentBaseColor);
    }

    public List<Node> findIntersections(Graph graph, Node node) {
        List<Node> intersections = new ArrayList<>();

        Set<String> wayCoordinates = new HashSet<>();

        for(int i = 0; i < coords.length; i += 2) {
            wayCoordinates.add(coords[i] + "," + coords[i+1]);
        }

        for(Node intersection : graph.nodes) {
            if(intersection == null || intersection == node) continue;

            String intersectionCoordinates = intersection.lon + "," + intersection.lat;

            if(wayCoordinates.contains(intersectionCoordinates)) {
                intersections.add(intersection);
            }
        }

        return intersections;
    }

    public List<Node> closestIntersections(Node node, Graph graph){ //potentiel rounding error?
        double lon = node.lon;
        double lat = node.lat;

        boolean indexFound = false;
        Node intersection = null;
        List<Node> intersections = findIntersections(graph, node);
        List<Node> list = new ArrayList<Node>();

        for (int i = 0; i < coords.length - 2; i += 2) {
            double lon1 = coords[i];
            double lat1 = coords[i + 1];
            double lon2 = coords[i + 2];
            double lat2 = coords[i + 3];

            if(!indexFound){
                for(Node n: intersections){
                    if(lon1 == n.lon && lat1 == n.lat){
                        intersection = n;
                        break;
                    }
                }
            }

            if(!indexFound && Math.abs(Util.distance(lon1, lat1, lon2, lat2) - (Util.distance(lon1, lat1, lon, lat) + Util.distance(lon, lat, lon2, lat2))) < 0.0000000001){
                indexFound = true;
                if(intersection != null){
                    list.add(intersection);
                }
            }

            if(indexFound){
                for(Node n: intersections){
                    if(lon2 == n.lon && lat2 == n.lat){
                        list.add(n);
                        return list;
                    }
                }
            }
        }
        return list;
    }
}
