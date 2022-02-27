package com.example.server.Routerplaner;

import java.util.Arrays;
import java.util.Stack;

import com.example.server.Localisation.*;

public class Dijkstra {
	private int[] dis;
	private int[] parent;
	private Graph graph;
	private int start;
	private int target;
	private int nrOfVisitedNodes;
	private boolean notAvailable;
	
	/**
	 * computes the shortest path given the parameters
	 * @param graph provided "city"
	 * @param start startnodeId
	 * @param target targetnodeId
	 *
	 */
	public Dijkstra(Graph graph, int start, int target){
		System.out.println("computing dijkstra...");
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
			
			if(out != null) {//out = [startId, endId, cost, firstSubEdge, secondSubEdge]
				for (int i = 0; i < out.length; i += 5) {
					if (dis[out[i]] + out[i+2] < dis[out[i+1]]) {
						dis[out[i+1]] = dis[out[i]] + out[i+2];
						//if(out[3] == -1){
							parent[out[i+1]] = out[i];
						// }else{
						// 	updateParents(out[3], out[4]);
						// }
						 if (heap.posInHeap[out[i+1]] != -1) {// in heap
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
		System.out.println("Dijkstra Computation took ["+time+"] milli seconds");
		
	}

    //update parents of node in a shortcut
    private void updateParents(final int firstEdge, final int secondEdge){
        int tempFirstEdge = firstEdge;
        int tempSecondEdge = secondEdge;
        Stack<Integer> edgesToBeExpand = new Stack<Integer>();
        edgesToBeExpand.push(tempFirstEdge);
        edgesToBeExpand.push(tempSecondEdge);

        int tempEdgeId = -1;
        while(!edgesToBeExpand.isEmpty()){
            tempEdgeId = edgesToBeExpand.pop();
            int[] tempEdgeArray = graph.getEdge(tempEdgeId);// [start, target, cost, first subEdgeId, second subEdgeId]
			//System.out.println(tempEdgeArray[0]+"\n");
            parent[tempEdgeArray[1]] = tempEdgeArray[0];        //update parents

            tempFirstEdge = tempEdgeArray[3];
            tempSecondEdge = tempEdgeArray[4];
            if(tempFirstEdge != -1){
                edgesToBeExpand.push(tempFirstEdge);
                edgesToBeExpand.push(tempSecondEdge);
            }
        }
    }

	public double getCost(int node){
		return dis[node];
	}

	public boolean getPathAvailable(){
		return notAvailable;
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
			//if(parent[tmp] != -1){
				backwardPath[i] = parent[tmp];
				tmp= parent[tmp];
			// }else{
			// 	System.out.println("node without parent: "+tmp);
			// 	break;
			// }
		}
		backwardPath[i] = start;
		// for(int j = 0; j < parent.length; j++){
		// 	parent[j] = -1;
		// }
		return backwardPath;
	}
	
	public String getShortestPathInLonLat(){
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
	}

	public static void main(String[] args) {
		//Graph g = new Graph("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/Graphfiles/germany.txt");
		//Quadtree q = new Quadtree("/Users/xinpang/Desktop/Studium/7. Semester/Bachelor Arbeit/Server/src/germany.txt");
		Graph g = new Graph("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_stuttgart.txt");
		//int start = (int) (Math.random() * g.getNodeNr());	
		int start = 4343;
		
		//int target = (int) (Math.random() * g.getNodeNr());
		int target = 5353;
		
			
		System.out.println("nr ofoutgoing edge: "+g.getNrOfOutgoingEdges(start));
		System.out.println("start: " + start);
		System.out.println("target: " + target);
		Dijkstra dij = new Dijkstra(g, start, target);
		System.out.println("Cost: " + dij.dis[target]);
		System.out.println("not available: "+dij.getPathAvailable());
		
		System.out.println(dij.getShortestPathInLonLat());
		
	}
}
