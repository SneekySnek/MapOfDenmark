package handin2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.stream.*;

import handin2.Pathfinding.AStarSP;
import handin2.Pathfinding.Graph;
import handin2.Relation.*;
import handin2.UI.OsmLoading;
import handin2.Way.*;

public class Model implements Serializable {

    RTree<RelationLand> landRelations = new RTree<>();
    RTree<WayWater> waterWays = new RTree<>();

    RTree<Way> waysDense = new RTree<>();
    RTree<Relation> relationsDense = new RTree<>();
    RTree<WayRoad> roadsDense = new RTree<>();

    RTree<Way> waysMid = new RTree<>();
    RTree<Relation> relationsMid = new RTree<>();
    RTree<WayRoad> roadsMid = new RTree<>();

    RTree<Way> waysLight = new RTree<>();
    RTree<Relation> relationsLight = new RTree<>();
    RTree<WayRoad> roadsLight = new RTree<>();
    public TST addressTST = new TST();
    Graph graph;
    public AStarSP aStarSP;

    /*
        The Hashmap contains default speed for different way road kinds. Based on speed limits from:
        https://wiki.openstreetmap.org/wiki/OSM_tags_for_routing/Maxspeed

        motorway_link and trunk_link differs from the specified speed in the link in
        order to make it less favourable for our Dijkstra/Astar algorithm to go off
        the motorway/trunk that it is currently on.
     */
    Map<String, Integer> speedMap = new HashMap<>() {{
        put("motorway", 130);
        put("motorway_link", 90);
        put("trunk", 80);
        put("trunk_link", 60);
        put("primary", 50);
        put("primary_link", 50);
        put("secondary", 50);
        put("secondary_link", 50);
        put("tertiary", 50);
        put("tertiary_link", 50);
        put("unclassified", 50);
        put("residential", 50);
        put("living_street", 15);
    }};

    double minlat, maxlat, minlon, maxlon;

    public static Model load(String filename, OsmLoading.ProgressHandler progressHandler) throws FileNotFoundException, IOException, ClassNotFoundException, XMLStreamException, FactoryConfigurationError {
        int bufferSize = 64 * 1024;

        if(filename.endsWith(".osm.obj.zip")) {
            try (var zipInput = new ZipInputStream(new FileInputStream(filename))) {
                zipInput.getNextEntry();
                File tempFile = File.createTempFile("tempModel", ".obj");

                try (var out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[bufferSize];
                    int len;
                    while ((len = zipInput.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }

                try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile), bufferSize))) {
                    return (Model) in.readObject();
                } finally {
                    tempFile.delete();
                }
            }
        } else if (filename.endsWith(".obj")) {
            try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename), bufferSize))) {
                return (Model) in.readObject();
            }
        }

        return new Model(filename, progressHandler);
    }


    public Model(String filename, OsmLoading.ProgressHandler progressHandler) throws XMLStreamException, FactoryConfigurationError, IOException {
        if (filename.endsWith(".osm.zip")) {
            parseZIP(filename, progressHandler);
        } else if (filename.endsWith(".osm")) {
            parseOSM(filename, progressHandler);
        }
        save(filename+".obj");
    }

    void save(String filename) throws FileNotFoundException, IOException {
        try (var out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
            out.writeObject(this);
        }
    }

    private void parseZIP(String filename, OsmLoading.ProgressHandler progressHandler) throws IOException, XMLStreamException, FactoryConfigurationError {
        var input = new ZipInputStream(new FileInputStream(filename));
        input.getNextEntry();
        parseOSM(input, filename, progressHandler);
    }

    private void parseOSM(String filename, OsmLoading.ProgressHandler progressHandler) throws IOException, XMLStreamException, FactoryConfigurationError {
        parseOSM(new FileInputStream(filename), filename, progressHandler);
    }

    private void parseOSM(InputStream inputStream, String filename, OsmLoading.ProgressHandler progressHandler) throws IOException, XMLStreamException, FactoryConfigurationError {
        var input = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(inputStream));
        Map<Long, Node> id2node = new HashMap<>();
        Map<Long, Way> id2way = new HashMap<>();
        Map<Long, WayRoad> id2wayRoad = new HashMap<>();
        var way = new ArrayList<Node>();
        List<Long> potentialIntersection = new ArrayList<>();
        Map<Long, Node> id2intersection = new HashMap<>();

        int maxSpeed = -1;
        boolean vehicle = false;
        boolean bike = false;
        boolean pedestrian = false;
        boolean road = false;

        String city = "", houseNumber = "", postcode = "", street = "";
        Long wayId = null;
        Long nodeId = null;
        boolean isJylland = false;
        boolean missingData = false;
        boolean addressCreated = false;

        Node tempNode = null;
        String wayKind = "";
        String roadKind = "";
        String naturalKind = "";

        double fileSizeInMB = getFileSizeInMegabits(filename);
        System.out.println("file in md" + fileSizeInMB);

        int count = 0;

        long startTime = System.currentTimeMillis();

        while (input.hasNext()) {
            if(filename.endsWith(".osm")) {
                count++;
                progressHandler.updateProgress(count, fileSizeInMB * 10000);
            }

            var tagKind = input.next();
            if (tagKind == XMLStreamConstants.START_ELEMENT) {
                var name = input.getLocalName();
                if (name == "bounds") {
                    minlat = Double.parseDouble(input.getAttributeValue(null, "minlat"));
                    maxlat = Double.parseDouble(input.getAttributeValue(null, "maxlat"));
                    minlon = Double.parseDouble(input.getAttributeValue(null, "minlon"));
                    maxlon = Double.parseDouble(input.getAttributeValue(null, "maxlon"));
                } else if (name == "node") {
                    tempNode = null;
                    city = "";
                    houseNumber = "";
                    postcode = "";
                    street = "";

                    var id = Long.parseLong(input.getAttributeValue(null, "id"));
                    var lat = Double.parseDouble(input.getAttributeValue(null, "lat"));
                    var lon = Double.parseDouble(input.getAttributeValue(null, "lon"));

                    tempNode = new Node(-lat, 0.56 * lon);
                    nodeId = id;
                } else if (name == "way") {
                    wayId = Long.parseLong(input.getAttributeValue(null, "id"));
                    way.clear();
                    potentialIntersection.clear();

                    maxSpeed = -1;
                    vehicle = false;
                    bike = false;
                    pedestrian = false;
                    road = false;

                    roadKind = "";
                    naturalKind = "";
                    wayKind = "";
                } else if (name == "tag") {
                    String v = input.getAttributeValue(null, "v");
                    String k = input.getAttributeValue(null, "k");

                    if(k.equals("name") && v.equals("Jylland")) isJylland = true;

                    if(k.equals("highway")) {
                        wayKind = k;
                        roadKind = v;
                        road = true;

                        switch (v) {
                            case "motorway", "trunk", "motorway_link", "trunk_link", "unclassified":
                                vehicle = true;
                                break;
                            case "primary", "secondary", "primary_link", "secondary_link", "tertiary_link", "living_street", "service", "residential", "tertiary":
                                vehicle = true;
                                bike = true;
                                pedestrian = true;
                                break;
                            case "cycleway":
                                bike = true;
                                pedestrian = true;
                                break;
                            case "pedestrian", "track", "footway", "bridleway", "steps", "path":
                                pedestrian = true;
                                break;
                        }
                    }

                    if(k.equals("bicycle") && v.equals("yes") || k.contains("cycleway") && !v.equals("no")) bike = true;
                    if(k.contains("sidewalk") && !v.equals("no")) pedestrian = true;

                    if (k.equals("maxspeed")){
                        try {
                            maxSpeed = Integer.parseInt(v);
                        } catch (RuntimeException err) {
                            maxSpeed = speedMap.getOrDefault(roadKind, 30);
                        }
                    } else if(k.equals("natural") && v.equals("peninsula") && isJylland) {
                        wayKind = "land";
                        isJylland = false;
                    } else if(k.equals("place") && v.equals("island") || k.equals("place") && v.equals("islet")) {
                        wayKind = "land";
                    } else if(k.equals("natural") && !road) {
                        wayKind = k;
                        naturalKind = v;
                    } else if (k.equals("building")) {
                        wayKind = k;
                    } else if(k.equals("leisure") && v.equals("park")) {
                        wayKind = "park";
                    } else if(k.equals("landuse") && v.equals("forest")) {
                        wayKind = "landuse";
                        naturalKind = "forest";
                    } else if(k.equals("landuse") && !v.equals("farmyard") && !v.equals("military") && !v.equals("industrial") && !v.equals("residential")) {
                        wayKind = "park";
                    }

                    switch (k) {
                        case "addr:city":
                            city = v;
                            break;
                        case "addr:housenumber":
                            houseNumber = v;
                            break;
                        case "addr:postcode":
                            postcode = v;
                            break;
                        case "addr:street":
                            street = v;
                            break;
                    }
                } else if (name == "nd") {
                    var ref = Long.parseLong(input.getAttributeValue(null, "ref"));
                    var node = id2node.get(ref);

                    if(node == null) continue;

                    if (node.hasAppeared) {
                        potentialIntersection.add(ref);
                    }
                    node.hasAppeared = true;
                    way.add(node);
                }
            } else if (tagKind == XMLStreamConstants.END_ELEMENT) {
                var name = input.getLocalName();

                if(name == "node") {
                    if(!city.isEmpty() && !street.isEmpty() && !houseNumber.isEmpty()) continue;
                    id2node.put(nodeId, tempNode);
                } else if(name.equals("way") && road) {

                    for(Long ref: potentialIntersection) {
                        Node n = id2node.get(ref);
                        id2intersection.put(ref, n);
                    }
                }

                if (name.equals("way")) {
                    Way newWay = new Way(way);

                    if(wayKind.equals("highway")) {
                        if(maxSpeed == -1) {
                            maxSpeed = speedMap.getOrDefault(roadKind, 30);
                        }

                        newWay = new WayRoad(way, roadKind, maxSpeed, vehicle, bike, pedestrian);

                        switch (roadKind) {
                            case "motorway", "motorway_link", "trunk", "trunk_link":
                                roadsMid.insert((WayRoad) newWay);
                                roadsLight.insert((WayRoad) newWay);
                                break;
                            case "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential":
                                roadsMid.insert((WayRoad) newWay);
                                break;
                            default:
                                break;
                        }

                        roadsDense.insert((WayRoad) newWay);
                        id2wayRoad.put(wayId, (WayRoad) newWay);
                        continue;
                    } else if (wayKind.equals("natural") && naturalKind.equals("water")) {
                        newWay = new WayWater(way);
                        waterWays.insert((WayWater) newWay);
                    } else if (wayKind.equals("building")) {
                        newWay = new WayBuilding(way);
                        waysDense.insert(newWay);
                    } else if(wayKind.equals("park")) {
                        newWay = new WayNature(way);
                        waysMid.insert(newWay);
                    } else if(wayKind.equals("land")) {
                        addWaySelfClosingWayLand(newWay);
                    }

                    id2way.put(wayId, newWay);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("First while loop: " + (endTime - startTime));
        startTime = System.currentTimeMillis();

        inputStream = resetInputStream(inputStream, filename);

        input = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(inputStream));

        graph = new Graph(id2intersection.values()); // create graph after intersections in counted

        Node currentIntersection = null;
        Node last = null;
        boolean calculateDistance = false;
        boolean shouldCreateChain = false;

        List<Way> relationOuter = new ArrayList<>();
        int relationOuterCoordsSize = 0;
        List<Way> relationInner = new ArrayList<>();
        int relationInnerCoordsSize = 0;
        List<Node> wayIntersectionNodes = new ArrayList<>();

        double distance = 0;

        while (input.hasNext()) {
            if(filename.endsWith(".osm")) {
                count++;
                progressHandler.updateProgress(count, fileSizeInMB * 10000);
            }

            var tagKind = input.next();
            if (tagKind == XMLStreamConstants.START_ELEMENT) {
                var name = input.getLocalName();

                if(name == "node") {
                    addressCreated = false;
                    tempNode = null;
                    city = "";
                    houseNumber = "";
                    postcode = "";
                    street = "";

                    var id = Long.parseLong(input.getAttributeValue(null, "id"));
                    var lat = Double.parseDouble(input.getAttributeValue(null, "lat"));
                    var lon = Double.parseDouble(input.getAttributeValue(null, "lon"));

                    tempNode = new Node(-lat, 0.56 * lon);
                    nodeId = id;
                } else if (name == "way") {
                    wayIntersectionNodes.clear();
                    currentIntersection = null;
                    last = null;
                    calculateDistance = false;
                    wayId = Long.parseLong(input.getAttributeValue(null, "id"));
                    road = false;
                } else if (name == "tag") {
                    String v = input.getAttributeValue(null, "v");
                    String k = input.getAttributeValue(null, "k");

                    if(k.equals("name") && v.equals("Jylland")) isJylland = true;

                    if(k.equals("highway")) {
                        wayKind = k;
                        roadKind = v;
                        road = true;
                    }

                    if(k.equals("natural") && v.equals("peninsula") && isJylland) {
                        wayKind = "land";
                        isJylland = false;
                    } else if(k.equals("place") && v.equals("island") || k.equals("place") && v.equals("islet")) {
                        wayKind = "land";
                    } else if(k.equals("natural")) {
                        wayKind = k;
                        naturalKind = v;
                    } else if (k.equals("building")) {
                        wayKind = k;
                    } else if(k.equals("leisure") && v.equals("park")) {
                        wayKind = "park";
                    } else if(k.equals("landuse") && v.equals("forest")) {
                        wayKind = "landuse";
                        naturalKind = "forest";
                    } else if(k.equals("landuse") && !v.equals("farmyard") && !v.equals("military") && !v.equals("industrial")) {
                        wayKind = "park";
                    }

                    switch (k) {
                        case "addr:city":
                            city = v;
                            break;
                        case "addr:housenumber":
                            houseNumber = v;
                            break;
                        case "addr:postcode":
                            postcode = v;
                            break;
                        case "addr:street":
                            street = v;
                            break;
                    }

                    if (!addressCreated && !city.isEmpty() && !houseNumber.isEmpty() && !street.isEmpty() && !postcode.isEmpty()) {
                        Address address = new Address(city, houseNumber, postcode, street); //Creates new address object
                        if (tempNode != null) {
                            tempNode.setAddress(address); //Sets address to node
                            addressTST.insert(address.getAddressToSTR(), tempNode); // puts address and Node into TST
                            addressCreated = true;
                        }
                    }
                } else if (name == "nd") {
                    var ref = Long.parseLong(input.getAttributeValue(null, "ref"));
                    Node current = id2intersection.get(ref);

                    if(current == null) continue;

                    wayIntersectionNodes.add(current);
                } else if (name == "member") {
                    String role = input.getAttributeValue(null, "role");
                    Long ref = Long.parseLong(input.getAttributeValue(null, "ref"));

                    if(role.equals("inner")) {
                        Way innerWay = id2way.get(ref);
                        if(innerWay == null || missingData) {
                            missingData = true;
                            continue;
                        };

                        relationInner.add(innerWay);
                        relationInnerCoordsSize += innerWay.coords.length;
                    } else if (role.equals("outer")) {
                        Way outerWay = id2way.get(ref);
                        if(outerWay == null || missingData) {
                            missingData = true;
                            continue;
                        };

                        relationOuter.add(outerWay);
                        relationOuterCoordsSize += outerWay.coords.length;

                        if(!shouldCreateChain && relationOuter.size() > 1) {
                            Way lastWay = relationOuter.get(relationOuter.size() - 2);
                            if(!Util.hasMatchingCoords(lastWay, outerWay)) {
                                shouldCreateChain = true;
                            }
                        }
                    }
                } else if(name == "relation") {
                    relationInner.clear();
                    relationInnerCoordsSize = 0;
                    relationOuter.clear();
                    relationOuterCoordsSize = 0;
                    wayKind = "";
                    naturalKind = "";
                    missingData = false;
                    shouldCreateChain = false;
                }
            } else if (tagKind == XMLStreamConstants.END_ELEMENT) {
                var name = input.getLocalName();

                if(name == "way" && road) {
                    for(Node intersection : wayIntersectionNodes) {
                        if(calculateDistance){
                            distance += Util.haversine(intersection.lat, intersection.lon, last.lat, last.lon);
                            last = intersection;
                        }

                        if (currentIntersection != null) {
                            WayRoad w = id2wayRoad.get(wayId);
                            graph.addEdge(currentIntersection, intersection, distance, w);
                        }
                        currentIntersection = intersection;
                        last = intersection;
                        distance = 0;
                        calculateDistance = true;
                    }
                } else if (name.equals("relation")) {
                    if(missingData) continue;

                    if(wayKind.equals("land")) {
                        addRelation("land", relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize, shouldCreateChain);
                    } else if(wayKind.equals("building")) {
                        addRelation("building", relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize, shouldCreateChain);
                    } else if (wayKind.equals("natural") && naturalKind.equals("water")) {
                        addRelation("water", relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize, shouldCreateChain);
                    } else if(wayKind.equals("landuse") && naturalKind.equals("forest")) {
                        addRelation("forest", relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize, shouldCreateChain);
                    }
                }
            }
        }

        endTime = System.currentTimeMillis();
        System.out.println("Second while loop: " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        aStarSP = new AStarSP(graph, roadsDense);
        endTime = System.currentTimeMillis();
        System.out.println("Astar " + (endTime - startTime));
    }

    public double getFileSizeInMegabits(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        long sizeInBytes = Files.size(path);
        return sizeInBytes / 1024.0 / 1024.0 * 8; // Convert bytes to megabits
    }

    private InputStream resetInputStream(InputStream inputStream, String filename) throws IOException {
        inputStream.close();

        if (filename.endsWith(".osm.zip")) {
            var input = new ZipInputStream(new FileInputStream(filename));
            input.getNextEntry();
            return input;
        } else {
            return new FileInputStream(filename);
        }
    }

    private void addWaySelfClosingWayLand(Way way) {
        RelationLand land = new RelationLand(way);
        landRelations.insert(land);
    }

    private void addRelation(String relationKind, List<Way> relationOuter, int relationOuterCoordsSize, List<Way> relationInner, int relationInnerCoordsSize, boolean shouldCreateChain) {
        Relation relation = null;

        if(shouldCreateChain) {
            Util.createWayChain(relationOuter);
        }

        // Make sure ways are flipped correctly, so that the ending latitude and longitude of
        // each way in the list are the start latitude and longitude for the following way in the list
        Util.preProcessWays(relationOuter);

        if(relationInner.size() > 1) {
            Util.preProcessWays(relationInner);
        }

        switch (relationKind) {
            case "land":
                relation = new RelationLand(relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize);
                break;
            case "building":
                relation = new RelationBuilding(relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize);
                break;
            case "water":
                relation = new RelationWater(relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize);
                break;
            case "forest":
                relation = new RelationForest(relationOuter, relationOuterCoordsSize, relationInner, relationInnerCoordsSize);
                break;
            default:
                break;
        }

        if(relation == null || relationOuter.isEmpty()) return;

        switch (relationKind) {
            case "land":
                landRelations.insert((RelationLand) relation);
                return;
            case "water", "forest":
                relationsMid.insert(relation);
                break;
            default:
                relationsDense.insert(relation);
                break;
        }
    }
}
