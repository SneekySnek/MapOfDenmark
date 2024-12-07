package handin2;

import handin2.Way.Way;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


class RTreeTest extends RTree<Way>{
    private RTree rTree;
    private RTree rTree2;
    @BeforeEach
    void setUp() {
        rTree = new RTree<Way>();

        // Creates 25 ways with 5 nodes and adds it to the RTree
        for (long i = 0; i < 25; i++) {
            ArrayList<Node> nodes = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                double lat = i * 10.0 + j;
                double lon = i * 20.0 + j;
                nodes.add(new Node(lat, lon));
            }
            Way way = new Way(nodes);
            rTree.insert(way);
        }
        /*int leafNodes = rTree.getNumberOfLeafNodes(rTree.root);
        System.out.println("Number of leaf nodes: " + leafNodes);

        int childNodes = rTree.getNumberOfInnerNodes(rTree.root);
        System.out.println("Number of child nodes: " + childNodes);

         */
    }


    @Test
    @DisplayName("Tests the RTree has inserted the ways properly by checking the number of leaf and inner nodes.")
    void insert() {
        int leafNodes = rTree.getNumberOfLeafNodes(rTree.getRoot());
        assertTrue(leafNodes >= 7 && leafNodes <= 25, "The RTree should have between 7 and 25 leaf nodes after inserting 25 ways, since it's a balanced tree");

        int innerNodes = rTree.getNumberOfInnerNodes(rTree.getRoot());
        assertTrue(innerNodes >= 3 && innerNodes <= 12, "The number of inner nodes should be between the ceil(12/4) = 3 and 12.");
    }

    @Test
    @DisplayName("Tests both search methods in the RTree")
    void searchBothMethods() {
        List<Way> results1 = rTree.search(0.0, 50.0, 0.0, 50.0);

        BoundingBox bbox = new BoundingBox(0.0, 50.0, 0.0, 50.0);
        List<Way> results2 = rTree.search(rTree.getRoot(), bbox);

        assertEquals(results1, results2, "Both search methods should return the same results");

        List<Way> allWays = new ArrayList<>();
        rTree.getWays(rTree.getRoot(), allWays);

        for (int i = 0; i < 3; i++) {
            Way way = allWays.get(i);
            assertTrue(results1.contains(way), "Search results should include the way: " + way);
        }

        for (int i = 3; i < 25; i++) {
            Way way = allWays.get(i);
            assertFalse(results1.contains(way), "Search results should not include the way: " + way);
        }
    }

    @Test
    @DisplayName("Tests the nearestNeighbour method in the RTree")
    void nearestNeighbour() {
        //It uses a new RTree because the other coordinates interfere with these once
        rTree2 = new RTree<Way>();

        //1st way nodes
        ArrayList<Node> way1Nodes = new ArrayList<>();
        way1Nodes.add(new Node(1.0, 1.0));
        way1Nodes.add(new Node(1.0, 2.0));
        way1Nodes.add(new Node(1.0, 3.0));
        way1Nodes.add(new Node(1.0, 4.0));

        //2st way nodes
        ArrayList<Node> way2Nodes = new ArrayList<>();
        way2Nodes.add(new Node(6.0, 1.0));
        way2Nodes.add(new Node(6.0, 2.0));
        way2Nodes.add(new Node(6.0, 3.0));
        way2Nodes.add(new Node(6.0, 4.0));

        //3rd way nodes
        ArrayList<Node> way3Nodes = new ArrayList<>();
        way3Nodes.add(new Node(0.0, 2.5));
        way3Nodes.add(new Node(8.0, 2.5));

        // create ways
        Way way1 = new Way(way1Nodes);
        Way way2 = new Way(way2Nodes);
        Way way3 = new Way(way3Nodes);

        Node node11 = new Node(3, 1);

        // Add ways to RTree
        rTree2.insert(way1);
        rTree2.insert(way2);
        rTree2.insert(way3);


        //For the new node which it creates on the nearest way
        Node expectedNode = new Node(3, 2.5);
        Object[] nearestNeighbour = rTree2.nearestNeighbour(node11.lon, node11.lat, "vehicle");
        Node actualNode = (Node) nearestNeighbour[0];

        assertEquals(expectedNode.lon, actualNode.lon, "Longitude should match of the newly created node on the nearest way");
        assertEquals(expectedNode.lat, actualNode.lat, "Latitude should match of the newly created node on the nearest way");
        assertNotNull(nearestNeighbour, "Result should not be null");
        assertEquals(2, nearestNeighbour.length, "Result array should have 2 elements");
        assertEquals(way3, nearestNeighbour[1], "The nearest way should be way3");

    }
}

