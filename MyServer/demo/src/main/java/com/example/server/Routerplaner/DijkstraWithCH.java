package com.example.server.Routerplaner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

public class DijkstraWithCH {
	int printInformation = 0;
	private int[] upwardDis;
	private int[] downwardDis;
	private int[] upwardParent;
	private int[] downwardParent;
	private GraphWithCH graph;
	private int source;
	private int target;
	private int nrOfSettledNodes;
	private boolean available = false;
	//private Set<Integer> settledNodes;
	//private Set<Integer> nodesReachedByUpward;
	//private Set<Integer> nodesReachedByDownward;
	private int middleNodeInShortestPath = -1;
	private int costOfPath = 0;
	private ArrayList<Integer> reachedNodes;
	public static boolean[] settledNodes;
	private MinHeap upwardHeap;
	private MinHeap downwardHeap;
	
	/**
	 * computes the shortest path given the parameters
	 * @param graph provided "city"
	 * @param s startnodeId
	 * @param t targetnodeId
	 *
	 */
	public DijkstraWithCH(GraphWithCH graph){
		nrOfSettledNodes= 0;
		this.upwardDis = new int[graph.getNodeNr()];
		this.downwardDis = new int[graph.getNodeNr()];
		this.upwardParent = new int[graph.getNodeNr()];
		this.downwardParent = new int[graph.getNodeNr()];
		this.graph = graph;
		this.reachedNodes = new ArrayList<Integer>();
		this.upwardHeap = new MinHeap(graph.getNodeNr());
		this.downwardHeap = new MinHeap(graph.getNodeNr());
		//settledNodes = new HashSet<>();
		//settledNodes = new boolean[graph.getNodeNr()];

		for (int i = 0; i < graph.getNodeNr(); i++) {
			upwardDis[i] = Integer.MAX_VALUE;
			downwardDis[i] = Integer.MAX_VALUE;
			upwardParent[i] = -1; // no parent
			downwardParent[i] = -1;
		}
	}

	public void computePath(int source, int target){
		boolean haltCondition = false;
		boolean settledUpward = false;
		boolean settledDownward = false;
		if(graph.getNodeLevel(source) == graph.getMaxLevel() && graph.getNodeLevel(target) == graph.getMaxLevel()){
			System.out.println("Both source and target are at the top level. No Path!");
			haltCondition = true;
		}
		this.source = source;
		this.target = target;
		upwardParent[source] = source;
		downwardParent[target] = target;
		
		upwardDis[source] = 0;
		downwardDis[target] = 0;

		//nodesReachedByUpward = new HashSet<Integer>();
		//nodesReachedByDownward = new HashSet<Integer>();
		upwardHeap.add(source, 0);
		downwardHeap.add(target, 0);
		
		
		int currentCostOfShortestPath = Integer.MAX_VALUE;
		int[] globalMin;
		while(!haltCondition) {
			settledUpward = false;
			settledDownward = false;
			if(upwardHeap.getSize() == 0 && downwardHeap.getSize() == 0){
				break;
			}
			if(upwardHeap.peek()[1] < downwardHeap.peek()[1]){
				globalMin = upwardHeap.remove();
				settledUpward = true;
			}else{
				globalMin = downwardHeap.remove();
				settledDownward = true;
			}
			//if(settledNodes.contains(globalMin[0])){//boolean array
			if(settledNodes[globalMin[0]] == true){
				int distance = upwardDis[globalMin[0]] + downwardDis[globalMin[0]];
				if(distance < currentCostOfShortestPath){
					currentCostOfShortestPath = distance;
				}
			}else{
				//settledNodes.add(globalMin[0]);
				settledNodes[globalMin[0]] = true;
			}
			if((settledUpward && upwardDis[globalMin[0]] > currentCostOfShortestPath) || (settledDownward && downwardDis[globalMin[0]] > currentCostOfShortestPath)){
				available = true;
				haltCondition = true;
				break;
			}
			//upward search:
			if(settledUpward){
				int[] out = graph.getOutgoingEdgesArrayUpwardIndex(globalMin[0]);
				int startIndex = -1;
				int endIndex = -1;
				int length = 0;
				if(out != null){
					startIndex = out[0];
					endIndex = out[1];
					length = endIndex - startIndex + 1;
				}else{
					length = 0;
				}
				if(length != 0) {
					int[] edgeArray = graph.getUpwardEdgeArray();
					for (int i = startIndex; i < endIndex; i += graph.getLengthOfEdgeElement()) {
						if (upwardDis[edgeArray[i]] + edgeArray[i+2] < upwardDis[edgeArray[i+1]]) {
							upwardDis[edgeArray[i+1]] = upwardDis[edgeArray[i]] + edgeArray[i+2];
							//nodesReachedByUpward.add(edgeArray[i+1]);//
							reachedNodes.add(edgeArray[i+1]);
							upwardParent[edgeArray[i+1]] = edgeArray[i];
							if (upwardHeap.posInHeap[edgeArray[i+1]] != -1) {// in heap
								upwardHeap.decreaseKey(edgeArray[i+1], upwardDis[edgeArray[i]] + edgeArray[i+2]);
							}else {
							upwardHeap.add(edgeArray[i+1], upwardDis[edgeArray[i+1]]);
							//System.out.println(edgeArray[i+1]+" with cost "+upwardDis[edgeArray[i+1]]+" in upward Heap added.");
							}
						}
					}
				}
			}else{//downward search
				int[] out = graph.getOutgoingEdgesArrayDownwardIndex(globalMin[0]);
				int startIndex = -1;
				int endIndex = -1;
				int length = 0;
				if(out != null){
					startIndex = out[0];
					endIndex = out[1];
					length = endIndex - startIndex + 1;
				}else{
					length = 0;
				}
				if(length != 0) {
					int[] edgeArray = graph.getDownwardEdgeArray();
					for (int i = startIndex; i < endIndex; i += graph.getLengthOfEdgeElement()) {
						if (downwardDis[edgeArray[i]] + edgeArray[i+2] < downwardDis[edgeArray[i+1]]) {
							downwardDis[edgeArray[i+1]] = downwardDis[edgeArray[i]] + edgeArray[i+2];
							//nodesReachedByDownward.add(edgeArray[i+1]);
							reachedNodes.add(edgeArray[i+1]);
							downwardParent[edgeArray[i+1]] = edgeArray[i];
							if (downwardHeap.posInHeap[edgeArray[i+1]] != -1) {// in heap
							 	downwardHeap.decreaseKey(edgeArray[i+1], downwardDis[edgeArray[i]] + edgeArray[i+2]);
							}else {
							downwardHeap.add(edgeArray[i+1], downwardDis[edgeArray[i+1]]);
							//System.out.println(edgeArray[i+1]+" with cost "+downwardDis[edgeArray[i+1]]+" in downwards Heap added.");
							}
						}
					}
				}
			}
			nrOfSettledNodes++;
		}

	}

	public void reset(){
		for (int i : reachedNodes) {
			upwardDis[i] = Integer.MAX_VALUE;
			downwardDis[i] = Integer.MAX_VALUE;
			upwardParent[i] = -1;
			downwardParent[i] = -1;
			settledNodes[i] = false;
		}
		nrOfSettledNodes = 0;
		middleNodeInShortestPath = -1;
		available = false;
		upwardHeap.reset();
		downwardHeap.reset();
		reachedNodes.clear();
		costOfPath = 0;
		
	}
    

	public boolean getPathAvailable(){
		return available;
	}
	
	public int getNrOfSettledNodes(){
		return nrOfSettledNodes;
	}

	public int getCostOfPath(){
		return costOfPath;
	}


	public int[] getShortestPathInNodeId() {
		long sTime = System.currentTimeMillis();
		//Set<Integer> intersection = new HashSet<Integer>(nodesReachedByUpward);
		//intersection.retainAll(nodesReachedByDownward);
		int shortestDis = Integer.MAX_VALUE;
		// for (Integer nodeId : intersection) {
		// 	int completeDis = upwardDis[nodeId] + downwardDis[nodeId];
		// 	if(completeDis < shortestDis){
		// 		shortestDis = completeDis;
		// 		middleNodeInShortestPath = nodeId;
		// 	}
		// }

		for (int nodeId : reachedNodes) {
			if(upwardDis[nodeId] == Integer.MAX_VALUE || downwardDis[nodeId] == Integer.MAX_VALUE){
				continue;
			}
			int completeDis = upwardDis[nodeId] + downwardDis[nodeId];
			if(completeDis < shortestDis){
				shortestDis = completeDis;
				middleNodeInShortestPath = nodeId;
			}
		}
		costOfPath = shortestDis;
		long etime = System.currentTimeMillis();
		long time = etime - sTime;
		if(printInformation != 0){
			System.out.println("the meeting point is "+middleNodeInShortestPath+". The cost is "+shortestDis + ". Finding middle point takes ["+time+"]ms.");
		}
		//upwards:
		int[] backwardPathUp = new int[graph.getNodeNr()];
		int[] backwardPathDown = new int[graph.getNodeNr()];
		int upwardNodeNr = 1;
		int downwardNodeNr = 0;
		for(int i = 0; i < backwardPathUp.length; i++) {
			backwardPathUp[i] = -1;
			backwardPathDown[i] = -1;
		}
		backwardPathUp[0] = middleNodeInShortestPath;
		int tmp = middleNodeInShortestPath;
		int i;
		for( i = 1; upwardParent[tmp] != source; i++) {
			backwardPathUp[i] = upwardParent[tmp];
			tmp= upwardParent[tmp];
			upwardNodeNr++;
			if(tmp == -1){
				System.out.println("node without parent: "+tmp);
			}
		}
		backwardPathUp[i] = source;
		upwardNodeNr++;
		//downwards:
		backwardPathDown[0] = middleNodeInShortestPath;
		tmp = middleNodeInShortestPath;
		for( i = 0; downwardParent[tmp] != target; i++) {
			backwardPathDown[i] = downwardParent[tmp];
			tmp= downwardParent[tmp];
			downwardNodeNr++;
			if(tmp == -1){
				System.out.println("node without parent: "+tmp);
			}
		}
		backwardPathDown[i] = target;
		downwardNodeNr++;
		//concatinate two path:
		int[] result = new int[upwardNodeNr + downwardNodeNr];
		i = 0;
		for(int j = backwardPathUp.length - 1; j >= 0; j--){
			if(backwardPathUp[j] == -1){
				continue;
			}
			result[i] = backwardPathUp[j];
			i++;
		}
		for (int j = 0; j < backwardPathDown.length; j++) {
			if(backwardPathDown[j] == -1){
				break;
			}
			result[i] = backwardPathDown[j];
			i++;
		}
		long startTime = System.currentTimeMillis();
		result = expandShortcuts(result);
		long time2 = System.currentTimeMillis() - startTime;
		System.out.println("expand path takes: " + time2 +"ms.");
		//reset reachedNodes array
		for (int nodeId : reachedNodes) {
			settledNodes[nodeId] = false;
		}
		return result;
	}

	private int[] expandShortcuts(int[] path){
		int[] resultWithoutShortcut;
		boolean shortcutExpaned = true;
		LinkedList<Integer> listWithoutShortcut = new LinkedList<>();
		for(int i = 0; i < path.length; i++){
			listWithoutShortcut.add(path[i]);
		}
		while(shortcutExpaned){
			shortcutExpaned = false;
			ListIterator<Integer> iterator = listWithoutShortcut.listIterator();
			int currentNode = -1;
			int nextNode = iterator.next();
			while(iterator.hasNext()){
				currentNode = nextNode;
				nextNode = iterator.next();
				int[] outgoingEdgesIndex = graph.getOutgoingEdgesArrayIndex(currentNode);
				int startIndex = outgoingEdgesIndex[0];
				int endIndex = outgoingEdgesIndex[1];
				int[] edgeArray = graph.getEdgeArray();
				for (int j = startIndex; j < endIndex; j+=graph.getLengthOfEdgeElement()) {
					if(edgeArray[j+1] == nextNode && edgeArray[j+3] != -1){
						shortcutExpaned = true;
						iterator.previous();
						iterator.add(graph.getEdge(edgeArray[j+3])[1]);
						nextNode = graph.getEdge(edgeArray[j+3])[1];
						break;//no second shortcut between two nodes.
					}
				}
			}
		}
		resultWithoutShortcut = new int[listWithoutShortcut.size()];
		ListIterator<Integer> iter = listWithoutShortcut.listIterator();
		while(iter.hasNext()){
			resultWithoutShortcut[iter.nextIndex()] = iter.next();
		}
		return resultWithoutShortcut;
	}

	public String getShortestPathInLonLat(){
		if(available){
			int[] path = getShortestPathInNodeId();
			int pathLength = path.length;
			double[][] shortestPathInLonLat = new double[pathLength][2];
			for(int i = 0; i < pathLength; i++) {
				shortestPathInLonLat[i][0] = graph.getLongitude(path[i]);
				shortestPathInLonLat[i][1] = graph.getLatitude(path[i]);
			}
			String pathInLonLat = Arrays.deepToString(shortestPathInLonLat);
			return pathInLonLat;
		}else{
			return "[" + source + ", "+ target + "]";
		}
	}

	public static void main(String[] args) {
		GraphWithCH g = new GraphWithCH("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_germany.txt");
		//Quadtree q = new Quadtree("/Users/xinpang/Desktop/Studium/7. Semester/Bachelor Arbeit/Server/src/germany.txt");
		
		//int start = (int) (Math.random() * g.getNodeNr());	
		int source = 127353;
		
		//int target = (int) (Math.random() * g.getNodeNr());
		int target = 259745;
		
		System.out.println("start: " + source);
		System.out.println("target: " + target);
		DijkstraWithCH.settledNodes = new boolean[g.getNodeNr()];
		DijkstraWithCH dij = new DijkstraWithCH(g);
		dij.computePath(source, target);
		System.out.println("available: "+dij.getPathAvailable());
		System.out.println(dij.getShortestPathInLonLat());
		
	}
}
