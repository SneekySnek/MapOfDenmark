package handin2;

import handin2.Way.Way;
import javafx.scene.paint.Color;

import java.util.*;

public class Util {

    private static final Color landColor = Color.rgb(247, 246, 246);
    private static final Color waterColor = Color.rgb(144, 218, 238);
    private static final Color natureColor = Color.rgb(195, 241, 213);
    private static final Color forestColor = Color.rgb(173, 235, 173);
    private static final Color baseColor = Color.rgb(189, 205, 217);
    private static final Color buildingColor = Color.rgb(232, 233, 237);
    private static final Color buildingStrokeColor = Color.rgb(215, 218, 230);
    private static final Color pathColor = Color.rgb(15, 83, 255);
    private static final Color motorwayColor = Color.rgb(152, 187, 193);
    private static final Color trunkColor = Color.rgb(152, 187, 193);
    private static final Color primaryColor = Color.rgb(139, 165, 193);
    private static final Color secondaryColor = Color.rgb(176, 198, 201);
    private static final Color tertiaryColor = Color.rgb(189, 217, 212);


    private static final Color blindlandColor = Color.rgb(50, 50, 50); // dark gray
    private static final Color blindnatureColor = Color.rgb(150, 150, 150); // medium gray
    private static final Color blindforestColor = Color.rgb(50, 50, 25); // dark green
    private static final Color blindbaseColor = Color.rgb(200, 200, 200); // light gray
    private static final Color blindbuildingColor = Color.rgb(225, 225, 225); // very light gray
    private static final Color blindPathColor = Color.rgb(163, 163, 248); // purple
    private static final Color blindbuildingStrokeColor = Color.rgb(255, 255, 255); // white
    private static final Color blindMotorwayColor = Color.rgb(0, 88, 100); // light blue
    private static final Color blindTrunkColor = Color.rgb(0, 88, 100); // light blue
    private static final Color blindPrimaryColor = Color.rgb(9, 10, 75); // dark blue
    private static final Color blindSecondaryColor = Color.rgb(1, 190, 194);
    private static final Color blindTertiaryColor = Color.rgb(0, 175, 160);

    public static Color currentLandColor = landColor;
    public static Color currentWaterColor = waterColor;
    public static Color currentNatureColor = natureColor;
    public static Color currentForestColor = forestColor;
    public static Color currentBaseColor = baseColor;
    public static Color currentBuildingColor = buildingColor;
    public static Color currentBuildingStrokeColor = buildingStrokeColor;
    public static Color currentPathColor = pathColor;
    public static Color currentMotorwayColor = motorwayColor;
    public static Color currentTrunkColor = trunkColor;
    public static Color currentPrimaryColor = primaryColor;
    public static Color currentSecondaryColor = secondaryColor;
    public static Color currentTertiaryColor = tertiaryColor;

    public static void toggleColorblindMode(boolean isColorBlindMode, View view) {
        if(!isColorBlindMode) {
            currentLandColor = landColor;
            currentWaterColor = waterColor;
            currentNatureColor = natureColor;
            currentForestColor = forestColor;
            currentBaseColor = baseColor;
            currentBuildingColor = buildingColor;
            currentBuildingStrokeColor = buildingStrokeColor;
            currentPathColor = pathColor;
            currentTrunkColor = trunkColor;
            currentPrimaryColor = primaryColor;
            currentMotorwayColor = motorwayColor;
            currentSecondaryColor = secondaryColor;
            currentTertiaryColor = tertiaryColor;
        } else {
            currentLandColor = blindlandColor;
            currentNatureColor = blindnatureColor;
            currentBaseColor = blindbaseColor;
            currentForestColor = blindforestColor;
            currentBuildingColor = blindbuildingColor;
            currentBuildingStrokeColor = blindbuildingStrokeColor;
            currentPathColor = blindPathColor;
            currentTrunkColor = blindTrunkColor;
            currentPrimaryColor = blindPrimaryColor;
            currentMotorwayColor = blindMotorwayColor;
            currentSecondaryColor = blindSecondaryColor;
            currentTertiaryColor = blindTertiaryColor;
        }

        view.redraw();
    }

    private static final int earthRadius = 6371;

    /**
     * Get distance between latitude and longitude coordinate pair
     * in km which takes into account the curvature of the earth.
     * Code has been adapted from:
     * @Reference https://www.geeksforgeeks.org/haversine-formula-to-find-distance-between-two-points-on-a-sphere/
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return the distance
     */
    public static double haversine(double lat1, double lon1,
                                   double lat2, double lon2) {

        double[] firstCoordinatePair = revertLonAndLat(lon1, lat1);
        double[] secondCoordinatePair = revertLonAndLat(lon2, lat2);

        lon1 = firstCoordinatePair[0];
        lat1 = firstCoordinatePair[1];
        lon2 = secondCoordinatePair[0];
        lat2 = secondCoordinatePair[1];

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return earthRadius * c;
    }

    public static double[] revertLonAndLat(double lon, double lat) {
        return new double[]{lon/0.56, Math.abs(lat)};
    }

    /**
     * Loop through ways list and flip way coordinates so that the ending longitude and
     * latitude of each way matches starting latitude and longitude og the proceeding way.
     * @param ways
     */
    public static void preProcessWays(List<Way> ways) {
        for(int i = 0; i < ways.size(); i++) {
            if(i == ways.size() - 1) break;

            double firstLonWay1 = ways.get(i).coords[0];
            double firstLatWay1 = ways.get(i).coords[1];
            double lastLonWay1 = ways.get(i).coords[ways.get(i).coords.length - 2];
            double lastLatWay1 = ways.get(i).coords[ways.get(i).coords.length - 1];

            double firstLonWay2 = ways.get(i+1).coords[0];
            double firstLatWay2 = ways.get(i+1).coords[1];
            double lastLonWay2 = ways.get(i+1).coords[ways.get(i+1).coords.length - 2];
            double lastLatWay2 = ways.get(i+1).coords[ways.get(i+1).coords.length - 1];

            if(firstLonWay1 == firstLonWay2 && firstLatWay1 == firstLatWay2) {
                reverseCoords(ways.get(i));
            } else if(firstLonWay1 == lastLonWay2 && firstLatWay1 == lastLatWay2) {
                reverseCoords(ways.get(i));
                reverseCoords(ways.get(i+1));
            } else if (lastLonWay1 == lastLonWay2 && lastLatWay1 == lastLatWay2) {
                reverseCoords(ways.get(i+1));
            }
        }
    }

    public static void reverseCoords(Way way) {
        for(int i = 0; i < way.coords.length / 2; i += 2) {
            double tempLon = way.coords[i];
            double tempLat = way.coords[i+1];

            way.coords[i] = way.coords[way.coords.length - i - 2];
            way.coords[i+1] = way.coords[way.coords.length - i - 1];

            way.coords[way.coords.length - i - 2] = tempLon;
            way.coords[way.coords.length - i - 1] = tempLat;
        }
    }

    /**
     * Create a chain where each way has a proceeding way with matching coordinates
     * given that there is another way with matching coordinates.
     * @param ways
     */
    public static void createWayChain(List<Way> ways) {
        for(int i = 1; i < ways.size(); i++) {
            Way matchWay = ways.get(i - 1);
            for(int k = i; k < ways.size(); k++) {
                Way currentWay = ways.get(k);
                if(!hasMatchingCoords(matchWay, currentWay)) continue;

                Way temp = ways.get(i);
                ways.set(i, currentWay);
                ways.set(k, temp);
            }
        }
    }

    public static boolean hasMatchingCoords(Way way1, Way way2) {
        double way1StartLon = way1.coords[0];
        double way1StartLat = way1.coords[1];

        double way2StartLon = way2.coords[0];
        double way2StartLat = way2.coords[1];

        double way1EndLon = way1.coords[way1.coords.length - 2];
        double way1EndLat = way1.coords[way1.coords.length - 1];

        double way2EndLon = way2.coords[way2.coords.length - 2];
        double way2EndLat = way2.coords[way2.coords.length - 1];

        if(way1StartLon == way2StartLon && way1StartLat == way2StartLat) return true;
        else if(way1EndLon == way2StartLon && way1EndLat == way2StartLat) return true;
        else if(way1StartLon == way2EndLon && way1StartLat == way2EndLat) return true;
        else if(way1EndLon == way2EndLon && way1EndLat == way2EndLat) return true;

        return false;
    }

    public static String kmToMeter(double km) {
        int meters = (int) (km * 1000);
        return meters + "m";
    }

    public static double distance(double lon1, double lat1, double lon2, double lat2){
        return Math.sqrt(Math.pow(lon1-lon2,2) + Math.pow(lat1-lat2,2));
    }

    public static boolean onSegment(double lon, double lat, double lon1, double lat1, double lon2, double lat2){
        return distance(lon, lat, lon1, lat1) + distance(lon, lat, lon2, lat2) - distance(lon1, lat1, lon2, lat2) < 0.0000000001;
    }

    public static double segmentLength(List<Double> wayCoords){
        double sum = 0;
        for (int i = 0; i < wayCoords.size()-2; i += 2) {
            double lon1 = wayCoords.get(i);
            double lat1 = wayCoords.get(i + 1);
            double lon2 = wayCoords.get(i + 2);
            double lat2 = wayCoords.get(i + 3);
            sum += haversine(lat1, lon1, lat2, lon2);
        }
        return sum;
    }
}
