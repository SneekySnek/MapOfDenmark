package handin2;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TST stands for Ternary Search Tree and is a data structure we use for efficient storing and searching for string addresses.
 */
public class TST implements Serializable {
    /**
     * This static inner node class represents nodes in the TST.
     * Each node contains a  character, a boolean value if the node is the end of an address.
     * It also contains references to the left, equal and right child nodes.
     * the mapNode is a reference to the innernode in the TST that containst the handin2.Node objects.
     */
    static class Node implements Serializable {
        char data;
        boolean isEndOfString;
        Node left, eq, right;
        handin2.Node mapNode;

        /**
         * Node constructor initializes a new Node with the given character.
         * The left, equal, and right children are all initialized to null.
         * The isEndOfString flag is set to false, and the mapNode reference is also set to null.
         *
         * @param data the character to store in the Node.
         */
        public Node(char data) {
            this.data = data;
            this.isEndOfString = false;
            this.left = null;
            this.eq = null;
            this.right = null;
            this.mapNode = null;
        }
    }


    private Node root;
    public TST() {
        root = null;
    }

    /**
     * This method inserts a new string address (in lowercase) and a mapNode into the TST.
     * The address is inserted character by character, and the mapNode is inserted when the end of the address is reached.
     * If the address or mapNode is null, a NullPointerException is thrown.
     * @param address
     * @param mapNode
     */
    public void insert(String address, handin2.Node mapNode) {
        if (address == null || mapNode == null) {
            throw new NullPointerException("Address and node cannot be null");
        }
        address = address.toLowerCase();
        root = insertUtil(root, address, 0, mapNode);
    }

    /**
     * The insertUtil method is a recursive helper method for inserting a new address and mapNode into the TST.
     * If the root is null, a new Node is created with the first character of the address.
     * If the current character is less than the node's data, the left subtree is traversed.
     * If the current character is greater than the node's data, the right subtree is traversed.
     * If the current character matches the node's data, the equal subtree is traversed
     * If the end of the address has been reached, the isEndOfString boolean is set to true, and the mapNode is stored in the node.
     * @param root
     * @param address
     * @param index
     * @param mapNode
     * @returns the root node of the TST
     */
    private Node insertUtil(Node root, String address, int index, handin2.Node mapNode) {
        if (root == null) {
            root = new Node(address.charAt(index));
        }

        if (address.charAt(index) < root.data) {
            root.left = insertUtil(root.left, address, index, mapNode);
        } else if (address.charAt(index) > root.data) {
            root.right = insertUtil(root.right, address, index, mapNode);
        } else {
            if (index + 1 < address.length()) {
                root.eq = insertUtil(root.eq, address, index + 1, mapNode);
            } else {
                root.isEndOfString = true;
                root.mapNode = mapNode; //stores mapNode when end of string is reached
                //System.out.println("Inserted address: " + address);
            }
        }
        return root;
    }

    /**
     * The search method searches for a given address in the TST by recursively traveling throughout the tst
     * The search input is a string address, which is converted to lowercase and trimmed for spaces.
     * searchUtil is called with the root node, the address, an index of 0, an empty prefix, and an empty list of results.
     * @param address
     * @returns a list of handin2.Node objects that match the given address.
     */
    public List<handin2.Node> search(String address) {
        address = address.toLowerCase().trim();
        List<handin2.Node> nodeResults = new ArrayList<>();
        searchUtil(root, address, 0, "", nodeResults);
        return nodeResults;
    }

    /**
     * The searchUtil like the insertUtil is a recursive helper method for searching for a given address in the TST.
     * It travels right, left or equal depending on the character in the address.
     * If the end of the address is reached, all nodes in the equal subtree are collected and added to the results list.
     * If a partial address is searched, the collectAll method is called to collect all nodes in the equal,left and right subtree after the searched prefix.
     * @param root
     * @param address
     * @param index
     * @param prefix
     * @param results
     */
    private void searchUtil(Node root, String address, int index, String prefix, List<handin2.Node> results) {
        if (root == null) {
            return;
        }

        if (index < address.length()) {
            char nextChar = address.charAt(index);

            // Traverse left subtree if the current character is less than the node's data
            if (nextChar < root.data) {
                searchUtil(root.left, address, index, prefix, results);
            }

            if (nextChar == root.data) {
                String newPrefix = prefix + root.data;
                if (root.isEndOfString) {
                    results.add(root.mapNode);
                }
                searchUtil(root.eq, address, index + 1, newPrefix, results);
            }

            if (nextChar > root.data) {
                searchUtil(root.right, address, index, prefix, results);
            }
        } else {
            collectAll(root.left,prefix,results);
            collectAll(root.eq, prefix + root.data, results);
            collectAll(root.right,prefix,results);
        }
    }

    /**
     * The collectAll method collects all nodes in the equal, left and right subtree of a given node.
     * @param root
     * @param prefix
     * @param results
     */
    private void collectAll(Node root, String prefix, List<handin2.Node> results) {
        if (root == null) {
            return;
        }

        if (root.isEndOfString) {
            results.add(root.mapNode);
        }

        collectAll(root.left, prefix, results);
        collectAll(root.eq, prefix + root.data, results);
        collectAll(root.right, prefix, results);
    }
}