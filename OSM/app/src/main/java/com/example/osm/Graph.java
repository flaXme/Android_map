package com.example.osm;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


/**
 * read static Graph date from File represent it as Arrays
 *
 *
 */
public class Graph {
    private int nodeNr;//number of the nodes
    private int edgeNr;//number of the edges
    private int lengthOfEdgeElement = 3;//Edge Element consist of endNode and metric vector
    private double[] latitude;//mapping: nodeId -> latitude of that node
    private double[] longitude;//mapping: nodeId -> longitude of that node
    private int[] nrOfOutgoingEdges;//mapping: nodeId -> number of outgoing edges of that node
    private int[] edgeArray;//stores serials of edge information in form: start node, end nodeId, cost
    private int[] nodeArray;// mapping: nodeId -> index of edgeArray for that node.

    /**
     * Constructor of the class Graph
     *
     * @param file - path of the graph file(with suffix .fmi or .txt), has to be valid path.
     */
    public Graph(String file) {
        System.out.println();
        System.out.print("Reading file ");
        long startTime = System.currentTimeMillis();
        readGraphFile(file);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println();
        System.out.println("Reading file took [" + totalTime + "] milliseconds");
    }

    public Graph(){
        System.out.println("This is a Graph with no argument.");
    }

    /**
     * main function of read graph information from the file.
     *
     * @param file
     */
    private void readGraphFile(String file) {
        String[] subgrah;
        subgrah = file.split("\n");
        int currentLine = 0;
        //skip all conments
        for(String line: subgrah){
            if(line.startsWith("#")){
                currentLine++;
            }else{
                break;
            }
        }

        nodeNr = Integer.parseInt(subgrah[currentLine]);
        currentLine++;//next line
        edgeNr = Integer.parseInt(subgrah[currentLine]);
        currentLine++;
        latitude = new double[nodeNr];//initialize latitude array
        longitude = new double[nodeNr];//initialize longitude array
        nrOfOutgoingEdges = new int[nodeNr];//init
        edgeArray = new int[edgeNr * lengthOfEdgeElement];//init
        nodeArray = new int[nodeNr];//init
        //initialize nodeArray and fill lat long array

        currentLine++;
        for (int i = 0; i < nodeNr ; currentLine++){

            String[] tempString = subgrah[currentLine].split(" ");// parse node information
//            for (int j = 0; j < tempString.length; j++){
//                System.out.print(tempString[j]+", ");
//            }
//            System.out.println("length of the string array: "+tempString.length);


            latitude[i] = Double.parseDouble(tempString[2]);//store latitude of node with newId i
            longitude[i] = Double.parseDouble(tempString[3]);//store longitude of node with newId i
            nodeArray[i] = -1;//initialize with value -1
            i++;
        }
        //fill edge array
        int index = 0;
        for(int j = 0;j < edgeNr; currentLine++){
            String[] tempString = subgrah[currentLine].split(" ");
            edgeArray[index] = Integer.parseInt(tempString[0]);//store start node of edge j
            index++;
            edgeArray[index] = Integer.parseInt(tempString[1]);//store end node of edge j
            index++;
            edgeArray[index] = Integer.parseInt(tempString[2]);//store cost of edge j
            index++;
            j++;
        }

        //fill nodeArray and nrOfoutgoingEdges array.
        for (int i = 0; i < edgeNr; i++) {
            if (nodeArray[edgeArray[i * lengthOfEdgeElement]] == -1) {//if the start if edge i is not in nodeArray initialized
                nodeArray[edgeArray[i * lengthOfEdgeElement]] = i * lengthOfEdgeElement;//then save the index of edgeArray in nodeArray
            }
            nrOfOutgoingEdges[edgeArray[i * lengthOfEdgeElement]] += 1;//count the number of outgoing edge for start node of every edge.
        }


    }


    int[] getEdgeArray() {
        return edgeArray;
    }

    int[] getNodeArray() {
        return nodeArray;
    }

    int getNrOfOutgoingEdges(int nodeID) {
        return nrOfOutgoingEdges[nodeID];
    }

    int[] getOutgingEdgesArray(int nodeID) {
        if (nrOfOutgoingEdges[nodeID] >= 1) {
            int startIndex = nodeArray[nodeID];
            int endIndex = getNrOfOutgoingEdges(nodeID) * lengthOfEdgeElement + startIndex;
            return Arrays.copyOfRange(edgeArray, startIndex, endIndex);
        }
        return null;
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
    public int[] getOutgoingEdgesArrayIndex(int nodeID) {
        if (nrOfOutgoingEdges[nodeID] >= 1) {
            int startIndex = nodeArray[nodeID];
            int endIndex = getNrOfOutgoingEdges(nodeID) * lengthOfEdgeElement + startIndex;
            int[] startAndEnd = {startIndex, endIndex};
            return startAndEnd;
        }
        return null;
    }


    public int maxNrOfOutgoingEdges() {
        int max = 0;
        for (int i = 0; i < nodeNr; i++) {
            if (getNrOfOutgoingEdges(i) > max) {
                max = getNrOfOutgoingEdges(i);
            }
        }
        return max;
    }
}
