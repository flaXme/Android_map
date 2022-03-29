package com.example.server.Routerplaner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.StreamSupport;


/**
 * read static Graph date from File represent it as Arrays
 * 
 * 
 */
public class GraphWithCH {
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
	private double minLatOfGraph = Double.MAX_VALUE;
	private double maxLatOfGraph = Double.MIN_VALUE;
	private double minLongOfGraph = Double.MAX_VALUE;
	private double maxLongOfGraph = Double.MIN_VALUE;
	private int maxLevel = -1;
	
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
			nrOfOutgoingEdges = new int[nodeNr];
			nodeArray = new int[nodeNr];
			upwardNodeArray = new int[nodeNr];//init
			downwardNodeArray = new int[nodeNr];
			edgeArray = new int[edgeNr * lengthOfEdgeElement];
            nodeLevel = new int[nodeNr];//init

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
				if(nodeLevel[i] > maxLevel){
					maxLevel = nodeLevel[i];
				}
				if(latitude[i] < minLatOfGraph){
					minLatOfGraph = latitude[i];
				}
				if(latitude[i] > maxLatOfGraph){
					maxLatOfGraph = latitude[i];
				}
				if(longitude[i] < minLongOfGraph){
					minLongOfGraph = longitude[i];
				}
				if(longitude[i] > maxLongOfGraph){
					maxLongOfGraph = longitude[i];
				}
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
				startIndexOfDownwardsEdge[i] = startIndexOfDownwardsEdge[i-1] + count[i-1];
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

	public int getNodeLevel(int nodeId){
		return nodeLevel[nodeId];
	}

	public int getMaxLevel(){
		return maxLevel;
	}

	public double getMinLatOfGraph(){
		return this.minLatOfGraph;
	}

	public double getMaxLatOfGraph(){
		return this.maxLatOfGraph;
	}

	public double getMinLongOfGraph(){
		return this.minLongOfGraph;
	}

	public double getMaxLongOfGraph(){
		return this.maxLongOfGraph;
	}

    int[] getEdge(int edgeId){
        return Arrays.copyOfRange(edgeArray, edgeId * lengthOfEdgeElement, edgeId * lengthOfEdgeElement + lengthOfEdgeElement);
    }

	int getEdgeId(int startIndex){
		return startIndex / lengthOfEdgeElement;
	}

	int[] getUpwardEdgeArray() {
		return upwardEdgeArray;
	}

	int[] getDownwardEdgeArray(){
		return downwardEdgeArray;
	}

	int[] getEdgeArray(){
		return edgeArray;
	}

	int[] getUpwardNodeArray() {
		return upwardNodeArray;
	}

	int[] getDowwardNodeArray(){
		return downwardNodeArray;
	}

	int[] getNodeArray(){
		return nodeArray;
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

	public int[] getOutgoingEdgesArrayIndex(int nodeID) {
		if (nrOfOutgoingEdges[nodeID] >= 1) {
			int startIndex = nodeArray[nodeID];
			int endIndex = nrOfOutgoingEdges[nodeID] * lengthOfEdgeElement + startIndex;
			int[] startAndEnd = {startIndex, endIndex};
			return startAndEnd;
		}
		return null;
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
	 * recuresively determine whether an edge is in the subgraph
	 * @param edgeId
	 * @param minLatitude
	 * @param maxLatitude
	 * @param minLongitude
	 * @param maxLongitude
	 * @return
	 */
	public boolean edgeInSubgraph(final int edgeId,final double minLatitude, final double maxLatitude, final double minLongitude, final double maxLongitude){
		int[] edge = getEdge(edgeId);//edge is in the format: [startId, endId, cost, firstSubedgeId, secondSubedegeId]
		int start = edge[0];
		int end = edge[1];
		int firstSubEdge = edge[3];
		int secondSubEdge = edge[4];
		if(!nodeInSubgraph(start, minLatitude, maxLatitude, minLongitude, maxLongitude) || !nodeInSubgraph(end, minLatitude, maxLatitude, minLongitude, maxLongitude)){
			return false;
		}else if(firstSubEdge == -1 ){
			return true;
		}else{
			return edgeInSubgraph(firstSubEdge, minLatitude, maxLatitude, minLongitude, maxLongitude) && edgeInSubgraph(secondSubEdge, minLatitude, maxLatitude, minLongitude, maxLongitude);
		}
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
		StringBuilder subgraph = new StringBuilder(1);
		subgraph.append("");
		System.out.print("calculating subgraph:");
		// check all nodes whether they are part of the subgraph:
		int newNodeId = 0;//new nodeId of subgraph
		HashMap<Integer,Integer> newIdOfNode = new HashMap<Integer,Integer>();//mapping: oldNodeId -> newNodeId
		for(int i = 0; i<nodeNr;i++){
			if (i % 1000000 == 0){
				System.out.print("#");//loading bar for nodes.
			}
			if(nodeInSubgraph(i, minLatitude, maxLatitude, minLongitude, maxLongitude)){
				//check whether the node has outgoing edges that goes across the bounding box
				int oldNrOfOutgoingEdges = nrOfOutgoingEdges[i];//the number of original outgoing edges
				//node information format: oldId, newId, latitude, longitude, cornerCase, level, separate with one space
				subgraph.append(Integer.toString(i) + " " + Integer.toString(newNodeId) + " " + Double.toString(getLatitude(i)) + " " + Double.toString(getLongitude(i)) + " " + Integer.toString(oldNrOfOutgoingEdges) + " " + Integer.toString(nodeLevel[i]) + "\n");
				//save the new id of nodes in subgraph.
				newIdOfNode.put(i, newNodeId);
				//next new id of the node in subgraph, will be invalid(off by +1) after the last for-iteration
				newNodeId++;
			}
		}

		//check all edges, whether they are part of the subgraph:(an edge is in the subgraph iff both of its ends are in the subgraph and all its subedges are in the subgraph)
		int edgeCounter = 0;
		LinkedHashMap<Integer, Integer> newIdOfEdge = new LinkedHashMap<>();//mapping: old edge id -> new edge id
		//go through the edge array and recored the new edge ids
		for (int i = 0; i < edgeArray.length; i+=lengthOfEdgeElement) {
			if (i % 1000000 == 0){
				System.out.print("$");//loading bar for edges.
			}
			int edgeId = getEdgeId(i);
			if(edgeInSubgraph(edgeId, minLatitude, maxLatitude, minLongitude, maxLongitude)){
				//System.out.println("edge " + edgeId + " is in the subgraph.");
				newIdOfEdge.put(edgeId, edgeCounter);
				//count the number of edge in subgraph
				edgeCounter++;
			}
		}
		//go through the edge array again and copy the edges in the bounding box.
		for (Integer i : newIdOfEdge.keySet()) {
			int[] edgeArray = getEdge(i);	
			int startNode = edgeArray[0];
			int endNode = edgeArray[1];
			int cost = edgeArray[2];
			int firstSubEdgeId;
			int secondSubedegeId;
			if(edgeArray[3] != -1){
				firstSubEdgeId = newIdOfEdge.get(edgeArray[3]);
				secondSubedegeId = newIdOfEdge.get(edgeArray[4]);
			}else{
				firstSubEdgeId = -1;
				secondSubedegeId = -1;
			}
			//edge information format: newId of startNode, newId of endNode, cost. id1, id2 separate with one space
			subgraph.append(Integer.toString(newIdOfNode.get(startNode)) + " " + Integer.toString(newIdOfNode.get(endNode)) + " " + Integer.toString(cost) + " " + Integer.toString(firstSubEdgeId) + " " + Integer.toString(secondSubedegeId) + "\n");
		}

		//add number of nodes and number of edges at begin of the string.
		subgraph.insert(0, "#\n#This is the subgraph. \n\n" + Integer.toString(newNodeId)  + "\n" + Integer.toString(edgeCounter) + "\n");
		System.out.println("Computing subgraph finished!");
		return subgraph.toString();
	}
}

