package handin2.Pathfinding;

import handin2.*;
import handin2.Way.WayRoad;

import java.io.Serializable;
import java.util.*;

/**
 * Pathfinding algorithm to find the shortest path on Graph.
 */
public class AStarSP implements Serializable {
    Graph graph;
    RTree rTree;

    public double timeSpent = 0;
    public int intersectionsPassed = 0;
    public int searchedEdges = 0;
    public int updatedBest = 0;

    static class AStarNode implements Comparable<AStarNode>{
        Node node;
        AStarNode cameFrom;
        Graph.Edge cameThrough;
        double gScore; //cheapest path from start to n currently known
        double fScore; //For node n, fScore[n] = gScore[n] + h(n)

        /**
         * Initial start of Pathfinding.
         * @param node Reference to node
         * @param cameFrom Previous AStarNode
         * @param cameThrough Graph.edge used to get to this
         * @param gScore Cheapest path from start to this currently known
         * @param fScore Assumed cost of path going through this node. gScore + h
         */
        AStarNode(Node node, AStarNode cameFrom, Graph.Edge cameThrough, double gScore, double fScore){
            this.node = node;
            this.cameFrom = cameFrom;
            this.cameThrough = cameThrough;
            this.gScore = gScore;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(AStarNode node) {
            return Double.compare(this.fScore, node.fScore);
        }
    }

    public AStarSP(Graph graph, RTree rTree){
        this.graph = graph;
        this.rTree = rTree;
    }

    /**
     * Finds shortest path.
     * If dijkstra = true, this algorithm turns into dijkstra, as h = 0.
     * Returns a path on the same road, if start and end is on the same road.
     * @param startAddress Coordinates of starting address
     * @param finishAddress Coordinates of finishing address
     * @param transportType Vehicle, bike and pedestrian are valid
     * @param dijkstra If true, finds path based on dijkstra
     * @return list of doubles to later use to draw a path.
     */
    public List<Double> findPath(Node startAddress, Node finishAddress, String transportType, boolean dijkstra){
        searchedEdges = 0;
        updatedBest = 0;
        long startTime = System.currentTimeMillis();

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        Map<Node, AStarNode> node2AStarNode = new HashMap<>();

        Object[] startNearest = rTree.nearestNeighbour(startAddress, transportType);
        Object[] finishNearest = rTree.nearestNeighbour(finishAddress, transportType);
        Node start = (Node) startNearest[0];
        Node finish = (Node) finishNearest[0];
        WayRoad startWay = (WayRoad) startNearest[1];
        WayRoad finishWay = (WayRoad) finishNearest[1];

        if(startWay == finishWay){
            return pathOnSameRoad(start, finish, startWay.coords);
        }

        graph.setStart(start);
        graph.setFinish(finish);

        //nearest intersections to start
        List<Node> startList = startWay.closestIntersections(start, graph);
        List<Node> finishList = finishWay.closestIntersections(finish, graph);

        for(Node n: startList){
            graph.addEdge(start, n, Util.haversine(start.lat, start.lon, n.lat, n.lon), startWay);
        }

        for(Node n: finishList){
            graph.addEdge(finish, n, Util.haversine(finish.lat, finish.lon, n.lat, n.lon), finishWay);
        }

        AStarNode startNode = new AStarNode(start, null, null, 0, calculateH(start, finish, startWay.maxSpeed ,transportType, dijkstra));

        openSet.add(startNode);

        while(!openSet.isEmpty()){
            AStarNode current = openSet.poll();
            if(current.node == finish){
                long endTime = System.currentTimeMillis();
                timeSpent = (double) (endTime - startTime) /1000;
                intersectionsPassed = node2AStarNode.size();
                updatedBest = updatedBest - node2AStarNode.size();

                return reconstructedPath(current, startNode);
            }

            for(Graph.Edge e: graph.getEdges(current.node)){
                if (passable(e, transportType)) {
                    searchedEdges++;
                    Node neighbourNode = e.getDestination();
                    AStarNode neighbour = node2AStarNode.getOrDefault(neighbourNode, new AStarNode(neighbourNode, null, e, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
                    node2AStarNode.put(neighbourNode, neighbour);

                    double tentative_gScore;
                    if(transportType.equals("vehicle")){
                        tentative_gScore = current.gScore + (e.distance / e.way.maxSpeed);
                    } else {
                        tentative_gScore = current.gScore + e.distance;
                    }

                    if (tentative_gScore < neighbour.gScore) {
                        updatedBest++;
                        neighbour.cameFrom = current;
                        neighbour.cameThrough = e;
                        neighbour.gScore = tentative_gScore;
                        neighbour.fScore = tentative_gScore + calculateH(neighbour.node, finish, e.way.maxSpeed, transportType, dijkstra);

                        if (!openSet.contains(neighbour)) {
                            openSet.add(neighbour);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Looks at boolean on way, to determine if the way is passable on the given transportType.
     * @param e current edge
     * @param transportType current transportation type
     * @return true or false
     */
    public boolean passable(Graph.Edge e, String transportType){
        if (transportType.equals("vehicle") && e.way.vehicle) { return true; }
        else if (transportType.equals("bike") && e.way.bike) { return true; }
        else if (transportType.equals("pedestrian") && e.way.pedestrian) { return true; }
        return false;
    }

    /**
     * Calculates the heuristic value, used for AStar priority queue
     * @param node current node
     * @param finish finish node
     * @param maxSpeed maximum speed of way the edge belongs on
     * @param transportType Vehicle, bike and pedestrian are valid
     * @param dijkstra if true, h = 0
     * @return double
     */
    public double calculateH(Node node, Node finish, int maxSpeed, String transportType, boolean dijkstra){ //Prøv at indsæt hastighed for vej man er på.
        if(dijkstra){ return 0; }

        double speed = maxSpeed * 1.5;
        if(speed > 130) { speed = 130; }
        if (transportType.equals("vehicle")) { return Math.sqrt(Math.pow(node.lon - finish.lon, 2) + Math.pow(node.lat - finish.lat, 2)) / speed; }
        else { return Math.sqrt(Math.pow(node.lon - finish.lon, 2) + Math.pow(node.lat - finish.lat, 2)); }
    }

    /**
     * Reconstructs the path from end to beginning.
     * @param node Last node until goal is found
     * @return List of AStarNode as path from end to beginning
     */
    public List<Node> reconstructedPathNodes(AStarNode node){ //Hvad ønsker vi at denne skal returne?
        List<Node> list = new ArrayList<>();
        list.add(node.node);
        while(node.cameFrom != null){
            node = node.cameFrom;
            list.add(node.node);
        }
        return list;
    }

    /**
     * Reconstructs the path from end to beginning.
     * @param node Last node until goal is found
     * @param start starting node (which is the last node added)
     * @return Path from end to beginning as List of double tuples
     */
    public List<Double> reconstructedPath(AStarNode node, AStarNode start){
        List<Double> coords = addStart(node);

        node = node.cameFrom;

        while(node.cameFrom.cameFrom != null){
            Node n1 = node.node;
            Node n2 = node.cameFrom.node;

            List<Double> tempCoords = new ArrayList<>();
            List<Double> tempCoordsRev = new ArrayList<>();

            boolean foundStart = false;
            boolean foundStartRev = false;
            boolean foundEnd = false;
            boolean foundEndRev = false;

            double[] wayCoords = node.cameThrough.way.coords;
            for (int i = 0; i < wayCoords.length; i += 2) {
                double lon = wayCoords[i];
                double lat = wayCoords[i+1];

                double lonRev = wayCoords[(wayCoords.length-2) - i];
                double latRev = wayCoords[(wayCoords.length-1) - i];

                if(!foundEnd){
                    if(lon == n1.lon && lat == n1.lat){
                        foundStart = true;
                    }
                    if(foundStart){
                        if(lon == n2.lon && lat == n2.lat){
                            foundEnd = true;
                        } else {
                            tempCoords.add(lon);
                            tempCoords.add(lat);
                        }
                    }
                }

                if(!foundEndRev){
                    if(lonRev == n1.lon && latRev == n1.lat){
                        foundStartRev = true;
                    }
                    if(foundStartRev){
                        if(lonRev == n2.lon && latRev == n2.lat){
                            foundEndRev = true;
                        } else {
                            tempCoordsRev.add(lonRev);
                            tempCoordsRev.add(latRev);
                        }
                    }
                }
            }

            if(foundStart && foundEnd && foundStartRev && foundEndRev){
                if(Util.segmentLength(tempCoords) < Util.segmentLength(tempCoordsRev)){
                    for (int i = 0; i < tempCoords.size(); i += 2) {
                        coords.add(tempCoords.get(i));
                        coords.add(tempCoords.get(i+1));
                    }
                } else {
                    for (int i = 0; i < tempCoordsRev.size(); i += 2) {
                        coords.add(tempCoordsRev.get(i));
                        coords.add(tempCoordsRev.get(i+1));
                    }
                }
            } else if(foundStart && foundEnd){
                for (int i = 0; i < tempCoords.size(); i += 2) {
                    coords.add(tempCoords.get(i));
                    coords.add(tempCoords.get(i+1));
                }
            } else if(foundStartRev && foundEndRev){
                for (int i = 0; i < tempCoordsRev.size(); i += 2) {
                    coords.add(tempCoordsRev.get(i));
                    coords.add(tempCoordsRev.get(i+1));
                }
            }
            node = node.cameFrom;
        }
        coords.addAll(addFinish(node));
        coords.add(start.node.lon);
        coords.add(start.node.lat);
        return coords;
    }

    /**
     * Helper function adds the segments from current to end
     * @param node First node
     * @return Path from end to beginning as List of double tuples
     */
    private List<Double> addStart(AStarNode node){
        List<Double> coords = new ArrayList<>();

        Node n1 = node.node;
        Node n2 = node.cameFrom.node;

        List<Double> tempCoords = new ArrayList<>();
        List<Double> tempCoordsRev = new ArrayList<>();

        boolean record = false;
        boolean recordRev = false;

        double[] wayCoords = node.cameThrough.way.coords;

        for (int i = 0; i < wayCoords.length-2; i += 2) {
            double lon1 = wayCoords[i];
            double lat1 = wayCoords[i+1];
            double lon2 = wayCoords[i+2];
            double lat2 = wayCoords[i+3];

            double lon1Rev = wayCoords[(wayCoords.length-2) - i];
            double lat1Rev = wayCoords[(wayCoords.length-1) - i];
            double lon2Rev = wayCoords[(wayCoords.length-4) - i];
            double lat2Rev = wayCoords[(wayCoords.length-3) - i];

            if(Util.onSegment(n1.lon, n1.lat, lon1, lat1, lon2, lat2)){
                record = true;
                tempCoords.add(n1.lon);
                tempCoords.add(n1.lat);
            } else if (record) {
                tempCoords.add(lon1);
                tempCoords.add(lat1);
            }
            if (n2.lon == lon2 && n2.lat == lat2){
                record = false;
            }

            if(Util.onSegment(n1.lon, n1.lat, lon1Rev, lat1Rev, lon2Rev, lat2Rev)){
                recordRev = true;
                tempCoordsRev.add(n1.lon);
                tempCoordsRev.add(n1.lat);
            } else if (recordRev) {
                tempCoordsRev.add(lon1Rev);
                tempCoordsRev.add(lat1Rev);
            }
            if (n2.lon == lon2Rev && n2.lat == lat2Rev){
                recordRev = false;
            }
        }
        if(!record){
            for (int i = 0; i < tempCoords.size(); i += 2) {
                coords.add(tempCoords.get(i));
                coords.add(tempCoords.get(i+1));
            }
        } else {
            for (int i = 0; i < tempCoordsRev.size(); i += 2) {
                coords.add(tempCoordsRev.get(i));
                coords.add(tempCoordsRev.get(i+1));
            }
        }
        return coords;
    }

    /**
     * Helper function adds the segments from current to start
     * @param node Last node
     * @return Path from end to beginning as List of double tuples
     */
    private List<Double> addFinish(AStarNode node){
        List<Double> coords = new ArrayList<>();

        Node n1 = node.node;
        Node n2 = node.cameFrom.node;

        List<Double> tempCoords = new ArrayList<>();
        List<Double> tempCoordsRev = new ArrayList<>();

        boolean foundStart = false;
        boolean foundStartRev = false;
        boolean foundEnd = false;
        boolean foundEndRev = false;

        double[] wayCoords = node.cameThrough.way.coords;

        for (int i = 0; i < wayCoords.length-2; i += 2) {
            double lon1 = wayCoords[i];
            double lat1 = wayCoords[i+1];
            double lon2 = wayCoords[i+2];
            double lat2 = wayCoords[i+3];

            double lon1Rev = wayCoords[(wayCoords.length-2) - i];
            double lat1Rev = wayCoords[(wayCoords.length-1) - i];
            double lon2Rev = wayCoords[(wayCoords.length-4) - i];
            double lat2Rev = wayCoords[(wayCoords.length-3) - i];

            if(!foundEnd){
                if(n1.lon == lon1 && n1.lat == lat1){
                    foundStart = true;
                }

                if (foundStart) {
                    tempCoords.add(lon1);
                    tempCoords.add(lat1);
                    if (Util.onSegment(n2.lon, n2.lat, lon1, lat1, lon2, lat2)){
                        foundEnd = true;
                    }
                }
            }

            if(!foundEndRev){
                if(n1.lon == lon1Rev && n1.lat == lat1Rev){
                    foundStartRev = true;
                }

                if (foundStartRev) {
                    tempCoordsRev.add(lon1Rev);
                    tempCoordsRev.add(lat1Rev);
                    if (Util.onSegment(n2.lon, n2.lat, lon1Rev, lat1Rev, lon2Rev, lat2Rev)){
                        foundEndRev = true;
                    }
                }
            }
        }
        if(foundStart && foundEnd){
            for (int i = 0; i < tempCoords.size(); i += 2) {
                coords.add(tempCoords.get(i));
                coords.add(tempCoords.get(i+1));
            }
        } else if (foundStartRev && foundEndRev) {
            for (int i = 0; i < tempCoordsRev.size(); i += 2) {
                coords.add(tempCoordsRev.get(i));
                coords.add(tempCoordsRev.get(i+1));
            }
        }
        return coords;
    }

    /**
     * Return path on same road, if both addresses fall on the same road.
     * @param n1 start
     * @param n2 finish
     * @param wayCoords road
     * @return Path from end to beginning as List of double tuples
     */
    private List<Double> pathOnSameRoad(Node n1, Node n2, double[] wayCoords){
        List<Double> coords = new ArrayList<>();

        List<Double> tempCoords = new ArrayList<>();
        List<Double> tempCoordsRev = new ArrayList<>();

        boolean foundStart = false;
        boolean foundStartRev = false;
        boolean foundEnd = false;
        boolean foundEndRev = false;

        for (int i = 0; i < wayCoords.length-2; i += 2) {
            double lon1 = wayCoords[i];
            double lat1 = wayCoords[i+1];
            double lon2 = wayCoords[i+2];
            double lat2 = wayCoords[i+3];

            double lon1Rev = wayCoords[(wayCoords.length-2) - i];
            double lat1Rev = wayCoords[(wayCoords.length-1) - i];
            double lon2Rev = wayCoords[(wayCoords.length-4) - i];
            double lat2Rev = wayCoords[(wayCoords.length-3) - i];

            if(!foundEnd){
                if (foundStart) {
                    tempCoords.add(lon1);
                    tempCoords.add(lat1);
                }
                if(Util.onSegment(n1.lon, n1.lat, lon1, lat1, lon2, lat2)){
                    foundStart = true;
                    tempCoords.add(n1.lon);
                    tempCoords.add(n1.lat);
                }
                if (foundStart && Util.onSegment(n2.lon, n2.lat, lon1, lat1, lon2, lat2)){
                    tempCoords.add(n2.lon);
                    tempCoords.add(n2.lat);
                    foundEnd = true;
                }
            }

            if(!foundEndRev){
                if (foundStartRev) {
                    tempCoordsRev.add(lon1Rev);
                    tempCoordsRev.add(lat1Rev);
                }
                if(Util.onSegment(n1.lon, n1.lat, lon1Rev, lat1Rev, lon2Rev, lat2Rev)){
                    foundStartRev = true;
                    tempCoordsRev.add(n1.lon);
                    tempCoordsRev.add(n1.lat);
                }
                if (foundStartRev && Util.onSegment(n2.lon, n2.lat, lon1Rev, lat1Rev, lon2Rev, lat2Rev)){
                    tempCoordsRev.add(n2.lon);
                    tempCoordsRev.add(n2.lat);
                    foundEndRev = true;
                }
            }
        }
        if(foundStart && foundEnd){
            for (int i = 0; i < tempCoords.size(); i += 2) {
                coords.add(tempCoords.get(i));
                coords.add(tempCoords.get(i+1));
            }
        } else if (foundStartRev && foundEndRev) {
            for (int i = 0; i < tempCoordsRev.size(); i += 2) {
                coords.add(tempCoordsRev.get(i));
                coords.add(tempCoordsRev.get(i+1));
            }
        }
        return coords;
    }
}