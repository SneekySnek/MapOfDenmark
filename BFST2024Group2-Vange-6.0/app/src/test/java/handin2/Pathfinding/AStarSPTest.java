package handin2.Pathfinding;
import handin2.Node;
import handin2.RTree;
import handin2.Way.WayRoad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AStarSPTest{
    private AStarSP aStarSP;
    private Graph graph;
    RTree rTree;
    ArrayList<Node>nodes;
    int orangeSpeed = 50;

    @BeforeEach
    void setUp() {
        double[][] coordinates = {
                //Green
                {2, 2},
                {2, 4},
                {2,6},

                //Purple
                {6,2},
                {6,4},
                {6,8},
                {6,10}

        };
        nodes = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i++) {
            nodes.add(new Node(coordinates[i][0], coordinates[i][1]));
        }

        ArrayList<Node> Green = new ArrayList<>();
        Green.add(nodes.get(0));
        Green.add(nodes.get(1));
        Green.add(nodes.get(2));

        ArrayList<Node> Orange = new ArrayList<>();
        Orange.add(nodes.get(1));
        Orange.add(nodes.get(4));

        ArrayList<Node> Blue = new ArrayList<>();
        Blue.add(nodes.get(1));
        Blue.add(nodes.get(5));

        ArrayList<Node> Purple = new ArrayList<>();
        Purple.add(nodes.get(3));
        Purple.add(nodes.get(4));
        Purple.add(nodes.get(5));
        Purple.add(nodes.get(6));


        // Create three WayRoads and add the ways to them
        WayRoad GreenWayRoad = new WayRoad(Green, "motorway", 50, true, false, false);
        WayRoad OrangeWayRoad = new WayRoad(Orange, "motorway", orangeSpeed, true, false, false);
        WayRoad BlueWayRoad = new WayRoad(Blue, "motorway", 50, true, false, false);
        WayRoad PurpleWayRoad = new WayRoad(Purple, "motorway", 50, true, false, false);


        List<Node>intersections = new ArrayList<>();
        intersections.add(nodes.get(1));
        intersections.add(nodes.get(4));
        intersections.add(nodes.get(5));

        graph = new Graph(intersections);
        // Add edges for Green
        graph.addEdge(nodes.get(1), nodes.get(4), 4, OrangeWayRoad);
        graph.addEdge(nodes.get(1), nodes.get(5), 4*Math.sqrt(2), BlueWayRoad);
        graph.addEdge(nodes.get(4), nodes.get(5), 4, PurpleWayRoad);


        rTree = new RTree();
        rTree.insert(GreenWayRoad);
        rTree.insert(OrangeWayRoad);
        rTree.insert(BlueWayRoad);
        rTree.insert(PurpleWayRoad);

        aStarSP = new AStarSP(graph, rTree);
    }

    //*path can be found at: https://www.desmos.com/calculator/ap4frhphau

    @Test
    @DisplayName("Tests findpath with no highways")
    void testFindPath() {
        orangeSpeed = 50;
        setUp();
        Node startNode = new Node(1, 3);
        Node finishNode = new Node(7, 9);
        List<Double> path = aStarSP.findPath(startNode, finishNode, "vehicle",false);
        System.out.println(path); //For manual testing
        assertEquals(8 , path.size());
        assertEquals(9.0, path.get(0)); assertEquals(6.0, path.get(1),"First node should be 9,6");
        assertEquals(8.0, path.get(2)); assertEquals(6.0, path.get(3),"Second node should be 8,6");
        assertEquals(3.0, path.get(6)); assertEquals(2.0, path.get(7),"Fourth node should be 4,2");
    }
    @Test
    @DisplayName("Tests findpath with orange highway")
    void testFindAltPath() {
        orangeSpeed = 130;
        setUp();
        Node startNode = new Node(1, 3);
        Node finishNode = new Node(7, 9);
        List<Double> path = aStarSP.findPath(startNode, finishNode, "vehicle",false);
        System.out.println(path); //For manual testing
        assertEquals(10, path.size());
        assertEquals(9.0, path.get(0)); assertEquals(6.0, path.get(1),"First node should be 9,6");
        assertEquals(8.0, path.get(2)); assertEquals(6.0, path.get(3),"Second node should be 8,6");
        assertEquals(4.0, path.get(4)); assertEquals(6.0, path.get(5),"Third node should be 4,6");
        assertEquals(4.0, path.get(6)); assertEquals(2.0, path.get(7),"Fourth node should be 4,2");
        assertEquals(3.0, path.get(8)); assertEquals(2.0, path.get(9),"Fifth node should be 3,2");
    }
    @Test
    @DisplayName("Tests findpath on the same way")
    void testFindPathSameWay() {
        orangeSpeed = 50;
        setUp();
        Node startNode = new Node(1, 3);
        Node finishNode = new Node(1, 2);
        List<Double> path = aStarSP.findPath(startNode, finishNode, "vehicle",false);
        System.out.println(path); //For manual testing
        assertEquals(4, path.size());
        assertEquals(2.0, path.get(3)); assertEquals(3.0, path.get(2),"First node should be 2,3");
        assertEquals(2.0, path.get(1)); assertEquals(4.0, path.get(0),"Second node should be 2,2");

    }
}


