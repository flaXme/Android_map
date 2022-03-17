package com.example.server.Routerplaner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class SubgraphDijCH {
    private int[] upwardDis;
    private int[] downwardDis;
    private int[] upwardParent;
    private int[] downwardParent;
    private Subgraph graph;
    private final int source;
    private final int target;
    private int nrOfSettledNodes;
    private boolean available = false;
    private Set<Integer> settledNodes;
    private Set<Integer> nodesReachedByUpward;
    private Set<Integer> nodesReachedByDownward;
    private int middleNodeInShortestPath = -1;
    private int costOfPath = 0;

    /**
     * computes the shortest path given the parameters
     *
     * @param graph provided "city"
     * @param s     startnodeId
     * @param t     targetnodeId
     */
    public SubgraphDijCH(Subgraph graph, int s, int t) {
        System.out.println("computing dijkstra...");
        nrOfSettledNodes = 0;
        long sTime = System.currentTimeMillis();
        this.upwardDis = new int[graph.getNodeNr()];
        this.downwardDis = new int[graph.getNodeNr()];
        this.upwardParent = new int[graph.getNodeNr()];
        this.downwardParent = new int[graph.getNodeNr()];
        this.graph = graph;
        this.source = s;
        this.target = t;
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

        MinHeap upwardHeap = new MinHeap(graph.getNodeNr());
        MinHeap downwardHeap = new MinHeap(graph.getNodeNr());
        nodesReachedByUpward = new HashSet<Integer>();
        nodesReachedByDownward = new HashSet<Integer>();
        upwardHeap.add(source, 0);
        downwardHeap.add(target, 0);

        boolean haltCondition = false;
        boolean settledUpward = false;
        boolean settledDownward = false;
        int currentCostOfShortestPath = Integer.MAX_VALUE;
        int[] globalMin;
        while (!haltCondition) {
            settledUpward = false;
            settledDownward = false;
            if(upwardHeap.getSize() == 0 && downwardHeap.getSize() == 0){
                break;
            }
            if (upwardHeap.peek()[1] < downwardHeap.peek()[1]) {
                globalMin = upwardHeap.remove();
                settledUpward = true;
            } else {
                globalMin = downwardHeap.remove();
                settledDownward = true;
            }
            if(settledNodes.contains(globalMin[0])){
                int distance = upwardDis[globalMin[0]] + downwardDis[globalMin[0]];
                if(distance < currentCostOfShortestPath){
                    currentCostOfShortestPath = distance;
                }
            }else{
                settledNodes.add(globalMin[0]);
            }
            if((settledUpward && upwardDis[globalMin[0]] > currentCostOfShortestPath) || (settledDownward && downwardDis[globalMin[0]] > currentCostOfShortestPath)){
                available = true;
                haltCondition = true;
                break;
            }
            //upward search:
            if (settledUpward) {
                int[] out = graph.getOutgoingEdgesArrayUpwardIndex(globalMin[0]);
                int startIndex = -1;
                int endIndex = -1;
                int length = 0;
                if (out != null) {
                    startIndex = out[0];
                    endIndex = out[1];
                    length = endIndex - startIndex + 1;
                } else {
                    length = 0;
                }
                if (length != 0) {
                    int[] edgeArray = graph.getUpwardEdgeArray();
                    for (int i = startIndex; i < endIndex; i += graph.getLengthOfEdgeElement()) {
                        if (upwardDis[edgeArray[i]] + edgeArray[i + 2] < upwardDis[edgeArray[i + 1]]) {
                            upwardDis[edgeArray[i + 1]] = upwardDis[edgeArray[i]] + edgeArray[i + 2];
                            nodesReachedByUpward.add(edgeArray[i + 1]);
                            upwardParent[edgeArray[i + 1]] = edgeArray[i];
                            if (upwardHeap.posInHeap[edgeArray[i + 1]] != -1) {// in heap
                                upwardHeap.decreaseKey(edgeArray[i + 1], upwardDis[edgeArray[i]] + edgeArray[i + 2]);
                            } else {
                                upwardHeap.add(edgeArray[i + 1], upwardDis[edgeArray[i + 1]]);
                                //System.out.println(edgeArray[i+1]+" with cost "+upwardDis[edgeArray[i+1]]+" in upward Heap added.");
                            }
                        }
                    }
                }
            } else {//downward search
                int[] out = graph.getOutgoingEdgesArrayDownwardIndex(globalMin[0]);
                int startIndex = -1;
                int endIndex = -1;
                int length = 0;
                if (out != null) {
                    startIndex = out[0];
                    endIndex = out[1];
                    length = endIndex - startIndex + 1;
                } else {
                    length = 0;
                }
                if (length != 0) {
                    int[] edgeArray = graph.getDownwardEdgeArray();
                    for (int i = startIndex; i < endIndex; i += graph.getLengthOfEdgeElement()) {
                        if (downwardDis[edgeArray[i]] + edgeArray[i + 2] < downwardDis[edgeArray[i + 1]]) {
                            downwardDis[edgeArray[i + 1]] = downwardDis[edgeArray[i]] + edgeArray[i + 2];
                            nodesReachedByDownward.add(edgeArray[i + 1]);
                            downwardParent[edgeArray[i + 1]] = edgeArray[i];
                            if (downwardHeap.posInHeap[edgeArray[i + 1]] != -1) {// in heap
                                downwardHeap.decreaseKey(edgeArray[i + 1], downwardDis[edgeArray[i]] + edgeArray[i + 2]);
                            } else {
                                downwardHeap.add(edgeArray[i + 1], downwardDis[edgeArray[i + 1]]);
                                //System.out.println(edgeArray[i+1]+" with cost "+downwardDis[edgeArray[i+1]]+" in downwards Heap added.");
                            }
                        }
                    }
                }
            }
            nrOfSettledNodes++;

        }

        long eTime = System.currentTimeMillis();
        long time = eTime - sTime;
        System.out.println("Dijkstra Computation with CH took [" + time + "] milli seconds");

    }


    public boolean getPathAvailable() {
        return available;
    }

    public int getNrOfSettledNodes() {
        return nrOfSettledNodes;
    }

    public int getCostOfPath(){
        return costOfPath;
    }


    public int[] getShortestPathInNodeId() {
        Set<Integer> intersection = new HashSet<Integer>(nodesReachedByUpward);
        intersection.retainAll(nodesReachedByDownward);
        if(intersection.isEmpty()){
            return new int[]{source, target};
        }
        int shortestDis = Integer.MAX_VALUE;
        for (Integer nodeId : intersection) {
            int completeDis = upwardDis[nodeId] + downwardDis[nodeId];
            if (completeDis < shortestDis) {
                shortestDis = completeDis;
                middleNodeInShortestPath = nodeId;
            }
        }
        System.out.println("the meeting point is " + middleNodeInShortestPath + ". The cost is " + shortestDis);
        costOfPath = shortestDis;
        //upwards:
        int[] backwardPathUp = new int[graph.getNodeNr()];
        int[] backwardPathDown = new int[graph.getNodeNr()];
        int upwardNodeNr = 1;
        int downwardNodeNr = 0;
        for (int i = 0; i < backwardPathUp.length; i++) {
            backwardPathUp[i] = -1;
            backwardPathDown[i] = -1;
        }
        backwardPathUp[0] = middleNodeInShortestPath;
        int tmp = middleNodeInShortestPath;
        int i;
        for (i = 1; upwardParent[tmp] != source; i++) {
            backwardPathUp[i] = upwardParent[tmp];
            tmp = upwardParent[tmp];
            upwardNodeNr++;
            if (tmp == -1) {
                System.out.println("node without parent: " + tmp);
            }
        }
        backwardPathUp[i] = source;
        upwardNodeNr++;
        //downwards:
        backwardPathDown[0] = middleNodeInShortestPath;
        tmp = middleNodeInShortestPath;
        for (i = 0; downwardParent[tmp] != target; i++) {
            backwardPathDown[i] = downwardParent[tmp];
            tmp = downwardParent[tmp];
            downwardNodeNr++;
            if (tmp == -1) {
                System.out.println("node without parent: " + tmp);
            }
        }
        backwardPathDown[i] = target;
        downwardNodeNr++;
        //concatinate two path:
        int[] result = new int[upwardNodeNr + downwardNodeNr];
        i = 0;
        for (int j = backwardPathUp.length - 1; j >= 0; j--) {
            if (backwardPathUp[j] == -1) {
                continue;
            }
            result[i] = backwardPathUp[j];
            i++;
        }
        for (int j = 0; j < backwardPathDown.length; j++) {
            if (backwardPathDown[j] == -1) {
                break;
            }
            result[i] = backwardPathDown[j];
            i++;
        }
        result = expandShortcuts(result);
        return result;
    }

    private int[] expandShortcuts(int[] path) {
        int[] resultWithoutShortcut;
        boolean shortcutExpaned = true;
        LinkedList<Integer> listWithoutShortcut = new LinkedList<>();
        for (int i = 0; i < path.length; i++) {
            listWithoutShortcut.add(path[i]);
        }
        // System.out.println("node list of path:");
        // for (Integer integer : listWithoutShortcut) {
        // 	System.out.println(integer);
        // }
        while (shortcutExpaned) {
            shortcutExpaned = false;
            for (int i = 0; i < listWithoutShortcut.size() - 1; i++) {
                int[] outgoingEdgesIndex = graph.getOutgoingEdgesArrayIndex(listWithoutShortcut.get(i).intValue());
                int startIndex = outgoingEdgesIndex[0];
                int endIndex = outgoingEdgesIndex[1];
                int[] edgeArray = graph.getEdgeArray();
                //System.out.println("erwartet: start:" + listWithoutShortcut.get(i)+"; end: "+listWithoutShortcut.get(i+1));
                for (int j = startIndex; j < endIndex; j += graph.getLengthOfEdgeElement()) {
                    //System.out.println("start: "+ edgeArray[j] + " end: " + edgeArray[j+1] + " first subedge: "+edgeArray[j+3]);
                    if (edgeArray[j + 1] == listWithoutShortcut.get(i + 1).intValue() && edgeArray[j + 3] != -1) {
                        shortcutExpaned = true;
                        //System.out.println("shortcut expanded.");
                        listWithoutShortcut.add(i + 1, graph.getEdge(edgeArray[j + 3])[1]);
                        break;//no second shortcut between two nodes.
                    }
                }
            }
        }
        resultWithoutShortcut = new int[listWithoutShortcut.size()];
        for (int i = 0; i < resultWithoutShortcut.length; i++) {
            resultWithoutShortcut[i] = listWithoutShortcut.get(i).intValue();
        }
        return resultWithoutShortcut;
    }

    public String getShortestPathInLonLat() {
        if(available) {
            int[] path = getShortestPathInNodeId();
            int pathLength = path.length;
            double[][] shortestPathInLonLat = new double[pathLength][2];
            for (int i = 0; i < pathLength; i++) {
                shortestPathInLonLat[i][0] = graph.getLongitude(path[i]);
                shortestPathInLonLat[i][1] = graph.getLatitude(path[i]);
            }
            String pathInLonLat = Arrays.deepToString(shortestPathInLonLat);
            return pathInLonLat;
        }else{
            return "["+source+", "+target+"]";
        }
    }
}
