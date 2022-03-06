package com.example.server.Routerplaner;

import java.util.Arrays;
import java.util.Stack;

import com.example.server.Localisation.*;

public class Dijkstra {
	int printInformation = 0;
	private int[] dis;
	private int[] parent;
	private Graph graph;
	private int start;
	private int target;
	private int nrOfVisitedNodes;
	private boolean available = false;
	
	/**
	 * computes the shortest path given the parameters
	 * @param graph provided "city"
	 * @param start startnodeId
	 * @param target targetnodeId
	 *
	 */
	public Dijkstra(Graph graph, int start, int target){
		if(printInformation != 0){
			System.out.println("computing dijkstra...");
		}
		nrOfVisitedNodes = 0;
		long sTime = System.currentTimeMillis();
		this.dis = new int[graph.getNodeNr()];
		this.parent = new int[graph.getNodeNr()];
		this.graph = graph;
		this.start = start;
		this.target = target;
		
		for (int i = 0; i < graph.getNodeNr(); i++) {
			dis[i] = Integer.MAX_VALUE;
			parent[i] = -1; // no parent
		}
		
		parent[start] = start;
		dis[start] = 0;
		
		MinHeap heap = new MinHeap(graph.getNodeNr());
		
		heap.add(start, 0);
		
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
			
		}
		long eTime = System.currentTimeMillis();
		long time = eTime - sTime;
		if(printInformation != 0){
			System.out.println("Dijkstra Computation took ["+time+"] milli seconds");
		}
	}


	public double getCost(int node){
		return dis[node];
	}

	public boolean getPathAvailable(){
		return available;
	}
	
	public int getNrOfVisitedNodes(){
		return nrOfVisitedNodes;
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
			backwardPath[i] = -7;
		}
		backwardPath[0] = target;
		int tmp = target;
		int i;
		for( i = 1; parent[tmp] != start; i++) {
			backwardPath[i] = parent[tmp];
			tmp= parent[tmp];
		}
		backwardPath[i] = start;
		return backwardPath;
	}
	
	public String getShortestPathInLonLat(){
		if(available){
			int[] path = getShortestPathTo();
			int pathLength = 0;
			for(int i = 0; i < path.length; i++) {
				if(path[i] != -7){
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
			return null;
		}
	}

	public static void main(String[] args) {
		Graph g = new Graph("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/Graphfiles/germany.txt");
		//Quadtree q = new Quadtree("/Users/xinpang/Desktop/Studium/7. Semester/Bachelor Arbeit/Server/src/germany.txt");
		//Graph g = new Graph("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_stuttgart.txt");
		//int start = (int) (Math.random() * g.getNodeNr());	
		int start = 14729716;
		
		//int target = (int) (Math.random() * g.getNodeNr());
		int target = 1732736;
		
		System.out.println("start: " + start);
		System.out.println("target: " + target);
		Dijkstra dij = new Dijkstra(g, start, target);
		System.out.println("Cost: " + dij.dis[target]);
		System.out.println("path available: "+dij.getPathAvailable());
		
		//System.out.println(dij.getShortestPathInLonLat());
		
	}
}
