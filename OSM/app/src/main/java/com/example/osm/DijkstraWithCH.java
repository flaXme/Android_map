package com.example.osm;
import java.util.Arrays;



//public class Dijkstra {
//    private int[] dis;
//    private int[] parent;
//    private Graph graph;
//    private int start;
//    private int target;
//    private int nrOfVisitedNodes;
//    private boolean notAvailable;
//
//    /**
//     * computes the shortest path given the parameters
//     *
//     * @param graph  provided "city"
//     * @param start  startnodeId
//     * @param target targetnodeId
//     */
//    public Dijkstra(Graph graph, int start, int target) {
//        //System.out.println("computing dijkstra...");
//        nrOfVisitedNodes = 0;
//        long sTime = System.currentTimeMillis();
//        this.dis = new int[graph.getNodeNr()];
//        this.parent = new int[graph.getNodeNr()];
//        this.graph = graph;
//        this.start = start;
//        this.target = target;
//
//        for (int i = 0; i < graph.getNodeNr(); i++) {
//            dis[i] = Integer.MAX_VALUE;
//            parent[i] = -1; // no parent
//        }
//
//        parent[start] = start;
//        dis[start] = 0;
//
//        MinHeap heap = new MinHeap(graph.getNodeNr());
//
//        heap.add(start, 0);
//
//        while (heap.getSize() > 0) {
//            int[] min = heap.remove();
//            int[] out = graph.getOutgingEdgesArray(min[0]);
//
//            if (out != null) {
//                for (int i = 0; i < out.length; i += 3) {
//                    if (dis[out[i]] + out[i + 2] < dis[out[i + 1]]) {
//                        dis[out[i + 1]] = dis[out[i]] + out[i + 2];
//                        parent[out[i + 1]] = out[i];
//                        if (heap.posInHeap[out[i + 1]] != -1) {// in heap
//                            heap.decreaseKey(out[i + 1], dis[out[i]] + out[i + 2]);
//                        } else {
//                            heap.add(out[i + 1], dis[out[i]] + out[i + 2]);
//                        }
//                    }
//                }
//
//
//            }
//
//        }
//        long eTime = System.currentTimeMillis();
//        long time = eTime - sTime;
//        //System.out.println("Dijkstra Computation took ["+time+"] milli seconds");
//
//    }
//
//    public double getCost(int node) {
//        return dis[node];
//    }
//
//    public boolean getPathAvailable() {
//        return notAvailable;
//    }
//
//    public int getNrOfVisitedNodes() {
//        return nrOfVisitedNodes;
//    }
//
//    public double getCostOfShortestPathTo(int nodeID) {
//        return this.dis[nodeID];
//    }
//
//    public double dotProduct(double a[], double b[]) {
//        double sum = 0;
//        for (int i = 0; i < a.length; i++) {
//            sum += a[i] * b[i];
//        }
//        return sum;
//    }
//
//    public int[] getShortestPathTo() {
//        int[] backwardPath = new int[graph.getNodeNr()];
//        for (int i = 0; i < backwardPath.length; i++) {
//            backwardPath[i] = -7;
//        }
//        backwardPath[0] = target;
//        int tmp = target;
//        int i;
//        for (i = 1; parent[tmp] != start; i++) {
//            if (parent[tmp] != -1) {
//                backwardPath[i] = parent[tmp];
//                tmp = parent[tmp];
//            } else {
//                System.out.println("node without parent: " + tmp);
//                break;
//            }
//        }
//        backwardPath[i] = start;
//        for (int j = 0; j < parent.length; j++) {
//            parent[j] = -1;
//        }
//        return backwardPath;
//    }
//
//    public String getShortestPathInLonLat() {
//        int[] path = getShortestPathTo();
//        int pathLength = 0;
//        for (int i = 0; i < path.length; i++) {
//            if (path[i] != -7) {
//                pathLength++;
//            } else {
//                break;
//            }
//        }
//        double[][] shortestPathInLonLat = new double[pathLength][2];
//        for (int i = 0; i < pathLength; i++) {
//            shortestPathInLonLat[i][0] = graph.getLongitude(path[i]);
//            shortestPathInLonLat[i][1] = graph.getLatitude(path[i]);
//        }
//        String pathInLonLat = Arrays.deepToString(shortestPathInLonLat);
//        return pathInLonLat;
//    }
//}


