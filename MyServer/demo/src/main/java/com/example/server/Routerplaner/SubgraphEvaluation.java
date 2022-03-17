package com.example.server.Routerplaner;

import java.io.FileWriter;
import java.io.IOException;

public class SubgraphEvaluation {
    public static void main(String[] args) {
        // GraphWithCH graphWithCH = new GraphWithCH("/Users/xinpang/Desktop/Studium/7.Semester/Bachelor Arbeit/CH/ch_germany.txt");
        // //random choose a subgraph with size 1/100 of the original.
        // double latRange = (graphWithCH.getMaxLatOfGraph() - graphWithCH.getMinLatOfGraph()) / 10;
        // double longRange = (graphWithCH.getMaxLongOfGraph() - graphWithCH.getMinLongOfGraph()) / 10;
        // double minLat =  graphWithCH.getMinLatOfGraph() + Math.random() * (graphWithCH.getMaxLatOfGraph() - graphWithCH.getMinLatOfGraph());
        // double maxLat = minLat + latRange;
        // double minLong = graphWithCH.getMinLongOfGraph() + Math.random() * (graphWithCH.getMaxLongOfGraph() - graphWithCH.getMinLongOfGraph());
        // double maxLong = minLong + longRange;
        // String subgraphString = graphWithCH.calculateSubgraph(minLat, maxLat, minLong, maxLong);
        // //write subgraph data in a local file
        // try {
        //     FileWriter writer = new FileWriter("subgraph.txt");
        //     writer.write(subgraphString);
        //     writer.close();
        // } catch (IOException e) {
        //     System.out.println("error in writing subgraph.");
        //     e.printStackTrace();
        // }
        Subgraph subgraph = new Subgraph("./subgraph.txt");

        int nrOfIteration = 1000;
        int nrOfValidQuery = 0;
        int equalCost = 0;

        for (int i = 0; i < nrOfIteration; i++) {
            System.out.println(i+1 + "-th test:");
            //choose a random source node
            int source = (int) (Math.random() * subgraph.getNodeNr());
            
            //choose a random target node
            int target = (int) (Math.random() * subgraph.getNodeNr());
           
            
            

            SubgraphDij dij = new SubgraphDij(subgraph, source, target);
            if(!dij.getPathAvailable()){
                System.out.println("source is: " + source);
                System.out.println("target is: " + target);
                System.out.println("no path available!");
                continue;
            }
            dij.getShortestPathInLonLat();
            nrOfValidQuery++;

            SubgraphDijCH  dijCH = new SubgraphDijCH(subgraph, source, target);
            if(!dijCH.getPathAvailable()){
                System.out.println("No path found in dijk with CH!");
                System.out.println("source: " + source + ", target: " + target);
                continue;
            }
            System.out.println("path available: " + dijCH.getPathAvailable());
            dijCH.getShortestPathInLonLat();

            
            //compare the cost of shortest paths
            if(dij.getCost(target) == dijCH.getCostOfPath()){
                equalCost++;
            }else{
                System.out.println("source is: " + source);
                System.out.println("target is: " + target);
                System.out.println("Cost with CH: " + dijCH.getCostOfPath());
                System.out.println("Cost without CH: " + dij.getCost(target));
            }

        }
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Here comes the results:");
        System.out.println(nrOfValidQuery + "/" + nrOfIteration + " queries are valid.");
        System.out.println("Both yields the path with same cost in : " + equalCost + "/" + nrOfValidQuery);
    }
}
