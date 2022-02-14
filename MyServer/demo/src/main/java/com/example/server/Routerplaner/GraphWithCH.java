package com.example.server.Routerplaner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.text.ExtendedMessageFormat;

/**
 * read static Graph date from File represent it as Arrays
 * 
 * 
 */
public class GraphWithCH {
	private int nodeNr;//number of the nodes
	private int edgeNr;//number of the edges
	private int lengthOfEdgeElement = 5;//Edge Element consist of start node, endNode , metric vector and two indecies of edges of shortcut
	private double[] latitude;//mapping: nodeId -> latitude of that node
	private double[] longitude;//mapping: nodeId -> longitude of that node
	private int[] nrOfOutgoingEdgesUpward;//mapping: nodeId -> number of outgoing edges of that node
	private int[] nrOfOutgoingEdgesDownward;
	//private int[] edgeArray;//stores serials of edge information in form: start node, end nodeId, cost
	private int[] upwardNodeArray;// mapping: nodeId -> index of edgeArray for that node. 
	private int[] downwardNodeArray;
    private int[] nodeLevel;// mapping: nodeId -> node level
    private boolean[] isShortcut;
	private int[] upwardEdgeArray;
	private int[] downwardEdgeArray;
	private int[] edgeArray;
	private int nrOfUpwardEdge;
	private int nrOfDownwardEdge;
	
	/**
	 * Constructor of the class Graph
	 * @param path - path of the graph file(with suffix .fmi or .txt or .graph), has to be valid path.
	 * 
	 */
	public GraphWithCH(String path) {
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
			nrOfOutgoingEdgesUpward = new int[nodeNr];//init
			nrOfOutgoingEdgesDownward = new int[nodeNr];
			//edgeArray = new int[edgeNr * lengthOfEdgeElement];//init
			upwardNodeArray = new int[nodeNr];//init
			downwardNodeArray = new int[nodeNr];
			edgeArray = new int[edgeNr * lengthOfEdgeElement];
            nodeLevel = new int[nodeNr];//init
            isShortcut = new boolean[edgeNr];//init

			String line;
			// initialize nodeArray and fill lat long array, the level of the nodes are stored as well.
			for (int i = 0; i < nodeNr; i++) {// read and save node information node by node(line by line).
				if (i % 1000000 == 0){
					System.out.print("#");//loading bar.
				}
				line = br.readLine();
				String[] tempString = line.split(" ");// parse node information
				latitude[i] = Double.valueOf(tempString[2]);//store latitude of node i
				longitude[i] = Double.valueOf(tempString[3]);//store longitude of node i
                nodeLevel[i] = Integer.valueOf(tempString[5]);// store node level of node i
                upwardNodeArray[i] = -1;
				downwardNodeArray[i] = -1;
			}
            //count number of up and downward edge:
			int index = 0;
			nrOfUpwardEdge = 0;
			nrOfDownwardEdge = 0;
			for (int i = 0; i < edgeNr; i++) {// read and save edge information edge by edge(line by line).
				if (i % 1000000 == 0){
					System.out.print("#");
				}
				line = br.readLine();
				String[] tempStringArray = line.split(" ");
				int start = Integer.parseInt(tempStringArray[0]);//store start node of edge i
				int end = Integer.parseInt(tempStringArray[1]);//store end node of edge i
				int cost = Integer.parseInt(tempStringArray[2]);//store cost of edge i;
				int firstSubEdgeId = Integer.parseInt(tempStringArray[5]);//store first edgeId of the shortcut
				int secondSubEdgeId = Integer.parseInt(tempStringArray[6]);//store second edgeId of the shortcut
				if(nodeLevel[start] < nodeLevel[end]){
					nrOfUpwardEdge++;
				}else{
					nrOfDownwardEdge++;
				}
                if(firstSubEdgeId != -1){
                    isShortcut[i] = true;
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
				if (i % 1000000 == 0){
					System.out.print("#");
				}
				line = br.readLine();
				String[] tempStringArray = line.split(" ");
				int start = Integer.parseInt(tempStringArray[0]);//store start node of edge i
				int end = Integer.parseInt(tempStringArray[1]);//store end node of edge i
				int cost = Integer.parseInt(tempStringArray[2]);//store cost of edge i;
				int firstSubEdgeId = Integer.parseInt(tempStringArray[5]);//store first edgeId of the shortcut
				int secondSubEdgeId = Integer.parseInt(tempStringArray[6]);//store second edgeId of the shortcut
				if(nodeLevel[start] < nodeLevel[end]){//store the edge information in upwardEdgeArray.
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
				}else{//store the edge information in downwardEdgeArray.
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
				}
			}
			reorderEdgeArray();
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    int[] getEdge(int edgeId){
        return Arrays.copyOfRange(edgeArray, edgeId * lengthOfEdgeElement, edgeId * lengthOfEdgeElement + lengthOfEdgeElement);
    }

	int[] getUpwardEdgeArray() {
		return upwardEdgeArray;
	}

	int[] getDownwardEdgeArray(){
		return downwardEdgeArray;
	}

	int[] getUpwardNodeArray() {
		return upwardNodeArray;
	}

	int[] getDowwardNodeArray(){
		return downwardNodeArray;
	}

	int getNrOfOutgoingEdgesUpward(int nodeID) {
		return nrOfOutgoingEdgesUpward[nodeID];
	}

	int getNrOfOutgoingEdgesDownward(int nodeID) {
		return nrOfOutgoingEdgesDownward[nodeID];
	}
	
    int getLengthOfEdgeElement(){
        return lengthOfEdgeElement;
    }

	void swapEdgeInformation(int indexOne, int indexTwo){
		int tempStart = downwardEdgeArray[indexOne];
		int tempEnd = downwardEdgeArray[indexOne + 1];
		int tempCost = downwardEdgeArray[indexOne + 2];
		int tempFistSubedgeId = downwardEdgeArray[indexOne + 3];
		int tempSecondSubedgeId = downwardEdgeArray[indexTwo + 4];
		downwardEdgeArray[indexOne] = downwardEdgeArray[indexTwo];
		downwardEdgeArray[indexOne + 1] = downwardEdgeArray[indexTwo + 1];
		downwardEdgeArray[indexOne + 2] = downwardEdgeArray[indexTwo + 2];
		downwardEdgeArray[indexOne + 3] = downwardEdgeArray[indexTwo + 3];
		downwardEdgeArray[indexOne + 4] = downwardEdgeArray[indexTwo + 4];
		downwardEdgeArray[indexTwo] = tempStart;
		downwardEdgeArray[indexTwo + 1] = tempEnd;
		downwardEdgeArray[indexTwo + 2] = tempCost;
		downwardEdgeArray[indexTwo + 3] = tempFistSubedgeId;
		downwardEdgeArray[indexTwo + 4] = tempSecondSubedgeId;
	}

	void reorderEdgeArray(){
		System.out.println();
		System.out.println("Reordering edges...");
		long startTime = System.currentTimeMillis();
		int indexOfNextEdge = 0;
		boolean reordered = false;
		for(int i = 0; i < downwardEdgeArray.length; i = indexOfNextEdge){
			if (i % 10000 == 0){
				System.out.print("#");
			}
			int currentStart = downwardEdgeArray[i];
			int j;
			if(!reordered){//the last node has only one outgoing edge.
				i += lengthOfEdgeElement;
				indexOfNextEdge += lengthOfEdgeElement;
			}
			reordered = false;
			for (j = indexOfNextEdge; j < downwardEdgeArray.length; j+= lengthOfEdgeElement) {
				if(downwardEdgeArray[j] != currentStart){
					continue;
				}else{
					swapEdgeInformation(indexOfNextEdge, j);
					reordered = true;
					indexOfNextEdge += lengthOfEdgeElement;
				}
			}
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		totalTime = totalTime / (1000*60);//time in minutes.
		System.out.println();
		System.out.println("Reordering downward edges took [" + totalTime +"] minutes");
	}

	// int[] getOutgingEdgesArray(int nodeID) {
	// 	if (nrOfOutgoingEdges[nodeID] >= 1) {
	// 		int startIndex = nodeArray[nodeID];
	// 		int endIndex = getNrOfOutgoingEdges(nodeID) * lengthOfEdgeElement + startIndex;
	// 		return Arrays.copyOfRange(edgeArray, startIndex, endIndex);
	// 	}
	// 	return null;
	// }

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


	// public int maxNrOfOutgoingEdges(){
	// 	int max = 0;
	// 	for(int i = 0; i < nodeNr; i++){
	// 		if(getNrOfOutgoingEdges(i) > max){
	// 			max = getNrOfOutgoingEdges(i);
	// 		}
	// 	}
	// 	return max;
	// }
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
		// int edgeCounter = 0;
		// for (int i = 0; i < edgeArray.length; i+=lengthOfEdgeElement) {
		// 	if (i % 1000000 == 0){
		// 		System.out.print("$");//loading bar for edges.
		// 	}
		// 	if(nodeInSubgraph(edgeArray[i], minLatitude, maxLatitude, minLongitude, maxLongitude) && nodeInSubgraph(edgeArray[i+1], minLatitude, maxLatitude, minLongitude, maxLongitude)){
		// 		//count the current edge as edges in subgraph
		// 		edgeCounter++;
		// 		int startNode = edgeArray[i];
		// 		int endNode = edgeArray[i+1];
		// 		//edge information format: newId of startNode, newId of endNode, cost. separate with one space
		// 		subgraphString = subgraphString + Integer.toString(newIdOf.get(startNode)) + " " + Integer.toString(newIdOf.get(endNode)) + " " + Integer.toString(edgeArray[i+2]) + "\n";
		// 	}
		// }

		//add number of nodes and number of edges at begin of the string.
		//subgraphString = "#\n#This is the subgraph. \n" + Integer.toString(newNodeId) + "\n" + Integer.toString(edgeCounter) + "\n" + subgraphString;
		// try {
		// 	Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }
		return subgraphString;
	}
	public static void main(String[] args) {
		GraphWithCH g = new GraphWithCH("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_stuttgart.txt");
		int[] upwardEdge = g.getUpwardEdgeArray();
		int[] downwardEdge = g.getDownwardEdgeArray();
		System.out.println("Upward:");
		// for (int i = 0; i < upwardEdge.length; i+=5) {
		// 	System.out.println(upwardEdge[i]+","+upwardEdge[i+1]+","+upwardEdge[i+2]+","+upwardEdge[i+3]+","+upwardEdge[i+4]);
		// }
		System.out.println("Downward:");
		for (int i = 0; i < downwardEdge.length; i+=5) {
			System.out.println(downwardEdge[i]+","+downwardEdge[i+1]+","+downwardEdge[i+2]+","+downwardEdge[i+3]+","+downwardEdge[i+4]);
		}
	}
}

