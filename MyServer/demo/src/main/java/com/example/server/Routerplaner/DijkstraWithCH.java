package com.example.server.Routerplaner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

public class DijkstraWithCH {
	private int[] upwardDis;
	private int[] downwardDis;
	private int[] upwardParent;
	private int[] downwardParent;
	private GraphWithCH graph;
	private int source;
	private int target;
	private int nrOfVisitedNodes;
	private boolean notAvailable;
	private Set<Integer> settledNodes;
	private Set<Integer> nodesReachedByUpward;
	private Set<Integer> nodesReachedByDownward;
	private int middleNodeInShortestPath = -1;
	
	/**
	 * computes the shortest path given the parameters
	 * @param graph provided "city"
	 * @param s startnodeId
	 * @param t targetnodeId
	 *
	 */
	public DijkstraWithCH(GraphWithCH graph, int s, int t){
		System.out.println("computing dijkstra...");
		nrOfVisitedNodes = 0;
		long sTime = System.currentTimeMillis();
		this.upwardDis = new int[graph.getNodeNr()];
		this.downwardDis = new int[graph.getNodeNr()];
		this.upwardParent = new int[graph.getNodeNr()];
		this.downwardParent = new int[graph.getNodeNr()];
		this.graph = graph;
		this.source = s;
		this.target = t;
		this.notAvailable = true;
		settledNodes = new HashSet<>();
		
		for (int i = 0; i < graph.getNodeNr(); i++) {
			upwardDis[i] = Integer.MAX_VALUE;
			downwardDis[i] = Integer.MAX_VALUE;
			upwardParent[i] = -1; // no parent
			downwardParent[i] = -1;
		}
		
		upwardParent[source] = source;
		downwardParent[target] = target;
		
		upwardDis[source] = 0;
		downwardDis[target] = 0;
		
		Comparator<int[]> distanceComparator = new Comparator<int[]>(){
			@Override
			public int compare(int[] vector1,int[] vector2){
				return vector1[1] - vector2[1];
			}
		};
		PriorityQueue<int[]> upwardHeap = new PriorityQueue<>(distanceComparator);
		PriorityQueue<int[]> downwardHeap = new PriorityQueue<>(distanceComparator);
		//MinHeap upwardHeap = new MinHeap(graph.getNodeNr());
		//MinHeap downwardHeap = new MinHeap(graph.getNodeNr());
		nodesReachedByUpward = new HashSet<Integer>();
		nodesReachedByDownward = new HashSet<Integer>();
		upwardHeap.add(new int[] {source, 0});
		downwardHeap.add(new int[] {target, 0});
		
		boolean haltCondition = false;
		boolean settledUpward = false;
		boolean settledDownward = false;
		int[] globalMin;
		while(!haltCondition) {
			settledUpward = false;
			settledDownward = false;
			if(upwardHeap.peek()[1] < downwardHeap.peek()[1]){
				globalMin = upwardHeap.remove();
				settledUpward = true;
			}else{
				globalMin = downwardHeap.remove();
				settledDownward = true;
			}
			if(settledNodes.contains(globalMin[0])){
				System.out.println("First node settled twice: "+globalMin[0]);
				haltCondition = true;
				notAvailable = false;
				break;
			}else{
				settledNodes.add(globalMin[0]);
			}
			//upward search:
			if(settledUpward){
				int[] out = graph.getOutgoingEdgesArrayUpwardIndex(globalMin[0]);
				int startIndex = out[0];
				int endIndex = out[1];
				int length = endIndex - startIndex + 1;
				int[] edgeArray = graph.getUpwardEdgeArray();
				if(length != 0) {
					for (int i = startIndex; i < endIndex; i += graph.getLengthOfEdgeElement()) {
						if (upwardDis[edgeArray[i]] + edgeArray[i+2] < upwardDis[edgeArray[i+1]]) {
							upwardDis[edgeArray[i+1]] = upwardDis[edgeArray[i]] + edgeArray[i+2];
							nodesReachedByUpward.add(edgeArray[i+1]);
							//if(edgeArray[i+3] == -1){//if the edge is a normal edge
							upwardParent[edgeArray[i+1]] = edgeArray[i];
							//}else{// if the edge is a short cut, update all parents.
								//updateParentsRecursive(tempEdgearray);
								//updateParents(edgeArray[3], edgeArray[4]);
							//}
							//if (upwardHeap.posInHeap[edgeArray[i+1]] != -1) {// in heap
							//	upwardHeap.decreaseKey(edgeArray[i+1], upwardDis[edgeArray[i]] + edgeArray[i+2]);
							//}else {
							upwardHeap.add(new int[] {edgeArray[i+1], upwardDis[edgeArray[i+1]]});
							System.out.println(edgeArray[i+1]+" with cost "+upwardDis[edgeArray[i+1]]+" in upward Heap added.");
							//}
						}
					}
				}
			}else{//downward search
				int[] out = graph.getOutgoingEdgesArrayDownwardIndex(globalMin[0]);
				int startIndex = out[0];
				int endIndex = out[1];
				int length = endIndex - startIndex + 1;
				if(length != 0) {
					int[] edgeArray = graph.getDownwardEdgeArray();
					for (int i = startIndex; i < endIndex; i += graph.getLengthOfEdgeElement()) {
						if (downwardDis[edgeArray[i]] + edgeArray[i+2] < downwardDis[edgeArray[i+1]]) {
							downwardDis[edgeArray[i+1]] = downwardDis[edgeArray[i]] + edgeArray[i+2];
							nodesReachedByDownward.add(edgeArray[i+1]);
							//if(edgeArray[i+3] == -1){//if the edge is a normal edge
							downwardParent[edgeArray[i+1]] = edgeArray[i];
							//}else{// if the edge is a short cut, update all parents.
								//updateParentsRecursive(tempEdgearray);
								//updateParents(edgeArray[3], edgeArray[4]);
							//}
							// if (downwardHeap.posInHeap[edgeArray[i+1]] != -1) {// in heap
							// 	downwardHeap.decreaseKey(edgeArray[i+1], downwardDis[edgeArray[i]] + edgeArray[i+2]);
							// }else {
							downwardHeap.add(new int[]{edgeArray[i+1], downwardDis[edgeArray[i+1]]});
							System.out.println(edgeArray[i+1]+" with cost "+downwardDis[edgeArray[i+1]]+" in downwards Heap added.");
							//}
						}
					}
				}
			}
			
			
		}
		long eTime = System.currentTimeMillis();
		long time = eTime - sTime;
		System.out.println("Dijkstra Computation with CH took ["+time+"] milli seconds");
		
	}
    //update parents of node in a shortcut
    // private void updateParents(final int firstEdge, final int secondEdge){
    //     int tempFirstEdge = firstEdge;
    //     int tempSecondEdge = secondEdge;
    //     Stack<Integer> edgesToBeExpand = new Stack<Integer>();
    //     edgesToBeExpand.push(tempFirstEdge);
    //     edgesToBeExpand.push(tempSecondEdge);

    //     int tempEdgeId = -1;
    //     while(!edgesToBeExpand.isEmpty()){
    //         tempEdgeId = edgesToBeExpand.pop();
    //         int[] tempEdgeArray = graph.getEdge(tempEdgeId);// [start, target, cost, first subEdgeId, second subEdgeId]
	// 		//System.out.println(tempEdgeArray[0]+"\n");
    //         parent[tempEdgeArray[1]] = tempEdgeArray[0];        //update parents

    //         tempFirstEdge = tempEdgeArray[3];
    //         tempSecondEdge = tempEdgeArray[4];
    //         if(tempFirstEdge != -1){
    //             edgesToBeExpand.push(tempFirstEdge);
    //             edgesToBeExpand.push(tempSecondEdge);
    //         }
    //     }
    // }
	// private void updateParentsRecursive(final int[] tempEdgeArray) {
	// 	int start = tempEdgeArray[0];
	// 	int end = tempEdgeArray[1];
	// 	int edge1 = tempEdgeArray[3];
	// 	int edge2 = tempEdgeArray[4];
	// 	if(edge1 != -1) {
	// 		updateParentsRecursive(graph.getEdge(edge1));
	// 		updateParentsRecursive(graph.getEdge(edge2));
	// 	}else{
	// 		parent[end] = start;
	// 	}
	// }

	public boolean getPathAvailable(){
		return notAvailable;
	}
	
	public int getNrOfVisitedNodes(){
		return nrOfVisitedNodes;
	}


	public int[] getShortestPathInNodeId() {
		System.out.println("Nodes reached by upward:");
		for (int i : nodesReachedByUpward) {
			System.out.println(i);
		}
		System.out.println("Node reached by downward:");
		for (int i : nodesReachedByDownward) {
			System.out.println(i);
		}
		Set<Integer> intersection = new HashSet<Integer>(nodesReachedByUpward);
		intersection.retainAll(nodesReachedByDownward);
		int shortestDis = Integer.MAX_VALUE;
		for (Integer nodeId : intersection) {
			int completeDis = upwardDis[nodeId] + downwardDis[nodeId];
			if(completeDis < shortestDis){
				shortestDis = completeDis;
				middleNodeInShortestPath = nodeId;
			}
		}
		System.out.println("the meeting point is "+middleNodeInShortestPath+". The cost is "+shortestDis);
		//upwards:
		int[] backwardPathUp = new int[graph.getNodeNr()];
		int[] backwardPathDown = new int[graph.getNodeNr()];
		int upwardNodeNr = 1;
		int downwardNodeNr = 1;
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
		//downwards:
		backwardPathDown[0] = middleNodeInShortestPath;
		tmp = middleNodeInShortestPath;
		for( i = 1; downwardParent[tmp] != target; i++) {
			backwardPathDown[i] = downwardParent[tmp];
			tmp= downwardParent[tmp];
			downwardNodeNr++;
			if(tmp == -1){
				System.out.println("node without parent: "+tmp);
			}
		}
		backwardPathDown[i] = target;
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
		return result;
	}
	
	public String getShortestPathInLonLat(){
		int[] path = getShortestPathInNodeId();
		int pathLength = path.length;
		// for(int i = 0; i < path.length; i++) {
		// 	if(path[i] != -1){
		// 		pathLength++;
		// 	}else{
		// 		break;
		// 	}
		// }
		double[][] shortestPathInLonLat = new double[pathLength][2];
		for(int i = 0; i < pathLength; i++) {
			shortestPathInLonLat[i][0] = graph.getLongitude(path[i]);
			shortestPathInLonLat[i][1] = graph.getLatitude(path[i]);
		}
		String pathInLonLat = Arrays.deepToString(shortestPathInLonLat);
		return pathInLonLat;
	}

	public static void main(String[] args) {
		GraphWithCH g = new GraphWithCH("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_stuttgart.txt");
		//Quadtree q = new Quadtree("/Users/xinpang/Desktop/Studium/7. Semester/Bachelor Arbeit/Server/src/germany.txt");
		
		//int start = (int) (Math.random() * g.getNodeNr());	
		int start = 3000;
		
		//int target = (int) (Math.random() * g.getNodeNr());
		int target = 5353;
		
		System.out.println("start: " + start);
		System.out.println("target: " + target);
		DijkstraWithCH dij = new DijkstraWithCH(g, start, target);
		System.out.println("not available: "+dij.getPathAvailable());
		System.out.println(dij.getShortestPathInLonLat());
		
	}
}
