package com.example.osm;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * read static Graph date from File represent it as Arrays
 *
 *
 */
//public class Graph {
//    private int nodeNr;//number of the nodes
//    private int edgeNr;//number of the edges
//    private int lengthOfEdgeElement = 3;//Edge Element consist of endNode and metric vector
//    private double[] latitude;//mapping: nodeId -> latitude of that node
//    private double[] longitude;//mapping: nodeId -> longitude of that node
//    private int[] nrOfOutgoingEdges;//mapping: nodeId -> number of outgoing edges of that node
//    private int[] edgeArray;//stores serials of edge information in form: start node, end nodeId, cost
//    private int[] nodeArray;// mapping: nodeId -> index of edgeArray for that node.
//    private double minLat = Double.MAX_VALUE;
//    private double maxLat = Double.MIN_VALUE;
//    private double minLongi = Double.MAX_VALUE;
//    private double maxLongi = Double.MIN_VALUE;
//
//    /**
//     * Constructor of the class Graph
//     *
//     * @param path - path of the graph file(with suffix .fmi or .txt), has to be valid path.
//     */
//    public Graph(String path) {
//        System.out.println();
//        System.out.print("Reading file ");
//        long startTime = System.currentTimeMillis();
//        readGraphFile(path);
//        long endTime = System.currentTimeMillis();
//        long totalTime = endTime - startTime;
//        System.out.println();
//        System.out.println("Reading file took [" + totalTime + "] milliseconds");
//    }
//
//    /**
//     * main function of read graph information from the file.
//     *
//     * @param path
//     */
//    private void readGraphFile(String path) {
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(path));
//            String line;
//            line = br.readLine();
//            while (line.startsWith("#")) {//read and ignore the comments and one blank line
//                System.out.println(line);
//                line = br.readLine();
//            }
//            //reading graph data
//            nodeNr = Integer.parseInt(line);// read the number of nodes
//            line = br.readLine();
//            edgeNr = Integer.parseInt(line);// read the number of edges
//            latitude = new double[nodeNr];//initialize latitude array
//            longitude = new double[nodeNr];//initialize longitude array
//            nrOfOutgoingEdges = new int[nodeNr];//init
//            edgeArray = new int[edgeNr * lengthOfEdgeElement];//init
//            nodeArray = new int[nodeNr];//init
//            line = br.readLine();//skip the empty line
//            // initialize nodeArray and fill lat long array
//            for (int i = 0; i < nodeNr; i++) {// read and save node information node by node(line by line).
//                if (i % 1000000 == 0) {
//                    System.out.print("#");//loading bar.
//                }
//                line = br.readLine();
//                String[] tempString = line.split(" ");// parse node information
//                latitude[i] = Double.valueOf(tempString[2]);//store latitude of node i
//                if(latitude[i] < minLat){//save the minimum of latitude
//                    minLat = latitude[i];
//                }
//                if(latitude[i] > maxLat){//save the maximum of latitude
//                    maxLat = latitude[i];
//                }
//                longitude[i] = Double.valueOf(tempString[3]);//store longitude of node i
//                if(longitude[i] < minLongi){//save the minimum of longitude
//                    minLongi = longitude[i];
//                }
//                if(longitude[i] > maxLongi){//save the maximum of longitude
//                    maxLat = longitude[i];
//                }
//                nodeArray[i] = -1;//initialize with value -1
//            }
//            // fill edgeArray
//            int index = 0;
//            for (int i = 0; i < edgeNr; i++) {// read and save edge information edge by edge(line by line).
//                if (i % 1000000 == 0) {
//                    System.out.print("#");
//                }
//                line = br.readLine();
//                String[] tempStringArray = line.split(" ");
//                edgeArray[index] = Integer.parseInt(tempStringArray[0]);//store start node of edge i
//                index++;
//                edgeArray[index] = Integer.parseInt(tempStringArray[1]);//store end node of edge i
//                index++;
//                edgeArray[index] = Integer.parseInt(tempStringArray[2]);//store cost of edge i
//                index++;
//            }
//
//            br.close();
//            //fill nodeArray and nrOfoutgoingEdges array.
//            for (int i = 0; i < edgeNr; i++) {
//                if (nodeArray[edgeArray[i * lengthOfEdgeElement]] == -1) {//if the start if edge i is not in nodeArray initialized
//                    nodeArray[edgeArray[i * lengthOfEdgeElement]] = i * lengthOfEdgeElement;//then save the index of edgeArray in nodeArray
//                }
//                nrOfOutgoingEdges[edgeArray[i * lengthOfEdgeElement]] += 1;//count the number of outgoing edge for start node of every edge.
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    int[] getEdgeArray() {
//        return edgeArray;
//    }
//
//    int[] getNodeArray() {
//        return nodeArray;
//    }
//
//    int getNrOfOutgoingEdges(int nodeID) {
//        return nrOfOutgoingEdges[nodeID];
//    }
//
//    int[] getOutgingEdgesArray(int nodeID) {
//        if (nrOfOutgoingEdges[nodeID] >= 1) {
//            int startIndex = nodeArray[nodeID];
//            int endIndex = getNrOfOutgoingEdges(nodeID) * lengthOfEdgeElement + startIndex;
//            return Arrays.copyOfRange(edgeArray, startIndex, endIndex);
//        }
//        return null;
//    }
//
//    public int getNodeNr() {
//        return nodeNr;
//    }
//
//    int getEdgeNr() {
//        return edgeNr;
//    }
//
//    public double getLatitude(int nodeID) {
//        return latitude[nodeID];
//    }
//
//    public double getLongitude(int nodeID) {
//        return longitude[nodeID];
//    }
//
//    /**
//     * @param nodeID
//     */
//    public int[] getOutgoingEdgesArrayIndex(int nodeID) {
//        if (nrOfOutgoingEdges[nodeID] >= 1) {
//            int startIndex = nodeArray[nodeID];
//            int endIndex = getNrOfOutgoingEdges(nodeID) * lengthOfEdgeElement + startIndex;
//            int[] startAndEnd = {startIndex, endIndex};
//            return startAndEnd;
//        }
//        return null;
//    }
//
//
//    public int maxNrOfOutgoingEdges() {
//        int max = 0;
//        for (int i = 0; i < nodeNr; i++) {
//            if (getNrOfOutgoingEdges(i) > max) {
//                max = getNrOfOutgoingEdges(i);
//            }
//        }
//        return max;
//    }
//
//    private double distanceBetween(double lat,double longi , int node){
//        return Math.sqrt(Math.pow((lat-latitude[node]),2)+Math.pow((longi-longitude[node]),2));
//    }
//
//    public int nearestNeighbour(double lat, double longi){
//        double min = Double.MAX_VALUE;
//        int nearestNeighbour = -1;
//        for(int i = 0; i < nodeNr; i++){
//            if(distanceBetween(lat, longi, i)< min){
//                nearestNeighbour = i;
//                min = distanceBetween(lat, longi, i);
//            }
//        }
//        return nearestNeighbour;
//    }
//
//    public double getMinLat(){
//        return minLat;
//    }
//
//    public double getMaxLat() {
//        return maxLat;
//    }
//
//    public double getMinLongi(){
//        return minLongi;
//    }
//
//    public double getMaxLongi(){
//        return maxLongi;
//    }
//}



/**
 * read static Graph date from File represent it as Arrays
 *
 *
 */
public class Graph {
    private int nodeNr;//number of the nodes
    private int edgeNr;//number of the edges
    private int lengthOfEdgeElement = 5;//Edge Element consist of start nodeID, endNodeID , metric vector and two id of subedges of shortcut
    private double[] latitude;//mapping: nodeId -> latitude of that node
    private double[] longitude;//mapping: nodeId -> longitude of that node
    private int[] nrOfOutgoingEdgesUpward;//mapping: nodeId -> number of outgoing edges of that node
    private int[] nrOfOutgoingEdgesDownward;
    private int[] nrOfOutgoingEdges;
    //private int[] edgeArray;//stores serials of edge information in form: start node, end nodeId, cost
    private int[] nodeArray;
    private int[] upwardNodeArray;// mapping: nodeId -> index of edgeArray for that node.
    private int[] downwardNodeArray;
    private int[] nodeLevel;// mapping: nodeId -> node level
    private int[] upwardEdgeArray;
    private int[] downwardEdgeArray;
    private int[] edgeArray;
    private int nrOfUpwardEdge;
    private int nrOfDownwardEdge;
    private int[] nodeInCornerCase;

    /**
     * Constructor of the class Graph
     *
     * @param path - path of the graph file(with suffix .fmi or .txt or .graph), has to be valid path.
     */
    public Graph(String path) {
        System.out.println();
        System.out.print("Reading file ");
        long startTime = System.currentTimeMillis();
        readGraphFile(path);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println();
        System.out.println("Reading file took [" + totalTime + "] milliseconds");
    }

    /**
     * main function of read graph information from the file.
     *
     * @param path
     */
    private void readGraphFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while (br.readLine().startsWith("#")) {//read and ignore the comments and one blank line
                //empty body
            }
            //reading graph data
            nodeNr = Integer.parseInt(br.readLine());// read the number of nodes
            edgeNr = Integer.parseInt(br.readLine());// read the number of edges
            latitude = new double[nodeNr];//initialize latitude array
            longitude = new double[nodeNr];//initialize longitude array
            nrOfOutgoingEdgesUpward = new int[nodeNr];//init
            nrOfOutgoingEdgesDownward = new int[nodeNr];
            nrOfOutgoingEdges = new int[nodeNr];
            nodeArray = new int[nodeNr];
            upwardNodeArray = new int[nodeNr];//init
            downwardNodeArray = new int[nodeNr];
            edgeArray = new int[edgeNr * lengthOfEdgeElement];
            nodeLevel = new int[nodeNr];//init
            nodeInCornerCase = new int[nodeNr];

            String line;
            // initialize nodeArray and fill lat long array, the level of the nodes are stored as well.
            for (int i = 0; i < nodeNr; i++) {// read and save node information node by node(line by line).
                if (i % 1000000 == 0) {
                    System.out.print("#");//loading bar.
                }
                line = br.readLine();
                String[] tempString = line.split(" ");// parse node information
                latitude[i] = Double.valueOf(tempString[2]);//store latitude of node i
                longitude[i] = Double.valueOf(tempString[3]);//store longitude of node i
                nodeInCornerCase[i] = Integer.valueOf(tempString[4]);// store whether the node is has outgoing edges across the bounding box
                nodeLevel[i] = Integer.valueOf(tempString[5]);// store node level of node i
                upwardNodeArray[i] = -1;
                downwardNodeArray[i] = -1;
                nodeArray[i] = -1;

            }
            //count number of up and downward edge:
            int index = 0;
            nrOfUpwardEdge = 0;
            nrOfDownwardEdge = 0;
            int[] count = new int[nodeNr];
            for (int i = 0; i < count.length; i++) {
                count[i] = 0;
            }
            for (int i = 0; i < edgeNr; i++) {// read and save edge information edge by edge(line by line).
                if (i % 1000000 == 0) {
                    System.out.print("#");
                }
                line = br.readLine();
                String[] tempStringArray = line.split(" ");
                int start = Integer.parseInt(tempStringArray[0]);//store start node of edge i
                int end = Integer.parseInt(tempStringArray[1]);//store end node of edge i
                int cost = Integer.parseInt(tempStringArray[2]);//store cost of edge i;
                int firstSubEdgeId = Integer.parseInt(tempStringArray[3]);//store first edgeId of the shortcut
                int secondSubEdgeId = Integer.parseInt(tempStringArray[4]);//store second edgeId of the shortcut
                if (nodeLevel[start] < nodeLevel[end]) {
                    nrOfUpwardEdge++;
                } else {
                    nrOfDownwardEdge++;
                    count[end]++;
                }
                edgeArray[index] = start;
                index++;
                edgeArray[index] = end;
                index++;
                edgeArray[index] = cost;
                index++;
                edgeArray[index] = firstSubEdgeId;
                index++;
                edgeArray[index] = secondSubEdgeId;
                index++;
                //System.out.println("start:" + start + " end: " + end + " cost: " + cost + " firstSubE: " + firstSubEdgeId + " secondSubE: " + secondSubEdgeId);
            }

            int[] startIndexOfDownwardsEdge = new int[nodeNr];
            for (int i = 0; i < startIndexOfDownwardsEdge.length; i++) {
                startIndexOfDownwardsEdge[i] = 0;
            }
            for (int i = 1; i < nodeNr; i++) {
                startIndexOfDownwardsEdge[i] = startIndexOfDownwardsEdge[i - 1] + count[i - 1];
            }
            for (int i = 0; i < startIndexOfDownwardsEdge.length; i++) {
                startIndexOfDownwardsEdge[i] *= lengthOfEdgeElement;
            }

            upwardEdgeArray = new int[nrOfUpwardEdge * lengthOfEdgeElement];
            downwardEdgeArray = new int[nrOfDownwardEdge * lengthOfEdgeElement];
            br.close();
            // read the file again and fill upwardEdgeArray and backwardEdgeArray.
            br = new BufferedReader(new FileReader(path));
            while (br.readLine().startsWith("#")) {//read and ignore the comments and one blank line
                //empty body
            }
            br.readLine();//skip node number
            br.readLine();//skip edge number
            for (int i = 0; i < nodeNr; i++) {//skip node information.
                br.readLine();
            }
            int upwardIndex = 0;
            int downwardIndex = 0;
            for (int i = 0; i < edgeNr; i++) {// read and save edge information edge by edge(line by line).
                if (i % 1000000 == 0) {
                    System.out.print("#");
                }
                line = br.readLine();
                String[] tempStringArray = line.split(" ");
                int start = Integer.parseInt(tempStringArray[0]);//store start node of edge i
                int end = Integer.parseInt(tempStringArray[1]);//store end node of edge i
                int cost = Integer.parseInt(tempStringArray[2]);//store cost of edge i;
                int firstSubEdgeId = Integer.parseInt(tempStringArray[3]);//store first edgeId of the shortcut
                int secondSubEdgeId = Integer.parseInt(tempStringArray[4]);//store second edgeId of the shortcut
                if (nodeLevel[start] < nodeLevel[end]) {//store the edge information in upwardEdgeArray.
                    upwardEdgeArray[upwardIndex] = start;
                    upwardIndex++;
                    upwardEdgeArray[upwardIndex] = end;
                    upwardIndex++;
                    upwardEdgeArray[upwardIndex] = cost;
                    upwardIndex++;
                    upwardEdgeArray[upwardIndex] = firstSubEdgeId;
                    upwardIndex++;
                    upwardEdgeArray[upwardIndex] = secondSubEdgeId;
                    upwardIndex++;
                } else {//store the edge information in downwardEdgeArray.
                    downwardIndex = startIndexOfDownwardsEdge[end];
                    downwardEdgeArray[downwardIndex] = end;
                    downwardIndex++;
                    downwardEdgeArray[downwardIndex] = start;
                    downwardIndex++;
                    downwardEdgeArray[downwardIndex] = cost;
                    downwardIndex++;
                    downwardEdgeArray[downwardIndex] = firstSubEdgeId;
                    downwardIndex++;
                    downwardEdgeArray[downwardIndex] = secondSubEdgeId;
                    downwardIndex++;
                    startIndexOfDownwardsEdge[end] += lengthOfEdgeElement;
                }
            }

            br.close();
            //fill nodeArray and nrOfoutgoingEdges array.
            for (int i = 0; i < nrOfUpwardEdge; i++) {
                if (upwardNodeArray[upwardEdgeArray[i * lengthOfEdgeElement]] == -1) {//if the start of edge i is not in nodeArray initialized
                    upwardNodeArray[upwardEdgeArray[i * lengthOfEdgeElement]] = i * lengthOfEdgeElement;//then save the index of edgeArray in nodeArray
                }
                nrOfOutgoingEdgesUpward[upwardEdgeArray[i * lengthOfEdgeElement]] += 1;//count the number of outgoing edge for start node of every edge.
            }
            for (int i = 0; i < nrOfDownwardEdge; i++) {
                if (downwardNodeArray[downwardEdgeArray[i * lengthOfEdgeElement]] == -1) {//if the start of edge i is not in nodeArray initialized
                    downwardNodeArray[downwardEdgeArray[i * lengthOfEdgeElement]] = i * lengthOfEdgeElement;//then save the index of edgeArray in nodeArray
                }
                nrOfOutgoingEdgesDownward[downwardEdgeArray[i * lengthOfEdgeElement]] += 1;//count the number of outgoing edge for start node of every edge.
            }
            for (int i = 0; i < edgeNr; i++) {

                if (nodeArray[edgeArray[i * lengthOfEdgeElement]] == -1) {//if the start of edge i is not in nodeArray initialized
                    nodeArray[edgeArray[i * lengthOfEdgeElement]] = i * lengthOfEdgeElement;//then save the index of edgeArray in nodeArray
                }
                nrOfOutgoingEdges[edgeArray[i * lengthOfEdgeElement]] += 1;//count the number of outgoing edge for start node of every edge.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int[] getEdge(int edgeId) {
        return Arrays.copyOfRange(edgeArray, edgeId * lengthOfEdgeElement, edgeId * lengthOfEdgeElement + lengthOfEdgeElement);
    }

    int getEdgeId(int startIndex) {
        return startIndex / lengthOfEdgeElement;
    }

    int[] getUpwardEdgeArray() {
        return upwardEdgeArray;
    }

    int[] getDownwardEdgeArray() {
        return downwardEdgeArray;
    }

    int[] getEdgeArray() {
        return edgeArray;
    }

    int[] getUpwardNodeArray() {
        return upwardNodeArray;
    }

    int[] getDowwardNodeArray() {
        return downwardNodeArray;
    }

    int[] getNodeArray() {
        return nodeArray;
    }

    int getNrOfOutgoingEdgesUpward(int nodeID) {
        return nrOfOutgoingEdgesUpward[nodeID];
    }

    int getNrOfOutgoingEdgesDownward(int nodeID) {
        return nrOfOutgoingEdgesDownward[nodeID];
    }

    int getLengthOfEdgeElement() {
        return lengthOfEdgeElement;
    }


    public int getNodeNr() {
        return nodeNr;
    }

    int getEdgeNr() {
        return edgeNr;
    }

    public double getLatitude(int nodeID) {
        return latitude[nodeID];
    }

    public double getLongitude(int nodeID) {
        return longitude[nodeID];
    }

    /**
     * @param nodeID
     */
    public int[] getOutgoingEdgesArrayUpwardIndex(int nodeID) {
        if (nrOfOutgoingEdgesUpward[nodeID] >= 1) {
            int startIndex = upwardNodeArray[nodeID];
            int endIndex = getNrOfOutgoingEdgesUpward(nodeID) * lengthOfEdgeElement + startIndex;
            int[] startAndEnd = {startIndex, endIndex};
            return startAndEnd;
        }
        return null;
    }

    public int[] getOutgoingEdgesArrayDownwardIndex(int nodeID) {
        if (nrOfOutgoingEdgesDownward[nodeID] >= 1) {
            int startIndex = downwardNodeArray[nodeID];
            int endIndex = getNrOfOutgoingEdgesDownward(nodeID) * lengthOfEdgeElement + startIndex;
            int[] startAndEnd = {startIndex, endIndex};
            return startAndEnd;
        }
        return null;
    }

    public int[] getOutgoingEdgesArrayIndex(int nodeID) {
        if (nrOfOutgoingEdges[nodeID] >= 1) {
            int startIndex = nodeArray[nodeID];
            int endIndex = nrOfOutgoingEdges[nodeID] * lengthOfEdgeElement + startIndex;
            int[] startAndEnd = {startIndex, endIndex};
            return startAndEnd;
        }
        return null;
    }
}

