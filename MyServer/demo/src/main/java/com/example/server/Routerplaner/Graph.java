package com.example.server.Routerplaner;

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
	 * @param path - path of the graph file(with suffix .fmi or .txt), has to be valid path.
	 * 
	 */
	public Graph(String path) {
		System.out.println();
		System.out.print("Reading file ");
		long startTime = System.currentTimeMillis();
		readGraphFile(path);
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println();
		System.out.println("Reading file took [" + totalTime +"] milliseconds");
	}
	/**
	 * main function of read graph information from the file.
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
			nrOfOutgoingEdges = new int[nodeNr];//init
			edgeArray = new int[edgeNr * lengthOfEdgeElement];//init
			nodeArray = new int[nodeNr];//init
			String line;
			// initialize nodeArray and fill lat long array
			for (int i = 0; i < nodeNr; i++) {// read and save node information node by node(line by line).
				if (i % 1000000 == 0){
					System.out.print("#");//loading bar.
				}
				line = br.readLine();
				String[] tempString = line.split(" ");// parse node information
				latitude[i] = Double.valueOf(tempString[2]);//store latitude of node i
				longitude[i] = Double.valueOf(tempString[3]);//store longitude of node i
				nodeArray[i] = -1;//initialize with value -1
			}
			// fill edgeArray
			int index = 0;
			for (int i = 0; i < edgeNr; i++) {// read and save edge information edge by edge(line by line).
				if (i % 1000000 == 0){
					System.out.print("#");
				}
				line = br.readLine();
				String[] tempStringArray = line.split(" ");
				edgeArray[index] = Integer.parseInt(tempStringArray[0]);//store start node of edge i
				index++;
				edgeArray[index] = Integer.parseInt(tempStringArray[1]);//store end node of edge i
				index++;
				edgeArray[index] = Integer.parseInt(tempStringArray[2]);//store cost of edge i
				index++;
				// edgeArray[index] = Integer.parseInt(tempStringArray[5]);//store cost of edge i
				// index++;
				// edgeArray[index] = Integer.parseInt(tempStringArray[6]);//store cost of edge i
				// index++;
			}
			
			br.close();
			//fill nodeArray and nrOfoutgoingEdges array.
			for (int i = 0; i < edgeNr; i++) {
				if (nodeArray[edgeArray[i * lengthOfEdgeElement]] == -1) {//if the start if edge i is not in nodeArray initialized
					nodeArray[edgeArray[i * lengthOfEdgeElement]] = i * lengthOfEdgeElement;//then save the index of edgeArray in nodeArray
				}
				nrOfOutgoingEdges[edgeArray[i * lengthOfEdgeElement]] += 1;//count the number of outgoing edge for start node of every edge.
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	int[] getEdge(int edgeId){
        return Arrays.copyOfRange(edgeArray, edgeId * lengthOfEdgeElement, edgeId * lengthOfEdgeElement + lengthOfEdgeElement);
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
	 * 
	 * @param nodeID
	 * 
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

	public int getLengthOfEdgeElement(){
		return lengthOfEdgeElement;
	}



	public int maxNrOfOutgoingEdges(){
		int max = 0;
		for(int i = 0; i < nodeNr; i++){
			if(getNrOfOutgoingEdges(i) > max){
				max = getNrOfOutgoingEdges(i);
			}
		}
		return max;
	}
	/**
	 * Given a nodeId and four argument about the chosen area, check whether the node is in the area.
	 * @param nodeId the node to be checked.
	 * @param minLatitude	subgraph format:	(minLogi, maxLat)*--------*(maxLogi, maxLat)			^ Latitude
	 * @param maxLatitude										 |		  |								|
	 * @param minLongitude										 |		  |								|
	 * @param maxLongitude						(minLogi, minLat)*--------*(minLogi, maxLat)			.------>Longitude
	 * @return ture if the node is in the area, else false.
	 */
	public boolean nodeInSubgraph(final int nodeId,final double minLatitude, final double maxLatitude, final double minLongitude, final double maxLongitude){
		boolean inLongitudeInterval = false;//whether the chosen node is in subgraph w.r.t. longitude
		boolean inLatitudeInterval = false;//whether the chosen node is in subgraph w.r.t. latitude
		//w.r.t. longitude:
		//case 1: subgraph does not cross the 0 grad longitude
		if(maxLongitude >= minLongitude){
			if(longitude[nodeId] >= minLongitude && longitude[nodeId] <= maxLongitude){
				inLongitudeInterval = true;
			}else{
				return false;//not in subgraph w.r.t. longitude
			}
		}else{
		//case 2: subgraph does cross the 0 grad longitude
			if(longitude[nodeId] >= minLongitude || longitude[nodeId] <= maxLongitude){
				inLongitudeInterval = true;
			}else{
				return false;//not in subgraph w.r.t. longitude
			}
		}

		//w.r.t. latitude:
		if(latitude[nodeId] >= minLatitude && latitude[nodeId] <= maxLatitude){
			inLatitudeInterval = true;
		}else{
			return false;
		}
		//if a node is in subgraph w.r.t. both longitude and latitude, then the node is indeed in the subgraph
		return inLongitudeInterval && inLatitudeInterval;
	}


	/**
	 * Given four argumens of the subgraph, compute and return the relevant nodes and edge information of the subgraph.
	 * @param minLatitude see method "nodeInSubgraph"
	 * @param maxLatitude
	 * @param minLongitude
	 * @param maxLongitude
	 * @return the String representation of the subgraph
	 */
	public String calculateSubgraph(final double minLatitude, final double maxLatitude, final double minLongitude, final double maxLongitude){
		String subgraphString = "\n";//the subgraph String starts with a blank line, for consistency reason.
		System.out.print("calculating subgraph:");
		// check all nodes whether they are part of the subgraph:
		int newNodeId = 0;//new nodeId of subgraph
		HashMap<Integer,Integer> newIdOf = new HashMap<Integer,Integer>();//mapping: oldNodeId -> newNodeId
		for(int i = 0; i<nodeNr;i++){
			if (i % 1000000 == 0){
				System.out.print("#");//loading bar for nodes.
			}
			if(nodeInSubgraph(i, minLatitude, maxLatitude, minLongitude, maxLongitude)){
				//node information format: oldId, newId, latitude, longitude. separate with one space
				subgraphString = subgraphString + Integer.toString(i) + " " + Integer.toString(newNodeId) + " " + Double.toString(getLatitude(i)) + " " + Double.toString(getLongitude(i)) + "\n";
				//save the new id of nodes in subgraph.
				newIdOf.put(i, newNodeId);
				//next new id of the node in subgraph, will be invalid(off by +1) after the last for-iteration
				newNodeId++;
			}
		}

		//check all edges, whether they are part of the subgraph:(an edge is in the subgraph iff both of its ends are in the subgraph)
		int edgeCounter = 0;
		for (int i = 0; i < edgeArray.length; i+=lengthOfEdgeElement) {
			if (i % 1000000 == 0){
				System.out.print("$");//loading bar for edges.
			}
			if(nodeInSubgraph(edgeArray[i], minLatitude, maxLatitude, minLongitude, maxLongitude) && nodeInSubgraph(edgeArray[i+1], minLatitude, maxLatitude, minLongitude, maxLongitude)){
				//count the current edge as edges in subgraph
				edgeCounter++;
				int startNode = edgeArray[i];
				int endNode = edgeArray[i+1];
				//edge information format: newId of startNode, newId of endNode, cost. separate with one space
				subgraphString = subgraphString + Integer.toString(newIdOf.get(startNode)) + " " + Integer.toString(newIdOf.get(endNode)) + " " + Integer.toString(edgeArray[i+2]) + "\n";
			}
		}

		//add number of nodes and number of edges at begin of the string.
		subgraphString = "#\n#This is the subgraph. \n" + Integer.toString(newNodeId) + "\n" + Integer.toString(edgeCounter) + "\n" + subgraphString;
		// try {
		// 	Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }
		return subgraphString;
	}
	
}
