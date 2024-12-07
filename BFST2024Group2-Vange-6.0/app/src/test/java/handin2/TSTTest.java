package handin2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TSTTest {

    private TST tst;
    private Node node1,node2,node3,node4, node5;

    private Address address1,address2,address3,address4, address5;



    @BeforeEach
    void setUp() {
        tst = new TST();
        node1 = new Node(1.0, 1.0);
        address1 = new Address("Hellerup", "1", "2900", "Strandvejen");
        node1.setAddress(address1);

        node4 = new Node(5.0, 1.0);
        address4 = new Address("Hellerup", "5", "2900", "Strandvejen");
        node4.setAddress(address4);

        node5 = new Node(5.0, 2.0);
        address5 = new Address("Hellerup", "29", "2900", "Strandvejen Alle");
        node5.setAddress(address5);

        node2 = new Node(2.0, 2.0);
        address2 = new Address("København S", "7", "2300", "Rued Langgaards Vej");
        node2.setAddress(address2);

        node3 = new Node(3.0, 2.0);
        address3 = new Address("København S", "2", "2300", "Grønjordsvej");
        node3.setAddress(address3);

        tst.insert(address1.getAddressToSTR(), node1);
        tst.insert(address2.getAddressToSTR(), node2);
        tst.insert(address3.getAddressToSTR(), node3);
        tst.insert(address4.getAddressToSTR(), node4);
        tst.insert(address5.getAddressToSTR(), node5);
    }

    /**
     * Compares the nodes inserted into the TST with the nodes returned by the search method
     * address.getAddressToSTR() is used as the key for the search method as the TST search returns a list of nodes
     */
    @Test
    @DisplayName("Checks searching for nodes in TST ")
    void insert_search() {
        assertEquals(node1, tst.search(address1.getAddressToSTR()).get(0));
        assertEquals(node2, tst.search(address2.getAddressToSTR()).get(0));
        assertEquals(node3, tst.search(address3.getAddressToSTR()).get(0));
        assertEquals(node4, tst.search(address4.getAddressToSTR()).get(0));
        assertEquals(node5, tst.search(address5.getAddressToSTR()).get(0));
    }

    /**
     * Checks if the coordinates of the nodes inserted into the TST are the same as the coordinates
     * of the nodes returned by the search method
     */
    @Test
    @DisplayName("Checking coordinates of the nodes")
    void checkCoordinates() {
        Node node1FromTST = tst.search(address1.getAddressToSTR()).get(0);
        assertEquals(1.0, node1FromTST.lat);
        assertEquals(1.0, node1FromTST.lon);

        Node node2FromTST = tst.search(address2.getAddressToSTR()).get(0);
        assertEquals(2.0, node2FromTST.lat);
        assertEquals(2.0, node2FromTST.lon);

        Node node3FromTST = tst.search(address3.getAddressToSTR()).get(0);
        assertEquals(3.0, node3FromTST.lat);
        assertEquals(2.0, node3FromTST.lon);

        Node node4FromTST = tst.search(address4.getAddressToSTR()).get(0);
        assertEquals(5.0, node4FromTST.lat);
        assertEquals(1.0, node4FromTST.lon);

        Node node5FromTST = tst.search(address5.getAddressToSTR()).get(0);
        assertEquals(5.0, node5FromTST.lat);
        assertEquals(2.0, node5FromTST.lon);
    }

    /**
     * Checks if the address objects of the nodes inserted into the TST are the same as the address objects
     * of the nodes returned by the search method. It also checks the general functionality of the search method
     * by searching for a node with a prefix of the address
     */
    @Test
    @DisplayName("Checking address objects of the nodes")
    void addressInTSTNodes() {
        String input = "Strand";
        Node node1FromTST = tst.search(input).get(0);
        assertEquals(address1, node1FromTST.getAddress());

        Node node4FromTST = tst.search(input).get(1);
        assertEquals(address4, node4FromTST.getAddress());

        Node node5FromTST = tst.search(input).get(2);
        assertEquals(address5, node5FromTST.getAddress());

        Node node2FromTST = tst.search("Rued").get(0);
        assertEquals(address2, node2FromTST.getAddress());

        Node node3FromTST = tst.search("g").get(0);
        assertEquals(address3, node3FromTST.getAddress());
    }


    @Test
    @DisplayName("Checking collectAll method")
    void testCollectAll() {
        String prefix = "Strandvejen";
        List<Node> results = tst.search(prefix);

        assertTrue(results.contains(node1));
        assertTrue(results.contains(node4));
        assertTrue(results.contains(node5));

        assertFalse(results.contains(node2));
        assertFalse(results.contains(node3));
    }

    @Test
    @DisplayName("Checking exception when address and node inserted are null")
    void nullException() {
        assertThrows(NullPointerException.class, () -> tst.insert(null, null));
    }
}