package handin2;

import handin2.Interfaces.Geometry;
import handin2.Interfaces.Spatial;
import handin2.Way.Way;
import handin2.Way.WayRoad;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.*;

/**
 * RTree containing Class K. Used to store and query Spacial objects, such as Ways or Nodes.
 * @param <K> type of Spacial Class, which is contained in RTree
 * @reference Guttman, A. (1984). "R-Trees: A Dynamic Index Structure for Spatial Searching" (PDF). Proceedings of the 1984 ACM SIGMOD international conference on Management of data – SIGMOD '84. p. 47. doi:10.1145/602259.602266. ISBN 978-0897911283. S2CID 876601.
 * @reference Beckmann, N.; Kriegel, H.-P.; Schneider, R.; Seeger, B. (1990). "The R*-tree: an efficient and robust access method for points and rectangles" (PDF). Proceedings of the 1990 ACM SIGMOD international conference on Management of data – SIGMOD '90. p. 322. doi:10.1145/93597.98741. ISBN 978-0897913652. S2CID 1460731.
 */
public class RTree<K extends Spatial> implements Serializable {
    private RTreeNode root;
    private final int MAX_CHILDREN = 4; //MAX_CHILDREN need testing
    private final int MIN_CHILDREN = MAX_CHILDREN/2;

    public RTree() {
        this.root = new LeafNode<K>();
    }

    /**
     * General class of RTreeNodes in RTree
     */
    static abstract class RTreeNode implements Spatial, Serializable, Geometry {
        RTreeNode parent;
        BoundingBox boundingBox;

        RTreeNode() {
            boundingBox = new BoundingBox();
        }

        public RTreeNode getParent(){
            return parent;
        }

        @Override
        public double distanceTo(double lon, double lat) {
            return boundingBox.distanceTo(lon, lat);
        }

        public BoundingBox getBoundingBox(){
            return boundingBox;
        }

        public abstract <T extends Spatial> List<T> getList();
        public abstract void addToList(Spatial object);
    }

    /**
     * A type of RTreeNode containing other RTreeNodes
     */
    static class InnerNode extends RTreeNode {
        List<RTreeNode> children;

        InnerNode() {
            super();
            this.children = new ArrayList<>();
        }

        @Override
        public List<RTreeNode> getList() {
            return children;
        }

        @Override
        public void addToList(Spatial object) {
            children.add((RTreeNode) object);
            boundingBox.combine(object);
            ((RTreeNode) object).parent = this;
        }
    }

    /**
     * A type of RTreeNode containing Spacial objects, such as Way, RoadWay or Nodes
     * @param <J> type of Spacial Class, which is contained in RTree
     */
    static class LeafNode<J extends Spatial> extends RTreeNode { //Giver det mening at leafNode extender Node? Den har ikke children.
        List<J> ways;

        public LeafNode() {
            super();
            this.ways = new ArrayList<>();
        }

        @Override
        public List<J> getList() {
            return ways;
        }

        @Override
        public void addToList(Spatial object) {
            ways.add((J) object);
            boundingBox.combine(object);
        }
    }

    /**
     * Insertion of Spacial object into RTree.
     * Invokes SplitNode if children exceeds maximum and passes child L.
     * Takes amortized O(logN)
     * @param way way can be substituted for any object extending Spacial
     */
    public void insert(K way) {
        LeafNode<K> chosenLeaf = chooseLeaf(root, way.getBoundingBox());
        List<K> list = chosenLeaf.getList();

        if(list.size() < MAX_CHILDREN) { //If L has room for another entry, install E
            chosenLeaf.addToList(way);
            adjustTree(chosenLeaf);
        } else { //Split if node is full
            RTreeNode n = splitNode(chosenLeaf, way);
            adjustTree(chosenLeaf, n);
        }
    }

    /**
     * Recursive function. Chooses the best Leaf to place the given boundingBox.
     * @param RTreeNode Current Node, which is looked at
     * @param boundingBox BoundingBox of inserted object
     * @return LeafNode
     */
    private LeafNode<K> chooseLeaf(RTreeNode RTreeNode, BoundingBox boundingBox){
        if(RTreeNode instanceof LeafNode){
            return (LeafNode<K>) RTreeNode;

        } else { //Recursively choose child with minimum increase
            double minIncrease = Double.MAX_VALUE;
            RTreeNode minChild = null;

            for(RTreeNode child: ((InnerNode) RTreeNode).children){
                BoundingBox b = child.getBoundingBox();
                double increase = boundingBox.combinePeek(b).area()-b.area();
                if(minIncrease == increase){
                    if(b.area() < minChild.getBoundingBox().area()){
                        minChild = child;
                    }
                } else if (minIncrease > increase){
                    minIncrease = increase;
                    minChild = child;
                }
            }
            return chooseLeaf(minChild, boundingBox);
        }
    }

    /**
     * Splits current RTreeNode into two Nodes containing old children LL and new child L.
     * @param split Node which has exceeded maximum children, and is getting split
     * @param e New child L to be installed
     * @return RTreeNode
     * @param <T> Spacial object contained in RTree
     */
    private <T extends Spatial> RTreeNode splitNode(RTreeNode split, T e){ //Skal tjekke om split = n0 eller om den skal være n1
        RTreeNode n0, n1;
        if(split instanceof LeafNode){ n0 = new LeafNode<K>();  n1 = new LeafNode<K>();  }
        else                         { n0 = new InnerNode(); n1 = new InnerNode(); }

        List<T> entries = split.getList();
        entries.add(e);

        pickSeeds(entries, n0, n1);

        for(int i = entries.size()-1; i >= 0; i--){
            T entry = entries.get(i);
            if(entries.size()+n0.getList().size() <= MIN_CHILDREN){
                n0.addToList(entry);
                entries.remove(entry);
            } else if(entries.size()+n1.getList().size() <= MIN_CHILDREN){
                n1.addToList(entry);
                entries.remove(entry);
            } else {
                pickNext(entries, n0, n1);
            }
        }

        for(Spatial entry: n0.getList()){
            split.addToList(entry);
        }

        split.getBoundingBox().replace(n0);
        return n1;
    }

    /**
     * Used in splitNode, finds child with the highest preference to get placed with certain seed.
     * Installs that child.
     * @param entries Entries of split Node
     * @param n0 RTreeNode containing seed0
     * @param n1 RTreeNode containing seed1
     * @param <T> Spacial object contained in Tree
     */
    private <T extends Spatial> void pickNext(List<T> entries, RTreeNode n0, RTreeNode n1){
        BoundingBox b0 = n0.getBoundingBox();
        BoundingBox b1 = n1.getBoundingBox();

        double maxDif = 0;
        T nextEntry = null;
        boolean addToN0 = false;

        for(T entry: entries){
            double d0 = b0.combinePeek(entry).area() - b0.area();
            double d1 = b1.combinePeek(entry).area() - b1.area();
            double dif = Math.abs(d0-d1);

            if(maxDif <= dif){
                maxDif = dif;
                nextEntry = entry;
                addToN0 = d0 < d1;
            }
        }

        if(addToN0) { n0.addToList(nextEntry); }
        else        { n1.addToList(nextEntry); }
        entries.remove(nextEntry);
    }

    /**
     * Chooses two seeds from the split Node
     * @param entries Children contained in split RTreeNode
     * @param n0 Places seed0 into n0
     * @param n1 Places seed1 into n1
     * @param <T> Spacial object contained in RTree
     */
    private <T extends Spatial> void pickSeeds(List<T> entries, RTreeNode n0, RTreeNode n1){ //Er det bedre at putte s0 i L eller LL? værd at tjekke?
        T s0Worst = null;
        T s1Worst = null;
        double dWorst = 0;

        for(int i = 0; i < entries.size(); i++){
            for(int j = i+1; j < entries.size(); j++){
                T s0 = entries.get(i);
                T s1 = entries.get(j);
                BoundingBox b0 = s0.getBoundingBox();
                BoundingBox b1 = s1.getBoundingBox();
                double d = b0.combinePeek(b1).area();
                if (d > dWorst) {
                    dWorst = d;
                    s0Worst = s0;
                    s1Worst = s1;
                }
            }
        }
        entries.remove(s0Worst);
        entries.remove(s1Worst);
        n0.addToList(s0Worst);
        n1.addToList(s1Worst);
    }


    /**
     * Adjusts parent boundingBoxes.
     * Called after insertion or split.
     * @param RTreeNode Node which needs boundingBox adjusted. All parent boundingBoxes are updated after.
     */
    private void adjustTree(RTreeNode RTreeNode){
        if(RTreeNode == root){
            return;
        }
        RTreeNode p = RTreeNode.getParent();
        BoundingBox b = p.getBoundingBox();

        b.replace(RTreeNode);
        for(Spatial e: p.getList()){
            b.combine(e);
        }

        adjustTree(p);
    }

    /**
     * Places n1 in n0. Invokes split if n0 exceeds maximum children.
     * Updates boundingBoxes and places the newly split RTreeNode with parent.
     * Splits parent if parent exceeds maximum children, and so on.
     * Updates boundingBoxes as it goes. Calls normal adjustTree, once n1 has been placed into RTree.
     * @param n0 RTreeNode which needs boundingBox adjusted. Will house n1.
     * @param n1 RTreeNode which needs to get placed into the RTree
     */
    private void adjustTree(RTreeNode n0, RTreeNode n1){

        if(n0 == root){
            InnerNode rootNew = new InnerNode();
            rootNew.addToList(n0);
            rootNew.addToList(n1);
            root = rootNew;
            return;
        }

        RTreeNode p = n0.getParent();
        BoundingBox b = p.getBoundingBox();

        if ((p.getList().size()+1) < MAX_CHILDREN){
            p.addToList(n1);
            b.replace(n0);
            for(Spatial e: p.getList()){
                b.combine(e);
            }
            adjustTree(p);
        } else {
            RTreeNode p1 = splitNode(p, n1);
            adjustTree(p, p1);
        }
    }

    public LeafNode<K> findLeaf(RTreeNode RTreeNode, K way){
        if(RTreeNode instanceof LeafNode){
            List<K> list = RTreeNode.getList();
            for(K entry: list){
                if(entry == way){ return (LeafNode<K>) RTreeNode; }
            }
        } else {
            List<RTreeNode> list = RTreeNode.getList();
            for(RTreeNode entry: list){
                if(RTreeNode.getBoundingBox().contains(way)){
                    LeafNode<K> l = findLeaf(entry, way);
                    if(l != null){ return l; }
                }
            }
        }
        return null;
    }

    /**
     * Returns all Spacial objects which boundingBoxes intersects with the queried boundingBox
     * @param RTreeNode Currently searched Node, starts at Root
     * @param boundingBox Queried boundingBox
     * @return List of Spacial objects
     */
    public List<K> search(RTreeNode RTreeNode, BoundingBox boundingBox){
        ArrayList<K> list = new ArrayList<>();

        if(RTreeNode instanceof LeafNode){
            for(K w: ((LeafNode<K>) RTreeNode).getList()){
                if(w.getBoundingBox().intersects(boundingBox)){
                    list.add(w);
                }
            }
        } else {
            for(RTreeNode n: ((InnerNode) RTreeNode).getList()){
                if(n.getBoundingBox().intersects(boundingBox)){
                    list.addAll(search(n, boundingBox));
                }
            }
        }
        return list;
    }

    public List<K> search(double minlat, double maxlat, double minlon, double maxlon){
        return search(root, new BoundingBox(minlat, maxlat, minlon, maxlon));
    }

    /**
     * Searches in RTree until nearest point on nearest way is found.
     * @param lon Queried lon
     * @param lat Queried lat
     * @return Nearest Node and Way
     */
    public Object[] nearestNeighbour(double lon, double lat, String transportType){
        Map<Node, WayRoad> node2way = new HashMap<>();
        PriorityQueue<Geometry> pq = new PriorityQueue<>(new Comparator<Geometry>() {
            @Override
            public int compare(Geometry o1, Geometry o2) {
                return Double.compare(o1.distanceTo(lon, lat), o2.distanceTo(lon, lat));
            }
        });

        pq.add(root);

        while(!pq.isEmpty()){
            Geometry current = pq.poll();
            if (current instanceof Node){
                return new Object[]{current, node2way.get((Node) current)};
            } else if (current instanceof WayRoad){
                WayRoad w = (WayRoad) current;
                if(w.vehicle || !transportType.equals("vehicle")){
                    double[] closestCoordinates = w.findClosestCoordinates(lon, lat);
                    Node node = new Node(closestCoordinates[1], closestCoordinates[0]);
                    node2way.put(node, w);
                    pq.add(node);
                }
            } else {
                for(Spatial entry: ((RTreeNode) current).getList()){
                    pq.add((Geometry) entry);
                }
            }
        }

        return null;
    }

    public Object[] nearestNeighbour(Node node, String transportType){
        return nearestNeighbour(node.lon, node.lat, transportType);
    }

    //TIL AT TEGNE

    public void draw(GraphicsContext gc, int layers) {
        RTreeNode n = root;
        List<RTreeNode> list = n.getList();
        gc.setStroke(Color.RED);
        drawBox(gc, n);
        for(int i = 0; i < layers; i++){
            if(i%3 == 0){ gc.setStroke(Color.GREEN); }
            else if(i%3 == 1){ gc.setStroke(Color.BLUE); }
            else { gc.setStroke(Color.RED); }

            drawBox(gc, list);
            List<RTreeNode> listNew = new ArrayList<>();

            for(RTreeNode g: list){
                listNew.addAll(g.getList());
            }
            list = listNew;
        }
    }

    public void drawBranch(GraphicsContext gc, int layers) {
        RTreeNode n = root;
        List<RTreeNode> list = n.getList();
        gc.setStroke(Color.RED);
        drawBox(gc, n);
        for(int i = 0; i < layers; i++){
            if(i%3 == 0){ gc.setStroke(Color.GREEN); }
            else if(i%3 == 1){ gc.setStroke(Color.BLUE); }
            else { gc.setStroke(Color.RED); }

            drawBox(gc, list);

            list = list.get(1).getList();
        }
    }


    public void drawBox(GraphicsContext gc, Spatial g) {
        BoundingBox b = g.getBoundingBox();
        gc.strokeRect(b.minlon, -b.maxlat, (b.maxlon-b.minlon), (b.maxlat-b.minlat));
    }

    public <T extends Spatial> void drawBox(GraphicsContext gc, List<T> list) {
        for(Spatial g: list){
            BoundingBox b = g.getBoundingBox();
            gc.strokeRect(b.minlon, -b.maxlat, (b.maxlon-b.minlon), (b.maxlat-b.minlat));
        }
    }
    //these are helper methods that help with JUnit tests
    int getNumberOfInnerNodes(RTreeNode node) {
        int count = 0;
        if (node instanceof RTree.InnerNode) {
            count++;
            for (RTree.RTreeNode child : ((RTree.InnerNode) node).getList()) {
                count += getNumberOfInnerNodes(child);
            }
        }
        return count;
    }

    int getNumberOfLeafNodes(RTreeNode node) {
        if (node instanceof RTree.LeafNode) {
            return 1;
        } else {
            int totalLeafNodes = 0;
            for (RTree.RTreeNode child : ((RTree.InnerNode) node).getList()) {
                totalLeafNodes += getNumberOfLeafNodes(child);
            }
            return totalLeafNodes;
        }
    }
    protected RTreeNode getRoot() {
        return root;
    }
    protected void getWays(RTreeNode node, List<Way> ways) {
        if (node instanceof RTree.LeafNode) {
            ways.addAll(((RTree.LeafNode<Way>) node).getList());
        } else {
            for (RTree.RTreeNode child : ((RTree.InnerNode) node).getList()) {
                getWays(child, ways);
            }
        }
    }

}