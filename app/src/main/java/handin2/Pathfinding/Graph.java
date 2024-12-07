package handin2.Pathfinding;

import handin2.Node;
import handin2.Way.WayRoad;

import java.io.Serializable;
import java.util.*;

/**
 * Graph representation of intersections, used for pathfinding.
 */
public class Graph implements Serializable {

    public Node[] nodes;
    Map<Node, Integer> nd2Id;
    List<Edge>[] adj;

    class Edge implements Serializable{
        int destination;
        double distance;
        WayRoad way;

        Edge(int destination, double distance, WayRoad way){
            this.distance = distance;
            this.way = way;
            this.destination = destination;
        }

        public Node getDestination(){
            return nodes[destination];
        }
    }

    /**
     * Initializer of Graph. Requires list of Intersections.
     * @param intersections list of Node intersection
     */
    public Graph(Collection<Node> intersections){
        int size = intersections.size();
        nodes = new Node[size+2];
        nd2Id = new HashMap<>();
        adj = (LinkedList<Edge>[]) new LinkedList[size+2];
//        for (int i = 0; i < size; i++){
//            adj[i] = new LinkedList<>();
//            nodes[i] = intersections.get(i);
//            nd2Id.put(intersections.get(i), i);
//        }

        int i = 0;
        for(Node intersection : intersections) {
            adj[i] = new LinkedList<>();
            nodes[i] = intersection;
            nd2Id.put(intersection, i);
            i++;
        }
    }

    /**
     * Adds directed edge.
     * @param source Start Node
     * @param destination Finish Node
     * @param distance Distance from start Node to finish Node along segments of Way
     * @param way WayRoad, which distance was calculated from
     */
    public void addDirectedEdge(Node source, Node destination, double distance, WayRoad way){
        adj[nd2Id.get(source)].add(new Edge(nd2Id.get(destination), distance, way));
    }

    /**
     * Adds two directed edges. From source to destination and wise versa.
     * @param source Start Node
     * @param destination Finish Node
     * @param distance Distance from start Node to finish Node along segments of Way
     * @param way WayRoad, which distance was calculated from
     */
    public void addEdge(Node source, Node destination, double distance, WayRoad way){
        int n1 = nd2Id.get(source);
        int n2 = nd2Id.get(destination);

        adj[n1].add(new Edge(n2, distance, way));
        adj[n2].add(new Edge(n1, distance, way));
    }

    public List<Edge> getEdges(Node node){
        return new ArrayList<>(adj[nd2Id.get(node)]);
    }

    /**
     * Adds StartNode to Graph, used for Pathfinding.
     * Replaces existing StartNode, if StartNode already exists in Graph
     * @param node StartNode in Pathfinding.
     */
    public void setStart(Node node){
        int index = nodes.length-2;

        if(nodes[index] != null){
            nd2Id.remove(nodes[index]);
            for (Edge start: adj[index]){
                adj[start.destination].remove(adj[start.destination].size()-1);
            }
        }
        nodes[index] = node;
        adj[index] = new LinkedList<>();
        nd2Id.put(node, index);
    }

    /**
     * Adds FinishNode to Graph, used for Pathfinding.
     * Replaces existing FinishNode, if FinishNode already exists in Graph
     * @param node FinishNode in Pathfinding.
     */
    public void setFinish(Node node){
        int index = nodes.length-1;

        if(nodes[index] != null){
            nd2Id.remove(nodes[index]);
            for (Edge start: adj[index]){
                adj[start.destination].remove(adj[start.destination].size()-1);
            }
        }
        nodes[index] = node;
        adj[index] = new LinkedList<>();
        nd2Id.put(node, index);
    }
}