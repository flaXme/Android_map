package com.example.server.Routerplaner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import com.example.server.Localisation.*;

public class SubgraphDij {
	int printInformation = 0;
	private int[] dis;
	private int[] parent;
	private Subgraph graph;
	private int source;
	private int target;
	private int nrOfSettledNodes;
	private boolean available = false;
	private MinHeap heap;
	
	/**
	 * computes the shortest path given the parameters
	 * @param graph provided "city"
	 * @param start startnodeId
	 * @param target targetnodeId
	 *
	 */
	public SubgraphDij(Subgraph graph){
		nrOfSettledNodes = 0;
		this.dis = new int[graph.getNodeNr()];
		this.parent = new int[graph.getNodeNr()];
		this.graph = graph;
		heap = new MinHeap(graph.getNodeNr());
		
		for (int i = 0; i < graph.getNodeNr(); i++) {
			dis[i] = Integer.MAX_VALUE;
			parent[i] = -1; // no parent
		}
	}

	public void computePath(int source, int target){
		this.source = source;
		this.target = target;
		parent[source] = source;
		dis[source] = 0;
		heap.add(source, 0);
		
		while(heap.getSize() > 0) {
			int[] min = heap.remove();
			int[] out = graph.getOutgingEdgesArray(min[0]);
			if(min[0] == target){
				available = true;
				break;
			}
			if(out != null) {//out = [startId, endId, cost]
				for (int i = 0; i < out.length; i += graph.getLengthOfEdgeElement()) {
					if (dis[out[i]] + out[i+2] < dis[out[i+1]]) {
						dis[out[i+1]] = dis[out[i]] + out[i+2];
						parent[out[i+1]] = out[i];
						if(heap.posInHeap[out[i+1]] != -1) {// in heap
						 	heap.decreaseKey(out[i+1], dis[out[i]] + out[i+2]);
						 }else {
							heap.add(out[i+1], dis[out[i+1]]);
						}
					}
				}
			}
			nrOfSettledNodes++;
		}
	}

	public void reset(){
		available = false;
		nrOfSettledNodes = 0;
		heap.reset();
		for (int i = 0; i < graph.getNodeNr(); i++) {
			dis[i] = Integer.MAX_VALUE;
			parent[i] = -1;
		}
	}


	public double getCost(int node){
		return dis[node];
	}

	public boolean getPathAvailable(){
		return available;
	}
	
	public int getNrOfSettledNodes(){
		return nrOfSettledNodes;
	}

	public double getCostOfShortestPathTo(int nodeID) {
		return this.dis[nodeID];
	}
	public double dotProduct(double a[], double b[]) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += a[i] * b[i];
		}
		return sum;
	}

	public int[] getShortestPathTo() {
		int[] backwardPath = new int[graph.getNodeNr()];
		for(int i = 0; i < backwardPath.length; i++) {
			backwardPath[i] = -1;
		}
		backwardPath[0] = target;
		int tmp = target;
		int i;
		for( i = 1; parent[tmp] != source; i++) {
			backwardPath[i] = parent[tmp];
			tmp= parent[tmp];
		}
		backwardPath[i] = source;
		int length = i + 1;
		int[] forwardPath = new int[length];
		int j = 0;
		while(i >=0){
			forwardPath[j] = backwardPath[i];
			j++;
			i--;
		}
		forwardPath = expandShortcuts(forwardPath);
		return forwardPath;
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
			int[] path = getShortestPathTo();
			int pathLength = 0;
			for(int i = 0; i < path.length; i++) {
				if(path[i] != -1){
					pathLength++;
				}else{
					break;
				}
			}
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
}
